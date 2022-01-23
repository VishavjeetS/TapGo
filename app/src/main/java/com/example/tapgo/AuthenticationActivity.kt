package com.example.tapgo

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.tapgo.Fragments.Login
import com.example.tapgo.Fragments.SignUp
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class AuthenticationActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener {
    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPagerAdapter: AuthenticationPagerAdapter
    private val titles = arrayListOf("Login", "SignUp")
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        val background = this@AuthenticationActivity.resources.getDrawable(R.drawable.img_bg)
        window.setBackgroundDrawable(background)

        //Initialize the viewPager and set the adapter
        viewPager2 = findViewById(R.id.authViewPager)
        tabLayout = findViewById(R.id.tabLayoutAuth)
        viewPagerAdapter = AuthenticationPagerAdapter(this)
        viewPager2.adapter = viewPagerAdapter
        TabLayoutMediator(tabLayout, viewPager2){tab, position ->
            tab.text = titles[position]
        }.attach()
    }
    class AuthenticationPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity){
        override fun getItemCount(): Int {
            return 2
        }

        override fun createFragment(position: Int): Fragment {
            return when(position){
                0 -> Login()
                1 -> SignUp()
                else -> Login()
            }
        }

    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(this)
        if(FirebaseAuth.getInstance().currentUser!=null){
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onAuthStateChanged(p0: FirebaseAuth) {
//        if(p0.currentUser!=null){
//            val intent = Intent(this,MainActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
    }

    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(this)
    }
}