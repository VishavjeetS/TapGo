package com.example.tapgo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(val context:Context, val messageList:ArrayList<MessageModal>):
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>(){
    private val left = 0;
    private val right = 1;
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, ): MessageAdapter.MessageViewHolder {
        return if(viewType==right){
            val messageView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_sender, parent, false)
            return MessageViewHolder(messageView)
        }
        else{
            val messageView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_reciever, parent, false)
            return MessageViewHolder(messageView)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(messageList[position].sender == FirebaseAuth.getInstance().currentUser?.uid.toString()){
            left
        }
        else{
            right
        }

    }

    override fun onBindViewHolder(holder: MessageAdapter.MessageViewHolder, position: Int) {
        val list = messageList[position]
        holder.message.text = list.message
        holder.time.text = list.timeStamp
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    class MessageViewHolder(view:View):RecyclerView.ViewHolder(view){
        val message = view.findViewById<TextView>(R.id.msg_send)
        val time : TextView = view.findViewById(R.id.timeStamp_send)

    }

}
