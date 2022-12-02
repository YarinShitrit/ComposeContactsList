package yarin.shitrit.composecontactslist.utils

import android.provider.ContactsContract
import androidx.loader.app.LoaderManager

/**
 * Object for providing the appropriate query projection for retrieving different contacts data
 */
object ContactsContractUtils {

    /**
     * Defines the [LoaderManager] loader ID for contacts list creation
     */
    const val CREATE_CONTACTS = 1

    /**
     * Defines the [LoaderManager] loader ID for detailed contact creation
     */
    const val DETAILED_CONTACT = 2

    // Defines the detailed contact query selection, = ? is added to avoid malicious SQL injection
    const val DetailedContactSelection: String = "${ContactsContract.Data.LOOKUP_KEY} = ?"

    // Defines the contacts list query projection
    val CreateContactsProjection = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.LOOKUP_KEY,
        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
        ContactsContract.Contacts.PHOTO_URI
    )

    // Defines the detailed contact query projection
    val DetailedContactDataProjection = arrayOf(
        ContactsContract.Data.MIMETYPE,
        ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
        ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
        ContactsContract.CommonDataKinds.Email.ADDRESS,
        ContactsContract.CommonDataKinds.Email.TYPE,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
        ContactsContract.CommonDataKinds.Phone.TYPE
    )
}