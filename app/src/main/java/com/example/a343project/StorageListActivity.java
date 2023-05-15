package com.example.a343project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

public class StorageListActivity extends AppCompatActivity {

    ListView listView;
    Button addButton, saveButton, signOutButton;
    EditText editText;

    UserData userData;
    ArrayList<String> stringStorageList;

    private final int DRIVE_REQUEST_CODE = 400;
    private final int SIGN_OUT_REQUEST_CODE = 401;
    GoogleSignInClient client;
    DriveHelper driveHelper;
    LocalStorageHelper localStorageHelper = new LocalStorageHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_list);

        Intent initIntent = getIntent();
        userData = (UserData) initIntent.getSerializableExtra("userData");

        //todo create intent if user deletes a storage option (May change to a local button tho if its easier)

        editText = (EditText) findViewById(R.id.editText);

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                userData.setStorageCursor(i);
                Intent intent = new Intent(StorageListActivity.this, FoodListActivity.class);
                intent.putExtra("userData", userData);
                startActivity(intent);
            }
        });

        addButton = (Button) findViewById(R.id.button2);
        addButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String inputStr = editText.getText().toString();
                if(!inputStr.equals("") && !inputStr.equals(null)){
                    userData.addStorageItem(new FoodStorage(inputStr));
                    updateStringStorageList();
                    saveData(userData.isOnline());
                    editText.setText("");
                }
            }
        });

        saveButton = (Button) findViewById(R.id.button4);
        saveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                saveData(userData.isOnline());
            }
        });

        signOutButton = (Button) findViewById(R.id.button6);
        signOutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                requestSignIn(SIGN_OUT_REQUEST_CODE);
            }
        });

        updateStringStorageList();
    }

    public void updateStringStorageList(){
        stringStorageList = userData.toStringList();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stringStorageList);
        listView.setAdapter(arrayAdapter);
    }

    public void saveData(boolean online){
        if(online){
            localStorageHelper.writeToFile(StorageListActivity.this, userData);
            requestSignIn(DRIVE_REQUEST_CODE);
        } else{
            localStorageHelper.writeToFile(StorageListActivity.this, userData);
            Toast.makeText(StorageListActivity.this, "Currently in Guest Mode", Toast.LENGTH_LONG).show();
        }
    }

    private void requestSignIn(int requestCode) {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();
        client = GoogleSignIn.getClient(this, signInOptions);
        //System.out.println("REQUESTING SIGN IN");
        startActivityForResult(client.getSignInIntent(), requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case DRIVE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    handleSignInIntent(data, StorageListActivity.this);
                    //System.out.println("ON ACTIVITY RESULT");
                } else {
                    //System.out.println("RESULT IS NOT OK ");
                }
                break;
            case SIGN_OUT_REQUEST_CODE:
                if(resultCode == RESULT_OK){
                    client.signOut();
                    Intent intent = new Intent(StorageListActivity.this, MainActivity.class);
                    startActivity(intent);
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
                        driveHelper.uploadFile(StorageListActivity.this);
                        //driveHelper.downloadFile(StorageListActivity.this);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //System.out.println("LOGIN FAILED");
                        e.printStackTrace();
                    }
                });
    }
}