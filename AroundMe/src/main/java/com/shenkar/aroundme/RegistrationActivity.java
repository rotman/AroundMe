package com.shenkar.aroundme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.afollestad.materialdialogs.MaterialDialog;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.User;
import com.shenkar.aroundme.bl.AroundMeApplication;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * registration activity handle user input of the registration
 * send to the controller that sends to the api to register the user to the server
 */
public class RegistrationActivity extends Activity implements View.OnClickListener{

    @Bind(R.id.fname)
    EditText fname;
    @Bind(R.id.email)
    EditText email;
    @Bind(R.id.pword)
    EditText pword;

    private static AroundMeApplication application;
    private static final String TAG = "RegistrationActivity";

    /**
     * Called when the activity is first created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration2);
        ButterKnife.bind(this);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
    }


    /*
     * back to login button
     */
    public void backToLogin(View v){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    /*
     * on click register takes all the params from the edit text box and sent
     * to the controller that sends to the api
     */
    public void registerOk(View v){
            String fullname = fname.getText().toString();
            String userEmail = email.getText().toString();
            String password = pword.getText().toString();
            if (!fullname.equals("") && !userEmail.equals("") && !password.equals("")) {
                final SharedPreferences prefs = getSharedPreferences(MainActivity.class.getSimpleName(),
                        Context.MODE_PRIVATE);
                String regId = prefs.getString(MainActivity.PROPERTY_REG_ID, "");
                User newUser = new User();
                newUser.setFullName(fullname);
                newUser.setMail(userEmail);
                newUser.setPassword(password);
                newUser.setImageUrl("");
                newUser.setLocation(null);
                if (regId !=null && !regId.equals("")) {
                    newUser.setRegistrationId(regId);
                }
                application = (AroundMeApplication)getApplication();
                User myUser = application.getController().registerUser(newUser);
                application.getController().saveMyUser(myUser);
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
            else {
                new MaterialDialog.Builder(this)
                        .title("Missing parameters")
                        .positiveText("OK")
                        .show();
            }
    }


    /*
     * send to the register with g plus activity
     */
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this,RegisterWithGPlusActivity.class);
        startActivity(intent);
    }
}
