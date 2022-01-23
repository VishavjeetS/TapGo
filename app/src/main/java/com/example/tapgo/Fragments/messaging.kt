package com.example.tapgo.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tapgo.MessageAdapter
import com.example.tapgo.MessageModal
import com.example.tapgo.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.util.*

class messaging : Fragment() {
    private lateinit var messageRecyclerView    : RecyclerView
    private lateinit var sendMsgET              : EditText
    private lateinit var sendMsgButton          : FloatingActionButton
    private lateinit var fStore                 : FirebaseFirestore
    private lateinit var fAuth                  : FirebaseAuth
    private lateinit var messageLayoutManager   : RecyclerView.LayoutManager
    private lateinit var messageAdapter         : MessageAdapter
    private lateinit var db                     : DocumentReference
    private lateinit var db1                    : DocumentReference
    private lateinit var userId                 : String
    private lateinit var friendId               : String
    private lateinit var chatroomid             : String
    private lateinit var chatId                 : String
    private lateinit var chatRoomUid            : String
    private var register                        : ListenerRegistration? = null
    private var register1                       : ListenerRegistration? = null

    private var messageInfo = arrayListOf<MessageModal>()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_messaging, container, false)
        messageRecyclerView     = view.findViewById(R.id.msgRecyclerView)
        sendMsgET               = view.findViewById(R.id.et_sendMsg)
        sendMsgButton           = view.findViewById(R.id.button_SendMsg)
        val values              = arguments
        if(values!=null) {
            friendId    = values.getString("friendName").toString()
            chatroomid  = values.getString("documentId").toString()
            initialization(chatroomid)
        }
        val contactBundle = arguments
        if(contactBundle!=null){
            friendId    = values!!.getString("friendUid").toString()
            chatroomid = values.getString("chatRoomID").toString()
            Log.d("logContactBundle", friendId)
            Log.d("logContactBundle", chatroomid)
            fetchChatRoomUid()
        }
        sendMsgButton.setOnClickListener {
            fetchMessageId()
        }
        return view
    }

    private fun fetchChatRoomUid(){
        fStore.collection("chats").whereEqualTo("chatroomid", chatroomid).get().addOnSuccessListener { query ->
            if(!query.isEmpty){
                chatRoomUid = query.documents[0].id
                Log.d("chatroomid",chatRoomUid)
                initialization(chatRoomUid)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fetchMessages(idMessages:String){
        register1 = fStore.collection("chats").document(chatroomid)
            .collection("message")
            .orderBy("id", Query.Direction.ASCENDING).addSnapshotListener { snapshot, exception ->
                if(exception!=null){
                    Log.d("Error", "Error in fetching data")
                }
                else{
                    val list = snapshot?.documents
                    if(list!=null){
                        for(doc in list){
                            db = fStore.collection("chats").document(doc.id).collection("message").document()
                            fStore.collection("chats").document(doc.id).collection("message")
                                .orderBy("id", Query.Direction.ASCENDING)
                                .addSnapshotListener { snapshot, exception ->
                                    if(snapshot!=null){
                                        val list = snapshot.documents
                                        messageInfo.clear()
                                        for (document in list){
                                            val obj = MessageModal(document.getString("messageSender").toString(),
                                                document.getString("message").toString(),
                                                document.getString("messageTime").toString())
                                            messageInfo.add(obj)
                                            messageAdapter = MessageAdapter(context as Activity, messageInfo)
                                            messageRecyclerView.adapter = messageAdapter
                                            messageRecyclerView.layoutManager = messageLayoutManager
                                            messageRecyclerView.scrollToPosition(list.size-1)
                                            messageRecyclerView.adapter!!.notifyDataSetChanged()
                                        }
                                    }
                                }
                        }
                    }
                }
            }
    }

    private fun fetchMessageId() {
        db = fStore.collection("chats").document(chatRoomUid)
            .collection("count").document("chatId")
        sendMsgButton.setOnClickListener {
            register = db.addSnapshotListener { value, error ->
                if(error!=null){
                    Log.d("", "")
                }
                else{
                    chatId = value?.getString("chatId").toString()
                    sendMessage()
                }
            }
        }
    }

    private fun recyclerViewBuild(id:String) {
        messageAdapter                    = MessageAdapter(context as Activity, messageInfo)
        messageRecyclerView.adapter       = messageAdapter
        messageRecyclerView.layoutManager = messageLayoutManager
        fetchMessages(id)
    }

    private fun initialization(id: String) {
        fStore                  = FirebaseFirestore.getInstance()
        fAuth                   = FirebaseAuth.getInstance()
        userId                  = fAuth.currentUser?.uid!!
        messageLayoutManager    = LinearLayoutManager(context)
        db1 = fStore.collection("chats").document(chatroomid).collection("message").document()
        recyclerViewBuild(id)
    }

    private fun sendMessage() {
        val message = sendMsgET.text.toString()
        if (TextUtils.isEmpty(message)) {
            sendMsgET.error = "Enter a message to send"
        } else {
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)
            val timeStamp = "$hour:$minute"
            val messageObj = mutableMapOf<String, Any>().also {
                it["message"] = message
                it["messageId"] = chatId
                it["messageSender"] = userId
                it["messageReceiver"] = friendId
                it["messageTime"] = timeStamp
            }
            db1.set(messageObj).addOnSuccessListener {
                Log.d("onSuccess", "Successfully Send Message")
            }
            val countid = mutableMapOf<String, String>()
            countid["chatid"] = (chatId.toInt() + 1).toString()
            db.set(countid).addOnSuccessListener {
                Log.d("onSuccess", "Successfully upDATED count messages")
            }
        }
    }
        override fun onDestroy() {
            register1!!.remove()
            super.onDestroy()
        }
}