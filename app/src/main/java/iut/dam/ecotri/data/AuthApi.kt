package iut.dam.ecotri.data

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query



interface AuthApi {

    @POST("auth/register")
    fun register(@Body body: RegisterRequest): Call<UserDto>

    @POST("auth/login")
    fun login(@Body body: LoginRequest): Call<LoginResponse>

    @POST("auth/change-password")
    fun changePassword(@Body body: ChangePasswordRequest): Call<ApiMessage>

    // 🔹 Mot de passe oublié
    @POST("auth/forgot-password")
    fun forgotPassword(@Body body: ForgotPasswordRequest): Call<ApiMessage>

    @POST("auth/update-profile")
    fun updateProfile(@Body body: UpdateProfileRequest): Call<UserDto>

}
