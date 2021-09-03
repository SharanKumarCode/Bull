package com.bullSaloon.bull.genericClasses.dataClasses

import android.os.Parcel
import android.os.Parcelable

class UploadImageServicePayload(
    private val userID: String,
    private val photoFileTempPath: String,
    private val saloonName: String,
    private val captionText: String,
    private val activityFlag: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        userID = parcel.readString().toString(),
        photoFileTempPath = parcel.readString().toString(),
        saloonName = parcel.readString().toString(),
        captionText = parcel.readString().toString(),
        activityFlag = parcel.readString().toString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userID)
        parcel.writeString(photoFileTempPath)
        parcel.writeString(saloonName)
        parcel.writeString(captionText)
        parcel.writeString(activityFlag)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getUserID(): String {
        return userID
    }

    fun getPhotoFileTempPath(): String {
        return photoFileTempPath
    }

    fun getSaloonName(): String {
        return saloonName
    }

    fun getCaptionText(): String {
        return captionText
    }

    fun getActivityFlag(): String {
        return activityFlag
    }

    companion object CREATOR : Parcelable.Creator<UploadImageServicePayload> {
        override fun createFromParcel(parcel: Parcel): UploadImageServicePayload {
            return UploadImageServicePayload(parcel)
        }

        override fun newArray(size: Int): Array<UploadImageServicePayload?> {
            return arrayOfNulls(size)
        }
    }
}