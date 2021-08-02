package com.bullSaloon.bull.genericClasses.dataClasses

import com.google.firebase.firestore.GeoPoint

data class SaloonDataClass(val saloonID: String?,
                           val saloonName:String?,
                           val areaName:String?,
                           val rating: Int?,
                           val imageSource:String?,
                           val openStatus:Boolean?,
                           val contact: String?,
                           val saloonAddress: String?,
                           val haircutPrice: Number?,
                           val reviewCount: Number?,
                           val locationData: GeoPoint?,
                           var distance: Float?)
