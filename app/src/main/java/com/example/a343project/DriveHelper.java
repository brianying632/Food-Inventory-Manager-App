package com.example.a343project;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.contentcapture.ContentCaptureCondition;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

public class DriveHelper {

    private final int DRIVE_REQUEST_CODE = 400;
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
                Toast.makeText(context.getApplicationContext(), "Check Drive API key", Toast.LENGTH_LONG).show();
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

        String filepath = "/data/user/0/com.example.a343project/files/TestFile.txt";
        downloadDriveFile(context).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context.getApplicationContext(), "Check Drive API key", Toast.LENGTH_LONG).show();
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
                        System.out.printf("Found file: %s (%s)\n", f.getName(), f.getId());
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
                System.out.println("SAVE FILE NOT FOUND ON DRIVE");
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

            //System.out.println("Data retrieved: "+outputStream);

            //Convert ByteArrayOutputStream to File and save
            ByteArrayOutputStream byteOutStream = (ByteArrayOutputStream) outputStream;
            String fileName = "TestFile.txt";
            File path = context.getApplicationContext().getFilesDir();
            //String path = "/data/user/0/com.example.a343project/files/TestFile.txt";
            try {
                outputStream = new FileOutputStream(new File(path, fileName));
                // writing bytes in to byte output stream
                byteOutStream.writeTo(outputStream);
                System.out.println("WROTE FROM DRIVE TO LOCAL FILE");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                outputStream.close();
            }

            return fileMetaData.getId();
        });
    }
}


