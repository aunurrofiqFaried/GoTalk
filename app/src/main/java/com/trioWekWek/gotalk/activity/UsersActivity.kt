package com.trioWekWek.gotalk.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.trioWekWek.gotalk.R
import com.trioWekWek.gotalk.adapter.UserAdapter
import com.trioWekWek.gotalk.databinding.ActivityUsersBinding
import com.trioWekWek.gotalk.model.User
import com.trioWekWek.gotalk.model.listUSer

class UsersActivity : AppCompatActivity() {
    private lateinit var  binding: ActivityUsersBinding
    private lateinit var adapter: UserAdapter
    private lateinit var recycle: RecyclerView
    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)
        recycle = findViewById(R.id.userRecyclerView)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        recycle.layoutManager = LinearLayoutManager(this,LinearLayout.VERTICAL,false)

        getUserList()
    }

    fun getUserList() {
        val firebase: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
        val databaseReference:DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
        databaseReference.addValueEventListener(object :ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                listUSer.clear()

                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val user = dataSnapShot.getValue(User::class.java)

                    if (!user!!.userId.equals(firebase.uid)) {
                        listUSer.add(user)
                    }
                }
                adapter = UserAdapter(this@UsersActivity, listUSer)
                recycle.adapter = adapter
            }
        })
    }
}