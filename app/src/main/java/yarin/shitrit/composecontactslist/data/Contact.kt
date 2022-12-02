package yarin.shitrit.composecontactslist.data

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Contact(
    var id: Long,
    var lookup_key: String,
    var display_name: String,
    var first_name: String,
    var last_name: String,
    var phones_list: MutableList<Phone>,
    var emails_list: MutableList<Email>,
    var photo_uri: Uri? = null

) : Parcelable {

    fun hasNumber(number: String) = phones_list.find { phone -> phone.number == number } != null
    fun hasEmail(address: String) = emails_list.find { email -> email.address == address } != null

}