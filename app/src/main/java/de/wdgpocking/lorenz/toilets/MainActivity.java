package de.wdgpocking.lorenz.toilets;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.support.design.widget.BottomSheetBehavior;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import de.wdgpocking.lorenz.toilets.Database.DatabaseHelper;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap map;
    private BottomSheetBehavior sheetBehavior;
    private boolean hud;
    private static final int PEEK_HEIGHT_COLLAPSED = 100;
    private GoogleMap.OnMapClickListener onMapClickListener;
    private GoogleMap.OnMapLongClickListener onMapLongClickListener;
    private DatabaseHelper localToilets;

    private ToiletManager toiletManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_bottom_sheet_layout);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        toiletManager = new ToiletManager();
        localToilets = new DatabaseHelper(getApplicationContext());

        hud = true;
        View bottomSheet = findViewById(R.id.bottom_sheet1);
        sheetBehavior = BottomSheetBehavior.from(bottomSheet);
        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        sheetBehavior.setPeekHeight(PEEK_HEIGHT_COLLAPSED);

        onMapClickListener = new GoogleMap.OnMapClickListener() {
            /**
             * @param latLng
             * Hides or Unhides HUD (BottomSheet + SearchBar)
             */
            @Override
            public void onMapClick(LatLng latLng) {
                //TODO
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if(hud){
                    sheetBehavior.setPeekHeight(0);
                }else {
                    sheetBehavior.setPeekHeight(PEEK_HEIGHT_COLLAPSED);
                }
                //switch if hud is shown or not
                hud = !hud;
            }
        };

        onMapLongClickListener = new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                /**
                 new toilet marker
                 menu from below should pop up --> like in google maps
                 options to give details to the toilet and submit it
                 should always be saved to local database for later use
                 */
                //TODO

                MarkerOptions mOpt = new MarkerOptions()
                        .position(latLng)
                        .title("Custom Toilet");
                Marker marker = map.addMarker(mOpt);
                ToiletInfo tInfo = new ToiletInfo()
                        .rating(5f)
                        .description("")
                        .price(0f);

                toiletManager.addToilet(marker, tInfo);

                showToiletInfo(marker);
            }
        };
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapLongClickListener(onMapLongClickListener);
        map.setOnMapClickListener(onMapClickListener);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        showToiletInfo(marker);
        return false;
    }

    private void showToiletInfo(Marker m){
        //TODO
        //LOAD ToiletInfo corresponding to Marker
        ToiletInfo tInfo = toiletManager.getToiletInfo(m);
        //Put ToiletInfo into Bottomsheet
        LinearLayout bottomsheet = findViewById(R.id.bottom_sheet_info);
        //bottomsheet.findViewById()
        //pull up bottomsheet
        //show hud
        hud = true;

    }
}
