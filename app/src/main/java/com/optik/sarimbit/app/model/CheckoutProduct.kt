package com.optik.sarimbit.app.model


import java.io.Serializable

data class CheckoutProduct(
    var image: String = "",
    var price: Int = 200000,
    var id: String = "",
    var name: String = "",
    var brand: String = "",
    var description: String = "",
    var categoryFrame: String = "",
    var colors: String="",
    var frameVariant: String="",
    var tags: ArrayList<String>? = ArrayList(),
    var quantity: Int = 0, // Stok yang tersedia di database

    // Tambahan untuk checkout
    var selectedQuantity: Int = 1, // Jumlah yang dipilih oleh user
    var totalPrice: Int = price * selectedQuantity, // Total harga berdasarkan jumlah
    var isChecked: Boolean = false, // Untuk menandai produk yang akan dibeli
    var isCheckedOut: Boolean = false
) : Serializable {

    fun updateTotalPrice() {
        totalPrice = price * selectedQuantity
    }
}
