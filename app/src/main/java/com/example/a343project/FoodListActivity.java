package com.example.a343project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

public class FoodListActivity extends AppCompatActivity {

    TextView storageNameView;
    ImageButton addFood, deleteStorage, goBack;
    ListView listView;

    UserData userData;
    FoodStorage foodStorage;
    String storageName;
    ArrayList<String> stringFoodList;

    private final int DRIVE_REQUEST_CODE = 400;
    DriveHelper driveHelper;
    LocalStorageHelper localStorageHelper = new LocalStorageHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        Intent intent = getIntent();
        userData = (UserData) intent.getSerializableExtra("userData");
        //todo may cause problems here check later
        foodStorage = userData.getStorageList().get(userData.getStorageCursor());
        storageName = foodStorage.getName();

        storageNameView = (TextView) findViewById(R.id.textView3);
        storageNameView.setText(storageName);

        addFood = (ImageButton) findViewById(R.id.imageButton4);
        addFood.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                foodStorage.setFoodCursor(-1);
                Intent intent = new Intent(FoodListActivity.this, FoodActivity.class);
                intent.putExtra("userData", userData);
                startActivity(intent);
            }
        });

        deleteStorage = (ImageButton) findViewById(R.id.imageButton3);
        deleteStorage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                userData.removeStorageItem(userData.getStorageCursor());
                saveData(userData.isOnline());
                //todo update cloud
                Intent intent = new Intent(FoodListActivity.this, StorageListActivity.class);
                intent.putExtra("userData", userData);
                startActivity(intent);
            }
        });

        goBack = (ImageButton) findViewById(R.id.imageButton2);
        goBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(FoodListActivity.this, StorageListActivity.class);
                intent.putExtra("userData", userData);
                startActivity(intent);
            }
        });

        listView = (ListView) findViewById(R.id.listView2);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                foodStorage.setFoodCursor(i);
                Intent intent = new Intent(FoodListActivity.this, FoodActivity.class);
                intent.putExtra("userData", userData);
                startActivity(intent);
            }
        });

        updateStringFoodList();
    }

    public void updateStringFoodList(){
        stringFoodList = userData.getStorageList().get(userData.getStorageCursor()).toStringList();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stringFoodList);
        listView.setAdapter(arrayAdapter);
    }

    public void saveData(boolean online){
//        if(online){
//            localStorageHelper.writeToFile(FoodListActivity.this, userData);
//            requestSignIn();
//        } else{
//            localStorageHelper.writeToFile(FoodListActivity.this, userData);
//        }
        localStorageHelper.writeToFile(FoodListActivity.this, userData);
    }

//    private void requestSignIn() {
//        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestEmail()
//                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
//                .build();
//        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);
//        System.out.println("REQUESTING SIGN IN");
//        startActivityForResult(client.getSignInIntent(), DRIVE_REQUEST_CODE);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case 400:
//                if (resultCode == RESULT_OK) {
//                    handleSignInIntent(data, FoodListActivity.this);
//                    System.out.println("ON ACTIVITY RESULT");
//                } else {
//                    System.out.println("RESULT IS NOT OK ");
//                }
//                break;
//        }
//    }
//
//    public void handleSignInIntent(Intent data, Context context) {
//        GoogleSignIn.getSignedInAccountFromIntent(data)
//                .addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
//                    @Override
//                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
//                        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
//                                context,
//                                Collections.singleton(DriveScopes.DRIVE_FILE)
//                        );
//
//                        credential.setSelectedAccount(googleSignInAccount.getAccount());
//
//                        Drive googleDriveService = new Drive.Builder(
//                                new NetHttpTransport(),
//                                new GsonFactory(),
//                                credential).setApplicationName("My Drive").build();
//
//                        driveHelper = new DriveHelper(googleDriveService);
//                        System.out.println("LOGIN SUCCESSFUL");
//                        //todo DRIVE EDITING GOES HERE AFTER SUCCESSFUL LOGIN
//                        driveHelper.uploadFile(FoodListActivity.this);
//                        //driveHelper.downloadFile(FoodListActivity.this);
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        System.out.println("LOGIN FAILED");
//                    }
//                });
//    }
}