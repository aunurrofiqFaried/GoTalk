package com.trioWekWek.gotalk.activity

import android.content.Intent
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.trioWekWek.gotalk.R
import com.trioWekWek.gotalk.databinding.ActivityProfileBinding
import com.trioWekWek.gotalk.databinding.ActivityUsersBinding
import com.trioWekWek.gotalk.model.User

//
class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseUser : FirebaseUser
    private lateinit var databaseReference: DatabaseReference
    private lateinit var imgBack: ImageView
    private lateinit var userName: TextView
    private lateinit var userImage: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        binding = ActivityProfileBinding.inflate(layoutInflater)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.uid)

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                userName = findViewById(R.id.userName)
                userImage = findViewById(R.id.userImage)
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

        imgBack.setOnClickListener{
            onBackPressed()
        }
    }
}