package de.wdgpocking.lorenz.toilets;

public class ToiletInfo {

    private float rating;
    private String name;

    public ToiletInfo(){
    }

    /*
        setters return object
        --> multiple setters in a row
     */
    public ToiletInfo rating(float rating){
        this.rating = rating;
        return this;
    }

    public ToiletInfo name(String name){
        this.name = name;
        return this;
    }
}
