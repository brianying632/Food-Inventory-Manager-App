package com.example.a343project;

import java.io.Serializable;
import java.util.ArrayList;

public class UserData implements Serializable {
    private String name;
    private ArrayList<FoodStorage> storageList;
    private int storageCursor;
    private int foodCursor;
    private boolean isOnline;
    public UserData(String name){
        this.name = name;
        storageList = new ArrayList<>();
        isOnline = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<FoodStorage> getStorageList() {
        return storageList;
    }

    public void setStorageList(ArrayList<FoodStorage> storageList) {
        this.storageList = storageList;
    }
    public int getStorageCursor() {
        return storageCursor;
    }
    public void setStorageCursor(int storageCursor) {
        this.storageCursor = storageCursor;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public int getFoodCursor(){
        return storageList.get(storageCursor).getFoodCursor();
    }

    public void setFoodCursor(int index){
        storageList.get(storageCursor).setFoodCursor(index);
    }

    public void addStorageItem(FoodStorage storage){
        storageList.add(storage);
    }

    public void removeStorageItem(int index){
        storageList.remove(index);
    }

    public void removeStorageItem(FoodStorage storage){
        storageList.remove(storage);
    }

    public ArrayList<String> toStringList(){
        ArrayList<String> strArray = new ArrayList<>();
        for(FoodStorage i : storageList){
            strArray.add(i.getName());
        }
        return strArray;
    }
}
