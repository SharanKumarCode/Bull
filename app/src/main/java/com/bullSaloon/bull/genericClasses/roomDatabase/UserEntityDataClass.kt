package com.bullSaloon.bull.genericClasses.roomDatabase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserEntityDataClass(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "user_name") val userName: String,
    @ColumnInfo(name = "user_id") val userID: String,
    @ColumnInfo(name = "mobile_number") val mobileNumber: String,
    @ColumnInfo(name = "profile_picture_path") val profilePicPath: String
)
