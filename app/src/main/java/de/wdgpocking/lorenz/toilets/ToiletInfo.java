package de.wdgpocking.lorenz.toilets;

public class ToiletInfo {

    /**
     * Extends usability of Marker bc Marker class cannot be inherited from
     * therefore must be saved with reference to Marker object see ToiletManager
     */

    private int ID;

    private float rating;
    private String description;
    private float price;
    private char currency;

    public ToiletInfo(){
    }

    /*
        using builder
        setters return object
        --> multiple setters in a row
     */
    public ToiletInfo rating(float rating){
        this.rating = rating;
        return this;
    }

    public ToiletInfo price(float price){
        this.price = price;
        return this;
    }

    public ToiletInfo description(String description){
        this.description = description;
        return this;
    }

    public float getRating() {
        return rating;
    }

    public String getDescription() {
        return description;
    }

    public float getPrice() {
        return price;
    }

    public int getID() {
        return ID;
    }

    public ToiletInfo setID(int ID) {
        this.ID = ID;
        return this;
    }

    public char getCurrency() {
        return currency;
    }

    public ToiletInfo setCurrency(char currency) {
        this.currency = currency;
        return this;
    }
}
