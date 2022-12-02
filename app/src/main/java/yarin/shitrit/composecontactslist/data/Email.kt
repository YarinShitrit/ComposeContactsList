package yarin.shitrit.composecontactslist.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Email(
    var address: String,
    var type: Int
) : Parcelable