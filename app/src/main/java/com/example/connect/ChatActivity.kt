package com.example.connect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var adapter: ChatAdapter

    private val messageList = ArrayList<Message>()
    private var myUsername: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // 🔐 Firebase Auth
        auth = FirebaseAuth.getInstance()
        val uid = auth.uid

        if (uid == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 🧩 Views
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val etMessage = findViewById<EditText>(R.id.etMessage)
        val btnSend = findViewById<Button>(R.id.btnSend)

        // 📡 Database reference
        database = FirebaseDatabase.getInstance()
            .reference
            .child("chats")
            .child("global")

        // 📋 RecyclerView setup
        adapter = ChatAdapter(messageList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 👤 Fetch my username
        FirebaseDatabase.getInstance().reference
            .child("users")
            .child(uid)
            .child("username")
            .get()
            .addOnSuccessListener {
                myUsername = it.value?.toString() ?: "Unknown"
            }

        // 📤 Send message
        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()

            if (text.isEmpty()) return@setOnClickListener

            val message = Message(
                senderId = uid,
                senderName = myUsername,
                message = text,
                timestamp = System.currentTimeMillis()
            )

            database.push()
                .setValue(message)
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to send", Toast.LENGTH_SHORT).show()
                }

            etMessage.text.clear()
        }

        // 📥 Receive messages (REALTIME)
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()

                for (snap in snapshot.children) {
                    val msg = snap.getValue(Message::class.java)
                    if (msg != null) {
                        messageList.add(msg)
                    }
                }

                adapter.notifyDataSetChanged()

                if (messageList.isNotEmpty()) {
                    recyclerView.scrollToPosition(messageList.size - 1)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ChatActivity,
                    "Database error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })


    }
}
