package com.bullSaloon.bull.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bullSaloon.bull.genericClasses.dataClasses.MyNicesData
import com.bullSaloon.bull.genericClasses.dataClasses.MyPhotosData

class YourProfileViewModel: ViewModel() {

    private var userPhotoData = MutableLiveData<MyPhotosData>()
    private var nicesDataList = MutableLiveData<MutableList<MyNicesData>>()
    private var nicesData = MutableLiveData<MyNicesData>()

    fun assignUserPhotoData(data: MyPhotosData){
        userPhotoData.value = data
    }

    fun getUserPhotoData(): MutableLiveData<MyPhotosData>{
        return userPhotoData
    }

    fun assignNicesData(data: MutableList<MyNicesData>){
        nicesDataList.value = data
    }

    fun getNicesDataList(): MutableLiveData<MutableList<MyNicesData>>{
        return nicesDataList
    }
}