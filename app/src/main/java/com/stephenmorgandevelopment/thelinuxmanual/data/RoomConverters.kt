package com.stephenmorgandevelopment.thelinuxmanual.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RoomConverters {
    private val type = object : TypeToken<List<String>>() {}.type

    @TypeConverter
    fun listToString(list: List<String>): String {
        return Gson().toJson(list, type)
    }

    @TypeConverter
    fun stringToList(string: String): List<String> {
        return Gson().fromJson(string, type)
    }

}