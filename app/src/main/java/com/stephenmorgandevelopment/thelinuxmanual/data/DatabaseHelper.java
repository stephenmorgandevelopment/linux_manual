package com.stephenmorgandevelopment.thelinuxmanual.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.stephenmorgandevelopment.thelinuxmanual.distros.UbuntuHtmlApiConverter;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String TAG = DatabaseHelper.class.getSimpleName();
    private static final String simpleCommandsName = "simple_commands";

    private final static int version = 2;

    private SQLiteDatabase database;
    private static DatabaseHelper helperInstance;

    private static final String TABLE_NAME_POSTFIX = "_SimpleCommands";
    private static String TABLE_NAME_PREFIX = UbuntuHtmlApiConverter.getReleaseString();

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_URL = "url";
    private static final String KEY_MAN_N = "manN";

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

    public static void changeTable(String table) {
        TABLE_NAME_PREFIX = table;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (UbuntuHtmlApiConverter.Release release : UbuntuHtmlApiConverter.Release.values()) {
            String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
                    + release.getName() + TABLE_NAME_POSTFIX + "("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, "
                    + "description TEXT, url TEXT, manN TEXT)";

            db.execSQL(CREATE_TABLE);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (UbuntuHtmlApiConverter.Release release : UbuntuHtmlApiConverter.Release.values()) {
            String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
                    + release.getName() + TABLE_NAME_POSTFIX + "("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, "
                    + "description TEXT, url TEXT, manN TEXT)";

            db.execSQL(CREATE_TABLE);
        }
    }

    public void addCommands(List<SimpleCommand> commands) {
        TABLE_NAME_PREFIX = UbuntuHtmlApiConverter.getReleaseString();

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
    }


    public void wipeTable() {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_PREFIX + TABLE_NAME_POSTFIX);

        database.execSQL("CREATE TABLE "
                + TABLE_NAME_PREFIX + TABLE_NAME_POSTFIX + "("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, "
                + "description TEXT, url TEXT, manN TEXT)");
    }

    public synchronized void updateCommand(SimpleCommand command) {
        ContentValues values = new ContentValues();

        values.put(KEY_NAME, command.getName());
        values.put(KEY_DESCRIPTION, command.getDescription());
        values.put(KEY_URL, command.getUrl());
        values.put(KEY_MAN_N, command.getManN());

        database.update(TABLE_NAME_PREFIX + TABLE_NAME_POSTFIX, values, KEY_ID + "=?", new String[]{String.valueOf(command.getId())});
    }

    public void close() {
        if (database != null) {
            database.close();
            database = null;
            helperInstance = null;
            Log.d(TAG, "Database successfully closed.");
        } else {
            Log.d(TAG, "Database already closed.");
        }
    }

    public List<SimpleCommand> partialMatches(String searchText) throws SQLiteException {
        if (database == null) {
            database = getReadableDatabase();
        }

        List<SimpleCommand> matches = new ArrayList<>();
        final String queryStart = "SELECT * FROM " + TABLE_NAME_PREFIX + TABLE_NAME_POSTFIX + " WHERE " + KEY_NAME;
        final String queryEnd =  searchText + "%' ORDER BY (" + KEY_NAME + " = '" + searchText + "') desc"
            + ", LENGTH(" + KEY_NAME + ")";

        final String query = searchText.length() >= 4
                ? queryStart + " LIKE '%" + queryEnd
                : queryStart + " LIKE '" + queryEnd;

        Cursor cursor = database.rawQuery(query, null);

        if (!cursor.moveToFirst()) {
            return matches;
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

        try (Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(id)})) {

            if (cursor.moveToFirst()) {
                return new SimpleCommand(id, cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4));
            }
        }

        return null;
    }

    public List<SimpleCommand> getCommandsByIds(List<Long> ids) {
        if (database == null) {
            database = getReadableDatabase();
        }

        final String query = "SELECT * FROM " + TABLE_NAME_PREFIX + TABLE_NAME_POSTFIX
                + " WHERE " + KEY_ID + "IN (?)";

        Cursor cursor = database.rawQuery(query, convertIdsToString(ids));

        List<SimpleCommand> simpleCommands = new ArrayList<>();
        if(cursor.moveToFirst()) {
            do {
                simpleCommands.add(
                        new SimpleCommand(cursor.getLong(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getInt(4)));
            } while(cursor.moveToNext());
        }
        cursor.close();

        return simpleCommands;
    }

    private String[] convertIdsToString(List<Long> ids) {
        StringBuilder idString = new StringBuilder("");
        for(Long id : ids) {
            idString.append(String.valueOf(id));
            idString.append(",");
        }
        String out = idString.toString().substring(0, idString.lastIndexOf(","));
        return new String[] {out};
    }

    public static boolean hasDatabase() {
        File dbFile = Helpers.getApplicationContext().getDatabasePath(simpleCommandsName);
        return dbFile.exists();
    }

    public boolean hasData() {
        return getCommandById(1) != null;
    }
}
