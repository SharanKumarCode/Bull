package com.bullSaloon.bull.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bullSaloon.bull.genericClasses.dataClasses.MyPhotosData

class YourProfilePhotoViewModel: ViewModel() {

    private var userPhotoData = MutableLiveData<MyPhotosData>()

    fun assignUserPhotoData(data: MyPhotosData){
        userPhotoData.value = data
    }

    fun getUserPhotoData(): MutableLiveData<MyPhotosData>{
        return userPhotoData
    }
}