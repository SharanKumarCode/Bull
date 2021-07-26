package com.bullSaloon.bull.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bullSaloon.bull.genericClasses.dataClasses.ShopDataPreviewClass

class MainActivityViewModel: ViewModel() {

    private var saloonDataList = MutableLiveData<MutableList<ShopDataPreviewClass>>()
    private var saloonData = MutableLiveData<ShopDataPreviewClass>()

    fun assignShopData(shop: MutableList<ShopDataPreviewClass>){
        saloonDataList.value = shop
    }

    fun getShopDataList(): MutableLiveData<MutableList<ShopDataPreviewClass>>{
        return saloonDataList
    }

    fun putShopData(id: String?){
        for (d in saloonDataList.value!!){
            if (d.saloonID == id){
                saloonData.value = d
            }
        }
    }

    fun getShopData():MutableLiveData<ShopDataPreviewClass>{
        return saloonData
    }



}