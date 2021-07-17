package com.bullSaloon.bull.genericClasses

//data class UserDataClass(
//    val user_id: String,
//    val user_name: String,
//    val mobileNumber: String,
//    val reviews: MutableList<HashMap<String,String>> = mutableListOf(hashMapOf(
//        "timestamp" to "",
//        "target_saloon_id" to "",
//        "rating" to "",
//        "review" to ""
//    )),
//    val photos: MutableList<HashMap<String,String>> = mutableListOf(hashMapOf(
//        "timestamp" to "",
//        "target_saloon_id" to "",
//        "saloon_name" to "",
//        "imageRef" to ""
//    )),
//    val nices: MutableList<HashMap<String,Any>> = mutableListOf(hashMapOf(
//        "timestamp" to "",
//        "target_user_id" to "",
//        "nice_status" to false,
//        "target_imageRef" to ""
//    )),
//    val comments: MutableList<HashMap<String,Any>> = mutableListOf(hashMapOf(
//        "timestamp" to "",
//        "target_user_id" to "",
//        "comment" to ""
//    )),
//    val saved_styles:MutableList<HashMap<String,Any>> = mutableListOf(hashMapOf(
//        "timestamp" to "",
//        "imageRef" to ""
//    ))
//)

data class UserDataClass(
    val user_id: String,
    val user_name: String,
    val mobileNumber: String)
