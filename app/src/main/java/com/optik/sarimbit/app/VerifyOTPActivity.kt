package com.optik.sarimbit.app

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.app.util.SessionManager
import com.optik.sarimbit.databinding.ActivityOtpVerificationBinding
import com.optik.sarimbit.databinding.DialogForgetPinBinding

class VerifyOTPActivity : BaseView() {

    private lateinit var binding: ActivityOtpVerificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnVerify.setOnClickListener {
            val enteredPin = binding.pinView.text.toString()
            if (enteredPin == SessionManager.getPIN(this)) {
                SessionManager.setIsLogin(this,true);
                Toast.makeText(this, "PIN benar! Login berhasil.", Toast.LENGTH_SHORT).show()
                goToPageAndClearPrevious(ActivityAdminHome::class.java)
            } else {
                Toast.makeText(this, "PIN salah. Coba lagi.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
        binding.tvForgetPin.setOnClickListener {
            showForgetPinDialog()
        }
    }
    private fun showForgetPinDialog() {
        val dialogBinding = DialogForgetPinBinding.inflate(layoutInflater)
        dialogBinding.tvQuestion.text = SessionManager.getKeySecQuestion(this)
        dialogBinding.layoutNewPin.visibility = View.GONE

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(true)
            .setPositiveButton("Submit", null)
            .setNegativeButton("Batal", null)
            .create()

        dialog.setOnShowListener {
            val btnSubmit = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

            btnSubmit.setOnClickListener {
                val answerInput = dialogBinding.etAnswer.text.toString().trim()
                val savedAnswer = SessionManager.getKeySecAnswer(this)

                if (answerInput.equals(savedAnswer, ignoreCase = true)) {
                    dialogBinding.layoutNewPin.visibility = View.VISIBLE
                    btnSubmit.text = "Simpan PIN"
                    btnSubmit.setOnClickListener {
                        val newPin = dialogBinding.etNewPin.text.toString().trim()
                        if (newPin.length ==6) {
                            val firestore = FirebaseFirestore.getInstance()
                            firestore.collection("admin").document(SessionManager.getId(this))
                                .update("pin", newPin)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "PIN berhasil diubah", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                    goToPageAndClearPrevious(LoginAdminActivity::class.java)
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Gagal menyimpan PIN ke server", Toast.LENGTH_SHORT).show()
                                }

                        } else {
                            Toast.makeText(this, "PIN harus minimal 4 digit", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Jawaban salah.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }


}
