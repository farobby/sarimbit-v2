package com.optik.sarimbit.app.model

data class ChatModel(
    val message: String?,
    val time: String,
    val isSender: Boolean,
    val questionList: List<String>? = ArrayList(), // Tambahkan untuk daftar pertanyaan

)
