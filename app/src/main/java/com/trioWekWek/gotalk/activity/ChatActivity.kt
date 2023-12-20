package com.trioWekWek.gotalk.activity

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
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
    private lateinit var  imgMessage : ImageView
    private lateinit var  btnSendMessage : ImageButton
    private lateinit var  btnSendImage : ImageButton
    private lateinit var  etMessage : EditText
    private lateinit var recycle: RecyclerView

    private lateinit var url: Uri
    private var filePath: Uri? = null
    private val PICK_IMAGE_REQUEST:Int = 2023
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference



    @SuppressLint("MissingInflatedId", "WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        recycle = findViewById(R.id.chatRecyclerView)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        recycle.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL,false)

        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        var intent = getIntent()
        var userId = intent.getStringExtra("userId")
        var userName = intent.getStringExtra("userName")

        imgProfile = findViewById(R.id.imgProfile)
        tvUserName = findViewById(R.id.tvUserName)
//        imgMessage = findViewById(R.id.imgMessage)
        imgBack = findViewById(R.id.imgBack)
        btnSendMessage = findViewById(R.id.btnSendMessage)
        btnSendImage = findViewById(R.id.btnSendImage)
        etMessage = findViewById(R.id.etMessage)


        val galerry = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                btnSendImage.setImageURI(it)
                url = it!!
            }
        )


        firebaseUser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("users").child(userId!!)

        imgBack.setOnClickListener{
            onBackPressed()
        }

        btnSendImage.setOnClickListener {
            galerry.launch("image/*")
            uploadImage()
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
                Toast.makeText(applicationContext,"message is empty",Toast.LENGTH_SHORT).show()
                etMessage.setText("")
            }else{
                sendMessage(firebaseUser!!.uid,userId,message, imgMessage = String())
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

    private fun uploadImage() {
        storage.getReference("image").child(System.currentTimeMillis().toString())
            .putFile(url)
            .addOnSuccessListener {task ->
                task.metadata!!.reference!!.downloadUrl
            }
    }

    private fun sendMessage(senderId:String,receiverId:String,message:String,imgMessage:String){
        var reference:DatabaseReference? = FirebaseDatabase.getInstance().getReference()

        var hashMap:HashMap<String,String> = HashMap()
        hashMap.put("senderId",senderId)
        hashMap.put("receiverId",receiverId)
        hashMap.put("message",message)
        hashMap.put("imgMessage",imgMessage)

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
