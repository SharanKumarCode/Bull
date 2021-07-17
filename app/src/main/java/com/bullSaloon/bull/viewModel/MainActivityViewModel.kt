package com.bullSaloon.bull.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bullSaloon.bull.genericClasses.ShopDataPreviewClass

class MainActivityViewModel: ViewModel() {

    private var shopDataList = MutableLiveData<MutableList<ShopDataPreviewClass>>()
    private var shopData = MutableLiveData<ShopDataPreviewClass>()

    fun assignShopData(shop: MutableList<ShopDataPreviewClass>){
        shopDataList.value = shop
    }

    fun getShopDataList(): MutableLiveData<MutableList<ShopDataPreviewClass>>{
        return shopDataList
    }

    fun putShopData(id: Number?){
        for (d in shopDataList.value!!){
            if (d.shopID == id){
                shopData.value = d
            }
        }
    }

    fun getShopData():MutableLiveData<ShopDataPreviewClass>{
        return shopData
    }

}