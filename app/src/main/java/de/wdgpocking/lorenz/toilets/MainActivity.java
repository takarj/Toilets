package de.wdgpocking.lorenz.toilets;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
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
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import de.wdgpocking.lorenz.toilets.Database.DataHandler;
import de.wdgpocking.lorenz.toilets.Database.DatabaseToilet;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback{

    protected static final int MY_LOCATION_PERMISSION = 1;
    protected static final int INTERNET_PERMISSION = 2;
    private static final int PORT = 9991;
    private static final String HOST = "192.168.2.109";
    private GoogleMap map;
    private BottomSheetBehavior sheetBehavior;
    private int PEEK_HEIGHT_COLLAPSED;
    private GoogleMap.OnMapLongClickListener onMapLongClickListener;
    private GoogleMap.OnMapClickListener onMapClickListener;
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
    private Polyline currentRoute;
    private LatLng currentLocation;
    private float lastOffset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.maps_bottom_sheet_layout);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        float peekHeight = getResources().getDimension(R.dimen.peekHeight) / 2;

        PEEK_HEIGHT_COLLAPSED = dpToPx(peekHeight);

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
            layoutParams.setMargins(0, 0, 30, 140);


            View compassButton = ((View) mapView.findViewById(1).getParent()).findViewById(5);      //move compassButton

            RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams)
                    compassButton.getLayoutParams();
            // position on left bottom  as compass button is in left column
            layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams2.setMargins(30, 0, 0, 140);
        }

        toiletManager = new ToiletManager();
        localToilets = new DataHandler(getApplicationContext());

        lastOffset = 0;

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

                createNewToilet("CustomToilet", latLng, "", 5f, 0f);

                //showToiletInfo(marker);
            }
        };

        onMapClickListener = new GoogleMap.OnMapClickListener(){
            @Override
            public void onMapClick(LatLng latLng) {
                currentMarker = null;
                clearBottomSheet();
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
                if((newState == BottomSheetBehavior.STATE_DRAGGING || newState == BottomSheetBehavior.STATE_EXPANDED) && currentMarker == null){
                    Toast.makeText(getApplicationContext(), "Please select a toilet", Toast.LENGTH_SHORT).show();
                    BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
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
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, MY_LOCATION_PERMISSION);
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.INTERNET}, INTERNET_PERMISSION);
            }
        }

        GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            }
        };

        map.setOnMyLocationChangeListener(myLocationChangeListener);

        map.setOnMapLongClickListener(onMapLongClickListener);
        map.setOnMarkerClickListener(onMarkerClickListener);
        map.setOnMapClickListener(onMapClickListener);

        loadAllToiletsFromDB();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_LOCATION_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        map.setMyLocationEnabled(true);
                    }
                }else{
                    //Toast.makeText(getApplicationContext(), "We can't help you then", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            case INTERNET_PERMISSION:
                break;
        }
    }

    public void findToiletsInRange(View v){
        //TODO
        new AsyncGetToilets().execute(getBounds());
    }

    public void uploadToilet(View v){
        //TODO
        if(currentMarker == null){
            Toast.makeText(getApplicationContext(), "Please select a toilet", Toast.LENGTH_SHORT).show();
        }else{
            saveInput();
            ToiletInfo tInfo = toiletManager.getToiletInfo(currentMarker);
            String[] params = new String[7];
            params[0] = currentMarker.getTitle();
            params[1] = String.valueOf(currentMarker.getPosition().latitude);
            params[2] = String.valueOf(currentMarker.getPosition().longitude);
            params[3] = tInfo.getDescription();
            params[4] = String.valueOf(tInfo.getRating());
            params[5] = String.valueOf(tInfo.getPrice());
            params[6] = String.valueOf(tInfo.getCurrency());
            new AsyncUploadToilet().execute(params);
        }
    }

    public void createNewToilet(String title, LatLng latLng, String description, float rating, float price){
        toiletManager.removeIfExists(latLng.latitude, latLng.longitude);

        MarkerOptions mOpt = new MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(getToiletMarkerBitmap());
        Marker marker = map.addMarker(mOpt);
        ToiletInfo tInfo = new ToiletInfo()
                .rating(rating)
                .description(description)
                .price(price)
                .setID(generateID());

        toiletManager.addToilet(marker, tInfo);
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
            Toast.makeText(getApplicationContext(), "Please select a toilet", Toast.LENGTH_SHORT).show();
        }else{
            //pop-up window "really wanna delete? lol"
            localToilets.deleteToiletByID(toiletManager.getToiletInfo(currentMarker).getID());
            toiletManager.removeToilet(currentMarker);
            currentMarker = null;
            clearBottomSheet();
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            Toast.makeText(getApplicationContext(), "Toilet deleted", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearBottomSheet(){
        nameTxt.setText("");
        descriptionTxt.setText("");
        priceTxt.setText("0.0");
        spinner.setSelection(0);
    }

    public void saveCurrentToDatabase(View v){
        if(currentMarker == null){
            Toast.makeText(getApplicationContext(), "Please select a toilet", Toast.LENGTH_SHORT).show();
        }else{
            saveInput();
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
            Toast.makeText(getApplicationContext(), "Toilet saved", Toast.LENGTH_SHORT).show();
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
        if(currentMarker == null){
            Toast.makeText(getApplicationContext(), "Please select a toilet", Toast.LENGTH_SHORT).show();
            return;
        }

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
            Toast.makeText(getApplicationContext(), "Please select a toilet", Toast.LENGTH_SHORT).show();
            return;
        }

        double latDest = currentMarker.getPosition().latitude;
        double lngDest = currentMarker.getPosition().longitude;

        //own location
        double latCur = currentLocation.latitude;
        double lngCur = currentLocation.longitude;

        String directionsKey = getString(R.string.google_directions_key);


        //load json
        //https://maps.googleapis.com/maps/api/directions/json?origin=myLoc&destination=latDest, lngDest&key=directionsKey&mode=walking
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + latCur + ", " + lngCur + "&destination=" + latDest + ", "+ lngDest + "&key=" + directionsKey + "&mode=walking";

        if(currentRoute != null) {
            currentRoute.remove();
        }

        new AsyncRoute().execute(url);
    }

    private JsonObject getJsonFromUrl(String urlString){
        try {
            // Connect to the URL using java's native library
            URL url = new URL(urlString);
            URLConnection request = url.openConnection();
            request.connect();

            // Convert to a JSON object to print data
            JsonParser jp = new JsonParser(); //from gson
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element
            JsonObject rootobj = root.getAsJsonObject(); //May be an array, may be an object.
            return rootobj;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
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

    private int dpToPx(float dp) {
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

    private String[] getBounds(){
        String[] bounds = new String[4];
        VisibleRegion vR = map.getProjection().getVisibleRegion();
        bounds[0] = String.valueOf(vR.latLngBounds.southwest.latitude);
        bounds[1] = String.valueOf(vR.latLngBounds.northeast.latitude);
        bounds[2] = String.valueOf(vR.latLngBounds.southwest.longitude);
        bounds[3] = String.valueOf(vR.latLngBounds.northeast.longitude);
        return bounds;
    }

    private class AsyncRoute extends AsyncTask<String, Integer, Long>{

        private JsonObject routeJson;

        @Override
        protected Long doInBackground(String... strings) {
            routeJson = getJsonFromUrl(strings[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            try {
                if(routeJson.getAsJsonArray("routes").size() == 0){
                    return;
                }
                JsonArray steps = routeJson.getAsJsonArray("routes").get(0).getAsJsonObject().getAsJsonArray("legs").get(0).getAsJsonObject().getAsJsonArray("steps");

                PolylineOptions polyOpt = new PolylineOptions();

                for(int i = 0; i < steps.size(); i++){
                    polyOpt.add(new LatLng(steps.get(i).getAsJsonObject().getAsJsonObject("start_location").get("lat").getAsDouble(),
                                    steps.get(i).getAsJsonObject().getAsJsonObject("start_location").get("lng").getAsDouble()),
                            new LatLng(steps.get(i).getAsJsonObject().getAsJsonObject("end_location").get("lat").getAsDouble(),
                                    steps.get(i).getAsJsonObject().getAsJsonObject("end_location").get("lng").getAsDouble()));
                }

                polyOpt.width(5);
                polyOpt.jointType(JointType.ROUND);
                polyOpt.color(getResources().getColor(R.color.color2));

                currentRoute = map.addPolyline(polyOpt);

            }catch(NullPointerException e){
                e.printStackTrace();
                return;
            }
        }
    }

    private class AsyncGetToilets extends AsyncTask<String, Integer, Long>{
        IOException exception;
        ArrayList<String[]> toilets;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            toilets = new ArrayList<>();
        }

        @Override
        protected Long doInBackground(String... strings) {
            try {
                Socket socket = new Socket(HOST, PORT);

                InputStream inputToClient = socket.getInputStream();
                OutputStream outputFromClient = socket.getOutputStream();

                Scanner scanner = new Scanner(inputToClient, "UTF-8");
                PrintWriter clientPrintOut = new PrintWriter(new OutputStreamWriter(outputFromClient, "UTF-8"), true);

                clientPrintOut.println("getToilets"
                    + "?val%" + strings[0]
                    + "?val%" + strings[1]
                    + "?val%" + strings[2]
                    + "?val%" + strings[3]);
                while(scanner.hasNextLine()){
                    String line = scanner.nextLine();
                    if(line.equals("done")){
                        socket.close();
                    }
                    if(line.startsWith("toilet")){
                        String[] params = line.split("\\?val%");
                        toilets.add(params);
                    }
                }
                socket.close();
            }catch(IOException e){
                exception = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            if(exception != null) {
                Log.e("IOEXception", exception.getMessage());
                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
            }else {
                for (String[] params : toilets) {
                    createNewToilet(params[1],
                            new LatLng(Double.parseDouble(params[2]), Double.parseDouble(params[3])),
                            params[4],
                            Float.parseFloat(params[5]),
                            Float.parseFloat(params[6]));
                }
                Toast.makeText(getApplicationContext(), "Toilets received: " + toilets.size(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class AsyncUploadToilet extends AsyncTask<String, Integer, Long>{
        IOException exception;
        @Override
        protected Long doInBackground(String... strings) {
            try {
                Socket socket = new Socket(HOST, PORT);

                OutputStream outputFromClient = socket.getOutputStream();

                PrintWriter clientPrintOut = new PrintWriter(new OutputStreamWriter(outputFromClient, "UTF-8"), true);

/*                clientPrintOut.println("submit"
                        + "?val%" + currentMarker.getTitle()
                        + "?val%" + currentMarker.getPosition().latitude
                        + "?val%" + currentMarker.getPosition().latitude
                        + "?val%" + tInfo.getDescription()
                        + "?val%" + tInfo.getRating()
                        + "?val%" + tInfo.getPrice()
                        + "?val%" + tInfo.getCurrency());*/
                clientPrintOut.println("submit"
                        + "?val%" + strings[0]
                        + "?val%" + strings[1]
                        + "?val%" + strings[2]
                        + "?val%" + strings[3]
                        + "?val%" + strings[4]
                        + "?val%" + strings[5]
                        + "?val%" + strings[6]);
                socket.close();
            }catch(IOException e){
                exception = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);
            if(exception != null) {
                Log.e("IOEXception", exception.getMessage());
                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(), "Toilet submitted", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
