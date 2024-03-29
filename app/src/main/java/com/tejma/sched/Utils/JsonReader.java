package com.tejma.sched.Utils;
import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class JsonReader {

    public static String getJsonFrom(Context context, File file) {
        String jsonString;
        try {
            FileInputStream is = new FileInputStream(file);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            jsonString = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
            return null;
        }

        return jsonString;
    }
}
