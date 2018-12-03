package de.wdgpocking.lorenz.toilets;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.support.design.widget.BottomSheetBehavior;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

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
    private BottomSheetBehavior.BottomSheetCallback bottomSheetCallback;

    private Marker currentMarker;

    private DataHandler localToilets;

    private ToiletManager toiletManager;

    private EditText nameTxt;
    private EditText descriptionTxt;
    private EditText priceTxt;
    private ImageButton editButton;
    private Spinner spinner;

    private boolean locked;

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
                        .title("Custom Toilet")
                        .icon(getToiletMarkerBitmap());
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
                showToiletInfo(marker);
                currentMarker = marker;
                return false;
            }
        };

        bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                saveInput();
                lockInput();
                hideKeyboard();
            }
        };

        sheetBehavior.setBottomSheetCallback(bottomSheetCallback);

        nameTxt = findViewById(R.id.nameTxt);
        descriptionTxt = findViewById(R.id.descriptionTxt);
        priceTxt = findViewById(R.id.priceTxt);
        editButton = findViewById(R.id.editBtn);

        spinner = (Spinner) findViewById(R.id.currencySpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currencies_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                       long id) {
                ((TextView) view).setTextColor(Color.BLACK);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });

        spinner.setSelection(0);

        lockInput();
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
                    //Toast.makeText(getApplicationContext(), "We can't help you then", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }
    }

    private void showToiletInfo(Marker m){
        //LOAD ToiletInfo corresponding to Marker
        ToiletInfo tInfo = toiletManager.getToiletInfo(m);

        nameTxt.setText(m.getTitle());
        descriptionTxt.setText(tInfo.getDescription());
        priceTxt.setText(String.valueOf(tInfo.getPrice()));
        String[] currencies = getResources().getStringArray(R.array.currencies_array);
        int i = 0;
        for(int j = 0; j < currencies.length; j++){
            if(currencies[j].charAt(0) == tInfo.getCurrency()){
                i = j;
            }
        }
        spinner.setSelection(i);
        //pull up bottomsheet
        //sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void deleteCurrent(View v){
        if(currentMarker == null){
            //Toast.makeText(getApplicationContext(), "please select a toilet", Toast.LENGTH_SHORT);
        }else{
            //pop-up window "really wanna delete? lol"
            localToilets.deleteToiletByID(toiletManager.getToiletInfo(currentMarker).getID());
            toiletManager.removeToilet(currentMarker);
            currentMarker.remove();
        }
    }

    public void saveCurrentToDatabase(View v){
        if(currentMarker == null){
            //Toast.makeText(getApplicationContext(), "please select a toilet", Toast.LENGTH_SHORT);
        }else{
            ToiletInfo toiletInfo = toiletManager.getToiletInfo(currentMarker);
            localToilets.addToilet(new DatabaseToilet()
                    .setID(toiletInfo.getID())
                    .setDescription(toiletInfo.getDescription())
                    .setLatlng(currentMarker.getPosition())
                    .setTitle(currentMarker.getTitle())
                    .setRating(toiletInfo.getRating())
                    .setPrice(toiletInfo.getPrice())
                    .setCurrency(toiletInfo.getCurrency())
            );
        }
    }

    public void confirmEntry(){
        saveInput();

        hideKeyboard();

        sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void saveInput(){
        if(currentMarker != null) {
            ToiletInfo tInfo = toiletManager.getToiletInfo(currentMarker);

            currentMarker.setTitle(nameTxt.getText().toString());
            tInfo.description(descriptionTxt.getText().toString());
            tInfo.price(Float.valueOf(priceTxt.getText().toString()));
            tInfo.setCurrency(spinner.getSelectedItem().toString().charAt(0));
        }
    }

    private void lockInput(){
        locked = true;

//        nameTxt.setInputType(InputType.TYPE_NULL);
//        descriptionTxt.setInputType(InputType.TYPE_NULL);
//        priceTxt.setInputType(InputType.TYPE_NULL);

        nameTxt.setFocusable(false);
        descriptionTxt.setFocusable(false);
        priceTxt.setFocusable(false);

        nameTxt.setFocusableInTouchMode(false);
        descriptionTxt.setFocusableInTouchMode(false);
        priceTxt.setFocusableInTouchMode(false);

        spinner.setEnabled(false);

        //set button to edit
        editButton.setImageResource(R.drawable.ic_edit);

//        nameTxt.setClickable(false);
//        descriptionTxt.setClickable(false);
//        priceTxt.setClickable(false);
    }

    private void allowInput(){
        locked = false;

        nameTxt.setFocusableInTouchMode(true);
        descriptionTxt.setFocusableInTouchMode(true);
        priceTxt.setFocusableInTouchMode(true);

        nameTxt.setFocusable(true);
        descriptionTxt.setFocusable(true);
        priceTxt.setFocusable(true);

        spinner.setEnabled(true);


        //set button to "done"
        editButton.setImageResource(R.drawable.ic_done);
//        nameTxt.setClickable(true);
//        descriptionTxt.setClickable(true);
//        priceTxt.setClickable(true);

//        nameTxt.setInputType(InputType.TYPE_CLASS_TEXT);
//        descriptionTxt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
//        priceTxt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
    }

    public void changeLock(View v){
        //if edit button clicked
        if(locked){
            allowInput();
        }else{
            confirmEntry();
            lockInput();
        }
    }

    public void showRoute(View v){
        if(currentMarker == null){
            return;
        }

        double latDest = currentMarker.getPosition().latitude;
        double lngDest = currentMarker.getPosition().longitude;

        //own location

        String directionsKey = getString(R.string.google_directions_key);


        //load json
        //https://maps.googleapis.com/maps/api/directions/json?origin=myLoc&destination=latDest, lngDest&key=directionsKey&mode=walking

        //draw polylines

        /*
        Polyline line = map.addPolyline(new PolylineOptions()
                .add(new LatLng(51.5, -0.1), new LatLng(40.7, -74.0))
                .width(5)
                .color(Color.BLUE));
        */
    }

    private void loadDatabaseToilet(DatabaseToilet dbT){
        MarkerOptions mOpt = new MarkerOptions()
                .position(dbT.getLatlng())
                .title(dbT.getTitle())
                .icon(getToiletMarkerBitmap());
        Marker m = map.addMarker(mOpt);
        ToiletInfo tInfo = new ToiletInfo()
                .description(dbT.getDescription())
                .price(dbT.getPrice())
                .rating(dbT.getRating())
                .setID(dbT.getID())
                .setCurrency(dbT.getCurrency());
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

    private BitmapDescriptor getToiletMarkerBitmap() {
        int height = 160;
        int width = 80;
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawableForDensity(R.drawable.ic_toilet_marker, 2000);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap marker = Bitmap.createScaledBitmap(b, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(marker);
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
