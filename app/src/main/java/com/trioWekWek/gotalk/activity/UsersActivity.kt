package com.trioWekWek.gotalk.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.messaging.FirebaseMessaging
import com.trioWekWek.gotalk.R
import com.trioWekWek.gotalk.adapter.UserAdapter
import com.trioWekWek.gotalk.databinding.ActivityUsersBinding
import com.trioWekWek.gotalk.firebase.FirebaseService
import com.trioWekWek.gotalk.model.User

class UsersActivity : AppCompatActivity() {
    private lateinit var  binding: ActivityUsersBinding
    private lateinit var recycle: RecyclerView
    private lateinit var imgProfile: ImageView
    private lateinit var imgBack: ImageView
    var userList = ArrayList<User>()

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)
        recycle = findViewById(R.id.userRecyclerView)
        binding = ActivityUsersBinding.inflate(layoutInflater)

        FirebaseService.sharedPref = getSharedPreferences("sharedPref", MODE_PRIVATE)
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            FirebaseService.token = it.token
        }

        recycle.layoutManager = LinearLayoutManager(this,LinearLayout.VERTICAL,false)

        imgProfile = findViewById(R.id.imgProfile)
        imgBack = findViewById(R.id.imgBack)


        imgBack.setOnClickListener{
            finishAffinity()
        }

        imgProfile.setOnClickListener {
            val intent = Intent(
                this@UsersActivity,
                ProfileActivity::class.java
            )
            startActivity(intent)
        }
        getUserList()
    }

    fun getUserList() {
        val firebase: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

        var userid = firebase.uid
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/$userid")

        val databaseReference:DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
        databaseReference.addValueEventListener(object :ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                val currentUser = snapshot.getValue(User::class.java)
                if (currentUser!!.profileImage == ""){
                    binding.imgProfile.setImageResource(R.drawable.profile_image)
                }else{
                    Glide.with(this@UsersActivity).load(currentUser.profileImage).into(binding.imgProfile)
                }

                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val user = dataSnapShot.getValue(User::class.java)

                    if (!user!!.userId.equals(firebase.uid)) {
                         userList.add(user)
                    }
                }
                val userAdapter = UserAdapter(this@UsersActivity, userList)
                recycle.adapter = userAdapter
            }
        })
    }
}