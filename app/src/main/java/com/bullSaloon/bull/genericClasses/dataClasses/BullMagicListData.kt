package com.bullSaloon.bull.genericClasses.dataClasses

data class BullMagicListData(
    val userId: String,
    val userName: String,
    val photoId: String,
    val imageRef: String,
    val timeStamp: String,
    val niceStatus: Boolean = false,
    val niceCount: Number = 0,
    val saloonName: String = "",
    val caption: String = "",
)
