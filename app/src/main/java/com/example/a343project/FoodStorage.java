package com.example.a343project;

import java.io.Serializable;
import java.util.ArrayList;

public class FoodStorage implements Serializable {
    private String name;
    private ArrayList<FoodItem> foodList;
    private int foodCursor;

    public FoodStorage(String name){
        this.name = name;
        foodList = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<FoodItem> getFoodList() {
        return foodList;
    }

    public void setFoodList(ArrayList<FoodItem> foodList) {
        this.foodList = foodList;
    }

    public int getFoodCursor() {
        return foodCursor;
    }

    public void setFoodCursor(int foodCursor) {
        this.foodCursor = foodCursor;
    }

    public void addFoodItem(FoodItem item){
        foodList.add(item);
    }

    public void removeFoodItem(int index){
        foodList.remove(index);
    }

    public void removeFoodItem(FoodItem item){
        foodList.remove(item);
    }

    public ArrayList<String> toStringList(){
        ArrayList<String> strArray = new ArrayList<>();
        for(FoodItem i : foodList){
            strArray.add(i.toString());
        }
        return strArray;
    }
}
