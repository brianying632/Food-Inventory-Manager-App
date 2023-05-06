package com.example.a343project;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import javax.annotation.Nullable;

public class FoodActivity extends AppCompatActivity {

    Button saveButton;
    ImageButton imageButton, deleteButton;
    EditText name, quantity, expDate, purDate;

    UserData userData;
    FoodItem foodItem;

    ActivityResultLauncher<Intent> ImageCaptureActivityResultLauncher;
    Uri photoURI;
    String currentPhotoPath;


    private final int DRIVE_REQUEST_CODE = 400;
    DriveHelper driveHelper;
    LocalStorageHelper localStorageHelper = new LocalStorageHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);

        Intent intent = getIntent();
        userData = (UserData) intent.getSerializableExtra("userData");

        name = (EditText) findViewById(R.id.editText2);
        quantity = (EditText) findViewById(R.id.editText3);
        expDate = (EditText) findViewById(R.id.editTextDate);
        purDate = (EditText) findViewById(R.id.editTextDate2);

        //todo debug maybe?
        saveButton = (Button) findViewById(R.id.button3);
        saveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //update foodItem
                if(userData.getFoodCursor() != -1){
                    if(!name.getText().toString().equals("")) foodItem.setName(name.getText().toString());
                    if(!quantity.getText().toString().equals(""))foodItem.setQuantity(quantity.getText().toString());
                    if(!expDate.getText().toString().equals(""))foodItem.setExpirationDate(new Date(expDate.getText().toString()));
                    if(!purDate.getText().toString().equals(""))foodItem.setPurchaseDate(new Date(purDate.getText().toString()));
                    if(photoURI != null) foodItem.setImage(photoURI);
                } else{
                    if(!(name.getText().toString().equals("") || quantity.getText().toString().equals("") ||
                            expDate.getText().toString().equals("") || purDate.getText().toString().equals(""))){
                        foodItem = new FoodItem(name.getText().toString(), quantity.getText().toString(),
                                new Date(expDate.getText().toString()), new Date(purDate.getText().toString()));
                        if(photoURI != null) foodItem.setImage(photoURI);
                        userData.getStorageList().get(userData.getStorageCursor()).addFoodItem(foodItem);
                    }
                }

                saveData(userData.isOnline());
                Intent intent = new Intent(FoodActivity.this, FoodListActivity.class);
                intent.putExtra("userData", userData);
                startActivity(intent);
            }
        });

        deleteButton = (ImageButton) findViewById(R.id.imageButton6);
        deleteButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(userData.getFoodCursor() != -1) {
                    userData.getStorageList().get(userData.getStorageCursor()).removeFoodItem(userData.getFoodCursor());
                }
                saveData(userData.isOnline());
                Intent intent = new Intent(FoodActivity.this, FoodListActivity.class);
                intent.putExtra("userData", userData);
                startActivity(intent);
            }
        });

        imageButton = (ImageButton) findViewById(R.id.imageButton5);
        imageButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //todo update and save food item image
                dispatchTakePictureIntent();
            }
        });

        if(userData.getFoodCursor() != -1){
            foodItem = userData.getStorageList().get(userData.getStorageCursor()).getFoodList().get(userData.getFoodCursor());
            name.setText(foodItem.getName());
            quantity.setText(foodItem.getQuantity());
            expDate.setText(foodItem.getExpirationDateString());
            purDate.setText(foodItem.getPurchaseDateString());
            if(foodItem.getImage() != null){
                imageButton.setImageURI(foodItem.getImage());
            }
        }

        ImageCaptureActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK){
                            imageButton.setImageURI(photoURI);
                        }
                    }
                });
    }

    public void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try{
            photoFile = createImageFile();
        } catch(IOException ex){
            //There was an error creating the file
        }

        if(photoFile != null){
            photoURI = FileProvider.getUriForFile(this,
                    "com.example.a343project.imageProvider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            ImageCaptureActivityResultLauncher.launch(takePictureIntent);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void saveData(boolean online){
//        if(online){
//            localStorageHelper.writeToFile(FoodActivity.this, userData);
//            requestSignIn();
//        } else{
//            localStorageHelper.writeToFile(FoodActivity.this, userData);
//        }
        localStorageHelper.writeToFile(FoodActivity.this, userData);
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
//                    handleSignInIntent(data, FoodActivity.this);
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
//                        driveHelper.uploadFile(FoodActivity.this);
//                        //driveHelper.downloadFile(FoodActivity.this);
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