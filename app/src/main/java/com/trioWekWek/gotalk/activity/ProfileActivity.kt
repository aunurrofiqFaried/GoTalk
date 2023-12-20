package com.trioWekWek.gotalk.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.Image
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContentProviderCompat.requireContext
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.trioWekWek.gotalk.R
import com.trioWekWek.gotalk.databinding.ActivityProfileBinding
//import com.trioWekWek.gotalk.databinding.ActivityProfileBinding
//import com.trioWekWek.gotalk.databinding.ActivityUsersBinding
import com.trioWekWek.gotalk.model.User
import java.io.IOException
import java.util.UUID

class ProfileActivity : AppCompatActivity() {
//    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseUser : FirebaseUser
    private lateinit var databaseReference: DatabaseReference
    private lateinit var imgBack: ImageView
    private lateinit var userImageProfile: ImageView
    private lateinit var etUserName: EditText
    private lateinit var btnSave: Button
    private lateinit var url: Uri
    private lateinit var usrID: String
    private lateinit var progressBar: ProgressBar

    private var filePath:Uri? = null
    private val PICK_IMAGE_REQUEST:Int = 2023
    private lateinit var storage:FirebaseStorage
    private lateinit var storageRef:StorageReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
//        binding = ActivityProfileBinding.inflate(layoutInflater)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.uid)

        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        userImageProfile = findViewById(R.id.userImageProfileEdit)
        imgBack = findViewById(R.id.imgBack)
        btnSave = findViewById(R.id.btnSave)

        val galerry = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                userImageProfile.setImageURI(it)
                url = it!!
            }
        )



        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                etUserName = findViewById(R.id.etUserName)

                etUserName.setText(user!!.userName)
                usrID = user.userId
                Glide.with(this@ProfileActivity).load(user.profileImage).placeholder(R.drawable.profile_image).into(userImageProfile)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext,error.message,Toast.LENGTH_SHORT).show()
            }
        })

        imgBack.setOnClickListener{
            onBackPressed()
        }

        userImageProfile.setOnClickListener {
            galerry.launch("image/*")
            btnSave.visibility = View.VISIBLE
        }


        btnSave.setOnClickListener {
            uploadImage(usrID,etUserName.text.toString())
        }
    }


    private fun uploadImage(id: String,username: String) {
        storage.getReference("image").child(System.currentTimeMillis().toString())
            .putFile(url)
            .addOnSuccessListener {task ->
                task.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener {
                        val user = mapOf<String,String>(
                            "userId" to id,
                            "userName" to username,
                            "profileImage" to it.toString()
                        )
                        val databaseReference = FirebaseDatabase.getInstance().getReference("users")
                        databaseReference.child(id).updateChildren(user).addOnSuccessListener {
                            Toast.makeText(this, "Update Data Berhasil", Toast.LENGTH_SHORT).show()
                        }
                    }

            }
    }

}