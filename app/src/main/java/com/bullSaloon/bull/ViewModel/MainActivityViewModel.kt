package com.bullSaloon.bull.ViewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bullSaloon.bull.shopDataPreviewClass

class MainActivityViewModel: ViewModel() {

    private var shopDataList = MutableLiveData<MutableList<shopDataPreviewClass>>()
    private var shopData = MutableLiveData<shopDataPreviewClass>()

    fun assignShopData(shop: MutableList<shopDataPreviewClass>){
        shopDataList.value = shop
    }

    fun getShopDataList(): MutableLiveData<MutableList<shopDataPreviewClass>>{
        return shopDataList
    }

    fun putShopData(id: Number?){
        for (d in shopDataList.value!!){
            if (d.shopID == id){
                shopData.value = d
            }
        }
    }

    fun getShopData():MutableLiveData<shopDataPreviewClass>{
        return shopData
    }

}