package com.stephenmorgandevelopment.thelinuxmanual.data;

import android.content.Context;
import android.util.Log;

import com.stephenmorgandevelopment.thelinuxmanual.models.Command;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LocalStorage {
    private static final String TAG = LocalStorage.class.getSimpleName();
    private static LocalStorage instance;
    private final File commandsDir;

    public static LocalStorage getInstance() {
        if (instance == null) {
            instance = new LocalStorage();
        }
        return instance;
    }

    @Inject
    public LocalStorage() {
        commandsDir = Helpers.getApplicationContext().getDir("commands", Context.MODE_PRIVATE);
    }

    public boolean hasCommand(long id) {
        File fileName = new File(commandsDir, String.valueOf(id));
        return fileName.exists();
    }

    public Command loadCommand(long id) throws IOException {
        File commandFile = new File(commandsDir, String.valueOf(id));
        StringBuilder json = new StringBuilder();

        if (commandFile.exists()) {
            FileInputStream inputStream = new FileInputStream(commandFile);
            InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

            char[] buffer = new char[4096];
            int bytesRead = 0;
            do {
                bytesRead = reader.read(buffer);
                json.append(String.valueOf(buffer));
            } while (bytesRead != -1);

            reader.close();
        }

        return Command.Companion.fromJson(id, json.toString().trim());
    }

    public void saveCommand(Command command) {
        File commandFile = new File(commandsDir, String.valueOf(command.getId()));

        if (!commandFile.exists()) {
            try {
                FileOutputStream outputStream = new FileOutputStream(commandFile);
                OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                outputWriter.write(command.dataMapToJsonString());
                outputWriter.close();
            } catch (IOException ioe) {
                Log.w(TAG, "Error saving: " + command.getId() + ioe.getMessage());
            }
        }
    }

    public void wipeAll() {
        File[] commands = commandsDir.listFiles();

        if (commands == null) {
            Log.w(TAG, "Error reading contents of commandsDir.  path: " + commandsDir.getPath());
            return;
        }

        for (File command : commands) {
            if (!command.delete()) {
                Log.i(TAG, "Error removing command with id: " + command.getName());
            }
        }
    }
}
