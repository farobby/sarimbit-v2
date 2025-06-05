package com.optik.sarimbit.app.model

import java.io.Serializable

data class Product(
    var image: String="",
    var price: Int=200000,
    var id: String="",
    var name: String="",
    var brand: String="",
    var description: String="",
    var categoryFrame: String="",
    var colors: ArrayList<String> = ArrayList(),
    var tags: ArrayList<String>? = ArrayList(),
    var variants: ArrayList<VariantModel> = ArrayList(),
    var quantity: Int=0,
): Serializable
