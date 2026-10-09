package com.example.data.session

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("friend_crat_session", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val userAdapter = moshi.adapter(User::class.java)

    companion object {
        private const val KEY_SESSION_TOKEN = "session_token"
        private const val KEY_USER_DATA = "user_data"
        const val SERVER_URL = "http://nile.hidencloud.com:24697"
    }

    var sessionToken: String?
        get() = prefs.getString(KEY_SESSION_TOKEN, null)
        set(value) {
            prefs.edit().putString(KEY_SESSION_TOKEN, value).apply()
        }

    var currentUser: User?
        get() {
            val json = prefs.getString(KEY_USER_DATA, null) ?: return null
            return try {
                userAdapter.fromJson(json)
            } catch (e: Exception) {
                null
            }
        }
        set(value) {
            if (value == null) {
                prefs.edit().remove(KEY_USER_DATA).apply()
            } else {
                prefs.edit().putString(KEY_USER_DATA, userAdapter.toJson(value)).apply()
            }
        }

    var appTheme: String
        get() = prefs.getString("app_theme", "lilac") ?: "lilac"
        set(value) {
            prefs.edit().putString("app_theme", value).apply()
        }

    fun isLoggedIn(): Boolean {
        return !sessionToken.isNullOrBlank()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
