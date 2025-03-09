package com.aregyan.compose.data.db

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value == null) return ""
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(type)
        return adapter.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isEmpty()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(type)
        return adapter.fromJson(value) ?: emptyList()
    }
    
    @TypeConverter
    fun fromTimestamp(timestamp: Timestamp?): Long {
        return timestamp?.seconds?.times(1000)?.plus(timestamp.nanoseconds / 1000000) ?: 0L
    }
    
    @TypeConverter
    fun toTimestamp(milliseconds: Long): Timestamp {
        return Timestamp(milliseconds / 1000, ((milliseconds % 1000) * 1000000).toInt())
    }
}
