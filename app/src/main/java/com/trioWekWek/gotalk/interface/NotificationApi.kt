package com.trioWekWek.gotalk.`interface`

import com.trioWekWek.gotalk.Constants.Constants.Companion.CONTENT_TYPE
import com.trioWekWek.gotalk.Constants.Constants.Companion.SERVER_KEY
import com.trioWekWek.gotalk.model.PushNotification
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationApi {
    @Headers("Authorization: key=$SERVER_KEY","Content-type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ):Response<ResponseBody>
}