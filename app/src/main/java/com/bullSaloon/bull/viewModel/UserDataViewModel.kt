package com.bullSaloon.bull.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bullSaloon.bull.genericClasses.UserDataClass

class UserDataViewModel: ViewModel() {

    private var userData = MutableLiveData<UserDataClass>()
    private var userBasicData = MutableLiveData<Map<String,String>>()

    fun assignUserData(data: UserDataClass){
        userData.value = data
        userBasicData.value = mapOf("user_name" to data.user_name, "user_id" to data.user_id, "mobileNumber" to  data.mobileNumber)
    }

    fun getUserBasicData(): MutableLiveData<Map<String,String>>{
        return userBasicData
    }

}