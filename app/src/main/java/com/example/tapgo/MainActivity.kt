package com.example.tapgo

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.SearchView
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.tapgo.Fragments.Calls
import com.example.tapgo.Fragments.Chats
import com.example.tapgo.Fragments.Status
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager2        : ViewPager2
    private lateinit var tabLayout         : TabLayout
    private lateinit var toolbar           : Toolbar
    private lateinit var appPagerAdapter   : AppPagerAdapter
    private lateinit var showContacts      : FloatingActionButton
    private val titles = arrayListOf("Chats", "Status", "Calls")

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar         = findViewById(R.id.toolbar_main)
        tabLayout       = findViewById(R.id.tablayout)
        viewPager2      = findViewById(R.id.viewpager)
        showContacts    = findViewById(R.id.btContacts)
        toolbar.title   = "Tap Go"
        toolbar.setTitleTextColor(Color.WHITE)
        setSupportActionBar(toolbar)

        //For Status Bar Color
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        val background = this@MainActivity.resources.getDrawable(R.drawable.gradientbg3)
        window.setBackgroundDrawable(background)


        //Make adapter for main activity
        appPagerAdapter = AppPagerAdapter(this)
        viewPager2.adapter = appPagerAdapter
        TabLayoutMediator(tabLayout, viewPager2){ tab, position ->
            tab.text = titles[position]
        }.attach()

        showContacts.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            intent.putExtra("OptionName", "friends")
            startActivity(intent)
        }
    }

    //Adapter class for main activity
    class AppPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int {
            return 3
        }

        override fun createFragment(position: Int): Fragment {
            return when(position)
            {
                0 -> Chats()
                1 -> Status()
                2 -> Calls()
                else -> Chats()
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        for (i in 0 until menu!!.size()) {
            val drawable = menu.getItem(i).icon
            if (drawable != null) {
                drawable.mutate()
                drawable.setColorFilter(resources.getColor(R.color.white),
                    PorterDuff.Mode.SRC_ATOP)
            }
        }
        val item = menu.findItem(R.id.Search)
        val searchView = item?.actionView as SearchView
        searchView.queryHint = "Search People"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                Toast.makeText(applicationContext, query, Toast.LENGTH_SHORT).show()
                return false
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.logout_item -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(applicationContext, AuthenticationActivity::class.java))
                finish()
                Toast.makeText(this,"Log out", Toast.LENGTH_SHORT).show()
            }
            R.id.setting_item -> {
                val intent = Intent(this, MenuActivity::class.java)
                intent.putExtra("OptionName", "Settings")
                startActivity(intent)
            }
            R.id.profile_item -> {
                val intent = Intent(this, MenuActivity::class.java)
                intent.putExtra("OptionName", "Profile")
                startActivity(intent)
            }
            R.id.searchContacts -> {
                val intent = Intent(this, MenuActivity::class.java)
                intent.putExtra("OptionName", "Search")
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}