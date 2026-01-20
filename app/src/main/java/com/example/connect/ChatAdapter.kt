package com.example.connect

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(
    private val messageList: List<Message>
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val currentUserId = FirebaseAuth.getInstance().uid

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        val container: LinearLayout = itemView as LinearLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messageList[position]

        holder.tvUsername.text = message.senderName
        holder.tvMessage.text = message.message

        val isSentByMe = message.senderId == currentUserId

        // 🔁 ALIGNMENT (LEFT / RIGHT)
        holder.container.gravity = if (isSentByMe) Gravity.END else Gravity.START

        // 🎨 BACKGROUND (NO GREEN)
        if (isSentByMe) {
            holder.tvMessage.setBackgroundResource(R.drawable.bg_message_sent)
            holder.tvUsername.visibility = View.GONE
        } else {
            holder.tvMessage.setBackgroundResource(R.drawable.bg_message_received)
            holder.tvUsername.visibility = View.VISIBLE
        }

        // 📏 MARGINS (WhatsApp-style spacing)
        val params = holder.tvMessage.layoutParams as LinearLayout.LayoutParams
        params.marginStart = if (isSentByMe) 80 else 8
        params.marginEnd = if (isSentByMe) 8 else 80
        params.topMargin = 4
        params.bottomMargin = 4
        holder.tvMessage.layoutParams = params
    }

    override fun getItemCount(): Int = messageList.size
}
