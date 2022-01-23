package com.example.tapgo.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tapgo.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class Chats : Fragment() {
    private lateinit var chatRecyclerView   : RecyclerView
    private lateinit var chatLayoutManager  : RecyclerView.LayoutManager
    private lateinit var chatAdapter        : ChatAdapter
    private lateinit var fStore             : FirebaseFirestore
    private lateinit var auth               : FirebaseAuth
    private val chatsInfo = arrayListOf<ChatsModal>()

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {
        // Inflate the layout for this fragment
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        requireActivity().window.statusBarColor = Color.TRANSPARENT
        val background = this@Chats.resources.getDrawable(R.drawable.gradientbg)
        requireActivity().window.setBackgroundDrawable(background)
        val view          = inflater.inflate(R.layout.fragment_chats, container, false)
        chatRecyclerView  = view.findViewById(R.id.chatContentRecyclerView)
        chatLayoutManager = LinearLayoutManager(context as Activity)
        fStore            = FirebaseFirestore.getInstance()
        auth              = FirebaseAuth.getInstance()
        fStore.collection("chats").whereArrayContains("uids", auth.currentUser!!.uid).addSnapshotListener { snapshot, error ->
            if(error!=null){
                Log.d("", "")
            }
            else {
                if (!snapshot?.isEmpty!!) {
                    chatsInfo.clear()
                    val list = snapshot.documents
                    for (doc in list){
                        fStore.collection("chats").document(doc.id)
                            .collection("message").orderBy("id", Query.Direction.DESCENDING)
                            .addSnapshotListener { msgsnapshot, error ->
                            if(snapshot!=null){
                                Log.d("onError", "Some error occurred")
                            }
                            else{
                                val id = msgsnapshot!!.documents[0]
                                val message = id.get("message").toString()
                                val receiver = id.get("receiver").toString()
                                val receiverImage = id.get("profileDp").toString()
                                val obj = ChatsModal(receiver, id.getString("id").toString(), message, receiverImage)
                                chatsInfo.add(obj)
                            }
                        }
                        chatAdapter = ChatAdapter(context as Activity, chatsInfo)
                        chatRecyclerView.adapter = chatAdapter
                        chatRecyclerView.layoutManager = chatLayoutManager
                        chatRecyclerView
                            .addItemDecoration(DividerItemDecoration(
                                chatRecyclerView.context, (
                                        chatLayoutManager as LinearLayoutManager).orientation))
                    }
                }
            }
        }
        return view
    }
}