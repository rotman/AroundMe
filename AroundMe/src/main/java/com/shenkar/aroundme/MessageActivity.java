package com.shenkar.aroundme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import com.afollestad.materialdialogs.MaterialDialog;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.Conversation;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.Message;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.User;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.appConst;
import com.shenkar.aroundme.bl.AroundMeApplication;
import com.google.api.client.util.DateTime;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * message activity shows the messages of a specific conversation
 * in the form of left and right bubbles
 */
public class MessageActivity extends ActionBarActivity {

    @Bind(R.id.sendMessageEditText)
    EditText sendMsgEditText;
    @Bind(R.id.messageRelativeLayout)
    RelativeLayout relativeLayout;
    @Bind(R.id.messagesListView)
    ListView lv;

    private AroundMeApplication application;
    private String friendMail;
    private String myMail;
    private static MessagesArrayAdapter adapter;
    private static final String TAG = "MessageActivity";
    private Conversation myConversation = new Conversation();
    private BroadcastReceiver newMessageReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Long mId = intent.getLongExtra(appConst.M_ID,-1);
            String friend = intent.getStringExtra(appConst.USER_MAIL_FOR_INTENT);
            if(mId!=-1) {
                if (friend.equals(friendMail)) {
                    //get the message from the dao.
                    adapter.add(application.getController().getMessage(mId));
                }
            }
        }
    };

    /**
     * Called when the activity is first created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        ButterKnife.bind(this);
        application = (AroundMeApplication) getApplication();
        //check flag of background
        checkBackground();
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#4daf4e"));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);
        adapter = new MessagesArrayAdapter(getApplicationContext(), R.layout.checkbox_list_item);
        lv.setAdapter(adapter);
        lv.setDivider(null);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras!=null) {
            String userName = extras.getString(appConst.USER_NAME_FOR_INTENT);
            friendMail = extras.getString(appConst.USER_MAIL_FOR_INTENT);
            String userImageUrl = extras.getString(appConst.USER_IMAGE_FOR_INTENT);
            myConversation.setId(extras.getLong(appConst.C_ID));
            myConversation.setDisplayName(userName);
            myConversation.setImageUrl(userImageUrl);
            User myUser = application.getController().getMyUser();
            myMail = myUser.getMail();
            handleActionBar();
        }
        //get all messages tfrom the controller
        List<Message> messages =  application.getController().getMessages(friendMail);
        for (Message m : messages) {
            adapter.add(m);
        }
        adapter.notifyDataSetChanged();

    }
    //on resume update the conversation list view
    @Override
    public void onResume() {
        super.onResume();
        IntentFilter f = new IntentFilter();
        f.addAction(appConst.NEW_MESSAGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(newMessageReciever, f);
    }


    //handle the action bar with the name and pic of the user
    private void handleActionBar() {
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#4daf4e"));
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(colorDrawable);
        actionBar.setTitle(Html.fromHtml("<font color='#ffffff'> &nbsp;" + friendMail + "</font>"));
        actionBar.setDisplayShowHomeEnabled(true);
        application.getController().getImage(friendMail, application.getController().getUserImageUrl(friendMail, 0),
                new ApplicationCallback<Bitmap>() {
                    @Override
                    public void done(Bitmap result, Exception e) {
                        Drawable d = new BitmapDrawable(getResources(), application.getController().createCircleBitmap(result));
                        actionBar.setIcon(d);
                    }
                });
    }

    //on click on send a message
    public void onClick(View view) {
        if (sendMsgEditText!=null) {
            String content = sendMsgEditText.getText().toString();
            Message msg = new Message();
            msg.setContnet(content);
            msg.setTo(friendMail);
            msg.setFrom(myMail);
            msg.setDownloaded(false);
            long timeInMillis = System.currentTimeMillis();
            DateTime dateTime = new DateTime(timeInMillis);
            msg.setTimestamp(dateTime);
            msg.setLocation(null);
            msg.setReadRadius(null);
            application = (AroundMeApplication)getApplication();
            if (application.getController().sendMessage(msg)) {
                application.getController().addMassage(msg);
                adapter.add(msg);
                application.getController().updateLastModified(friendMail);
                sendMsgEditText.setText("");
            }
            else {
                new MaterialDialog.Builder(this)
                        .title("Message Failure")
                        .content("please try again")
                        .positiveText("OK")
                        .show();
            }

        }
    }

    //check witch background is flagged now
    private void checkBackground(){
        final SharedPreferences prefs = MessageActivity.this.getSharedPreferences(BgPickerActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
        int backgroundSelected = prefs.getInt(appConst.BG_NUM, -1);
        switch (backgroundSelected){
            case -1:
                relativeLayout.setBackgroundColor(0xebebeb);
                break;
            case 0:
                relativeLayout.setBackgroundResource(R.drawable.bg0);
                break;
            case 1:
                relativeLayout.setBackgroundResource(R.drawable.bg1);
                break;
            case 2:
                relativeLayout.setBackgroundResource(R.drawable.bg2);
                break;
            case 3:
                relativeLayout.setBackgroundResource(R.drawable.bg3);
                break;
        }

    }

    //on pause check local broadcast manager
    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(newMessageReciever);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
