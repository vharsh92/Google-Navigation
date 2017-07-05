package com.example.googlenavigation;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class SplashScreenActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleApiClient;
    private LocationManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
                if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                    buildAlertMessageNoGps();
                }else if(!checkNetwork()){
                    Toast.makeText(SplashScreenActivity.this,"No Internet. Turn on the internet",Toast.LENGTH_LONG).show();
                }else {
                    startActivity(new Intent(SplashScreenActivity.this,MapsActivity.class));
                }
            }
        },1000);
    }

    private void buildAlertMessageNoGps() {

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(SplashScreenActivity.this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                status.startResolutionForResult(
                                        SplashScreenActivity.this, 1000);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            });
        }

    }

    public boolean checkNetwork(){
        ConnectivityManager connManager = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager teleManager =  (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);

        NetworkInfo netInfo = connManager.getActiveNetworkInfo();
        if (netInfo == null ||!connManager.getBackgroundDataSetting())
        {
            //Toast.makeText(getApplicationContext(), "Network Connection Error", Toast.LENGTH_LONG).show();
            return false;
        }

        int type = netInfo.getType();
        int subType = netInfo.getSubtype();

        if (type == ConnectivityManager.TYPE_WIFI || type == ConnectivityManager.TYPE_MOBILE)
        {
            return netInfo.isConnected();
        } else if (type == ConnectivityManager.TYPE_MOBILE && subType == TelephonyManager.NETWORK_TYPE_UMTS && !teleManager.isNetworkRoaming())
        {
            return netInfo.isConnected();
        } else
        {
            return false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
