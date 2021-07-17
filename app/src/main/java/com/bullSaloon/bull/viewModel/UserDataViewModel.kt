package com.bullSaloon.bull.viewModel

import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bullSaloon.bull.genericClasses.UserDataClass
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.FirebaseAuthKtxRegistrar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.Serializable

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