package de.wdgpocking.lorenz.toilets;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

public class ToiletManager {
    private HashMap<Marker, ToiletInfo> toilets;

    public ToiletManager(){ }

    public void addToilet(Marker marker, ToiletInfo toiletInfo){
        toilets.put(marker, toiletInfo);
    }

    public void removeToilet(Marker marker){
        toilets.remove(marker);
        marker.remove();
    }

    public ToiletInfo getToiletInfo(Marker marker){
        return toilets.get(marker);
    }
}
