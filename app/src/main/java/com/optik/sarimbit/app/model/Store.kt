package com.optik.sarimbit.app.model

import java.io.Serializable

data class Store(
    var name: String = "",
    var hours: String = "",
    var phone: String = "",
    var imageUrl: String = "",
    var address: String = "",
    var date: String = "",
    var linkGmaps: String = "",
    var city: String = "",
    var photos: ArrayList<String>? = ArrayList()
) : Serializable
