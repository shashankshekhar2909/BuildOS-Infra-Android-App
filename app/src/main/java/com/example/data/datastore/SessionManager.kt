package com.example.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "buildos_session")

class SessionManager(private val context: Context) {

    companion object {
        private val JWT_TOKEN = stringPreferencesKey("jwt_token")
        private val BASE_URL = stringPreferencesKey("base_url")
        private val USERNAME = stringPreferencesKey("username")
        private val USER_ROLE = stringPreferencesKey("user_role")
        private val DEMO_MODE = booleanPreferencesKey("demo_mode")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[JWT_TOKEN]
    }

    val baseUrlFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[BASE_URL] ?: "https://os.buildwithshashank.com"
    }

    val usernameFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USERNAME]
    }

    val userRoleFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USERNAME]?.let { preferences[USER_ROLE] ?: "viewer" }
    }

    val demoModeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DEMO_MODE] ?: false // default to false so users see real-time data from the backend by default!
    }

    suspend fun saveSession(token: String, username: String, role: String) {
        context.dataStore.edit { preferences ->
            preferences[JWT_TOKEN] = token
            preferences[USERNAME] = username
            preferences[USER_ROLE] = role
        }
    }

    suspend fun saveBaseUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[BASE_URL] = url
        }
    }

    suspend fun saveDemoMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DEMO_MODE] = enabled
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(JWT_TOKEN)
            preferences.remove(USERNAME)
            preferences.remove(USER_ROLE)
        }
    }
}
