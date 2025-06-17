package com.example.applibros.data

import android.content.Context
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión del contexto para acceder a DataStore
private val Context.dataStore by preferencesDataStore(name = "user_preferences")

// Claves dinámicas por usuario
object UserPreferencesKeys {
    fun fontSizeKey(userId: String) = floatPreferencesKey("font_size_$userId")
    fun fontFamilyKey(userId: String) = stringPreferencesKey("font_family_$userId")
}

// Gestor de preferencias por usuario
class UserPreferencesManager(
    private val context: Context,
    private val userId: String
) {
    // Tamaño de fuente del usuario
    val fontSizeFlow: Flow<Float> = context.dataStore.data
        .map { prefs -> prefs[UserPreferencesKeys.fontSizeKey(userId)] ?: 16f }

    // Fuente del usuario
    val fontFamilyFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[UserPreferencesKeys.fontFamilyKey(userId)] ?: "Default" }

    // Guardar tamaño
    suspend fun saveFontSize(size: Float) {
        context.dataStore.edit { prefs ->
            prefs[UserPreferencesKeys.fontSizeKey(userId)] = size
        }
    }

    // Guardar fuente
    suspend fun saveFontFamily(fontName: String) {
        context.dataStore.edit { prefs ->
            prefs[UserPreferencesKeys.fontFamilyKey(userId)] = fontName
        }
    }
}