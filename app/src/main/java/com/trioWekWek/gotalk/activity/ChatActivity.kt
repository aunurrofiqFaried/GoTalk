package com.trioWekWek.gotalk.activity

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.gson.Gson
import com.trioWekWek.gotalk.R
import com.trioWekWek.gotalk.RetrofitInstance
import com.trioWekWek.gotalk.adapter.ChatAdapter
import com.trioWekWek.gotalk.adapter.UserAdapter
import com.trioWekWek.gotalk.databinding.ActivityUsersBinding
import com.trioWekWek.gotalk.model.Chat
import com.trioWekWek.gotalk.model.NotificationData
import com.trioWekWek.gotalk.model.PushNotification
import com.trioWekWek.gotalk.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    var firebaseUser:FirebaseUser? = null
    var reference:DatabaseReference? = null
    var chatlist = ArrayList<Chat>()
    var topic = ""
    private lateinit var  binding: ActivityUsersBinding
    private lateinit var imgProfile : ImageView
    private lateinit var tvUserName : TextView
    private lateinit var  imgBack : ImageView
    private lateinit var  btnSendMessage : ImageButton
    private lateinit var  etMessage : EditText
    private lateinit var recycle: RecyclerView



    @SuppressLint("MissingInflatedId", "WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        recycle = findViewById(R.id.chatRecyclerView)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        recycle.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL,false)

        var intent = getIntent()
        var userId = intent.getStringExtra("userId")
        var userName = intent.getStringExtra("userName")

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

        btnSendMessage.setOnClickListener{
            var message:String = etMessage.text.toString()

            if (message.isEmpty()){
                Toast.makeText(applicationContext,"message is empety",Toast.LENGTH_SHORT).show()
                etMessage.setText("")
            }else{
                sendMessage(firebaseUser!!.uid,userId,message)
                etMessage.setText("")
                topic = "/topics/$userId"
                PushNotification(NotificationData(userName!!,message),
                topic).also {
                    sendNotification(it)
                }
            }
        }

        readMessage(firebaseUser!!.uid,userId)
    }

    private fun sendMessage(senderId:String,receiverId:String,message:String){
        var reference:DatabaseReference? = FirebaseDatabase.getInstance().getReference()

        var hashMap:HashMap<String,String> = HashMap()
        hashMap.put("senderId",senderId)
        hashMap.put("receiverId",receiverId)
        hashMap.put("message",message)

        reference!!.child("chat").push().setValue(hashMap)
    }

    fun readMessage(senderId: String,receiverId: String) {
        val databaseReference:DatabaseReference =
            FirebaseDatabase.getInstance().getReference("chat")
        databaseReference.addValueEventListener(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatlist.clear()
                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val chat = dataSnapShot.getValue(Chat::class.java)

                    if (chat!!.senderId.equals(senderId) && chat!!.receiverId.equals(receiverId) ||
                        chat!!.senderId.equals(receiverId) && chat!!.receiverId.equals(senderId)
                    ) {
                        chatlist.add(chat)
                    }
                }
                val chatAdapter = ChatAdapter(this@ChatActivity, chatlist)
                recycle.adapter = chatAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO()
            }
        })
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful){
                Toast.makeText(this@ChatActivity, "Response ${Gson().toJson(response)}",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this@ChatActivity, response.errorBody().toString(),Toast.LENGTH_SHORT).show()

            }
        }catch (e:Exception){

        }
    }
}
