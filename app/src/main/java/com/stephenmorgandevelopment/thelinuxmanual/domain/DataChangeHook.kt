package com.stephenmorgandevelopment.thelinuxmanual.domain

//import android.content.Context
//import com.stephenmorgandevelopment.thelinuxmanual.data.DatabaseHelper
//import com.stephenmorgandevelopment.thelinuxmanual.utils.Preferences
//import dagger.hilt.android.qualifiers.ApplicationContext
//import javax.inject.Inject
//
//
//class DataChangeHook @Inject constructor(
//    @ApplicationContext private val context: Context,
//    private val preferences: Preferences,
//    private val databaseHelper: DatabaseHelper,
//) {
//    private val databaseName get() =  "${preferences.currentRelease}_SimpleCommands"
//    private val readOnlyDatabase = databaseHelper.readableDatabase
//
//    suspend operator fun invoke() {
//        readOnlyDatabase
//    }
//}