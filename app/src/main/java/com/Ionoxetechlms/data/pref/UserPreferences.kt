package com.Ionoxetechlms.data.pref

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper object for persistent student session management
 */
object UserPreferences {
    private const val PREF_NAME = "ionoxe_lms_user_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_STUDENT_ID = "student_id"
    private const val KEY_STUDENT_NAME = "student_name"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUserSession(context: Context, studentId: Int, studentName: String) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putInt(KEY_STUDENT_ID, studentId)
            putString(KEY_STUDENT_NAME, studentName)
            apply()
        }
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getStudentId(context: Context): Int {
        return getPrefs(context).getInt(KEY_STUDENT_ID, 999)
    }

    fun getStudentName(context: Context): String {
        return getPrefs(context).getString(KEY_STUDENT_NAME, "Student") ?: "Student"
    }

    fun clearSession(context: Context) {
        getPrefs(context).edit().apply {
            clear()
            apply()
        }
    }
}
