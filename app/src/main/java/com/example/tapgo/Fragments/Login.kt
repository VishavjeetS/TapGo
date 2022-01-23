package com.example.tapgo.Fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import androidx.activity.result.ActivityResultLauncher
import com.example.tapgo.MainActivity
import com.example.tapgo.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseUser


class Login : Fragment() {
    private lateinit var enterEmail          : EditText
    private lateinit var enterPassword       : EditText
    private lateinit var loginButton         : Button
    private lateinit var googleButton        : Button
    private lateinit var googleSignInOptions : GoogleSignInOptions
    private lateinit var mGoogleSignInClient : GoogleSignInClient
    private lateinit var mAuth               : FirebaseAuth
    private lateinit var v                   : View
    private lateinit var personFetchedName   : String
    private lateinit var personFetchedEmail  : String
    private lateinit var db                  : DocumentReference
    private lateinit var fStore              : FirebaseFirestore

    private companion object {
        private const val RC_SIGN_IN = 1011;
        private const val TAG = "GOOGLE_SIGN_IN_TAG"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v =  inflater.inflate(R.layout.fragment_login, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enterEmail      = v.findViewById(R.id.text_email_login)
        enterPassword   = v.findViewById(R.id.text_password_login)
        loginButton     = v.findViewById(R.id.Login)
        googleButton    = v.findViewById(R.id.Sign_in_google)
        mAuth           = FirebaseAuth.getInstance()
        fStore          = FirebaseFirestore.getInstance()

        loginButton.setOnClickListener {
            val email = enterEmail.text.toString()
            val password = enterPassword.text.toString()
            when {
                TextUtils.isEmpty(email) -> {
                    enterEmail.error = "Email is required!"
                }
                TextUtils.isEmpty(password) -> {
                    enterPassword.error = "Enter password!"
                }
                else -> signIn(email, password)
            }
        }

        googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_server_client))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions)


        googleButton.setOnClickListener {
            Log.d(TAG, "OnCreate: begin Google SignIn")
            val intent = mGoogleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                val account = task.getResult(ApiException::class.java)
                val user = mAuth.currentUser
                Log.d("Gmail ID", "firebaseAuthWithGoogle: $account")
                firebaseAuthWithGoogle(account?.idToken)
                if (user != null) {
                    updateUI(user)
                }
            }
            catch (e:ApiException){
                Log.d("Error", "Google Sign in failed ", e)
            }
        }
    }

    private fun updateUI(user:FirebaseUser) {
        val acct = GoogleSignIn.getLastSignedInAccount(requireContext())
        if (acct != null) {
            personFetchedName = acct.displayName.toString()
            personFetchedEmail = acct.email.toString()

            val uid = user.uid
            db = fStore.collection("Users").document(uid.toString())
            val obj = mutableMapOf<String, String>()
            obj["userName"] = personFetchedName
            obj["userStatus"] = ""
            obj["email"] = personFetchedEmail
            db.set(obj).addOnSuccessListener {
                Toast.makeText(context, "Welcome! $personFetchedEmail", Toast.LENGTH_SHORT).show()
                Log.d("Fetched", "Account Detail fetched, personName: ${personFetchedName}, email: $personFetchedEmail")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount: begin firebase auth with google account")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                //Login Success
                Log.d(TAG, "firebaseAuthWithGoogleAccount: LoggedIn")

                //Get Logged User
                val firebaseUser = mAuth.currentUser

                //get user info
                val uid = firebaseUser!!.uid
                val email = firebaseUser.email

                Log.d(TAG, "firebaseAuthWithGoogleAccount: Email: $email")
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Uid: $uid")


                //Check if user is new or existing
                if(authResult.additionalUserInfo!!.isNewUser){
                    //User is new - Account Created
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Account Created....\n$email")
                    Toast.makeText(context, "Account Created....\n$email", Toast.LENGTH_SHORT).show()
                    updateUI(firebaseUser)
                }
                else{
                    //Existing User
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Existing User\n$email")
                    Toast.makeText(context, "Existing User\n$email", Toast.LENGTH_SHORT).show()
                    updateUI(firebaseUser)
                }

                //Start Profile Activity
                startActivity(Intent(context, MainActivity::class.java))

            }
            .addOnFailureListener { e ->
                //Login Failed
                Log.d(TAG, "firebaseAuthWithGoogleAccount: Login Failed due to ${e.message}")
                Toast.makeText(context, "Login Failed due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun signIn(email:String, password:String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener {task ->
            if(task.isSuccessful){
                Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(context, MainActivity::class.java))
            }
        }
    }
}