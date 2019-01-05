package de.wdgpocking.lorenz.toilets;

import com.google.android.gms.maps.model.Marker;

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

    public void removeIfExists(double lat, double lng){
        for(Marker m : toilets.keySet()){
            if(m.getPosition().latitude == lat && m.getPosition().longitude == lng){
                removeToilet(m);
                return;
            }
        }
    }

    public ToiletInfo getToiletInfo(Marker marker){
        return toilets.get(marker);
    }
}
