package com.example.a343project;

import android.media.Image;
import android.net.Uri;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FoodItem implements Serializable {
    private String name, quantity;
    private Date expirationDate, purchaseDate;
    private String image;
    private final String datePattern = "MM/dd/yyyy";

    public FoodItem(){
        this.name = null;
        this.quantity = null;
        this.expirationDate = null;
        this.purchaseDate = null;
    }

    public FoodItem(String name, String quantity, Date expirationDate, Date purchaseDate) {
        this.name = name;
        this.quantity = quantity;
        this.expirationDate = expirationDate;
        this.purchaseDate = purchaseDate;
    }

    public String getName() {
        return name;
    }

    public String getQuantity() {
        return quantity;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public Uri getImage() {
        if(image == null) return null;
        return Uri.parse(image);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public void setImage(Uri image) {
        this.image = image.toString();
    }

    public String toString(){
        DateFormat dateFormat = new SimpleDateFormat(datePattern);
        return name + "(" + quantity + ")  |  Exp: " + dateFormat.format(expirationDate);
    }

    public String getExpirationDateString(){
        DateFormat dateFormat = new SimpleDateFormat(datePattern);
        return dateFormat.format(expirationDate);
    }

    public String getPurchaseDateString(){
        DateFormat dateFormat = new SimpleDateFormat(datePattern);
        return dateFormat.format(purchaseDate);
    }
}
