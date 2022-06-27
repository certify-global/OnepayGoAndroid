package com.onepay.onepaygo.common;

import android.content.Context;
import android.os.Environment;

import com.onepay.onepaygo.data.AppSharedPreferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class LoggerUtil {

    static void logMessagesToFile(Context context) {
        String dirPath = Environment.getExternalStorageDirectory() + File.separator + "OnePayGo" +  File.separator;
        String fileName = dirPath + "CrashLog" + ".log";
        File file = new File(fileName);
        file.mkdirs();
        AppSharedPreferences.Companion.writeSp(AppSharedPreferences.Companion.getSharedPreferences(context), PreferencesKeys.LogFilePath, fileName); //TODO1: Optimize

        //clears a file
        if (file.exists()) {
            file.delete();
        }

        try {
            String command = String.format("logcat -d -v threadtime *:*");
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String currentLine = null;

            while ((currentLine = reader.readLine()) != null) {
                result.append(currentLine);
                result.append("\n");
            }

            FileWriter out = new FileWriter(file);
            out.write(result.toString());
            out.close();
        } catch (IOException e) {
            Logger.error("LoggerUtil", "logMessagesToFile()-> Error in writing to File");
        }
    }

    public static void deleteLogFile() {
        String dirPath = Environment.getExternalStorageDirectory() + File.separator + "myPassID" + File.separator;
        String fileName = dirPath + "CrashLog " + ".log";
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
            File file1 = new File(dirPath);
            if (file1.exists() && file1.isDirectory()) {
                file1.delete();
            }
        }
    }
}
