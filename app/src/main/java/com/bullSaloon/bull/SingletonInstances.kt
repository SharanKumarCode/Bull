package com.bullSaloon.bull

import android.annotation.SuppressLint
import android.util.Log
import com.google.android.gms.common.internal.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

object SingletonInstances {

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    @SuppressLint("StaticFieldLeak")
    private lateinit var fireStore: FirebaseFirestore

    init {
        Log.i("TAGSplashScreenActivity" , "SingletonInstances instance is created")

        assignProductionReferences()

    }

    private fun assignProductionReferences(){
        Log.i("TAGSplashScreenActivity" , "SingletonInstances instance assigning production references")

        auth = FirebaseAuth.getInstance()

        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        fireStore = FirebaseFirestore.getInstance()

    }

    private fun assignEmulatorReferences(){
        Log.i("TAGSplashScreenActivity" , "SingletonInstances instance assigning emulator references")
        val localHost = "10.0.2.2"

        auth = FirebaseAuth.getInstance()
        auth.useEmulator(localHost, 9099)

        storage = FirebaseStorage.getInstance()
        storage.useEmulator(localHost, 9199)
        storageRef = storage.reference

        fireStore = FirebaseFirestore.getInstance()
        fireStore.useEmulator(localHost, 8080)

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()

        fireStore.firestoreSettings = settings
    }

    fun getAuthInstance(): FirebaseAuth {
        return auth
    }

    fun getFireStoreInstance(): FirebaseFirestore {
        return fireStore
    }

    fun getStorageReference(): StorageReference {
        return storageRef
    }
}