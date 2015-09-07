package com.shenkar.aroundme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.Conversation;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.UserAroundMe;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.appConst;
import com.shenkar.aroundme.bl.AroundMeApplication;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * this activity gives you the list of all user
 * on the server to add to my conversation screen
 */
public class AllUsersActivity extends Activity {
    private UsersListBaseAdapter adapter;
    private static AroundMeApplication application;
    private static final String TAG = "AllUsersActivity";

    // Find all the views on the page
    @Bind(R.id.allUsersListView)
    ListView listView;

    /**
     * Called when the activity is first created
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);
        ButterKnife.bind(this);
        //get application to get controller
        application = (AroundMeApplication)getApplication();
        if (listView != null) {
            final SharedPreferences prefs = getSharedPreferences(MainActivity.class.getSimpleName(),
                    Context.MODE_PRIVATE);
            String userMail = prefs.getString(appConst.MY_MAIL,"");
            //get all user from the controller and set adapter list view
            adapter = new UsersListBaseAdapter(this,application.getController().getAllUsers(userMail).getItems());
            listView.setAdapter(adapter);
            //listener for clicking a name and adding to conversation list
            //and added functionality of setting last modified to zero when entering a conversation
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    UserAroundMe newUser = (UserAroundMe)adapter.getItem(position);
                    Conversation conversation = new Conversation();
                    conversation.setDisplayName(newUser.getDisplayName());
                    conversation.setUser1(application.getController().getMyUser());
                    conversation.setUser2(newUser);
                    conversation.setUnreadMessages(0);
                    conversation.setLastModified(0);
                    long cid = -1;
                    cid = application.getController().checkIfConversationExists(newUser.getMail());
                    Log.i(TAG, "cid is:"+cid);
                    if (cid == -1) {
                        cid = application.getController().addConversation(conversation);
                        Log.i(TAG, "cid is:"+cid);
                    }
                    Intent intent = new Intent(getApplicationContext(),MessageActivity.class);
                    intent.putExtra(appConst.USER_NAME_FOR_INTENT,newUser.getDisplayName());
                    intent.putExtra(appConst.USER_MAIL_FOR_INTENT,newUser.getMail());
                    intent.putExtra(appConst.USER_IMAGE_FOR_INTENT,newUser.getImageUrl());
                    startActivity(intent);
                }
            });
        }
    }

    /**
     * on back pressed update the user that were added to the conversation list
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        MainActivity.getAdapter().notifyDataSetChanged();
    }
}
