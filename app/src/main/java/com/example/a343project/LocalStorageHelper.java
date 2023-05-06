package com.example.a343project;

import android.content.Context;
import android.widget.Toast;

import com.google.api.services.drive.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class LocalStorageHelper {
    private String fileName = "TestFile.txt";

    public void writeToFile(Context context, UserData userData){
        File path = context.getApplicationContext().getFilesDir();
        System.out.println("LOCAL PATH:" + path.toString());
        try{
            FileOutputStream writer = new FileOutputStream(new File(path, fileName));
            ObjectOutputStream objWriter = new ObjectOutputStream(writer);
            objWriter.writeObject(userData);
            writer.close();
            objWriter.close();
            //Toast.makeText(context.getApplicationContext(), "Wrote to File" + fileName, Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public UserData readFromFile(Context context){
        File path = context.getApplicationContext().getFilesDir();
        File readFrom = new File(path, fileName);
        UserData newUserData = new UserData("Guest");
        try {
            FileInputStream stream = new FileInputStream(readFrom);
            ObjectInputStream objStream = new ObjectInputStream(stream);
            newUserData = (UserData) objStream.readObject();
            if(newUserData == null) {
                newUserData = new UserData("Guest");
                System.out.println("SAVE DATA WAS NULL");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            newUserData = new UserData("Guest");
            writeToFile(context, newUserData);
            return newUserData;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return newUserData;
    }
}
