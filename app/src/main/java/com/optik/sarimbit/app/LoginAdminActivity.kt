package com.optik.sarimbit.app

import android.graphics.PorterDuff
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.ktx.Firebase
import com.optik.sarimbit.R
import com.optik.sarimbit.app.model.AdminModelFirestore
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.app.util.CallbackFireStore
import com.optik.sarimbit.app.util.SessionManager
import com.optik.sarimbit.databinding.ActivityLoginAdminBinding
import com.optik.sarimbit.databinding.PopupForgetPasswordBinding

class LoginAdminActivity: BaseView() {
    private lateinit var binding: ActivityLoginAdminBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
        binding.btnLogin.setOnClickListener {
            if (binding.etEmail.isTextNotEmpty() && binding.etPassword.isTextNotEmpty()) {
                login(binding.etEmail.getValue(), binding.etPassword.getValue())
            }
        }
        binding.ivShowPass.setOnClickListener {
            if (binding.ivShowPass.tag == "0") {
                binding.ivShowPass.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.colorSecondary
                    ), PorterDuff.Mode.SRC_IN
                );
                binding.ivShowPass.tag = "1"
                binding.etPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
            } else {
                binding.ivShowPass.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.light_gray_2
                    ), PorterDuff.Mode.SRC_IN
                );
                binding.ivShowPass.tag = "0"
                binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            binding.etPassword.setSelection(binding.etPassword.getValue().length)
        }
        binding.tvForgetPass.setOnClickListener {
            showDialogForgetPass()
        }


        if (SessionManager.getIsLogin(this)) {
            goToPageAndClearPrevious(HomeActivity::class.java)
        }
    }

    private fun login(email: String, password: String) {
        showLoading("Loading")
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        getUserData(it.uid)
                    }
                } else {
                    hideLoading()
                    showToast(
                        task.exception?.message.toString()
                    )
                }
            }
    }


    private fun getUserData(userId: String) {
        firestoreUtil.readDataByDocumentId(
            "",
            TABLE_ADMIN,
            userId,
            false,
            object : CallbackFireStore.ReadDocumentData {
                override fun onSuccessReadData(document: DocumentSnapshot) {
                    if (document.exists()) {
                        val name = document.data?.get(AdminModelFirestore.NAME).toString()
                        val email = document.data?.get(AdminModelFirestore.EMAIL).toString()
                        val pin = document.data?.get(AdminModelFirestore.PIN).toString()
                        val securityQuestion = document.data?.get(AdminModelFirestore.SECURITY_QUESTION).toString()
                        val securityAnswer = document.data?.get(AdminModelFirestore.SECURITY_ANSWER).toString()
                        SessionManager.setAdminData(
                            this@LoginAdminActivity,
                            name, userId, email, pin, securityQuestion, securityAnswer
                        )
                        goToPageAndClearPrevious(VerifyOTPActivity::class.java)
                        hideLoading()
                    } else {
                        auth.signOut()
                        hideLoading()
                        showToast("Akun tidak ditemukan")
                    }
                }

                override fun onFailedReadData(message: String?) {
                    showToast(message)
                    hideLoading()
                }
            }
        )
    }
    private fun showDialogForgetPass() {
        val alertDialog: AlertDialog
        val builder = AlertDialog.Builder(this)
        val binding: PopupForgetPasswordBinding = PopupForgetPasswordBinding
            .inflate(LayoutInflater.from(this))
        builder.setView(binding.root)
        alertDialog = builder.create()
        binding.tvSubmit.setOnClickListener { _: View? ->
            if(binding.etEmail.isTextNotEmpty()){
                forgotPassword(binding.etEmail.getValue())
                alertDialog.dismiss()
            }
        }
        alertDialog.show()
    }

    private fun forgotPassword(email: String) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                showToast("Cek email untuk reset password")
            } else {
                showToast("Gagal reset password")
            }
        }
    }
}