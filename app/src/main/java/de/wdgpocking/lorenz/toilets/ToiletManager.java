package de.wdgpocking.lorenz.toilets;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

public class ToiletManager {
    private HashMap<MarkerOptions, ToiletInfo> toilets;
    private GoogleMap map;

    public ToiletManager(GoogleMap map){
        this.map = map;
    }

    public void addToilet(MarkerOptions markerOptions, ToiletInfo toiletInfo){
        map.addMarker(markerOptions);
        toilets.put(markerOptions, toiletInfo);
    }

    public void removeToilet(MarkerOptions markerOptions){
        toilets.remove(markerOptions);
        map.clear();
        for(MarkerOptions mOpt : toilets.keySet()){
            map.addMarker(mOpt);
        }
    }

    public ToiletInfo getToiletInfo(MarkerOptions markerOptions){
        return toilets.get(markerOptions);
    }
}
