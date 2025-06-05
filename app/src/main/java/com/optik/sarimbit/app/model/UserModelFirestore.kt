package com.optik.sarimbit.app.model

class UserModelFirestore(
    var id:String= "",
    var name:String= "",
    var email:String= "",
) {
    companion object {
        const val ID = "id"
        const val NAME = "name"
        const val EMAIL = "email"
    }
}