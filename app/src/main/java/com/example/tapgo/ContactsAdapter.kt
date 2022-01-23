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
import com.example.tapgo.User
import com.squareup.picasso.Picasso

class ContactsAdapter(val context:Context, val contactList: ArrayList<User>):
    RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {
    class ContactViewHolder(view:View):RecyclerView.ViewHolder(view) {
        val name        : TextView  = view.findViewById(R.id.contact_name)
        val email       : TextView  = view.findViewById(R.id.contact_email)
        val status      : TextView  = view.findViewById(R.id.contact_status)
        val image       : ImageView = view.findViewById(R.id.contacts_img)
        val userContent : CardView  = view.findViewById(R.id.userContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val contactView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_contacts, parent, false)
        return ContactViewHolder(contactView)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val list = contactList[position]
        holder.name.text = list.profileName
        holder.email.text = list.profileEmail
        holder.status.text = list.profileStatus
        Picasso.get().load(list.profileDP).error(R.drawable.ic_person).into(holder.image)
        holder.userContent.setOnClickListener {
            val intent = Intent(context, MenuActivity::class.java).also {
                it.putExtra("OptionName", "contactMessaging")
                it.putExtra("chatroomID", list.chatRoomId)
                it.putExtra("friendUid", list.profileUid)
                it.putExtra("friendName", list.profileName)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return  contactList.size
    }

}