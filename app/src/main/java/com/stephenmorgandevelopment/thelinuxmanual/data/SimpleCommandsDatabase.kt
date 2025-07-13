package com.stephenmorgandevelopment.thelinuxmanual.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.stephenmorgandevelopment.thelinuxmanual.models.MatchingItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Singleton

@Singleton
@Database(entities = [MatchingItem::class], version = 1)
@TypeConverters(RoomConverters::class)
abstract class SimpleCommandsDatabase : RoomDatabase() {
    abstract fun dao(): SimpleCommandsDao

    suspend fun hasData(): Boolean = withContext(Dispatchers.IO) {
        dao().getSingleCommand() != null
    }

    companion object {
        const val TABLE_NAME = "SimpleCommands"
    }
}
