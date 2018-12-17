package de.wdgpocking.lorenz.toilets.Database;

import com.google.android.gms.maps.model.LatLng;

public class DatabaseToilet {
    private String title;
    private LatLng latlng;
    private String description;
    private float rating;
    private float price;

    public DatabaseToilet(){
    }

    public String getTitle() {
        return title;
    }

    public DatabaseToilet setTitle(String title) {
        this.title = title;
        return this;
    }

    public LatLng getLatlng() {
        return latlng;
    }

    public DatabaseToilet setLatlng(LatLng latlng) {
        this.latlng = latlng;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public DatabaseToilet setDescription(String description) {
        this.description = description;
        return this;
    }

    public float getRating() {
        return rating;
    }

    public DatabaseToilet setRating(float rating) {
        this.rating = rating;
        return this;
    }

    public float getPrice() {
        return price;
    }

    public DatabaseToilet setPrice(float price) {
        this.price = price;
        return this;
    }
}
