package com.example.tapgo

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tapgo.Fragments.Contacts
import com.example.tapgo.Fragments.Profile
import com.example.tapgo.Fragments.Settings
import com.example.tapgo.Fragments.messaging
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.activity_menu.*

class MenuActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    private lateinit var toolbar              : Toolbar
    private lateinit var frameLayout          : FrameLayout
    private lateinit var optionValue          : String
    private lateinit var queryTerm            : String
    private lateinit var searchRecyclerView   : RecyclerView
    private lateinit var searchLayoutManager  : RecyclerView.LayoutManager
    private lateinit var searchAdapter        : SearchAdapter
    private var register: ListenerRegistration? = null
    private val searchInfo = arrayListOf<User>()
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //For Status Bar Color
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        val background = this@MenuActivity.resources.getDrawable(R.drawable.gradientbg)
        window.setBackgroundDrawable(background)

        toolbar          = findViewById(R.id.toolbar_menu)
        frameLayout      = findViewById(R.id.frameLayout)
        if(intent!=null){
            optionValue = intent.getStringExtra("OptionName").toString()
            when(optionValue){
                "Profile" -> {
                    frameLayout.visibility = View.VISIBLE
                    supportFragmentManager.beginTransaction().replace(R.id.frameLayout, Profile()).commit()
                    toolbar_menu.title = "Profile"
                }
                "Settings" -> {
                    frameLayout.visibility = View.VISIBLE
                    supportFragmentManager.beginTransaction().replace(R.id.frameLayout, Settings()).commit()
                    toolbar_menu.title = "Settings"
                }
                "Search" -> {
                    searchRecyclerView = findViewById(R.id.search_recyclerView)
                    searchLayoutManager = LinearLayoutManager(this)
                    searchRecyclerView.visibility = View.VISIBLE
                    toolbar_menu.title = "Search Users"
                    setSupportActionBar(toolbar_menu)
                    searchRecyclerView.addItemDecoration(
                        DividerItemDecoration(
                            searchRecyclerView.context,
                            (searchLayoutManager as LinearLayoutManager).orientation))
                }
                "friends" -> {
                    frameLayout.visibility = View.VISIBLE
                    supportFragmentManager.beginTransaction().replace(R.id.frameLayout, Contacts()).commit()
                    toolbar_menu.title = "Friends"
                }
                "chatMessaging" -> {
                    frameLayout.visibility = View.VISIBLE
                    toolbar.title          = intent.getStringExtra("receiverName")
                    val fragmentName       = messaging()
                    val transaction        = supportFragmentManager.beginTransaction()
                    val bundle             = Bundle()
                    bundle.putString("documentId", intent.getStringExtra("chatroom"))
                    bundle.putString("documentId", intent.getStringExtra("receiverName"))
                    fragmentName.arguments = bundle
                    transaction.replace(R.id.frameLayout, fragmentName).commit()
                }
                "contactMessaging" -> {
                    frameLayout.visibility  = View.VISIBLE
                    toolbar.title           = intent.getStringExtra("friendName")
                    val fragmentName        = messaging()
                    val transaction         = supportFragmentManager.beginTransaction()
                    val contactBundle       = Bundle()
                    contactBundle.putString("chatRoomID",intent.getStringExtra("chatroomID"))
                    contactBundle.putString("friendUid", intent.getStringExtra("friendUid"))
                    fragmentName.arguments = contactBundle
                    transaction.replace(R.id.frameLayout, fragmentName).commit()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.contacts_menu, menu)
        val searchView = menu?.findItem(R.id.contacts_search)?.actionView as SearchView
        searchView.isSubmitButtonEnabled = true
        searchView.setOnQueryTextListener(this)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if(query != null){
            queryTerm = query
            if(queryTerm.isNotEmpty()){
                searchUsers()
            }
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if(newText != null){
            queryTerm = newText
            if(queryTerm.isNotEmpty()){
                searchUsers()
            }
        }
        return true
    }

    private fun searchUsers() {
        register = FirebaseFirestore.getInstance()
            .collection("Users")
            .orderBy("userName").startAt(queryTerm)
//            .whereGreaterThanOrEqualTo("userName", queryTerm)
            .addSnapshotListener {snapshot, error ->
                if(error!=null){
                    Log.e("onError", "Some error occured")
                }
                else{
                    if(!snapshot?.isEmpty!!){
                        searchInfo.clear()
                        val searchList = snapshot.documents
                        for(i in searchList){
                            if(i.id == FirebaseAuth.getInstance().currentUser?.uid){
                                Log.d("UserAccount", "This is user account so can't be displayed")
                            }
                            else {
                                val obj = User(
                                    i.id,
                                    i.getString("userName").toString(),
                                    i.getString("email").toString(),
                                    i.getString("userStatus").toString(),
                                    i.getString("userDP").toString(),
                                "0"
                                )
                                searchInfo.add(obj)
                                searchAdapter = SearchAdapter(this, searchInfo)
                                searchRecyclerView.adapter = searchAdapter
                                searchRecyclerView.layoutManager = searchLayoutManager
                            }
                        }
                    }
                }
        }
    }

    override fun onDestroy() {
        register?.remove()
        super.onDestroy()
    }
}