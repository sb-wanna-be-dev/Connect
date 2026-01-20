package com.example.connect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        // 🧩 Views
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {

            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // 🛑 Basic validation
            if (username.isEmpty()) {
                etUsername.error = "Username required"
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                etEmail.error = "Email required"
                return@setOnClickListener
            }

            if (password.length < 6) {
                etPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            // 🔐 Create user
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {

                    val uid = auth.uid!!

                    // 👤 User object to save in DB
                    val userMap = HashMap<String, Any>()
                    userMap["uid"] = uid
                    userMap["username"] = username
                    userMap["email"] = email

                    // 💾 Save to Realtime Database
                    FirebaseDatabase.getInstance().reference
                        .child("users")
                        .child(uid)
                        .setValue(userMap)
                        .addOnSuccessListener {
                            // ✅ Go to chat
                            startActivity(Intent(this, ChatActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Failed to save user data",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        it.message ?: "Registration failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}
