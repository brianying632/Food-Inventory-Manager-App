package com.example.a343project;

import android.app.ProgressDialog;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveHelper {
    private final Executor executor = Executors.newSingleThreadExecutor();
    private Drive drive;
    LocalStorageHelper localStorageHelper = new LocalStorageHelper();

    DriveHelper(){}
    DriveHelper(Drive drive){
        this.drive = drive;
    }

    public void uploadFile(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Uploading to Google Drive");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        String filepath = "/data/user/0/com.example.a343project/files/TestFile.txt";
        createDriveFile(filepath).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                //System.out.println("Check Drive API key");
            }
        });
    }

    public Task<String> createDriveFile(String filePath){
        return Tasks.call(executor, () -> {
            com.google.api.services.drive.model.File fileMetaData = new com.google.api.services.drive.model.File();
            fileMetaData.setName("FIM_APP_DATA");
            java.io.File file = new java.io.File(filePath);

            FileContent mediaContent = new FileContent("text/plain", file);
            com.google.api.services.drive.model.File myFile = null;
            try {
                //Look for any old save files and delete them
                List<com.google.api.services.drive.model.File> files = new ArrayList<>();
                String pageToken = null;
                do {
                    FileList result = drive.files().list().setQ("mimeType='text/plain'").execute();
                    for (com.google.api.services.drive.model.File f : result.getFiles()) {
                        System.out.printf("Found file: %s (%s)\n", f.getName(), f.getId());
                        if(f.getName().equals("FIM_APP_DATA")){
                            drive.files().delete(f.getId()).execute();
                        }
                    }

                    files.addAll(result.getFiles());
                }while(pageToken != null);

                //create new Save File
                myFile = drive.files().create(fileMetaData, mediaContent).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (myFile == null) {
                throw new IOException("Null when requesting file creation");
            }
            return myFile.getId();
        });
    }

    public void downloadFile(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Downloading from Google Drive");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        downloadDriveFile(context).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                //System.out.println("Check Drive API key");
            }
        });
    }

    public Task<String> downloadDriveFile(Context context){
        return Tasks.call(executor, () -> {
            com.google.api.services.drive.model.File fileMetaData = null;

            try {
                //Look for any old save and save them
                List<com.google.api.services.drive.model.File> files = new ArrayList<>();
                String pageToken = null;
                do {
                    FileList result = drive.files().list().setQ("mimeType='text/plain'").execute();
                    for (com.google.api.services.drive.model.File f : result.getFiles()) {
                        //System.out.printf("Found file: %s (%s)\n", f.getName(), f.getId());
                        if(f.getName().equals("FIM_APP_DATA")){
                            fileMetaData = f;
                        }
                    }

                    files.addAll(result.getFiles());
                }while(pageToken != null);

            } catch (Exception e) {
                e.printStackTrace();
            }

            //todo might be a loop btw
            if(fileMetaData == null){
                localStorageHelper.writeToFile(context, new UserData("Guest"));
                uploadFile(context);
                System.out.println("SAVE FILE NOT FOUND ON DRIVE, CREATING NEW SAVE DRIVE SAVE FILE");
                return null;
            }

            System.out.println(fileMetaData);

            //over here download fileMetaData to local storage
            OutputStream outputStream = null;
            try {
                outputStream = new ByteArrayOutputStream();
                drive.files().get(fileMetaData.getId()).executeMediaAndDownloadTo(outputStream);
            } catch (GoogleJsonResponseException e) {
                System.out.println("Unable to move file: " + e.getDetails());
            }

            System.out.println("Data retrieved: "+ outputStream);

            //Convert ByteArrayOutputStream to File and save
            ByteArrayOutputStream byteOutStream = (ByteArrayOutputStream) outputStream;
            ByteArrayInputStream byteInStream = new ByteArrayInputStream(byteOutStream.toByteArray());
            ObjectInputStream inputStream = new ObjectInputStream(byteInStream);
            UserData newUserData = (UserData) inputStream.readObject();
            localStorageHelper.writeToFile(context, newUserData);

//            String fileName = "TestFile.txt";
//            File path = context.getApplicationContext().getFilesDir();
//            System.out.println("LOCAL PATH:" + path.toString());

            //String path = "/data/user/0/com.example.a343project/files/TestFile.txt";
//            try {
//                outputStream = new FileOutputStream(new File(path, fileName));
//                // writing bytes in to byte output stream
//                byteOutStream.writeTo(outputStream);
//                System.out.println("WROTE FROM DRIVE TO LOCAL FILE");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            outputStream.close();
            byteOutStream.close();
            inputStream.close();
            byteInStream.close();

            return fileMetaData.getId();
        });
    }
}


