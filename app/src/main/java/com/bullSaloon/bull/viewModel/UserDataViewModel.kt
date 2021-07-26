package com.bullSaloon.bull.viewModel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bullSaloon.bull.genericClasses.SingletonUserData
import com.bullSaloon.bull.genericClasses.dataClasses.UserDataClass

class UserDataViewModel: ViewModel() {

    private var userBasicData = MutableLiveData<UserDataClass>()
    var userbasic = MutableLiveData<MutableMap<String,String>>()
    private var userDataProfilePic = MutableLiveData<Bitmap>()

    fun assignBasicUserData(data: UserDataClass){
        userBasicData.value = data
    }

    fun getUserBasicData(): MutableLiveData<UserDataClass>{
        return userBasicData
    }

    fun assignProfilePic(data: Bitmap){
        userDataProfilePic.value = data
    }

    fun getProfilePic(): MutableLiveData<Bitmap>{
        return userDataProfilePic
    }



}