package com.optik.sarimbit.app.model

data class OrderModel(
    val id: String,
    val customerName: String,
    val date: String,
    val price: String,
    val status: String // Bisa: "Baru", "Selesai", dll.
)
