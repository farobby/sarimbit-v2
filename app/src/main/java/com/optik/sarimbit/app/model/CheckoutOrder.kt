package com.optik.sarimbit.app.model


import java.io.Serializable

data class CheckoutOrder(
    var orderId: String = "",
    var userId: String = "",
    var totalPrice: Int = 0,
    var customerName: String = "",
    var customerPhone: String = "",
    var status: String = "",
    var receipt: String = "",
    var timestamp: Long = 0L,
    var items: List<CheckoutProduct> = emptyList()
) : Serializable
