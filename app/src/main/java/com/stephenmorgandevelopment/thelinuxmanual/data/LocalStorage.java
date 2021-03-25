package com.stephenmorgandevelopment.thelinuxmanual.data;

import android.content.Context;

import com.stephenmorgandevelopment.thelinuxmanual.models.Command;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LocalStorage {
    private static LocalStorage instance;
    private final File commandsDir;

    public static LocalStorage getInstance() {
        if(instance == null) {
            instance = new LocalStorage();
        }
        return instance;
    }

    private LocalStorage() {
        commandsDir = Helpers.getApplicationContext()
                .getDir("commands", Context.MODE_PRIVATE);


    }

    public boolean hasCommand(long id) {
        List<String> fileNames =  Arrays.asList(commandsDir.list());
        return fileNames.contains(String.valueOf(id));
    }

    public Command loadCommand(long id) throws IOException {
        File commandFile = new File(commandsDir, String.valueOf(id));
        StringBuilder json = new StringBuilder();

        if(commandFile.exists()) {
            FileReader fileInput = new FileReader(commandFile);

            char[] buffer = new char[4096];
            int bytesRead = 0;
            do {
                bytesRead = fileInput.read(buffer);
                json.append(String.valueOf(buffer));
            } while(bytesRead != -1);

            fileInput.close();
        }

        return new Command(id, Command.parseMapFromJson(json.toString()));
    }

    public void saveCommand(Command command) throws IOException {
        File commandFile = new File(commandsDir, String.valueOf(command.getId()));

        if (!commandFile.exists()) {
            FileWriter outputWriter = new FileWriter(commandFile);
            outputWriter.write(command.dataMapToJsonString());
            outputWriter.close();
        }
    }
}
