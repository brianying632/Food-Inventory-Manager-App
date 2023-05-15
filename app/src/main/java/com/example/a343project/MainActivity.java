package com.example.a343project;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

//import com.google.firebase.database.ktx.FirebaseDatabaseKtxRegistrar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Date;
import java.lang.Object;
import android.Manifest;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    Button guestButton;
    ImageButton googleButton;

    UserData userData;

    //String userToken;
    //FirebaseDatabase

    private final int DRIVE_REQUEST_CODE = 400;
    DriveHelper driveHelper;
    LocalStorageHelper localStorageHelper = new LocalStorageHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }

        guestButton = (Button) findViewById(R.id.button);
        guestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //grab local file and check if it exists or not
                userData = localStorageHelper.readFromFile(MainActivity.this);
                userData.setOnline(false);
                Intent intent = new Intent(MainActivity.this, StorageListActivity.class);
                intent.putExtra("userData", userData);
                MainActivity.this.startActivity(intent);
            }
        });

        //todo UNDER CONSTRUCTIONS RN LOL
        googleButton = (ImageButton) findViewById(R.id.imageButton);
        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //downloads file from google drive onto app data and reads it
                requestSignIn();
//                userData = localStorageHelper.readFromFile(MainActivity.this);
//                userData.setOnline(true);
//                Intent intent = new Intent(MainActivity.this, StorageListActivity.class);
//                intent.putExtra("userData", userData);
//                MainActivity.this.startActivity(intent);

                //create Test data to preload
//                createTestingData();
//                localStorageHelper.writeToFile(MainActivity.this, userData);
//                requestSignIn();
            }
        });
    }

    public void createTestingData() {
        userData = new UserData("Brian");
        userData.addStorageItem(new FoodStorage("Fridge"));
        userData.addStorageItem(new FoodStorage("Freezer"));
        userData.addStorageItem(new FoodStorage("Pantry"));
        userData.getStorageList().get(0).addFoodItem(new FoodItem("Apple", "5", new Date("5/6/2023"), new Date("3/2/2023")));
        userData.getStorageList().get(0).addFoodItem(new FoodItem("Raw Chicken", "4 lb", new Date("4/28/2023"), new Date("3/2/2023")));
        userData.getStorageList().get(0).addFoodItem(new FoodItem("Milk", "1 carton", new Date("5/2/2023"), new Date("3/2/2023")));
        userData.getStorageList().get(1).addFoodItem(new FoodItem("Ice Cream", "1 Pint", new Date("4/28/2023"), new Date("3/2/2023")));
        userData.getStorageList().get(2).addFoodItem(new FoodItem("bread", "1 loaf", new Date("5/2/2023"), new Date("3/2/2023")));
    }

    private void requestSignIn() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);
        //System.out.println("REQUESTING SIGN IN");
        startActivityForResult(client.getSignInIntent(), DRIVE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 400:
                if (resultCode == RESULT_OK) {
                    handleSignInIntent(data, MainActivity.this);
                    //System.out.println("ON ACTIVITY RESULT");
                } else {
                    //System.out.println("RESULT IS NOT OK ");
                }
                break;
        }
    }

    public void handleSignInIntent(Intent data, Context context) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                                context,
                                Collections.singleton(DriveScopes.DRIVE_FILE)
                        );

                        credential.setSelectedAccount(googleSignInAccount.getAccount());

                        Drive googleDriveService = new Drive.Builder(
                                new NetHttpTransport(),
                                new GsonFactory(),
                                credential).setApplicationName("My Drive").build();

                        driveHelper = new DriveHelper(googleDriveService);
                        //System.out.println("LOGIN SUCCESSFUL");

                        //todo DRIVE EDITING GOES HERE AFTER SUCCESSFUL LOGIN
                        //driveHelper.uploadFile(MainActivity.this);
                        driveHelper.downloadFile(MainActivity.this); //download file to local storage
                        userData = localStorageHelper.readFromFile(MainActivity.this); //read from local storage
                        userData.setOnline(true);
                        Intent intent = new Intent(MainActivity.this, StorageListActivity.class);
                        intent.putExtra("userData", userData);
                        MainActivity.this.startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("LOGIN FAILED");
                    }
                });
    }
}