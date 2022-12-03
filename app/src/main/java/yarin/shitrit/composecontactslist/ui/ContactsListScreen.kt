package yarin.shitrit.composecontactslist.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.launch
import yarin.shitrit.composecontactslist.R
import yarin.shitrit.composecontactslist.data.Contact
import yarin.shitrit.composecontactslist.viewmodel.ContactsListUiState
import yarin.shitrit.composecontactslist.viewmodel.ContactsViewModel

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun ContactsListScreen(
    viewModel: ContactsViewModel,
    onContactClick: (contact: Contact) -> Unit
) {
    val uiState by viewModel.contactsListUiState.collectAsStateWithLifecycle()
    ContactsListContent(viewModel = viewModel, onContactClick = onContactClick, uiState = uiState)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactsListContent(
    modifier: Modifier = Modifier,
    viewModel: ContactsViewModel,
    onContactClick: (contact: Contact) -> Unit,
    listState: LazyListState = rememberLazyListState(),
    uiState: ContactsListUiState
) {
    val searchFilter = uiState.searchFilter
    val searchTrailingIcon = @Composable {
        IconButton(onClick = {}) {
            Icon(
                Icons.Default.Search, contentDescription = "", tint = Color.Black
            )
        }
    }
    val searchLeadingIcon = @Composable {
        IconButton(
            onClick = {
                viewModel.filterContactsByString("")
            },
        ) {
            Icon(
                Icons.Default.Clear, contentDescription = "", tint = Color.Black
            )
        }
    }
    Box(modifier = modifier) {
        val scope = rememberCoroutineScope()
        Column {
            TextField(
                value = searchFilter,
                singleLine = true,
                onValueChange = {
                    uiState.searchFilter = it
                    viewModel.filterContactsByString(it)
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White,
                    focusedIndicatorColor = colorResource(id = R.color.blue),
                    cursorColor = colorResource(id = R.color.blue)
                ),
                leadingIcon = if (searchFilter.isNotBlank()) searchLeadingIcon else null,
                trailingIcon = if (searchFilter.isBlank()) searchTrailingIcon else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            )
            LazyColumn(
                Modifier.fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                uiState.groupedContacts.forEach { (initial, contacts) ->
                    stickyHeader {
                        StickyHeader(
                            initial = initial.toString()[0],
                            Modifier.fillParentMaxWidth(),
                        )
                    }
                    items(contacts) { contact ->
                        ContactListItem(
                            contact = contact, modifier = Modifier.fillMaxWidth(), onContactClick
                        )
                        Divider(thickness = 0.5.dp, color = colorResource(id = R.color.blue))
                    }
                }
            }
            if (uiState.isEmpty) {
                Text(
                    text = "No contacts to display",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    color = colorResource(id = R.color.blue)
                )
            }
        }
        val showButton = remember { derivedStateOf { listState.firstVisibleItemIndex } }.value > 0
        // if (showButton) LocalFocusManager.current.clearFocus() //To close keyboard if open while scrolling
        AnimatedVisibility(
            visible = showButton,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {

            ScrollToTopButton(onClick = {
                scope.launch {
                    listState.scrollToItem(0)
                }
            })
        }
    }
}

@Composable
fun ScrollToTopButton(onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(bottom = 20.dp), Alignment.BottomCenter
    ) {
        Button(
            onClick = { onClick() },
            modifier = Modifier
                .shadow(10.dp, shape = CircleShape)
                .clip(shape = CircleShape)
                .size(60.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.White, contentColor = colorResource(id = R.color.blue)
            )
        ) {
            Icon(Icons.Filled.KeyboardArrowUp, "arrow up")
        }
    }
}

@Composable
fun StickyHeader(initial: Char, modifier: Modifier) {
    Surface(color = Color.LightGray) {
        Text(
            text = initial.toString(),
            modifier,
            color = colorResource(id = R.color.blue),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ContactListItem(
    contact: Contact, modifier: Modifier = Modifier, onContactClick: (contact: Contact) -> Unit
) {
    Row(
        modifier
            .clickable(onClick = {
                onContactClick(contact)
            })
            .padding(6.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        if (contact.photo_uri == null) {
            Box {
                Image(
                    painter = painterResource(id = R.drawable.profile_bg),
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(
                            CircleShape
                        )
                        .fillMaxHeight()
                )
                Text(
                    text = contact.display_name[0].toString(),
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            GlideImage(
                model = contact.photo_uri,
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            )
        }
        Text(
            text = contact.display_name,
            Modifier.padding(start = 20.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}