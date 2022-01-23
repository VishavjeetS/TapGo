package com.example.tapgo

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class ChatAdapter(val context: Context, private val chatList: ArrayList<ChatsModal>):RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    class ChatViewHolder(val view:View):RecyclerView.ViewHolder(view){
        val name        : TextView  = view.findViewById(R.id.receiver_name)
        val message     : TextView  = view.findViewById(R.id.txt_message)
        val image       : ImageView = view.findViewById(R.id.chat_img)
        val chatContent : CardView   = view.findViewById(R.id.chat_content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val chatView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_contacts, parent, false)
        return ChatViewHolder(chatView)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val list = chatList[position]
        holder.name.text = list.receiver
        holder.message.text = list.message
        Picasso.get().load(list.receiverImage).error(R.drawable.ic_person).into(holder.image)
        holder.chatContent.setOnClickListener {
            val intent = Intent(context, MenuActivity::class.java).also {
                it.putExtra("OptionName", "chatMessaging")
                it.putExtra("chatroom",list.docId)
                it.putExtra("receiverName", list.receiver)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }
}