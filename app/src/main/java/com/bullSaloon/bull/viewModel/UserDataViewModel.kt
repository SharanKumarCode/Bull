package com.bullSaloon.bull.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bullSaloon.bull.genericClasses.dataClasses.UserDataClass

class UserDataViewModel: ViewModel() {

    private var userBasicData = MutableLiveData<UserDataClass>()
    var userbasic = MutableLiveData<MutableMap<String,String>>()

    fun assignBasicUserData(data: UserDataClass){
        userBasicData.value = data
    }

    fun getUserBasicData(): MutableLiveData<UserDataClass>{
        return userBasicData
    }

}