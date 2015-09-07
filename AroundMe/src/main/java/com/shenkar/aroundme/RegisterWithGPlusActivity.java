package com.shenkar.aroundme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.User;
import com.shenkar.aroundme.bl.AroundMeApplication;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * handle the g+ progress bar display and the backend registration with g+
 */
public class RegisterWithGPlusActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    @Bind(R.id.registrationInProgressTextView)
    TextView progressTextView;
    @Bind(R.id.progressBar)
    ProgressBar mProgress;
    private GoogleApiClient mGoogleApiClient;
    private boolean mIntentInProgress;
    private static final int RC_SIGN_IN = 0;
    private static final String TAG = "RegisterWithGPlusActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_with_gplus);
        ButterKnife.bind(this);
        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
            String personName = currentPerson.getDisplayName();
            String personPhoto = currentPerson.getImage().getUrl();
            String personPassword = currentPerson.getId();
            final SharedPreferences prefs = getSharedPreferences(MainActivity.class.getSimpleName(),
                    Context.MODE_PRIVATE);
            String regId = prefs.getString(MainActivity.PROPERTY_REG_ID, "");
            User newUser = new User();
            newUser.setRegistrationId(regId);
            newUser.setImageUrl(personPhoto);
            newUser.setMail(email);
            newUser.setFullName(personName);
            newUser.setLocation(null);
            newUser.setPassword(personPassword);
            AroundMeApplication application = (AroundMeApplication) getApplication();
            User myUser = application.getController().registerUser(newUser);
            if (myUser != null) {
                application.getController().saveMyUser(myUser);
            }
            else {
                application.getController().saveMyUser(newUser);
            }
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (!mIntentInProgress && connectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                startIntentSenderForResult(connectionResult.getResolution().getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            }
            catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            mIntentInProgress = false;
            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

}
