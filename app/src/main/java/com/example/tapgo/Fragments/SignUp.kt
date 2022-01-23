package com.example.tapgo.Fragments

import android.content.Intent
import android.net.Uri
import android.nfc.Tag
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.tapgo.MainActivity
import com.example.tapgo.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class SignUp : Fragment() {
    private lateinit var enterUserName       : EditText
    private lateinit var enterEmail          : EditText
    private lateinit var enterPassword       : EditText
    private lateinit var signUpBtn           : Button
    private lateinit var fAuth               : FirebaseAuth
    private lateinit var fStore              : FirebaseFirestore
    private lateinit var db                  : DocumentReference
    private lateinit var personFetchedName   : String
    private lateinit var personFetchedEmail  : String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)

        enterUserName       = view.findViewById(R.id.text_userName)
        enterEmail          = view.findViewById(R.id.text_email_signup)
        enterPassword       = view.findViewById(R.id.text_password_signup)
        signUpBtn           = view.findViewById(R.id.Sign_up)
        fAuth               = FirebaseAuth.getInstance()
        fStore              = FirebaseFirestore.getInstance()
        signUpBtn.setOnClickListener {
            Log.d("SignUp Button", "SignUp Button Tapped")
            val userName = enterUserName.text.toString()
            val email = enterEmail.text.toString()
            val password = enterPassword.text.toString()
            if(TextUtils.isEmpty(email)){
                enterEmail.error = "Email is required!"
            }
            else if(TextUtils.isEmpty(userName)){
                enterUserName.error = "Enter first name!"
            }
            else if(TextUtils.isEmpty(password)){
                enterPassword.error = "Enter password!"
            }
            else
                createAccount(email, password)
        }
        return view
    }

    private fun fetched(user: FirebaseUser) {
        val acct = GoogleSignIn.getLastSignedInAccount(requireContext())
        if (acct != null) {
            personFetchedName = acct.displayName.toString()
            personFetchedEmail = acct.email.toString()

            Log.d("Fetched", "Account Detail fetched, personName: ${personFetchedName}, email: $personFetchedEmail")
        }
    }

    private fun createAccount(email:String, password:String) {
        val userName = enterUserName.text.toString()
        fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {task ->
            if(task.isSuccessful){
                val uid = fAuth.currentUser?.uid
                db = fStore.collection("Users").document(uid.toString())
                val obj = mutableMapOf<String, String>()
                obj["userName"] = userName
                obj["userStatus"] = ""
                obj["email"] = email
                obj["userPassword"] = password
                db.set(obj).addOnSuccessListener {
                    startActivity(Intent(context, MainActivity::class.java))
                    Toast.makeText(context, "Account Created!", Toast.LENGTH_SHORT).show()
                    Log.d("On Success", "User Created Successfully")
                }
                db.set(obj).addOnFailureListener {

                }
            }
            else{
                Log.d("Error SignIn", "Error can't sign in ${task.toString()}")
                Toast.makeText(context, "Error! email already exists", Toast.LENGTH_SHORT).show()
            }
        }
    }
}