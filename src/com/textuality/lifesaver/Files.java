/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.textuality.lifesaver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

public class Files {

    private static final String DIR = "LifeSaver-F";
    private static final String CALL_LOG = "CallLog";
    private static final String MESSAGE_LOG = "MessageLog";

    private static File directory = null;

    public static void die(Context context, Throwable e) {
        Uri uri = Uri.parse("content://com.textuality.lifesaver.NoStorage/");
        Intent intent = new Intent(context, NoStorage.class);
        intent.setData(uri);
        intent.putExtra("problem", e.getMessage());
        context.startActivity(intent);
    }

    public static boolean backupExists() {
        File calls = new File(Environment.getExternalStorageDirectory(), DIR
                + "/" + CALL_LOG);
        File messages = new File(Environment.getExternalStorageDirectory(), DIR
                + "/" + MESSAGE_LOG);
        return (calls.exists() || messages.exists());
    }

    public static PrintStream printCallLog(Context context) {
        try {
            return printer(CALL_LOG);
        } catch (IOException e) {
            die(context, e);
            return null;
        }
    }

    public static BufferedReader readCallLog(Context context) {
        try {
            return reader(CALL_LOG);
        } catch (IOException e) {
            die(context, e);
            return null;
        }
    }

    public static PrintStream printMessageLog(Context context) {
        try {
            return printer(MESSAGE_LOG);
        } catch (IOException e) {
            die(context, e);
            return null;
        }
    }

    public static BufferedReader readMessageLog(Context context) {
        try {
            return reader(MESSAGE_LOG);
        } catch (IOException e) {
            die(context, e);
            return null;
        }
    }

    private static BufferedReader reader(String name) throws IOException {
        checkDir();
        return new BufferedReader(new FileReader(new File(directory, name)));
    }

    private static PrintStream printer(String name) throws IOException {
        checkDir();
        File log = new File(directory, name);
        if (log.exists())
            log.delete();
        log.createNewFile();
        return new PrintStream(new FileOutputStream(log));
    }

    private static void checkDir() throws IOException {
        if (directory == null) {
            File external = Environment.getExternalStorageDirectory();
            
            directory = new File(external, DIR);
            if (!directory.exists()) {
                if (!directory.mkdir())
                    throw new IOException(directory.toString());
                    
            } else if (!directory.isDirectory()) {
                directory.delete();
                directory.mkdir();
            }
        }
    }
}
