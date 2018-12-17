package de.wdgpocking.lorenz.toilets;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.support.design.widget.BottomSheetBehavior;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import de.wdgpocking.lorenz.toilets.Database.DatabaseHelper;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap map;
    protected static final int MY_LOCATION_PERMISSION = 1;
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

        //move MyLocationButton to bottom-right
        View mapView = mapFragment.getView();
        if (mapView != null &&
                mapView.findViewById(1) != null) {
            // Get the button view: mylocationbutton --> id 2
            //compassButton --> id 5
            View locationButton = ((View) mapView.findViewById(1).getParent()).findViewById(2);

            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 120);


            View compassButton = ((View) mapView.findViewById(1).getParent()).findViewById(5);      //move compassButton

            RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams)
                    compassButton.getLayoutParams();
            // position on left bottom  as compass button is in left column
            layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams2.setMargins(30, 0, 0, 120);
        }

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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);

            Location location = map.getMyLocation();

            if (location != null) {
                LatLng myLocation = new LatLng(location.getLatitude(),
                        location.getLongitude());
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                        10));
            }

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_PERMISSION);
            }
        }

        map.setOnMapLongClickListener(onMapLongClickListener);
        map.setOnMapClickListener(onMapClickListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_LOCATION_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        map.setMyLocationEnabled(true);
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "We can't help you then", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
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

    private void centerMapOnMyLocation() {
        location = map.getMyLocation();

        if (location != null) {
            LatLng myLocation = new LatLng(location.getLatitude(),
                    location.getLongitude());
        }
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
                Constants.MAP_ZOOM));
    }
}
