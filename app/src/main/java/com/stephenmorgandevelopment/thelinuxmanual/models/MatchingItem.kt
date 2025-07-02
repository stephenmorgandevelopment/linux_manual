package com.stephenmorgandevelopment.thelinuxmanual.models

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.stephenmorgandevelopment.thelinuxmanual.data.SimpleCommandsDatabase.Companion
import kotlinx.parcelize.Parcelize

@Entity(tableName = Companion.TABLE_NAME)
@Parcelize
@Immutable
data class MatchingItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val description: String? = null,
    val url: String,
    val manN: Int,
) : Parcelable {

    @Ignore
    constructor(simpleCommand: SimpleCommand) :
            this(
                simpleCommand.id,
                simpleCommand.name,
                simpleCommand.description,
                simpleCommand.url,
                simpleCommand.manN,
            )

    val needsDescription get() = this.description.isNullOrBlank()

    fun toSimpleCommand() = SimpleCommand(id, name, description, url, manN)
}

fun List<SimpleCommand>.toMatchingItems() = this.map {
    with(it) {
        MatchingItem(0L, name, description, url, manN)
    }
}

fun SimpleCommand.toMatchingItem() = MatchingItem(this)
val SimpleCommand.needsDescription get() = description.isNullOrBlank()

