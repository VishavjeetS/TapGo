package com.example.tapgo.Fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.tapgo.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_profile.*
import java.io.ByteArrayOutputStream

class Profile : Fragment() {
    private lateinit var imgProfileUserName         : TextView
    private lateinit var layoutName                  : TextView
    private lateinit var layoutEmail                 : TextView
    private lateinit var layoutStatus                : TextView
    private lateinit var profileImage                : CircleImageView
    private lateinit var profileImageAdd             : ImageView
    private lateinit var profileNameLayout           : TextInputLayout
    private lateinit var profileEmailLayout          : TextInputLayout
    private lateinit var profileStatusLayout         : TextInputLayout
    private lateinit var profileNameEdit             : TextInputEditText
    private lateinit var profileEmailEdit            : TextInputEditText
    private lateinit var profileStatusEdit           : TextInputEditText
    private lateinit var profileUpdateButton         : Button
    private lateinit var profileSaveButton           : Button
    private lateinit var auth                        : FirebaseAuth
    private lateinit var fStore                      : FirebaseFirestore
    private lateinit var db                          : DocumentReference
    private lateinit var userId                      : String
    private lateinit var image                       : ByteArray
    private lateinit var storageRef                  : StorageReference
    val register = registerForActivityResult(ActivityResultContracts.TakePicturePreview()){
        uploadImage(it)
    }

    @SuppressLint("ResourceType", "UseCompatLoadingForDrawables")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View? {
        // Inflate the layout for this fragment
        //For Status Bar Color
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        requireActivity().window.statusBarColor = Color.TRANSPARENT
        val background = this@Profile.resources.getDrawable(R.drawable.gradientbg)
        requireActivity().window.setBackgroundDrawable(background)
        val view =  inflater.inflate(R.layout.fragment_profile, container, false)
        auth                = FirebaseAuth.getInstance()
        fStore              = FirebaseFirestore.getInstance()
        userId              = auth.currentUser!!.uid
        storageRef          = FirebaseStorage.getInstance().reference.child("$userId/profilePhoto")
        imgProfileUserName = view.findViewById(R.id.profile_userName)
        layoutName          = view.findViewById(R.id.layout_name)
        layoutEmail         = view.findViewById(R.id.layout_email)
        layoutStatus        = view.findViewById(R.id.layout_status)
        profileImage        = view.findViewById(R.id.profile_image)
        profileImageAdd     = view.findViewById(R.id.profile_img_add)
        profileNameLayout   = view.findViewById(R.id.text_name_profile)
        profileEmailLayout  = view.findViewById(R.id.text_email_profile)
        profileStatusLayout = view.findViewById(R.id.text_status_profile)
        profileNameEdit     = view.findViewById(R.id.ed_text_name_profile)
        profileEmailEdit    = view.findViewById(R.id.ed_text_email_profile)
        profileStatusEdit   = view.findViewById(R.id.ed_text_status_profile)
        profileUpdateButton = view.findViewById(R.id.profile_update)
        profileSaveButton   = view.findViewById(R.id.profile_save)

        profileUpdateButton.visibility = View.VISIBLE
        db = fStore.collection("Users").document(userId)
        db.addSnapshotListener { value, error ->
            if(error != null){
                Log.d("Error", "Unable to fetch data")
            }
            else{
                layoutName.text     = value?.getString("userName")
                layoutEmail.text    = value?.getString("email")
                layoutStatus.text   = value?.getString("userStatus")
                imgProfileUserName.text = value?.getString("userName")
                Picasso.get().load(value?.getString("userDP")).error(R.drawable.ic_person).into(profileImage)
            }
        }
        profileUpdateButton.setOnClickListener {
            Log.d("Button Pressed", "Update Button Tapped")
            layoutName.visibility           = View.GONE
            layoutEmail.visibility          = View.GONE
            layoutStatus.visibility         = View.GONE
            profileNameLayout.visibility    = View.VISIBLE
            profileEmailLayout.visibility   = View.VISIBLE
            profileStatusLayout.visibility  = View.VISIBLE
            profileUpdateButton.visibility  = View.GONE
            profileSaveButton.visibility    = View.VISIBLE
            profileNameEdit.text            = Editable.Factory.getInstance().newEditable(layoutName.text.toString())
            profileEmailEdit.text           = Editable.Factory.getInstance().newEditable(layoutEmail.text.toString())
            profileStatusEdit.text          = Editable.Factory.getInstance().newEditable(layoutStatus.text.toString())
        }
        profileSaveButton.setOnClickListener {
            layoutName.visibility           = View.VISIBLE
            layoutEmail.visibility          = View.VISIBLE
            layoutStatus.visibility         = View.VISIBLE
            profileUpdateButton.visibility  = View.VISIBLE
            profileNameLayout.visibility    = View.GONE
            profileEmailLayout.visibility   = View.GONE
            profileStatusLayout.visibility  = View.GONE
            profileSaveButton.visibility    = View.GONE
            val obj = mutableMapOf<String, String>()
            obj["userName"]    = profileNameEdit.text.toString()
            obj["email"]        = profileEmailEdit.text.toString()
            obj["userStatus"]   = profileStatusEdit.text.toString()
            db.set(obj).addOnSuccessListener {
                Toast.makeText(requireContext(),"Data successfully updated", Toast.LENGTH_SHORT).show()
                Log.d("Success", "Data successfully updated")
            }
        }

        profileImageAdd.setOnClickListener {
            capturePhoto()
        }
        return view
    }

    private fun capturePhoto() {
        register.launch(null)
    }

    private fun uploadImage(it: Bitmap?) {
        val baos = ByteArrayOutputStream()
        it?.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        image = baos.toByteArray()
        storageRef.putBytes(image).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener {
                val obj = mutableMapOf<String, String>()
                obj["userDP"] = it.toString()
                db.update(obj as Map<String, Any>).addOnSuccessListener {
                    Log.d("onSuccess", "Profile Photo Uploaded")
                }
            }
        }
    }
}