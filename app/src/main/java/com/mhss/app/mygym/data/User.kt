package com.mhss.app.mygym.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val name: String,
    val id: String,
    val type: String,
    var gym: String,
    var state: String,
    var sub_end: Long
): Parcelable {
    constructor() : this(""," ","","","", 0)
}
