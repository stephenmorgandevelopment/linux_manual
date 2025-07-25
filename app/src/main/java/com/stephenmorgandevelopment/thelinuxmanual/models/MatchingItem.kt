package com.stephenmorgandevelopment.thelinuxmanual.models

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.stephenmorgandevelopment.thelinuxmanual.data.SimpleCommandsDatabase.Companion
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Entity(tableName = Companion.TABLE_NAME)
@Parcelize
@Immutable
data class MatchingItem @JvmOverloads constructor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    @ColumnInfo(name = "description")
    val descriptionPreview: String? = null,
    val url: String,
    val sections: List<String>? = null,
) : Parcelable {
    @IgnoredOnParcel
    val needsDescription get() = this.descriptionPreview.isNullOrBlank()
}
