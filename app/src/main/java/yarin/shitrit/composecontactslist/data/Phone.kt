package yarin.shitrit.composecontactslist.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Phone(
    var number: String,
    var type: Int
) : Parcelable