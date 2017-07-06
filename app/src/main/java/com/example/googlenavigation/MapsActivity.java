package com.example.googlenavigation;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.googlenavigation.model.DirectionResult;
import com.example.googlenavigation.model.Route;
import com.example.googlenavigation.model.Steps;
import com.example.googlenavigation.utils.APIClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private GoogleMap mMap;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private static final int REQUEST_CODE_AUTOCOMPLETE_SOURCE = 1;
    private static final int REQUEST_CODE_AUTOCOMPLETE_DROP = 2;
    private Location mLastKnownLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private TextView tvLocation, tvLocationDrop;
    private CardView cardViewSource, cardViewDrop;
    private Marker markerSource, markerDrop;
    private Button btnGetRoutes;
    private List<Polyline> polylineList = new ArrayList<>();
    ArrayList<String> directions = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        tvLocation = (TextView) findViewById(R.id.tvLocation);
        tvLocationDrop = (TextView) findViewById(R.id.tvLocationDrop);
        cardViewSource = (CardView) findViewById(R.id.card_view);
        cardViewDrop = (CardView) findViewById(R.id.cardviewDrop);
        btnGetRoutes = (Button) findViewById(R.id.btnGetRoutes);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        cardViewSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAutocompleteActivity(1);
            }
        });

        cardViewDrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAutocompleteActivity(2);
            }
        });

        tvLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAutocompleteActivity(1);
            }
        });

        tvLocationDrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAutocompleteActivity(2);
            }
        });

        btnGetRoutes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(markerSource!=null && markerDrop!=null)
                    getDirection();
                else
                    Toast.makeText(MapsActivity.this,"Select source and destination",Toast.LENGTH_LONG).show();
            }
        });
    }

    public void getDirection(){
        String str_origin =  markerSource.getPosition().latitude + "," + markerSource.getPosition().longitude;

        // Destination of route
        String str_dest = markerDrop.getPosition().latitude + "," + markerDrop.getPosition().longitude;

        // Sensor enabled
        Boolean sensor = false;
        String mode = "driving";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode + "&alternatives=" +true;

        GoogleRouteAPI googleRouteAPI = APIClient.getClient().create(GoogleRouteAPI.class);

        Call<DirectionResult> call = googleRouteAPI.getRoutes(str_origin,str_dest,sensor,mode,true);


        call.enqueue(new Callback<DirectionResult>() {
            @Override
            public void onResponse(Call<DirectionResult> call, Response<DirectionResult> response) {
                DirectionResult directionResults = (DirectionResult) response.body();
                Log.i("TTT", response.body() + "");
                String[] drc;
                ArrayList<LatLng> routelist = new ArrayList<LatLng>();
                if (directionResults.getRoutes().size() > 0) {
                    ArrayList<LatLng> decodelist;
                    for(int j=0;j<directionResults.getRoutes().size();j++) {
                        Polyline pline;
                        Route routeA = directionResults.getRoutes().get(j);
                        if (routeA.getLegs().size() > 0) {
                            List<Steps> steps = routeA.getLegs().get(0).getSteps();
                            Steps step;
                            com.example.googlenavigation.model.Location location;
                            String polyline;
                            drc = new String[steps.size()];
                            for (int i = 0; i < steps.size(); i++) {
                                step = steps.get(i);
                                location = step.getStart_location();
                                routelist.add(new LatLng(location.getLat(), location.getLng()));
                                polyline = step.getPolyline().getPoints();
                                decodelist = DirectionsJSONParser.decodePoly(polyline);
                                routelist.addAll(decodelist);
                                location = step.getEnd_location();
                                drc[i] = Html.fromHtml(step.getHtml_instructions()).toString();
                                routelist.add(new LatLng(location.getLat(), location.getLng()));


                            }
                            if(j%2==0){
                                PolylineOptions rectLine = new PolylineOptions().width(10).color(
                                        Color.GRAY);

                                for (int k = 0; k < routelist.size(); k++) {
                                    rectLine.add(routelist.get(k));
                                }
                                pline = mMap.addPolyline(rectLine);
                                pline.setClickable(true);
                                polylineList.add(pline);
                                routelist.clear();
                            }else if(j%3==0){
                                PolylineOptions rectLine = new PolylineOptions().width(10).color(
                                        Color.YELLOW);

                                for (int k = 0; k < routelist.size(); k++) {
                                    rectLine.add(routelist.get(k));
                                }
                                pline = mMap.addPolyline(rectLine);
                                pline.setClickable(true);
                                polylineList.add(pline);
                                routelist.clear();
                            }else {
                                PolylineOptions rectLine = new PolylineOptions().width(10).color(
                                        Color.RED);

                                for (int k = 0; k < routelist.size(); k++) {
                                    rectLine.add(routelist.get(k));
                                }
                                pline = mMap.addPolyline(rectLine);
                                pline.setClickable(true);
                                polylineList.add(pline);
                                routelist.clear();
                            }
                            directions.add(Arrays.toString(drc));
                        }
                    }
                }

                mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
                    @Override
                    public void onPolylineClick(Polyline polyline) {
                        for(int i=0;i<polylineList.size();i++){
                            String id = polylineList.get(i).getId();
                            if(polyline.getId().equals(polylineList.get(i).getId())){
                                Toast.makeText(getApplicationContext(),directions.get(i),Toast.LENGTH_LONG).show();
                                break;
                            }
                        }

                    }
                });

            }

            @Override
            public void onFailure(Call<DirectionResult> call, Throwable t) {
                Log.i("TTT",t.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (checkAndRequestPermissions()) {
            setUpMap();
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            }
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    public void setUpMap() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

    }

    private void openAutocompleteActivity(int tag) {
        try {
            // The autocomplete activity requires Google Play Services to be available. The intent
            // builder checks this and throws an exception if it is not the case.
            if(tag==1){
                Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                        .build(MapsActivity.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE_SOURCE);
            }else {
                Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                        .build(MapsActivity.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE_DROP);
            }

        } catch (GooglePlayServicesRepairableException e) {
            // Indicates that Google Play Services is either not installed or not up to date. Prompt
            // the user to correct the issue.
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    0 /* requestCode */).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            // Indicates that Google Play Services is not available and the problem is not easily
            // resolvable.
            String message = "Google Play Services is not available: " +
                    GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

            //Log.e("TTT", message);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_AUTOCOMPLETE_SOURCE) {
            //Log.i("TTT","requestcode"+requestCode);
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                tvLocation.setText(place.getName());
                if(markerSource!=null)
                    markerSource.remove();
                markerSource = mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("source"));
                CameraUpdate u = CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 16);
                mMap.animateCamera(u);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
            } else if (resultCode == RESULT_CANCELED) {

            }
        }

        if (requestCode == REQUEST_CODE_AUTOCOMPLETE_DROP) {
            //Log.i("TTT","requestcode"+requestCode);
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                tvLocationDrop.setText(place.getName());
                if(markerDrop!=null)
                    markerDrop.remove();
                markerDrop = mMap.addMarker(new MarkerOptions().position(place.getLatLng()).title("drop"));
                CameraUpdate u = CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 16);
                mMap.animateCamera(u);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
            } else if (resultCode == RESULT_CANCELED) {

            }
        }
    }


    private  boolean checkAndRequestPermissions() {
        int permissionSendMessage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        mMap.setMyLocationEnabled(true);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        setUpMap();
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            showDialogOK("Location Services Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    setUpMap();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

}
