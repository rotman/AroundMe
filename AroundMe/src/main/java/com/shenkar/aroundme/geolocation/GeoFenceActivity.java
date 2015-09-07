package com.shenkar.aroundme.geolocation;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.GeoPt;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.appConst;
import com.shenkar.aroundme.MainActivity;
import com.shenkar.aroundme.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import java.util.ArrayList;
import java.util.List;

/**
 * activity that handle the geofence view
 * when a user has sent me a location based message this screen will apear
 * to let the user know that a location based message has been sent
 */
public class GeoFenceActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private static final String TAG = "GeoFenceActivity";
    private GoogleApiClient mGoogleApiClient;
    private GeoPt mGeoPt;
    private long msgId;
    private static List<Geofence> mGeoFenceList = new ArrayList<>();
    private int geoFenceRadius;
    private PendingIntent mGeofencePendingIntent;
    private String msgContent;
    private String msgFrom;
    private String msgTo;

    /**
     * Called when the activity is first created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_fence);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mGeoPt = new GeoPt();
            mGeoPt.setLatitude(extras.getFloat(appConst.MY_LAT));
            mGeoPt.setLongitude(extras.getFloat(appConst.MY_LONG));
            msgId = extras.getLong(appConst.M_ID);
            geoFenceRadius = extras.getInt(appConst.GEOFENCE_RADIUS_IN_METERS);
            msgContent = extras.getString(appConst.MESSAGE_CONTENT);
            msgFrom = extras.getString(appConst.MESSAGE_FROM);
            msgTo = extras.getString(appConst.MY_MAIL);
            buildGoogleApiClient();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeoFenceList);
        return builder.build();
    }

    private PendingIntent getGeoFencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        intent.putExtra(appConst.MESSAGE_FROM, msgFrom);
        intent.putExtra(appConst.MESSAGE_CONTENT, msgContent);
        intent.putExtra(appConst.MY_MAIL,msgTo);
        intent.putExtra(appConst.MY_LONG, mGeoPt.getLongitude());
        intent.putExtra(appConst.MY_LAT, mGeoPt.getLatitude());
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    public void activateGeoFence() {
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeoFencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            securityException.getMessage();
        }
    }

    public void addGeoFence() {
            mGeoFenceList.add(new Geofence.Builder()
                    .setRequestId(String.valueOf(msgId))
                    .setCircularRegion(mGeoPt.getLatitude(), mGeoPt.getLongitude(), geoFenceRadius)
                    .setExpirationDuration(appConst.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build());

            activateGeoFence();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "onConnected");
        addGeoFence();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            Toast.makeText(this, "GeoFence Added", Toast.LENGTH_SHORT).show();
        }
        else {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    status.getStatusCode());
            Log.e(TAG, errorMessage);
        }
        //add counter for the page
        new CountDownTimer(3000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                Intent intent = new Intent(GeoFenceActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }.start();
    }
}
