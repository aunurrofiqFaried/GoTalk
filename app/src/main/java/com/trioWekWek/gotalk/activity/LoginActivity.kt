package com.trioWekWek.gotalk.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.trioWekWek.gotalk.R
import com.trioWekWek.gotalk.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseUser : FirebaseUser
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = FirebaseAuth.getInstance()
//        firebaseUser = auth.currentUser!!
//        cek apa user sudah login akan di arahkan ke useractivity
        if (auth.currentUser != null){
            val intent = Intent(
                this@LoginActivity, UsersActivity
                ::class.java
            )
            startActivity(intent)
            finish()
        }

        binding.btnSignInLogin.setOnClickListener {
            val email = binding.etEmailLogin.text.toString()
            val password = binding.etPasswordLogin.text.toString()

            if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {
                Toast.makeText(
                    applicationContext,
                    "email and password are required",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                auth!!.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) {
                        if (it.isSuccessful) {
                            binding.etEmailLogin.setText("")
                            binding.etPasswordLogin.setText("")
                            val intent = Intent(
                                this@LoginActivity, UsersActivity
                                ::class.java
                            )
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "email or password invalid",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }

        binding.btnSignUpLogin.setOnClickListener {
            val intent = Intent(
                this@LoginActivity,
                SignUpActivity::class.java
            )
            startActivity(intent)
            finish()
        }
    }
}