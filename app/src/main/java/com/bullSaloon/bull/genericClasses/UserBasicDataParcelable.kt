package com.bullSaloon.bull.genericClasses

import android.os.Parcel
import android.os.Parcelable

class UserBasicDataParcelable(
    val user_id: String,
    val user_name: String,
    val mobileNumber: String): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(user_id)
        parcel.writeString(user_name)
        parcel.writeString(mobileNumber)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserBasicDataParcelable> {
        override fun createFromParcel(parcel: Parcel): UserBasicDataParcelable {
            return UserBasicDataParcelable(parcel)
        }

        override fun newArray(size: Int): Array<UserBasicDataParcelable?> {
            return arrayOfNulls(size)
        }
    }

}