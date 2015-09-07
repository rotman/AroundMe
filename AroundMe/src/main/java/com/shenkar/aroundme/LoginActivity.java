package com.shenkar.aroundme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.User;
import com.shenkar.aroundme.bl.AroundMeApplication;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * login activity handle the view for the login page
 */
public class LoginActivity extends Activity {

    // Find all the views on the page
    @Bind(R.id.email2)
    EditText email;
    @Bind(R.id.pword2)
    EditText pword;
    private static final String TAG = "LoginActivity";

    /**
     * Called when the activity is first created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        ButterKnife.bind(this);
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#4daf4e"));
    }

    /*
     * on click on the login button handle all the checking if empty in field
     * and send to the controller
     */
    public void loginOk(View v){
            String userEmail = email.getText().toString();
            String password = pword.getText().toString();
            if (!userEmail.equals("") && !password.equals("")) {
                AroundMeApplication application = (AroundMeApplication) getApplication();
                final SharedPreferences prefs = getSharedPreferences(MainActivity.class.getSimpleName(),
                        Context.MODE_PRIVATE);
                String regId = prefs.getString(MainActivity.PROPERTY_REG_ID, "");
                //send to the controller data from user
                User myUser = application.getController().loginUser(userEmail, password, regId);
                if (myUser != null) {
                    //saves the user and send to the main activity
                    application.getController().saveMyUser(myUser);
                    Intent mIntent = new Intent(this,MainActivity.class);
                    startActivity(mIntent);
                }
                else {
                    new MaterialDialog.Builder(this)
                            .title("Name or password incorrect")
                            .positiveText("OK")
                            .show();
                }
            }
            else {
                new MaterialDialog.Builder(this)
                        .title("Missing parameters")
                        .positiveText("OK")
                        .show();
            }
    }

    /*
     * send to register activity
     */
    public void goToRegister(View v){
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

    /*
     *on back pressed after logout the user cannot return
     * to his account without login in
     */
    @Override
    public void onBackPressed() {
        Toast.makeText(this,"Please re-login",Toast.LENGTH_SHORT).show();
    }
}
