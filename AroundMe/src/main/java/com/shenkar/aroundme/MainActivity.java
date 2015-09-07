package com.shenkar.aroundme;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import com.afollestad.materialdialogs.MaterialDialog;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.Conversation;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.GeoPt;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.appConst;
import com.shenkar.aroundme.bl.AroundMeApplication;
import com.shenkar.aroundme.deviceinfoendpoint.Deviceinfoendpoint;
import com.shenkar.aroundme.deviceinfoendpoint.model.DeviceInfo;
import com.shenkar.aroundme.swipemenulistview.CustomArrayAdapter;
import com.shenkar.aroundme.swipemenulistview.SwipeMenu;
import com.shenkar.aroundme.swipemenulistview.SwipeMenuCreator;
import com.shenkar.aroundme.swipemenulistview.SwipeMenuItem;
import com.shenkar.aroundme.swipemenulistview.SwipeMenuListView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationServices;
import com.melnykov.fab.FloatingActionButton;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * main activity where all conversation are displayed
 */
public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    @Bind(R.id.fabLists)
    FloatingActionButton fab;
    @Bind(R.id.conversationsListView)
    SwipeMenuListView listView;
    @Bind(R.id.emptyConversation)
    TextView tvEmpty;

    static final String TAG = "MainActivity";
    String SENDER_ID = "1047488186224";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int USER_MAIL_REQUEST_CODE = 2;
    private GoogleApiClient mGoogleApiClient;
    private String userMail;
    private static CustomArrayAdapter adapter;
    private static AroundMeApplication application;
    GoogleCloudMessaging gcm;
    Context context;
    String regid;
    private BroadcastReceiver newConversationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Long cid = intent.getLongExtra(appConst.C_ID,-1);
            if(cid!=-1) {
                createAdapter();
            }
        }
    };

    /**
     * Called when the activity is first created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //action bar details
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#4daf4e"));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#ffffff'> My Friend's List </font>"));

        context = getApplicationContext();
        application = (AroundMeApplication) getApplication();
        //application.getController().registerOnDataSourceChanged(this);
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context); //get the regId from Shared Preference
            if (regid.isEmpty()) { // if the regId doens't exists
                registerInBackground(); // create one
            }
            EndpointApiCreator.initialize(null);
            SharedPreferences prefs = getGCMPreferences(context);
            userMail = prefs.getString(appConst.MY_MAIL, "");
            if (userMail == null || userMail.equals("")) { //check if the user is already logged in
                Intent intent = new Intent(this, LoginActivity.class); //if not
                intent.putExtra(appConst.REGISTRATION_ID, regid); // go to login or register
                startActivityForResult(intent, USER_MAIL_REQUEST_CODE);
            } else
                Log.i(TAG, "onCreate:" + userMail);
            buildGoogleApiClient();
        } else
            Log.i(TAG, "No valid Google Play Services APK found.");
        List<Conversation> list = application.getController().getAllConversations();
        Collections.reverse(list);
        //on clicking the + button send to all user activity
        fab.attachToListView(listView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AllUsersActivity.class);
                startActivity(intent);
            }
        });
        //swipe menu options
        if (listView != null) {
            SwipeMenuCreator creator = new SwipeMenuCreator() {
                @Override
                public void create(SwipeMenu menu) {
                    //create an action that will be showed on swiping an item in the list
                    SwipeMenuItem item1 = new SwipeMenuItem(
                            getApplicationContext());
                    item1.setBackground(new ColorDrawable(Color.GRAY));
                    // set width of an option (px)
                    item1.setWidth(200);
                    item1.setTitle("Open");
                    item1.setTitleSize(18);
                    item1.setTitleColor(Color.WHITE);
                    menu.addMenuItem(item1);
                    SwipeMenuItem item2 = new SwipeMenuItem(
                            getApplicationContext());
                    // set item background
                    item2.setBackground(new ColorDrawable(Color.RED));
                    item2.setWidth(200);
                    item2.setTitle("Delete");
                    item2.setTitleSize(18);
                    item2.setTitleColor(Color.WHITE);
                    menu.addMenuItem(item2);
                }
            };

            //set MenuCreator
            listView.setMenuCreator(creator);
            // set SwipeListener
            listView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {
                @Override
                public void onSwipeStart(int position) {
                    // swipe start
                }

                @Override
                public void onSwipeEnd(int position) {
                    // swipe end
                }
            });

            //item click listener swipe menu delete or open
            listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                    final Conversation c = adapter.getItem(position);
                    switch (index) {
                        //open option
                        case 0:
                            c.setUnreadMessages(0);
                            application.getController().updateUnreadMessages(c.getUser2().getMail(),false);
                            Intent intent = new Intent(getBaseContext(), MessageActivity.class);
                            intent.putExtra(appConst.USER_MAIL_FOR_INTENT, c.getUser2().getMail());
                            intent.putExtra(appConst.USER_NAME_FOR_INTENT, c.getUser2().getDisplayName());
                            intent.putExtra(appConst.USER_IMAGE_FOR_INTENT, c.getUser2().getImageUrl());
                            intent.putExtra(appConst.C_ID, c.getId());
                            startActivity(intent);
                            break;
                        //delete option
                        case 1:
                            new MaterialDialog.Builder(MainActivity.this)
                                    .title("Delete Conversation?")
                                    .content("All messages from " + c.getUser2().getMail() + " will be deleted")
                                    .negativeText("NO")
                                    .positiveText("YES")
                                    .callback(new MaterialDialog.ButtonCallback() {
                                        @Override
                                        public void onPositive(MaterialDialog dialog) {
                                            super.onPositive(dialog);
                                            application.getController().removeConversation(c);
                                            adapter.remove(adapter.getItem(position));
                                            createAdapter();
                                            Toast.makeText(context, "Conversation Deleted", Toast.LENGTH_SHORT).show();
                                        }
                                    }).show();
                            break;
                    }
                    return false;
                }
            });
            //on click listener open the specific conversation
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Conversation c = adapter.getItem(position);
                    c.setUnreadMessages(0);
                    application.getController().updateUnreadMessages(c.getUser2().getMail(),false);
                    Intent intent = new Intent(getBaseContext(), MessageActivity.class);
                    intent.putExtra(appConst.USER_MAIL_FOR_INTENT, c.getUser2().getMail());
                    intent.putExtra(appConst.USER_NAME_FOR_INTENT, c.getUser2().getDisplayName());
                    intent.putExtra(appConst.USER_IMAGE_FOR_INTENT, c.getUser2().getImageUrl());
                    intent.putExtra(appConst.C_ID, c.getId());
                    startActivity(intent);
                }
            });

        }

    }

    /*
     * create the adapter using costume array adapter
     * and populating it using the controller's get all conversation
     */
    private void createAdapter() {
        List<Conversation> list = application.getController().getAllConversations();
        if(list.size() == 0){
            tvEmpty.setVisibility(View.VISIBLE);

        }else{
            tvEmpty.setVisibility(View.INVISIBLE);
        }
        adapter = new CustomArrayAdapter(this, R.layout.conversation_list_item, list);
        listView.setAdapter(adapter);

    }

    /*
     * populate action bar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /*
     *handle clicking on menu option in the action bar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this,SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_map:
                Intent in = new Intent(this,MapActivity.class);
                in.putExtra(appConst.MY_MAIL,userMail);
                startActivity(in);
                return true;

            case R.id.action_AR:
                Intent intent2 = new Intent(this,ARViewerActivity.class);
                startActivity(intent2);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static Bitmap getBitmapFromURL(final String src) {
        class getImageTask extends AsyncTask<Void, Void, Bitmap> {
            @Override
            protected Bitmap doInBackground(Void... params) {
                try {
                    URL url = new URL(src);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(input);
                    return myBitmap;
                }
                catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                    return null;
                }
            }
        }
        try {
            return new getImageTask().execute().get();
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
            return null;
        } catch (ExecutionException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK && requestCode == USER_MAIL_REQUEST_CODE){
            Bundle extras = data.getExtras();
            if(extras != null){
                userMail = extras.getString(appConst.MY_MAIL);
                Log.i(TAG, "my result is : " + userMail);
            }
        }
    }

    /**
     * registration to backend using the api
     */
    private void sendRegistrationIdToBackend() {
        try {
            Deviceinfoendpoint endpoint = EndpointApiCreator
                    .getApi(Deviceinfoendpoint.class);
            DeviceInfo existingInfo = endpoint.getDeviceInfo(regid).execute();

            boolean alreadyRegisteredWithEndpointServer = false;
            if (existingInfo != null
                    && regid.equals(existingInfo.getDeviceRegistrationID())) {
                alreadyRegisteredWithEndpointServer = true;
            }

            if (!alreadyRegisteredWithEndpointServer) {
				/*
				 * We are not registered as yet. Send an endpoint message
				 * containing the GCM registration id and some of the device's
				 * product information over to the backend. Then, we'll be
				 * registered.
				 */
                DeviceInfo deviceInfo = new DeviceInfo();
                endpoint.insertDeviceInfo(
                        deviceInfo
                                .setDeviceRegistrationID(regid)
                                .setTimestamp(System.currentTimeMillis())
                                .setDeviceInformation(
                                        URLEncoder
                                                .encode(android.os.Build.MANUFACTURER
                                                                + " "
                                                                + android.os.Build.PRODUCT,
                                                        "UTF-8"))).execute();
            }
        }
        catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }

    private void registerInBackground() {
        new RegistrationTask().execute();
    }

    @Override
    public void onStart(){
        super.onStart();
        mGoogleApiClient.connect();
        createAdapter();
    }

    /*
     * on connected send the location of the user to the server and save it
     * in the shared preference for future usage
     */
    @Override
    public void onConnected(Bundle bundle) { //send user location when going to main activity
        mGoogleApiClient.connect();
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            GeoPt geoPt = new GeoPt();
            geoPt.setLatitude((float)mLastLocation.getLatitude());
            geoPt.setLongitude((float)mLastLocation.getLongitude());
            SharedPreferences prefs = context.getSharedPreferences(MapActivity.class.getSimpleName(),
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putFloat(appConst.MY_LAT, (float)mLastLocation.getLatitude());
            editor.putFloat(appConst.MY_LONG, (float)mLastLocation.getLongitude());
            editor.apply();
            if(userMail !=null) {
                application = (AroundMeApplication)getApplication();
                application.getController().sendUserLocation(userMail, geoPt);
            }
            else{
                Log.i(TAG, "userMail = null");
            }
        }

    }


    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed" + connectionResult.getErrorCode());
    }

    @Override
    public void onResult(Status status) {

    }

    @Override
    public void onStop(){
        super.onStop();
        mGoogleApiClient.disconnect();
    }


    class RegistrationTask extends AsyncTask<Void, Integer, String>{

        @Override
        protected String doInBackground(Void... params) {
            String msg;
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }
                regid = gcm.register(SENDER_ID);
                msg = "Device registered, registration ID=" + regid;
                storeRegistrationId(context,regid);

                // You should send the registration ID to your server over HTTP,
                // so it can use GCM/HTTP or CCS to send messages to your app.
                // The request to your server should be authenticated if your app
                // is using accounts.
                sendRegistrationIdToBackend();

                // For this demo: we don't need to send it because the device
                // will send upstream messages to a server that echo back the
                // message using the 'from' address in the message.

                // Persist the registration ID - no need to register again.
                //  storeRegistrationId(context, regid);
            } catch (IOException ex) {
                msg = "Error :" + ex.getMessage();
                Log.e(TAG, ex.getMessage());
                // If there is an error, don't just keep trying to register.
                // Require the user to click a button again, or perform
                // exponential back-off.
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String msg) {
            Log.i(TAG,msg);
        }
    }

    public String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /*
     * stor the registration id in the shared preference
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }


    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            Log.e(TAG, e.getMessage());
            throw new RuntimeException("Could not get package name: " + e);
        }
    }


    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the registration ID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /*
     * when resuming to this screen local broadcast manager handle putting
     * the new conversation added
     */
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
        IntentFilter f = new IntentFilter();
        f.addAction(appConst.NEW_CONVERSATION);
        LocalBroadcastManager.getInstance(this).registerReceiver(newConversationReceiver, f);
        Log.i(TAG, "i resumed: " + userMail);
    }

    /*
     * on pause unregister broadcast manger
     */
    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(newConversationReceiver);
    }

    /*
     * check play services to use gcm services
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /*
     * get the array adapter to show the conversation list view
     */
    public static CustomArrayAdapter getAdapter() {
        return adapter;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


}


