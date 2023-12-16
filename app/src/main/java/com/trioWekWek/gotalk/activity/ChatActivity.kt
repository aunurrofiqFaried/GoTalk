package com.trioWekWek.gotalk.activity

import android.content.BroadcastReceiver
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
    private lateinit var  btnSendMessage : ImageButton
    private lateinit var  etMessage : EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        var intent = getIntent()
        var userId = intent.getStringExtra("userId")

        imgProfile = findViewById(R.id.imgProfile)
        tvUserName = findViewById(R.id.tvUserName)
        imgBack = findViewById(R.id.imgBack)
        btnSendMessage = findViewById(R.id.btnSendMessage)
        etMessage = findViewById(R.id.etMessage)


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

        btnSendMessage.setOnClickListener({
            var message:String = etMessage.text.toString()

            if (message.isEmpty()){
                Toast.makeText(applicationContext,"message is empety",Toast.LENGTH_SHORT).show()
            }else{
                sendMessage(firebaseUser!!.uid,userId,message)
            }
        })


    }

    private fun sendMessage(senderId:String,receiverId:String,message:String){
        var reference:DatabaseReference? = FirebaseDatabase.getInstance().getReference()

        var hashMap:HashMap<String,String> = HashMap()
        hashMap.put("senderId",senderId)
        hashMap.put("receiverId",receiverId)
        hashMap.put("message",message)

        reference!!.child("Chat").push().setValue(hashMap)
    }
}