package com.stephenmorgandevelopment.thelinuxmanual.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.stephenmorgandevelopment.thelinuxmanual.data.SimpleCommandsDatabase.Companion.TABLE_NAME
import com.stephenmorgandevelopment.thelinuxmanual.models.MatchingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SimpleCommandsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(simpleCommand: MatchingItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(simpleCommands: List<MatchingItem>)

    @Update
    fun update(simpleCommand: MatchingItem): Int

    @Delete
    suspend fun delete(simpleCommand: MatchingItem): Int

    @Query("DELETE FROM $TABLE_NAME")
    fun wipeTable()

    @Query("SELECT * FROM $TABLE_NAME WHERE id=:id")
    suspend fun getCommandBy(id: Long): MatchingItem?

    @Query(
        "SELECT * FROM $TABLE_NAME WHERE name LIKE :searchText " +
                "ORDER BY (name = :searchText) DESC, LENGTH(name)"
    )
    fun partialMatches(searchText: String): Flow<List<MatchingItem>>
}