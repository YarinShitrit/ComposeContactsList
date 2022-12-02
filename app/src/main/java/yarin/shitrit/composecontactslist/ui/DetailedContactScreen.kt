package yarin.shitrit.composecontactslist

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import yarin.shitrit.composecontactslist.data.*
import yarin.shitrit.composecontactslist.viewmodel.ContactsViewModel

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun DetailedContactScreen(onBackClick: () -> Unit, viewModel: ContactsViewModel) {
    val uiState by viewModel.detailedContactUiState.collectAsStateWithLifecycle()
    uiState.contact?.let { it1 ->
        DetailedContactContent(
            onBackClick = onBackClick,
            viewModel = viewModel,
            detailedContact = it1
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun DetailedContactContent(
    onBackClick: () -> Unit, viewModel: ContactsViewModel, detailedContact: Contact
) {
    detailedContact.let { contact: Contact ->
        Box(modifier = Modifier) {
            IconButton(onClick = {
                onBackClick()
                viewModel.setDetailedContact(null)
            }) {
                Icon(
                    Icons.Default.ArrowBack, contentDescription = null, tint = Color.Black
                )
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Box(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    //If user has no photo -> display a colored background with his first letter
                    if (contact.photo_uri == null) {
                        Image(
                            painter = painterResource(id = R.drawable.profile_bg),
                            contentDescription = null,
                            modifier = Modifier
                                .size(200.dp)
                                .clip(
                                    CircleShape
                                )
                                .fillMaxHeight()
                        )
                        Text(
                            text = contact.first_name[0].toString(),
                            modifier = Modifier.align(Alignment.Center),
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold
                        )
                        //Else, display his photo thumbnail
                    } else {
                        GlideImage(
                            model = contact.photo_uri,
                            contentDescription = null,
                            modifier = Modifier
                                .size(200.dp)
                                .clip(CircleShape)
                        )
                    }
                }
                Text(
                    text = contact.display_name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(
                        top = 50.dp, start = 10.dp, end = 10.dp
                    )
                )
                Divider(
                    thickness = 1.dp,
                    color = colorResource(id = R.color.blue),
                    modifier = Modifier.padding(
                        top = 10.dp, start = 10.dp, end = 10.dp
                    )
                )
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(10.dp)
                ) {
                    items(contact.phones_list) { phone ->
                        PhoneRow(phone)
                        PhoneTypeRow(phone)
                    }
                    if (contact.emails_list.isNotEmpty()) {
                        item {
                            Divider(
                                thickness = 1.dp,
                                color = colorResource(id = R.color.blue),
                                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
                            )
                        }
                    }
                    items(contact.emails_list) { email ->
                        EmailRow(email)
                        EmailTypeRow(email)
                    }
                }
            }
        }
    }
}

@Composable
fun PhoneRow(phone: Phone) {
    Text(
        text = phone.number,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 10.dp)
    )
}

@Composable
fun PhoneTypeRow(phone: Phone) {
    Row {
        Image(painterResource(id = R.drawable.ic_baseline_phone_20), contentDescription = null)
        Text(
            text = PhoneType.TYPE.getOrDefault(phone.type, "Other"),
            fontSize = 16.sp,
            fontWeight = FontWeight.Light
        )
    }
}

@Composable
fun EmailRow(email: Email) {
    Text(
        text = email.address,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 10.dp)
    )
}

@Composable
fun EmailTypeRow(email: Email) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(painterResource(id = R.drawable.ic_baseline_email_20), contentDescription = null)
        Text(
            text = EmailType.TYPE.getOrDefault(email.type, "Other"),
            fontSize = 16.sp,
            fontWeight = FontWeight.Light
        )
    }
}
