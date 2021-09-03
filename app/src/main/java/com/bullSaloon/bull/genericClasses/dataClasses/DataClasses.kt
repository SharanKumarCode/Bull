package com.bullSaloon.bull.genericClasses.dataClasses

import android.graphics.Bitmap
import com.google.firebase.firestore.GeoPoint

data class MyNicesData(
    val targetUserID: String,
    val targetPhotoID: String,
    val targetUserName: String,
    val targetImageRef: String,
    val targetUserProfilePicRef: String,
    val timeStamp: String
)

data class MyPhotosData(
    val photoID: String,
    val imageRef: String,
    val userName: String,
    val userID: String,
    val nices: Int,
    val timestamp: String,
    val saloonName: String,
    val caption: String
)

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

data class SaloonDataClass(val saloonID: String?,
                           val saloonName:String?,
                           val areaName:String?,
                           val rating: Int?,
                           val imageSource:String?,
                           val openStatus:Boolean?,
                           val contact: String?,
                           val saloonAddress: String?,
                           val haircutPrice: Number?,
                           val reviewCount: Number?,
                           val locationData: GeoPoint?,
                           var distance: Float?)

data class UserDataClass(
    val user_id: String,
    val user_name: String,
    val mobileNumber: String,
    val profilePicBitmap: Bitmap? = null)

data class CommentDataClass(
    val commentID: String,
    val commentUserID: String,
    val comment: String,
    val timestamp: String,
    val photoUserID: String
)
