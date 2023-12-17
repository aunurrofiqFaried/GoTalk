package com.trioWekWek.gotalk

import android.app.Notification
import android.provider.SyncStateContract
import com.trioWekWek.gotalk.Constants.Constants
import com.trioWekWek.gotalk.`interface`.NotificationApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {
    companion object{
        private val retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        val api by lazy {
            retrofit.create(NotificationApi::class.java)
        }
    }
}