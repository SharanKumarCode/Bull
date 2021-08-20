package com.bullSaloon.bull.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bullSaloon.bull.genericClasses.dataClasses.BullMagicListData
import com.bullSaloon.bull.genericClasses.dataClasses.SaloonDataClass
import com.google.firebase.firestore.GeoPoint

class MainActivityViewModel: ViewModel() {

    private var saloonDataList = MutableLiveData<MutableList<SaloonDataClass>>()
    private var saloonData = MutableLiveData<SaloonDataClass>()
    private var userLocationData = MutableLiveData<GeoPoint>()
    private var saloonRefreshDataState = MutableLiveData<SaloonRefreshData>()
    private var bullMagicListData = MutableLiveData<MutableList<BullMagicListData>>()
    val temp = mutableListOf<BullMagicListData>()

    fun assignShopData(shop: MutableList<SaloonDataClass>){
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

    fun putBullMagicData(data: BullMagicListData){
        temp.add(data)
        bullMagicListData.value = temp
    }

    fun getBullMagicDataList(): MutableLiveData<MutableList<BullMagicListData>>{
        return bullMagicListData
    }

    fun assignUserLocationData(data: GeoPoint){

        userLocationData.value = data
    }

    fun getUserLocationData(): MutableLiveData<GeoPoint>{
        if (userLocationData.value == null){
            userLocationData.value = GeoPoint(0.0,0.0)
        }
        return userLocationData
    }

    fun setSaloonRefreshState(data: SaloonRefreshData){
        saloonRefreshDataState.value = data
    }

    fun getSaloonRefreshState(): MutableLiveData<SaloonRefreshData>{
        return saloonRefreshDataState
    }

    data class SaloonRefreshData(
        val saloonPhotosState: Boolean = false,
        val saloonReview: Boolean = false
    )

}