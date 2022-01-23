package com.example.tapgo

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList

class SearchAdapter(val context: Context, private val searchList:ArrayList<User>):RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    class SearchViewHolder(view:View):RecyclerView.ViewHolder(view){
        val name      : TextView  = view.findViewById(R.id.contact_name)
        val email     : TextView  = view.findViewById(R.id.contact_email)
        val status    : TextView  = view.findViewById(R.id.contact_status)
        val image     : ImageView = view.findViewById(R.id.contacts_img)
        val addButton : Button    = view.findViewById(R.id.add_friend)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val contactView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_contacts, parent, false)
        return SearchAdapter.SearchViewHolder(contactView)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val list = searchList[position]
        holder.name.text            = list.profileName
        holder.email.text           = list.profileEmail
        holder.status.text          = list.profileStatus
        Picasso.get().load(list.profileDP).error(R.drawable.ic_person).into(holder.image)
        FirebaseFirestore.getInstance()
            .collection("Users").document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection("friends").whereEqualTo(FieldPath.documentId(), list.profileUid)
            .addSnapshotListener { snapshot, error ->
                if(error!=null){
                    Log.e("onError", "Some error occured")
                }
                else{
                    if (!snapshot?.isEmpty!!){
                        holder.addButton.visibility = View.GONE
                    }
                    else
                        holder.addButton.visibility = View.VISIBLE
                }
            }
        holder.addButton.setOnClickListener {
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val min = c.get(Calendar.MINUTE)
            val timestamp = "$hour:$min"
            val obj = mutableMapOf<String,String>().also {
                it["time"] = timestamp
            }
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid.toString()
            val friendUid = list.profileUid
            FirebaseFirestore.getInstance()
                .collection("Users")
                .document(currentUid)
                .collection("friends").document(friendUid).set(obj)
                .addOnSuccessListener {
                    Log.d("onSuccess", "Successfully added with ${list.profileUid}")
                }
            val obj1 = mutableMapOf<String,ArrayList<String>>().also {
                it["uids"] = arrayListOf(currentUid, friendUid)
            }
            FirebaseFirestore.getInstance().collection("chats").document().set(obj1)
                .addOnSuccessListener {
                    Log.d("onSuccess", "Successfully chat created with ${list.profileUid}")
            }
        }
    }

    override fun getItemCount(): Int {
        return searchList.size
    }

}
