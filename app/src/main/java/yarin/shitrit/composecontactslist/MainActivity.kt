package yarin.shitrit.composecontactslist

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.MemoryCategory
import dagger.hilt.android.AndroidEntryPoint
import yarin.shitrit.composecontactslist.navigation.ScreenRoute
import yarin.shitrit.composecontactslist.utils.ContactsContractUtils
import yarin.shitrit.composecontactslist.viewmodel.ContactsViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity(), LoaderManager.LoaderCallbacks<Cursor> {
    private val contactsViewModel: ContactsViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Glide.get(this).setMemoryCategory(MemoryCategory.HIGH)
        setContent {
            Surface(color = Color.White, modifier = Modifier.fillMaxHeight()) {
                InitApp()
            }
        }
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (!isGranted) {
                    Toast.makeText(
                        this,
                        "Sorry, we cannot display your contacts without the permission :(",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            initCreateContactsLoaderManager()
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return when (id) {
            ContactsContractUtils.CREATE_CONTACTS -> {
                CursorLoader(
                    this,
                    ContactsContract.Contacts.CONTENT_URI,
                    ContactsContractUtils.CreateContactsProjection,
                    null,
                    null,
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC"
                )
            }
            ContactsContractUtils.DETAILED_CONTACT -> {
                CursorLoader(
                    this,
                    ContactsContract.Data.CONTENT_URI,
                    ContactsContractUtils.DetailedContactDataProjection,
                    ContactsContractUtils.DetailedContactSelection,
                    args?.getStringArray("selectionArgs"),
                    null
                )
            }
            else -> {
                CursorLoader(this)
            }
        }
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        if (cursor != null && !cursor.isClosed) {
            try {
                when (loader.id) {
                    ContactsContractUtils.CREATE_CONTACTS -> {
                        contactsViewModel.initContactsListFromCursor(cursor)
                    }
                    ContactsContractUtils.DETAILED_CONTACT -> {
                        contactsViewModel.initDetailedContactFromCursor(cursor)
                    }
                    else -> {
                        cursor.close()
                    }
                }
            } catch (e: IllegalArgumentException) {
                Toast.makeText(this, "Unable to display contacts", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
    }

    private fun initCreateContactsLoaderManager() {
        val lm = LoaderManager.getInstance(this)
        lm.restartLoader(ContactsContractUtils.CREATE_CONTACTS, null, this)

    }

    private fun initDetailedContactLoaderManager(lookupKey: String, contactId: Long) {
        val lm = LoaderManager.getInstance(this)
        val bundle = Bundle().apply {
            putStringArray("selectionArgs", arrayOf(lookupKey))
            putLong("contactId", contactId)
        }
        lm.restartLoader(ContactsContractUtils.DETAILED_CONTACT, bundle, this)
    }

    @Composable
    fun InitApp() {
        val navController = rememberNavController()
        NavHost(
            navController = navController, startDestination = ScreenRoute.ContactsListScreen.route
        ) {
            composable(route = ScreenRoute.ContactsListScreen.route) {
                ContactsListScreen(
                    viewModel = hiltViewModel(this@MainActivity)
                ) { contact ->
                    navController.navigate(route = ScreenRoute.DetailedContactScreen.route)
                    contactsViewModel.setDetailedContact(contact.id)
                    initDetailedContactLoaderManager(contact.lookup_key, contact.id)
                }
            }
            composable(route = ScreenRoute.DetailedContactScreen.route) {
                DetailedContactScreen(
                    viewModel = hiltViewModel(this@MainActivity),
                    onBackClick = {
                        navController.navigate(route = ScreenRoute.ContactsListScreen.route) {
                            popUpTo(ScreenRoute.ContactsListScreen.route) { inclusive = false }
                        }
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}