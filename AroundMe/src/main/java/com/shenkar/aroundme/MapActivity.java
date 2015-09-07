package com.shenkar.aroundme;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.GeoPt;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.Message;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.UserAroundMe;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.UserAroundMeCollection;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.appConst;
import com.shenkar.aroundme.bl.AroundMeApplication;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.util.DateTime;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * map activity that handle the GUI of all user around me and location
 * based messages
 */
public class MapActivity extends Activity {
    private GoogleMap mMap;
    private static int radius = 10000;
    private static int msgRadius = 10000;
    private static AroundMeApplication application;
    private static final String TAG = "MapActivity";
    private String myMail;
    @Bind(R.id.radiusBar)
    SeekBar radiusBar;


    /**
     * Called when the activity is first created
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        myMail = intent.getStringExtra(appConst.MY_MAIL);
        application = (AroundMeApplication)getApplication();
        UserAroundMeCollection coll = application.getController().getUsersAroundMe(myMail,radius);
        radiusBar.setProgress(radius);
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                locationBasedMessage(latLng);
            }

        });
        mMap.setMyLocationEnabled(true);
        radiusBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radius = progress;
                Toast.makeText(MapActivity.this, "radius:" + String.valueOf(radius / 1000) + " Km",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                UserAroundMeCollection coll = application.getController().getUsersAroundMe(myMail, radius);
                showInMap(coll);
            }
        });
        showInMap(coll);
    }

    /*
     * get all the user from the server and show the on the map
     * including user pic
     */
    private void showInMap(UserAroundMeCollection coll) {
        if (coll.size() > 0) {
            mMap.clear();
            List<UserAroundMe> users = coll.getItems();
            for (final UserAroundMe user : users) {
                final MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.alpha(0.7f);
                String mail = user.getMail();
                String url = user.getImageUrl();
                String name = user.getDisplayName();
                markerOptions.title(name);
                markerOptions.position(new LatLng(user.getLocation().getLatitude(), user.getLocation().getLongitude()));
                markerOptions.snippet("Status: online");
                application.getController().getImage(mail, url, new ApplicationCallback<Bitmap>() {
                    @Override
                    public void done(Bitmap result, Exception e) {
                        if (e == null) {
                            Bitmap bhalfsize = Bitmap.createScaledBitmap(result, result.getWidth() / 2, result.getHeight() / 2, false);
                            markerOptions.icon(BitmapDescriptorFactory
                                    .fromBitmap(application.getController().createCircleBitmap(bhalfsize)));
                        } else
                            Log.e(TAG, e.getMessage());
                    }
                });
                //add the marker with all the parameter
                mMap.addMarker(markerOptions);
                if (user.getMail().equals(myMail)) {
                    CircleOptions circleOptions = new CircleOptions();
                    circleOptions.radius(radius);
                    circleOptions.fillColor(0x5500ff00);
                    circleOptions.strokeWidth(1);
                    circleOptions.center(markerOptions.getPosition());
                    mMap.addCircle(circleOptions);
                }
            }
        }
    }

    /*
     * on click on the green button send a location based message using
     * my last knows location
     */
    public void currentLocationMessageOnClick(View view){
        final SharedPreferences prefs = getApplicationContext().getSharedPreferences(MapActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
        float lat = prefs.getFloat(appConst.MY_LAT, 0);
        float lng = prefs.getFloat(appConst.MY_LONG, 0);
        LatLng latLng = new LatLng(lat, lng);
        Log.i(TAG, String.valueOf(lat) + " " + String.valueOf(lng));
        locationBasedMessage(latLng);
    }

    /*
     * method that handles the location based message interface
     */
    public void locationBasedMessage(final LatLng latLng){
        String[] array = {"One user","all users"};
        new MaterialDialog.Builder(MapActivity.this)
                .title("Location based message")
                .negativeText("Cancel")
                .items(array)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        final List<UserAroundMe> users = application.getController().getAllUsers(myMail).getItems();
                        switch (i) {
                            case 0:
                                int size = users.size();
                                String[] usersNames = new String[size];
                                for (int j=0 ; j<size ; j++) {
                                    usersNames[j] = users.get(j).getMail();
                                }
                                new MaterialDialog.Builder(MapActivity.this)
                                        .title("Choose user")
                                        .items(usersNames)
                                        .negativeText("Cancel")
                                        .itemsCallback(new MaterialDialog.ListCallback() {
                                            @Override
                                            public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                                                final String userMailToSend = charSequence.toString();
                                                new MaterialDialog.Builder(MapActivity.this)
                                                        .title("say something to "+ userMailToSend)
                                                        .negativeText("Cancel")
                                                        .inputType(InputType.TYPE_CLASS_TEXT)
                                                        .input("", "", new MaterialDialog.InputCallback() {
                                                            @Override
                                                            public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                                                                String contentToSend = charSequence.toString();
                                                                Message message = new Message();
                                                                message.setContnet(contentToSend);
                                                                message.setFrom(myMail);
                                                                message.setTo(userMailToSend);
                                                                GeoPt geoPt = new GeoPt();
                                                                geoPt.setLatitude((float)latLng.latitude);
                                                                geoPt.setLongitude((float) latLng.longitude);
                                                                message.setLocation(geoPt);
                                                                message.setReadRadius(msgRadius);
                                                                message.setTimestamp(new DateTime(System.currentTimeMillis()));
                                                                application.getController().sendMessage(message);
                                                            }
                                                        }).show();
                                            }
                                        }).show();
                                break;
                            case 1:
                                new MaterialDialog.Builder(MapActivity.this)
                                        .title("Say Something to Everyone")
                                        .negativeText("Cancel")
                                        .inputType(InputType.TYPE_CLASS_TEXT)
                                        .input("", "", new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                                                String contentToEveryone = charSequence.toString();
                                                GeoPt geoPt = new GeoPt();
                                                geoPt.setLatitude((float) latLng.latitude);
                                                geoPt.setLongitude((float) latLng.longitude);
                                                Message message = new Message();
                                                message.setContnet(contentToEveryone);
                                                message.setFrom(myMail);
                                                message.setLocation(geoPt);
                                                message.setReadRadius(msgRadius);
                                                message.setTimestamp(new DateTime(System.currentTimeMillis()));
                                                application.getController().sendLocationMessageToEveryone(message, users);
                                            }
                                        }).show();
                                break;
                        }
                    }
                }).show();
    }
}
