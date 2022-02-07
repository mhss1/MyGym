package com.mhss.app.mygym.data

data class Exercise(
    val name: String,
    val sets: String,
    val date: Long
){
    constructor(): this("", "", 0)
}