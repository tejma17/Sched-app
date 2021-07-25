package com.tejma.sched.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExportTimetable {
    
    public static void exportFile(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("Classes", Context.MODE_PRIVATE);
        String jsonString = sharedPreferences.getString("Lectures", null);
        String fileName = "sched_export_"+System.currentTimeMillis()+".scd";
        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            if (jsonString != null) {
                fos.write(jsonString.getBytes());
            }
            fos.close();

            File file = new File(context.getFilesDir(), fileName);
            Intent intentShareFile = new Intent(Intent.ACTION_SEND);
            intentShareFile.setType("text/scd");
            intentShareFile.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, context.getPackageName(), file));
            intentShareFile.putExtra(Intent.EXTRA_SUBJECT, "Sharing File...");
            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...");
            context.startActivity(Intent.createChooser(intentShareFile, "Share File"));

        } catch (FileNotFoundException fileNotFound) {
            Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show();
        } catch (IOException ioException) {
            Toast.makeText(context, "IO Exception", Toast.LENGTH_SHORT).show();
        }
    }


}
