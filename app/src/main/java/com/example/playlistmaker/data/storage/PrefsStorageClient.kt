package com.example.playlistmaker.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.playlistmaker.data.StorageClient
import com.google.gson.Gson
import java.lang.reflect.Type

class PrefsStorageClient<T>(
    context: Context,
    prefsName: String,
    private val dataKey: String,
    private val type: Type,
) : StorageClient<T> {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    private val gson = Gson()

    override fun storeData(data: T) {
        prefs.edit {
            putString(dataKey, gson.toJson(data, type))
        }
    }

    override fun getData(): T? {
        val dataJson = prefs.getString(dataKey, null) ?: return null
        return gson.fromJson(dataJson, type)
    }
}
