package com.trioWekWek.gotalk.activity

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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
import com.trioWekWek.gotalk.databinding.ActivityUsersBinding
import com.trioWekWek.gotalk.model.User
import java.io.IOException
import java.util.UUID

//
class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseUser : FirebaseUser
    private lateinit var databaseReference: DatabaseReference
    private lateinit var imgBack: ImageView
    private lateinit var userName: TextView
    private lateinit var userImage: ImageView
    private lateinit var userImageProfile: ImageView
    private lateinit var btnSave: Button
    private lateinit var progressBar: ProgressBar

    private var filePath:Uri? = null
    private val PICK_IMAGE_REQUEST:Int = 2023
    private lateinit var storage:FirebaseStorage
    private lateinit var storageRef:StorageReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        binding = ActivityProfileBinding.inflate(layoutInflater)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.uid)

        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference



        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                userName = findViewById(R.id.userName)
                userImage = findViewById(R.id.userImageProfile)

                userName.text = user!!.userName

                if (user.userImage == ""){
                    userImage.setImageResource(R.drawable.profile_image)
                }else{
                    Glide.with(this@ProfileActivity).load(user.userImage).into(userImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext,error.message,Toast.LENGTH_SHORT).show()
            }
        })

        imgBack = findViewById(R.id.imgBack)
        btnSave = findViewById(R.id.btnSave)
        userImageProfile = findViewById(R.id.userImageProfile)

        imgBack.setOnClickListener{
            onBackPressed()
        }

        userImageProfile.setOnClickListener {
            choseeImage()
        }

        btnSave.setOnClickListener {
            uploadImage()
        }
    }

    private fun choseeImage(){
        val intent:Intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent,"Select Image"),PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode != null){
            filePath = data!!.data
            try {
                var bitmap:Bitmap = MediaStore.Images.Media.getBitmap(contentResolver,filePath)
                userImageProfile.setImageBitmap(bitmap)
                btnSave.visibility = View.VISIBLE
            }catch (e:IOException){
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage(){
        if (filePath != null){


            var ref:StorageReference = storageRef.child("image/"+UUID.randomUUID().toString())
            ref.putFile(filePath!!)
                .addOnSuccessListener {
                        progressBar = findViewById(R.id.progressBar)
                        progressBar.visibility = View.GONE
                        Toast.makeText(applicationContext,"Uploaded",Toast.LENGTH_SHORT).show()
                        btnSave.visibility = View.GONE
                }
                .addOnFailureListener {
                        progressBar.visibility = View.GONE
                        Toast.makeText(applicationContext,"Failed" + it.message,Toast.LENGTH_SHORT).show()
                }
        }
    }

}