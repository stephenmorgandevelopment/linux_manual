package com.stephenmorgandevelopment.thelinuxmanual.databases;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.view.inputmethod.ExtractedTextRequest;

import com.stephenmorgandevelopment.thelinuxmanual.distros.Ubuntu;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String TAG = DatabaseHelper.class.getSimpleName();

    public static final String simpleCommandsName = "simple_commands";
    private final static int version = 1;

    private static DatabaseHelper helperInstance;

    private static final String TABLE_NAME_POSTFIX = "_SimpleCommands";
    private static String TABLE_NAME_PREFIX = Ubuntu.getReleaseString();;

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_URL = "url";
    private static final String KEY_MAN_N = "manN";

    private SQLiteDatabase database;

    public static DatabaseHelper getInstance() {
        if (helperInstance == null) {
            helperInstance = new DatabaseHelper();
        }

        return helperInstance;
    }

    private DatabaseHelper() {
        super(Helpers.getApplicationContext(), simpleCommandsName, null, version);

        database = getWritableDatabase();
    }

    public void changeTable(String table) {
        TABLE_NAME_PREFIX = table;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        for (Ubuntu.Release release : Ubuntu.Release.values()) {
            String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
                    + release.getName() + TABLE_NAME_POSTFIX + "("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, "
                    + "description TEXT, url TEXT, manN TEXT)";

            db.execSQL(CREATE_TABLE);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void addCommands(List<SimpleCommand> commands) {
        TABLE_NAME_PREFIX = Ubuntu.getReleaseString();

        if (commands == null) {
            Log.e(TAG, "Failed adding commands to database.  commands == null");
            return;
        }

        if (database == null || database.isReadOnly()) {
            database = getWritableDatabase();
        }

        String insertString = "INSERT INTO " + TABLE_NAME_PREFIX + TABLE_NAME_POSTFIX + " ("
                + KEY_NAME + ", " + KEY_DESCRIPTION + ", " + KEY_URL + ", " + KEY_MAN_N
                + ") VALUES (?, ?, ?, ?)";

        SQLiteStatement insertStatement = database.compileStatement(insertString);

        database.beginTransaction();

        for (SimpleCommand command : commands) {

            insertStatement.bindString(1, command.getName());
            insertStatement.bindString(2, command.getDescription());
            insertStatement.bindString(3, command.getUrl());
            insertStatement.bindString(4, String.valueOf(command.getManN()));

            command.setId(insertStatement.executeInsert());
            insertStatement.clearBindings();
        }

        database.setTransactionSuccessful();
        database.endTransaction();

        Log.d(TAG, "Successfully added commands for page " + commands.get(0).getManN() + ".");
        //database.close();
    }


    private void wipeTable() {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_PREFIX + TABLE_NAME_POSTFIX);
        database.execSQL("CREATE TABLE "
                + TABLE_NAME_PREFIX + TABLE_NAME_POSTFIX + "("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, "
                + "description TEXT, url TEXT, manN TEXT)");
    }

//    public void addCommand(SimpleCommand command) {
//        SQLiteDatabase db = getWritableDatabase();
//        addCommand(command, db);
//        db.close();
//    }

    //    public void addCommand(SimpleCommand command, SQLiteDatabase db) {
//    private void addCommand(SimpleCommand command) {
////        if (database == null || database.isReadOnly()) {
////            database = getWritableDatabase();
////        }
//
//        ContentValues values = new ContentValues();
//
//        values.put(KEY_NAME, command.getName());
//        values.put(KEY_DESCRIPTION, command.getDescription());
//        values.put(KEY_URL, command.getUrl());
//        values.put(KEY_MAN_N, command.getManN());
//
//        long row = database.insert(TABLE_NAME, null, values);
//        command.setId(row);
//    }

    public synchronized void updateCommand(SimpleCommand command) {
        if (database == null || database.isReadOnly()) {
            database = getWritableDatabase();
        }

        ContentValues values = new ContentValues();

        values.put(KEY_NAME, command.getName());
        values.put(KEY_DESCRIPTION, command.getDescription());
        values.put(KEY_URL, command.getUrl());
        values.put(KEY_MAN_N, command.getManN());

        int rowsUpdated = database.update(TABLE_NAME_PREFIX + TABLE_NAME_POSTFIX, values, KEY_NAME + "=?", new String[]{command.getName()});
        Log.i(TAG, "Rows updated: " + rowsUpdated);
    }

    public void close() {
        if (database != null) {
            database.close();
            database = null;
            this.helperInstance = null;
            Log.d(TAG, "Database successfully closed.");
        } else {
            Log.d(TAG, "Database already closed.");
        }
    }

    /**
     * Searches local database for a match by command name.
     *
     * @param command
     * @return Returns command's id on match, or -1 if not found.
     */
    public long contains(SimpleCommand command) {
        if (database == null) {
            database = getReadableDatabase();
        }

        final String query = "SELECT * FROM " + TABLE_NAME_PREFIX + TABLE_NAME_POSTFIX
                + " WHERE " + KEY_NAME + "=?";// + command.getName();

        Cursor cursor = database.rawQuery(query, new String[]{command.getName()});

        long id = cursor.moveToFirst() ?
                cursor.getLong(cursor.getColumnIndex(KEY_ID))
                : -1;

        cursor.close();
        return id;
    }

    public List<SimpleCommand> partialMatches(String searchText) {
        if (database == null) {
            database = getReadableDatabase();
        }

        List<SimpleCommand> matches = new ArrayList<>();

        final String query = searchText.length() >= 4
                ? "SELECT * FROM " + TABLE_NAME_PREFIX + TABLE_NAME_POSTFIX + " WHERE " + KEY_NAME + " LIKE '%" + searchText + "%'"
                : "SELECT * FROM " + TABLE_NAME_PREFIX + TABLE_NAME_POSTFIX + " WHERE " + KEY_NAME + " LIKE '" + searchText + "%'";

        Cursor cursor = database.rawQuery(query, null);

        if (!cursor.moveToFirst()) {
            return null;
        }

        do {
            matches.add(new SimpleCommand(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4)));
        } while (cursor.moveToNext());

        cursor.close();
        return matches;
    }

    public SimpleCommand getCommandById(long id) {
        if (database == null) {
            database = getReadableDatabase();
        }

        final String query = "SELECT * FROM " + TABLE_NAME_PREFIX + TABLE_NAME_POSTFIX
                + " WHERE " + KEY_ID + "=?";

        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(id)});

        if (cursor.moveToFirst()) {
            return new SimpleCommand(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4));
        }

        return null;
    }

    public static boolean hasDatabase() {
//        SQLiteDatabase check = null;
//
//        try {
//            String path = Helpers.getApplicationContext().getDatabasePath(simpleCommandsName).getAbsolutePath();
//            check = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
//        } catch (Exception e) {
//            Log.d(TAG, "Database does not exist yet.");
//        }
//
//        return check != null;
        File dbFile = Helpers.getApplicationContext().getDatabasePath(simpleCommandsName);
        return dbFile.exists();
    }

}
