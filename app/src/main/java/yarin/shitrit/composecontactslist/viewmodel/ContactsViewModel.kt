package yarin.shitrit.composecontactslist.viewmodel

import android.database.Cursor
import android.provider.ContactsContract
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import yarin.shitrit.composecontactslist.data.Contact
import yarin.shitrit.composecontactslist.data.Email
import yarin.shitrit.composecontactslist.data.Phone
import javax.inject.Inject

data class ContactsListUiState(
    val groupedContacts: Map<Char, List<Contact>>, var searchFilter: String, var isEmpty: Boolean
)

data class DetailedContactUiState(
    val contact: Contact? = null
)

/**
 * ViewModel that handles the business logic for the contacts list
 */
@HiltViewModel
class ContactsViewModel @Inject constructor(savedStateHandle: SavedStateHandle) : ViewModel() {

    //Data to store for preserving state for app screens after process killed
    private val searchFilter = savedStateHandle.getLiveData(key = "searchFilter", initialValue = "")
    private val savedDetailedContact =
        savedStateHandle.getLiveData<Contact>(key = "detailedContact")

    private val _contactsListUiState =
        MutableStateFlow(ContactsListUiState(mapOf(), searchFilter = "", isEmpty = false))
    val contactsListUiState: StateFlow<ContactsListUiState> = _contactsListUiState.asStateFlow()

    private val _detailedContactUiState = MutableStateFlow(DetailedContactUiState())
    val detailedContactUiState: StateFlow<DetailedContactUiState> =
        _detailedContactUiState.asStateFlow()

    private var allContacts: HashMap<Long, Contact> = hashMapOf()
    private var detailedContact: Contact? = null

    init {
        //updates the detailedContactScreen with the contact from savedStateHandle
        _detailedContactUiState.update { currentState ->
            currentState.copy(contact = savedDetailedContact.value)
        }
    }

    /**
     * Update the contacts list ui state according according to filter if exists
     */
    private fun updateContactsListScreen(filter: String = searchFilter.value ?: "") {
        viewModelScope.launch(Dispatchers.IO) {
            searchFilter.postValue(filter)
            val list = getFilteredContactsList(filter)
            _contactsListUiState.update { currentState ->
                currentState.copy(
                    groupedContacts = list.groupBy { contact -> contact.display_name[0] },
                    searchFilter = filter,
                    isEmpty = list.isEmpty()
                )
            }
        }
    }

    /**
     * return a mutableList of the contacts according to filter
     */
    private fun getFilteredContactsList(filter: String): MutableList<Contact> {
        var list = allContacts.values.toMutableList()
        list.sortBy { it.display_name }
        list = list.filter { contact ->
            (contact.display_name.contains(
                filter, ignoreCase = true
            ) || filter.isEmpty())
        }.toMutableList()
        return list
    }

    /**
     * Update the detailed contact ui state according
     */
    private fun updateDetailedContactScreen(contact: Contact) {
        savedDetailedContact.postValue(contact)
        _detailedContactUiState.update { currentState ->
            currentState.copy(contact = contact)
        }
    }

    /**
     * Read the cursor and create the contacts list accordingly
     */
    fun initContactsListFromCursor(cursor: Cursor) {
        viewModelScope.launch(Dispatchers.IO) {
            if (cursor.count > 0) {
                allContacts = createContactsListFromCursorAsync(cursor).await()
                updateContactsListScreen()
            }
            cursor.close()
        }
    }

    /**
     * Read the cursor and create the detailed contact accordingly
     */
    fun initDetailedContactFromCursor(cursor: Cursor) {
        viewModelScope.launch(Dispatchers.IO) {
            if (cursor.count > 0) {
                // awaits for the detailed contact data to be updated
                val contact = createDetailedContactFromCursorAsync(cursor).await()
                if (contact != null) {
                    updateDetailedContactScreen(contact)
                }
            }
            cursor.close()
        }
    }

    /**
     * sets the current detailed contact by id
     */
    fun setDetailedContact(contactId: Long?) {
        allContacts[contactId]?.let { contact ->
            detailedContact = contact
        }
    }

    /**
     * Creates new contacts from the cursor and assign first name, last name and photo uri to each contact
     */
    private fun createContactsListFromCursorAsync(cursor: Cursor) =
        viewModelScope.async(Dispatchers.IO) {
            val newContacts = HashMap<Long, Contact>()
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val lookupKey =
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY))
                val displayName =
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY))
                val photoUri =
                    cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI))
                newContacts[id] = Contact(
                    id = id,
                    display_name = displayName,
                    lookup_key = lookupKey,
                    first_name = "",
                    last_name = "",
                    phones_list = mutableListOf(),
                    emails_list = mutableListOf(),
                    photo_uri = photoUri?.toUri()
                )
            }
            newContacts
        }

    /**
     * Creates a detailed contact with all its emails and phone numbers from the cursor
     */
    private fun createDetailedContactFromCursorAsync(cursor: Cursor) =
        viewModelScope.async(Dispatchers.IO) {
            while (cursor.moveToNext()) {
                when (cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Data.MIMETYPE))) {
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE -> {
                        val firstName =
                            cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME))
                                ?: ""
                        val lastName =
                            cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME))
                                ?: ""
                        detailedContact?.first_name = firstName
                        detailedContact?.last_name = lastName
                    }

                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> {
                        var number =
                            cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        val type =
                            cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE))
                        number = number.replace("-", "").replace(" ", "").trim()
                        val phone = Phone(number, type)
                        detailedContact?.let { contact ->
                            if (!contact.hasNumber(number)) contact.phones_list.add(
                                phone
                            )
                        }
                    }

                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> {
                        val address =
                            cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS))
                        val type =
                            cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.TYPE))
                        val email = Email(address, type)
                        detailedContact?.let { contact ->
                            if (!contact.hasEmail(email.address)) contact.emails_list.add(
                                email
                            )
                        }
                    }
                }
            }
            detailedContact
        }

    /**
     * Calls updateContactsList with the given String filter
     */
    fun filterContactsByString(filter: String) = updateContactsListScreen(filter)
}