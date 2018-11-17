package de.wdgpocking.lorenz.toilets;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

public class ToiletManager {
    private HashMap<Marker, ToiletInfo> toilets;

    public ToiletManager(){
        toilets = new HashMap<>();
    }

    public void addToilet(Marker marker, ToiletInfo toiletInfo){
        toilets.put(marker, toiletInfo);
    }

    public void removeToilet(Marker marker){
        toilets.remove(marker);
        marker.remove();
    }

    public boolean checkID(int id){
        for(Map.Entry<Marker, ToiletInfo> entry : toilets.entrySet()){
            if(entry.getValue().getID() == id){
                return true;
            }
        }
        return false;
    }

    public ToiletInfo getToiletInfo(Marker marker){
        return toilets.get(marker);
    }
}
