package de.wdgpocking.lorenz.toilets;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.View;
import android.support.design.widget.BottomSheetBehavior;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Random;

import de.wdgpocking.lorenz.toilets.Database.DataHandler;
import de.wdgpocking.lorenz.toilets.Database.DatabaseToilet;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap map;
    protected static final int MY_LOCATION_PERMISSION = 1;
    private BottomSheetBehavior sheetBehavior;
    private int PEEK_HEIGHT_COLLAPSED;
    private GoogleMap.OnMapLongClickListener onMapLongClickListener;
    private GoogleMap.OnMarkerClickListener onMarkerClickListener;

    private Marker currentMarker;

    private DataHandler localToilets;

    private ToiletManager toiletManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_bottom_sheet_layout);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        PEEK_HEIGHT_COLLAPSED = dpToPx(50);

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
        localToilets = new DataHandler(getApplicationContext());

        View bottomSheet = findViewById(R.id.bottom_sheet1);
        sheetBehavior = BottomSheetBehavior.from(bottomSheet);
        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        sheetBehavior.setPeekHeight(PEEK_HEIGHT_COLLAPSED);

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
                        .price(0f)
                        .setID(generateID());

                toiletManager.addToilet(marker, tInfo);

                //showToiletInfo(marker);
            }
        };

        onMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(getApplicationContext(), "marker clicked", Toast.LENGTH_SHORT);
                showToiletInfo(marker);
                currentMarker = marker;
                return false;
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
            map.setMyLocationEnabled(true);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_PERMISSION);
            }
        }

        map.setOnMapLongClickListener(onMapLongClickListener);
        map.setOnMarkerClickListener(onMarkerClickListener);

        loadAllToiletsFromDB();
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

    private void showToiletInfo(Marker m){
        //LOAD ToiletInfo corresponding to Marker
        ToiletInfo tInfo = toiletManager.getToiletInfo(m);
        EditText nameTxt = findViewById(R.id.nameTxt);
        EditText descriptionTxt = findViewById(R.id.descriptionTxt);
        EditText priceTxt = findViewById(R.id.priceTxt);

        nameTxt.setText(m.getTitle());
        descriptionTxt.setText(tInfo.getDescription());
        priceTxt.setText(String.valueOf(tInfo.getPrice()));
        //pull up bottomsheet
        //sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void deleteCurrent(View v){
        if(currentMarker == null){
            Toast.makeText(getApplicationContext(), "please select a toilet", Toast.LENGTH_SHORT);
        }else{
            //pop-up window "really wanna delete? lol"
            localToilets.deleteToiletByID(toiletManager.getToiletInfo(currentMarker).getID());
            toiletManager.removeToilet(currentMarker);
            currentMarker.remove();
        }
    }

    public void saveCurrentToDatabase(View v){
        if(currentMarker == null){
            Toast.makeText(getApplicationContext(), "please select a toilet", Toast.LENGTH_SHORT);
        }else{
            ToiletInfo toiletInfo = toiletManager.getToiletInfo(currentMarker);
            localToilets.addToilet(new DatabaseToilet()
                    .setID(toiletInfo.getID())
                    .setDescription(toiletInfo.getDescription())
                    .setLatlng(currentMarker.getPosition())
                    .setTitle(currentMarker.getTitle())
                    .setRating(toiletInfo.getRating())
                    .setPrice(toiletInfo.getPrice())
            );

            Toast.makeText(getApplicationContext(), "toilet saved", Toast.LENGTH_SHORT);
        }
    }

    public void confirmEntry(View v){
        EditText nameTxt = findViewById(R.id.nameTxt);
        EditText descriptionTxt = findViewById(R.id.descriptionTxt);
        EditText priceTxt = findViewById(R.id.priceTxt);

        ToiletInfo tInfo = toiletManager.getToiletInfo(currentMarker);

        currentMarker.setTitle(nameTxt.getText().toString());
        tInfo.description(descriptionTxt.getText().toString());
        tInfo.price(Float.valueOf(priceTxt.getText().toString()));

        //unfocus all edittexts
        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void loadDatabaseToilet(DatabaseToilet dbT){
        MarkerOptions mOpt = new MarkerOptions()
                .position(dbT.getLatlng())
                .title(dbT.getTitle());
        Marker m = map.addMarker(mOpt);
        ToiletInfo tInfo = new ToiletInfo()
                .description(dbT.getDescription())
                .price(dbT.getPrice())
                .rating(dbT.getRating())
                .setID(dbT.getID());
        toiletManager.addToilet(m, tInfo);
    }

    private void loadAllToiletsFromDB(){
        ArrayList<DatabaseToilet> toilets = localToilets.getAllToilets();
        for(DatabaseToilet t : toilets){
            loadDatabaseToilet(t);
        }
    }

    private int generateID(){
        int id;
        do{
            id = randomInt(0, 100000);
        }while(toiletManager.checkID(id));

        return id;
    }

    private int randomInt(int min, int max){
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
