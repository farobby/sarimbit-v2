package com.optik.sarimbit.app.util

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FirebaseFirestore.setLoggingEnabled(true)
        val firestore = FirebaseFirestore.getInstance()
        firestore.useEmulator("10.0.2.2", 8080)
    }
}