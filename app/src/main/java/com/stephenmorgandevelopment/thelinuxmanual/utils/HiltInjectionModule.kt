package com.stephenmorgandevelopment.thelinuxmanual.utils

import android.content.Context
import androidx.room.Room
import com.stephenmorgandevelopment.thelinuxmanual.data.SimpleCommandsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class AppBindingModule {

    @Provides
    fun roomDatabase(
        @ApplicationContext applicationContext: Context,
    ): SimpleCommandsDatabase = Room.databaseBuilder(
        applicationContext,
        SimpleCommandsDatabase::class.java,
        "simple_commands",
    ).build()
}

//@InstallIn(ViewModelComponent::class)
//@Module
//class ManPageBindingModule {
//
//    @Provides
//    fun manPageViewModel(
//
//    )
//}