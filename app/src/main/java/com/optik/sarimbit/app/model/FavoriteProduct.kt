package com.optik.sarimbit.app.model

import java.io.Serializable

data class FavoriteProduct(
    var image: String="",
    var price: Int=200000,
    var id: String="",
    var idUser: String="",
    var name: String="",
    var brand: String="",
    var description: String="",
    var categoryFrame: String="",
    var colors: ArrayList<String>? = ArrayList(),
    var tags: ArrayList<String>? = ArrayList(),
    var quantity: Int=0,
): Serializable