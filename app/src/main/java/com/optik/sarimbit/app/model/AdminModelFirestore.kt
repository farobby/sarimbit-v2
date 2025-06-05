package com.optik.sarimbit.app.model

class AdminModelFirestore(
    var id:String= "",
    var name:String= "",
    var email:String= "",
    var pin:String= "",
    var securityAnswer:String= "",
    var securityQuestion:String= "",
) {
    companion object {
        const val ID = "id"
        const val NAME = "name"
        const val EMAIL = "email"
        const val SECURITY_QUESTION = "security_question"
        const val SECURITY_ANSWER = "security_answer"
        const val PIN = "pin"
    }
}