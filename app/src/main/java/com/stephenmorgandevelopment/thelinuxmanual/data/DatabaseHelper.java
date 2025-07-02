package com.stephenmorgandevelopment.thelinuxmanual.data;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteException;
//import android.database.sqlite.SQLiteOpenHelper;
//import android.database.sqlite.SQLiteStatement;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//import androidx.sqlite.db.SupportSQLiteOpenHelper;
//
//import com.stephenmorgandevelopment.thelinuxmanual.distros.AvailableReleases;
//import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
//import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;
//import com.stephenmorgandevelopment.thelinuxmanual.utils.Preferences;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.inject.Singleton;
//
//@Singleton
//public class DatabaseHelper extends SQLiteOpenHelper {
//    public static final String TAG = DatabaseHelper.class.getSimpleName();
//    private static final String simpleCommandsName = "simple_commands";
//
//    private final static int version = 2;
//
//    private SQLiteDatabase database;
//    private final Preferences mPreferences;
//
//    public static final String TABLE_NAME = "SimpleCommands";
/// /    private static String TABLE_NAME_PREFIX = UbuntuHtmlApiConverter.getReleaseString();
/// /    private String TABLE_NAME_PREFIX = null;
//
//    private static final String KEY_ID = "id";
//    private static final String KEY_NAME = "name";
//    private static final String KEY_DESCRIPTION = "description";
//    private static final String KEY_URL = "url";
//    private static final String KEY_MAN_N = "manN";
//
//    public DatabaseHelper(
//            @NonNull Context applicationContext,
//            @NonNull Preferences preferences
//    ) {
//        super(applicationContext, simpleCommandsName, null, version);
//        mPreferences = preferences;
////        TABLE_NAME_PREFIX = mPreferences.getCurrentRelease();
//
//        database = getWritableDatabase();
//    }
//
//    public void changeTable(String table) {
////        TABLE_NAME_PREFIX = table;
//    }
//
//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
//                + TABLE_NAME + "("
//                + "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, "
//                + "description TEXT, url TEXT, manN TEXT)";
//
//        db.execSQL(CREATE_TABLE);
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
//                + TABLE_NAME + "("
//                + "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, "
//                + "description TEXT, url TEXT, manN TEXT)";
//
//        db.execSQL(CREATE_TABLE);
//    }
//
//    public void addCommandsUsingRoom(List<SimpleCommand> commands) {
//
//    }
//
//    public void addCommands(List<SimpleCommand> commands) {
//        if (commands == null) {
//            Log.e(TAG, "Failed adding commands to database.  commands == null");
//            return;
//        }
//
//        if (database == null || database.isReadOnly()) {
//            database = getWritableDatabase();
//        }
//
//        String insertString = "INSERT INTO " + /*TABLE_NAME_PREFIX +*/ TABLE_NAME + " ("
//                + KEY_NAME + ", " + KEY_DESCRIPTION + ", " + KEY_URL + ", " + KEY_MAN_N
//                + ") VALUES (?, ?, ?, ?)";
//
//        SQLiteStatement insertStatement = database.compileStatement(insertString);
//        database.beginTransaction();
//
//        for (SimpleCommand command : commands) {
//
//            insertStatement.bindString(1, command.getName());
//            insertStatement.bindString(2, command.getDescription());
//            insertStatement.bindString(3, command.getUrl());
//            insertStatement.bindString(4, String.valueOf(command.getManN()));
//
//            command.setId(insertStatement.executeInsert());
//            insertStatement.clearBindings();
//        }
//
//        database.setTransactionSuccessful();
//        database.endTransaction();
//
//        Log.d(TAG, "Successfully added commands for page " + commands.get(0).getManN() + ".");
//    }
//
//
//    public void wipeTable() {
//        database.execSQL("DROP TABLE IF EXISTS " + /*TABLE_NAME_PREFIX + */TABLE_NAME);
//
//        database.execSQL("CREATE TABLE "
//                /*+ TABLE_NAME_PREFIX*/ + TABLE_NAME + "("
//                + "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, "
//                + "description TEXT, url TEXT, manN TEXT)");
//    }
//
//    public synchronized void updateCommandUsingRoom(SimpleCommand command) {
//
//    }
//
//    public synchronized void updateCommand(SimpleCommand command) {
//        ContentValues values = new ContentValues();
//
//        values.put(KEY_NAME, command.getName());
//        values.put(KEY_DESCRIPTION, command.getDescription());
//        values.put(KEY_URL, command.getUrl());
//        values.put(KEY_MAN_N, command.getManN());
//
//        database.update(/*TABLE_NAME_PREFIX +*/ TABLE_NAME, values, KEY_ID + "=?", new String[]{String.valueOf(command.getId())});
//    }
//
//    public void close() {
//        if (database != null) {
//            database.close();
//            database = null;
//            Log.d(TAG, "Database successfully closed.");
//        } else {
//            Log.d(TAG, "Database already closed.");
//        }
//    }
//
//    public List<SimpleCommand> partialMatches(String searchText) throws SQLiteException {
//        if (database == null) {
//            database = getReadableDatabase();
//        }
//
//        List<SimpleCommand> matches = new ArrayList<>();
//        final String queryStart = "SELECT * FROM " +/* TABLE_NAME_PREFIX +*/ TABLE_NAME + " WHERE " + KEY_NAME;
//        final String queryEnd = searchText + "%' ORDER BY (" + KEY_NAME + " = '" + searchText + "') desc"
//                + ", LENGTH(" + KEY_NAME + ")";
//
//        final String query = searchText.length() >= 4
//                ? queryStart + " LIKE '%" + queryEnd
//                : queryStart + " LIKE '" + queryEnd;
//
//        Cursor cursor = database.rawQuery(query, null);
//
//        if (!cursor.moveToFirst()) {
//            return matches;
//        }
//
//        do {
//            matches.add(new SimpleCommand(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4)));
//        } while (cursor.moveToNext());
//
//        Log.i("Database", "matches size is " + matches.size());
//
//        cursor.close();
//        return matches;
//    }
//
//    public SimpleCommand getCommandById(long id) {
//        if (database == null) {
//            database = getReadableDatabase();
//        }
//
//        final String query = "SELECT * FROM " + /*TABLE_NAME_PREFIX +*/ TABLE_NAME
//                + " WHERE " + KEY_ID + "=?";
//
//        try (Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(id)})) {
//
//            if (cursor.moveToFirst()) {
//                return new SimpleCommand(id, cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4));
//            }
//        }
//
//        return null;
//    }
//
//    public List<SimpleCommand> getCommandsByIds(List<Long> ids) {
//        if (database == null) {
//            database = getReadableDatabase();
//        }
//
//        final String query = "SELECT * FROM " +/* TABLE_NAME_PREFIX +*/ TABLE_NAME
//                + " WHERE " + KEY_ID + "IN (?)";
//
//        Cursor cursor = database.rawQuery(query, convertIdsToString(ids));
//
//        List<SimpleCommand> simpleCommands = new ArrayList<>();
//        if (cursor.moveToFirst()) {
//            do {
//                simpleCommands.add(
//                        new SimpleCommand(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4)));
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//
//        return simpleCommands;
//    }
//
//    private String[] convertIdsToString(List<Long> ids) {
//        StringBuilder idString = new StringBuilder("");
//        for (Long id : ids) {
//            idString.append(String.valueOf(id));
//            idString.append(",");
//        }
//        String out = idString.toString().substring(0, idString.lastIndexOf(","));
//        return new String[]{out};
//    }
//
//    public boolean hasDatabase() {
//        File dbFile = Helpers.getApplicationContext().getDatabasePath(simpleCommandsName);
//        return dbFile.exists();
//    }
//
//    public boolean hasData() {
//        return getCommandById(1) != null;
//    }
//}
