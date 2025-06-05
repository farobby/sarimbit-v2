package com.optik.sarimbit.app

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import com.optik.sarimbit.R
import com.optik.sarimbit.app.model.UserModelFirestore
import com.optik.sarimbit.databinding.ActivityLoginBinding
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.app.util.CallbackFireStore
import com.optik.sarimbit.app.util.SessionManager

class LoginActivity : BaseView() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private val RC_SIGN_IN = 9001
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private var tableName = "user"
    private var typeUser = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
        typeUser = intent.getIntExtra("typeUser", TYPE_STUDENT)
        binding.btnRegister.setOnClickListener {
            goToPage(RegistrationActivity::class.java)
        }
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
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("427253955401-fo4pst4emqkf7fs2qa9r989tsqjfkbuv.apps.googleusercontent.com") // Replace with your Web Client ID from Firebase
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

// Set Google Sign-In Button Click Listener
        binding.google.setOnClickListener {
            signInWithGoogle()
        }

        if (SessionManager.getIsLogin(this)) {
            goToPageAndClearPrevious(HomeActivity::class.java)
        }
    }
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                handleGoogleSignInResult(account)
                Log.d("LoginActivity", "Google sign-trying")
//                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.d("LoginActivity", "Google sign-in failed ${e.message}")
                showToast("Google Sign-In failed")
            }
        }
    }
    private fun handleGoogleSignInResult(account: GoogleSignInAccount?) {
        if (account != null) {
            val email = account.email ?: return
            val userId = account.id ?: return
            val name = account.displayName ?: ""

            // Check if email exists in Firestore
            checkUserExists(email, userId, name)
        } else {
            showToast("Google Sign-In failed")
        }
    }
    private fun checkUserExists(email: String, id: String, name: String) {
        firestoreUtil.readDataSpecificCondition("",
            TABLE_CUSTOMER, UserModelFirestore.EMAIL, email,
            object : CallbackFireStore.ReadData {
                override fun onSuccessReadData(document: QuerySnapshot?) {
                    if (document != null && !document.isEmpty) {
                        // Email already exists, login success
                        val userDoc = document.documents[0]
                        val userId = userDoc.getString(UserModelFirestore.ID) ?: id
                        SessionManager.setCustData(this@LoginActivity, name, userId, email)
                        goToPageAndClearPrevious(HomeActivity::class.java)
                    } else {
                        // Email not found, register new user
                        registerData(id, name, email)
                    }
                }

                override fun onFailedReadData(message: String?) {
                    showToast("Failed to check user: $message")
                }
            }
        )
    }
    private fun registerData(id: String, name: String, email: String) {
        val data = HashMap<String, String>()
        data[UserModelFirestore.ID] = id
        data[UserModelFirestore.NAME] = name
        data[UserModelFirestore.EMAIL] = email

        firestoreUtil.insertData(
            TABLE_CUSTOMER, id, data,
            object : CallbackFireStore.TransactionData {
                override fun onSuccessTransactionData() {
                    SessionManager.setCustData(this@LoginActivity, name, id, email)
                    hideLoading()
                    goToPageAndClearPrevious(HomeActivity::class.java)
                }

                override fun onFailedTransactionData(message: String?) {
                    hideLoading()
                    showToast("Registration failed: $message")
                }
            }
        )
    }



    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        getUserData(it.uid)
                    }
                } else {
                    showToast("Authentication Failed")
                }
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
            tableName,
            userId,
            false,
            object : CallbackFireStore.ReadDocumentData {
                override fun onSuccessReadData(document: DocumentSnapshot) {
                    if (document.exists()) {
                        val name = document.data?.get(UserModelFirestore.NAME).toString()
                        val email = document.data?.get(UserModelFirestore.EMAIL).toString()
                        SessionManager.setCustData(
                            this@LoginActivity,
                            name, userId, email
                        )
                        goToPageAndClearPrevious(HomeActivity::class.java)
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

}