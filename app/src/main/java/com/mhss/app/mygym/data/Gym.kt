package com.mhss.app.mygym.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Gym(
    val name: String,
    val owner: String,
    val id: String
): Parcelable {
    constructor(): this("", "", "")
}