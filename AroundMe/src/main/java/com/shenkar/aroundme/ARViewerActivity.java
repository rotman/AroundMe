package com.shenkar.aroundme;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.Toast;

import com.appspot.enhanced_cable_88320.aroundmeapi.model.UserAroundMe;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.UserAroundMeCollection;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.appConst;
import com.shenkar.aroundme.bl.AroundMeApplication;
import com.wikitude.architect.ArchitectView;
import com.wikitude.architect.StartupConfiguration;

import org.json.JSONArray;

import java.io.IOException;

public class ARViewerActivity extends Activity {

    private final static double  TEST_LATITUDE =  32.019704;
    private final static double  TEST_LONGITUDE = 34.739239;
    private final static double  TEST_ALTITUDE = 16.5;
    protected LocationManager locationManager;
    protected ArchitectView                architectView;
    protected ArchitectView.SensorAccuracyChangeListener sensorAccuracyListener;
    protected Location                    lastKnownLocaton;
    protected Location srcLoc,destLoc;
    protected LocationListener              locationListener;
    protected ArchitectView.ArchitectUrlListener urlListener;
    protected JSONArray poiData;
    protected boolean isLoading = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arviewer);
        GLSurfaceView mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setEGLContextClientVersion(2);
        // check if the device fulfills the SDK'S minimum requirements
        if (!ArchitectView.isDeviceSupported(this)) {
            Toast.makeText(this, "minimum requirements not fulfilled",
                    Toast.LENGTH_LONG).show();
            this.finish();
            return;
        }

        this.architectView = (ArchitectView) this
                .findViewById(R.id.architectView);
        final StartupConfiguration config = new StartupConfiguration(appConst.WIKITUDE_LICENSE_KEY);
        this.architectView.onCreate(config);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {

                // Called when a new location is found by the network location provider.
                //makeUseOfNewLocation(location);
                architectView.setLocation(location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getAccuracy());
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.architectView.onPostCreate();
        AroundMeApplication application = (AroundMeApplication) getApplication();
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            this.architectView.load("index.html");
            UserAroundMeCollection coll = application.getController().getAllUsers("ruben@gmail.com");
            for(UserAroundMe userAroundMe : coll.getItems()){
                if(userAroundMe.getLocation() != null) {
                    this.architectView.setLocation(userAroundMe.getLocation().getLatitude(),
                            userAroundMe.getLocation().getLongitude(), TEST_ALTITUDE, 100f);
                    //TODO: send image url
                }
            }


           /*
           UserAroundMeCollection coll = application.getController().getAllUsers("ruben@gmail.com");
           List<UserAroundMe> users = coll.getItems();
           for (final UserAroundMe user : users){
               //int counter = 1;
               JSONObject jsonObject = new JSONObject();
               try {
                   //jsonObject.accumulate("id", counter);
                   if(user.getLocation() !=null) {
                       jsonObject.put("latitude", user.getLocation().getLatitude());
                       jsonObject.put("longitude", user.getLocation().getLongitude());
                       jsonObject.put("altitude", 40);
                       architectView.callJavascript("loadPoisFromJsonDataFn('" + jsonObject + ")'");
                   }
               } catch (JSONException e) {
                   e.printStackTrace();
               }

           }*/
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        this.architectView.setLocation(TEST_LATITUDE, TEST_LONGITUDE, TEST_ALTITUDE, 5f);
        architectView.onResume();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        this.architectView.setLocation(TEST_LATITUDE, TEST_LONGITUDE, TEST_ALTITUDE, 5f);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.architectView.setLocation(TEST_LATITUDE, TEST_LONGITUDE, TEST_ALTITUDE, 5f);
        architectView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // call mandatory live-cycle method of architectView
        if ( this.architectView != null ) {
            this.architectView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if ( this.architectView != null ) {
            this.architectView.onLowMemory();
        }
    }

    public static final boolean isVideoDrawablesSupported() {
        String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        return extensions != null && extensions.contains( "GL_OES_EGL_image_external" ) && android.os.Build.VERSION.SDK_INT >= 14 ;
    }

}
