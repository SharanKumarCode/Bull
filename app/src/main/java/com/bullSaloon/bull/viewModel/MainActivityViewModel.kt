package com.bullSaloon.bull.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bullSaloon.bull.genericClasses.dataClasses.SaloonDataClass
import com.google.firebase.firestore.GeoPoint

class MainActivityViewModel: ViewModel() {

    private var saloonDataList = MutableLiveData<MutableList<SaloonDataClass>>()
    private var saloonData = MutableLiveData<SaloonDataClass>()
    private var userLocationData = MutableLiveData<GeoPoint>()

    fun assignShopData(shop: MutableList<SaloonDataClass>){
        Log.i("TAGLocation","assignShopData")
        saloonDataList.value = shop
    }

    fun getShopDataList(): MutableLiveData<MutableList<SaloonDataClass>>{
        return saloonDataList
    }

    fun putShopData(id: String?){
        for (d in saloonDataList.value!!){
            if (d.saloonID == id){
                saloonData.value = d
            }
        }
    }

    fun getShopData():MutableLiveData<SaloonDataClass>{
        return saloonData
    }

    fun assignUserLocationData(data: GeoPoint){

        Log.i("TAGLocation","user location obtained : $data")
        userLocationData.value = data
    }

    fun getUserLocationData(): MutableLiveData<GeoPoint>{
        Log.i("TAGLocation","user location provided : ${userLocationData.value}")
        if (userLocationData.value == null){
            userLocationData.value = GeoPoint(0.0,0.0)
        }
        Log.i("TAGLocation","user location modified : ${userLocationData.value}")
        return userLocationData
    }



}