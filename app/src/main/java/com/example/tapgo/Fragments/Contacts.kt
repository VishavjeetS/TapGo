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
import com.example.tapgo.ContactsAdapter
import com.example.tapgo.R
import com.example.tapgo.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Contacts : Fragment() {
    private lateinit var contactsRecyclerView   : RecyclerView
    private lateinit var contactLayoutManager   : RecyclerView.LayoutManager
    private lateinit var contactsAdapter        : ContactsAdapter
    private lateinit var fStore                 : FirebaseFirestore
    private lateinit var auth                   : FirebaseAuth
    private lateinit var userId                 : String
    private val contactInfo = arrayListOf<User>()
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {
        // Inflate the layout for this fragment
        //For Status Bar Color
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        requireActivity().window.statusBarColor = Color.TRANSPARENT
        val background = this@Contacts.resources.getDrawable(R.drawable.gradientbg)
        requireActivity().window.setBackgroundDrawable(background)
        val view =  inflater.inflate(R.layout.fragment_contacts, container, false)
        contactsRecyclerView = view.findViewById(R.id.contacts_recycler_view)
        contactLayoutManager = LinearLayoutManager(context as Activity)
        fStore               = FirebaseFirestore.getInstance()
        auth                 = FirebaseAuth.getInstance()
        userId               = auth.currentUser?.uid.toString()
        fStore.collection("Users").document(userId).collection("friends").get().addOnSuccessListener {
            if(!it.isEmpty){
                contactInfo.clear()
                val listContact = it.documents
                for(i in listContact){
                    val friendId = i.id
                    val chatRoomId = i.getString("chatroomid")
                    fStore.collection("Users").document(friendId).addSnapshotListener { value, error ->
                        if (error!=null){
                            Log.d("", "")
                        }
                        else if(i.id == auth.currentUser?.uid){
                            Log.d("User Account", "This is user account so can't be displayed")
                        }
                        else{
                            val contact = User(
                                friendId,
                                value!!.getString("userName").toString(),
                                value.getString("email").toString(),
                                value.getString("userStatus").toString(),
                                value.getString("userDP").toString(),
                                chatRoomId.toString()
                            )
                            contactInfo.add(contact)
                            contactsAdapter = ContactsAdapter(context as Activity, contactInfo)
                            contactsRecyclerView.adapter = contactsAdapter
                            contactsRecyclerView.layoutManager = contactLayoutManager
                            contactsRecyclerView.addItemDecoration(
                                DividerItemDecoration(
                                    contactsRecyclerView.context,
                                    (contactLayoutManager as LinearLayoutManager).orientation))
                        }
                    }
                }
            }
        }
        return view
    }


}