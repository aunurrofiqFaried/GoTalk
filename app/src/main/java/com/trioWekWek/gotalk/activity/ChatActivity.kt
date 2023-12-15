package com.trioWekWek.gotalk.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.trioWekWek.gotalk.R
import com.trioWekWek.gotalk.model.User

class ChatActivity : AppCompatActivity() {
    var firebaseUser:FirebaseUser? = null
    var reference:DatabaseReference? = null
    private lateinit var imgProfile : ImageView
    private lateinit var tvUserName : TextView
    private lateinit var  imgBack : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        var intent = getIntent()
        var userId = intent.getStringExtra("userId")

        imgProfile = findViewById(R.id.imgProfile)
        tvUserName = findViewById(R.id.tvUserName)
        imgBack = findViewById(R.id.imgBack)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("users").child(userId!!)

        imgBack.setOnClickListener{
            onBackPressed()
        }

        reference!!.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                val user = snapshot.getValue(User::class.java)
                tvUserName.text = user!!.userName
                if (user.profileImage == ""){
                    imgProfile.setImageResource(R.drawable.profile_image)
                }else{
                    Glide.with(this@ChatActivity).load(user.profileImage).into(imgProfile)
                }
            }
        })


    }
}