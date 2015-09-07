package com.shenkar.aroundme.bl;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;
import com.appspot.enhanced_cable_88320.aroundmeapi.Aroundmeapi;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.Conversation;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.GeoPt;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.Message;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.User;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.UserAroundMe;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.UserAroundMeCollection;
import com.appspot.enhanced_cable_88320.aroundmeapi.model.appConst;
import com.shenkar.aroundme.ApplicationCallback;
import com.shenkar.aroundme.BgPickerActivity;
import com.shenkar.aroundme.MainActivity;
import com.shenkar.aroundme.R;
import com.shenkar.aroundme.data.ConversationsDAO;
import com.shenkar.aroundme.data.ConversationsIDAO;
import com.shenkar.aroundme.data.MessagesDAO;
import com.shenkar.aroundme.data.MessagesIDAO;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * the controller for all the app that contains all the method that calls the api and
 * internal logic
 */
public class MainController {
    private Context context;
    private ConversationsIDAO cDao;
    private MessagesIDAO mDao;
    private static GeoPt mGeoPt;
    private static final String TAG = "MainController";
    private List<OnDataSourceChangeListener> dataSourceChangedListenrs = new ArrayList<>();

    private static Aroundmeapi aroundmeapi = new Aroundmeapi(AndroidHttp.newCompatibleTransport(), new GsonFactory(),
            new HttpRequestInitializer() {
                public void initialize(HttpRequest httpRequest) {
                }
            });

    /*
     * hash map of the conversations
     */
    private HashMap<Integer,Conversation> conversations;
    public MainController(Context context){
        this.context = context;
        cDao = ConversationsDAO.getInstance(context);
        mDao = MessagesDAO.getInstance(context);
    }


    /*
     * the method that calls the api to register the user - done asychronisly
     */
    private class RegisterUserTask extends AsyncTask <User,Void,User>{
        @Override
        protected User doInBackground(User... params) {
            try {
                return aroundmeapi.register(params[0]).execute();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        }
    }

    /*
     * the method that execute the register user task to call the api
     */
    public User registerUser(final User newUser) {
        try {
            return new RegisterUserTask().execute(newUser).get();
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
            return null;
        } catch (ExecutionException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    /*
     * the method that calls the api to login the user - done asychronisly
     */
    private class LoginUserTask extends AsyncTask <String,Void,User>{
        @Override
        protected User doInBackground(String... params) {
            try {
                return aroundmeapi.login(params[0],params[1],params[2]).execute();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        }
    }

    /*
     * the method that execute the login user task to call the api
     */
    public User loginUser(final String mail,final String password,final String regId) {
        try {
            return new LoginUserTask().execute(mail,password,regId).get();
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
            return null;
        } catch (ExecutionException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    /*
     * send the user location using the api by receiving the user mail and the location
     * from the get last known location
     */
    public void sendUserLocation(final String mail, final GeoPt geoPt){
       mGeoPt = geoPt;
       new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    aroundmeapi.reportUserLocation(mail,geoPt).execute();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
                return null;
            }
        }.execute();
    }

    public void sendGcmMessage(final String mail, final String msg) {
        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    aroundmeapi.sendGcmMessage(mail,msg).execute();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
                return null;
            }
        }.execute();
    }


    /*
     * send the message through the api by wrapping the message in a Message object
     * and sending it to the api
     */
    private class sendMessageTask extends AsyncTask<Message,Void,Boolean> {
        @Override
        protected Boolean doInBackground(Message... params) {
            try {
                aroundmeapi.sendMessage(params[0]).execute();
                return true;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            return false;
        }
    }

    /*
     * calls the send message task and sends the wrapped message
     */
    public boolean sendMessage(Message m) {
        try {
            return new sendMessageTask().execute(m).get();
        }
        catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
        }
        catch (ExecutionException e) {
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    /*
     * send my current location to everyone using a for each loop
     */
    public void sendLocationMessageToEveryone(Message message, List<UserAroundMe> users){
        String myMail = getMyUser().getMail();
        for(UserAroundMe userAroundMe : users){
            if(!userAroundMe.getMail().equals(myMail)) {
                message.setTo(userAroundMe.getMail());
                sendMessage(message);
            }
        }
    }

    /*
     * get users is a method to get all user around me with the parameters being
     * my current location and the radius
     */
    private class GetUsersTask extends AsyncTask<String,Void,UserAroundMeCollection> {
        @Override
        protected UserAroundMeCollection doInBackground(String... params) {
            try {
                float lat = mGeoPt.getLatitude();
                float lng = mGeoPt.getLongitude();
                int radius = Integer.parseInt(params[0]);
                return aroundmeapi.getUsersAroundMe(lat,lng, radius,params[1]).execute();
            }
            catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        }
    }

    /*
     * check if the conversation exist to not add a query to the database
     * using for each loop
     */
    public long checkIfConversationExists(String friendMail) {
        if (conversations != null && conversations.size() > 0) {
            for (Conversation c : conversations.values()) {
                if (c.getUser2().getMail().equals(friendMail)) {
                    return c.getId();
                }
            }
        }
        return  -1;
    }

    /*
     * send to get users task to get all user around me
     */
    public UserAroundMeCollection getUsersAroundMe(String mail,int radius) {
        try {
            String strRadius = String.valueOf(radius);
            return new GetUsersTask().execute(strRadius,mail).get();
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
            return null;
        } catch (ExecutionException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    /*
     * methos to get a list of all the user from the server
     */
    private class GetAllUsersTask extends AsyncTask<String,Void,UserAroundMeCollection> {
        @Override
        protected UserAroundMeCollection doInBackground(String... params) {
            try {
                return aroundmeapi.getAllUsers(params[0]).execute();
            }
            catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        }
    }

    /*
     * execute the get all users task
     */
    public UserAroundMeCollection getAllUsers(final String mail) {
        try {
            return new GetAllUsersTask().execute(mail).get();
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage());
            return null;
        } catch (ExecutionException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    /*
     * returns a list of conversation and get from the database all of the conversation
     * and sort them
     */
    public List<Conversation> getAllConversations() {
        try {
            if (conversations != null) {
                ArrayList<Conversation> arrayList = new ArrayList<>(conversations.values());
                return sortConversations(arrayList);
            }
            cDao.open();
            List<Conversation> cl = cDao.getConversations();
            cDao.close();
            populateConversationsCache(cl);
            return cl;
        }
        catch (Exception e) {
            // in case of error, return empty list.
            Log.e(TAG, e.getMessage());
            return new ArrayList<>();
        }
    }

    /*
     * sort the conversation
     */
    private ArrayList<Conversation> sortConversations(ArrayList<Conversation> arrayList) {
        Collections.sort(arrayList, new Comparator<Conversation>() {
            @Override
            public int compare(Conversation lhs, Conversation rhs) {
                return (int)(rhs.getLastModified() - lhs.getLastModified());
            }
        });
        return arrayList;
    }

    /*
     * hold a cache with all the conversation to minimize loading time
     */
    private void populateConversationsCache(List<Conversation> cList) {
        conversations = new HashMap<Integer, Conversation>();
        for (Conversation conversation : cList) {
            conversations.put((int) conversation.getId(), conversation);
        }
    }


    /*
     * calls the database to add a new conversation to it
     */
    public long addConversation(Conversation conversation) {
        try {
            cDao.open();
            conversation.setLastModified(System.currentTimeMillis());
            Conversation retCon = cDao.addConversation(conversation);
            cDao.close();
            if (retCon == null) return -1;
            if (conversations.containsKey(retCon.getId())) {
                return -1;
            }
            conversations.put((int)retCon.getId(),retCon);
            invokeDataSourceChanged();
            return retCon.getId();

        }
        catch (Exception e) {
            Log.e("MainController", e.getMessage());
            return -1;
        }
    }

    /*
     * after a message is received this method is called to update the database
     */
    public void updateUnreadMessages(String friendMail,boolean isNew) {
        try {
            cDao.open();
            int counter = cDao.updateUnreadMessages(friendMail,isNew);
            cDao.close();
            for (Conversation c : conversations.values()) { //update the cache
                if (c.getUser2().getMail().equals(friendMail)) {
                    c.setUnreadMessages(counter);
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void updateLastModified(String friendMail) {
        try {
            cDao.open();
            cDao.updateLastModified(friendMail);
            cDao.close();
            for (Conversation c : conversations.values()) {
                if (c.getUser2().getMail().equals(friendMail)) {
                    c.setLastModified(System.currentTimeMillis());
                }
            }
        }
        catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /*
     * when deleting a convesation this method calls the database method
     */
    public void removeConversation(Conversation c) {
        try {
            //open the database connection
            cDao.open();
            cDao.removeConversation(c);
            //remove from the local cache.
            removeFromCache(c);
            //close the connection.
            cDao.close();
            //remove all the conversation's messages
            mDao.open();
            mDao.removeConversationMessages(c.getUser2().getMail());
            mDao.close();
            invokeDataSourceChanged();
        }
        catch (Exception e) {
            Log.e(TAG,e.getMessage());
        }
    }

    /*
     * when deleting a conversation this method removes it from the cache
     */
    private void removeFromCache(Conversation c){
        if(conversations.containsKey((int) c.getId()))
            conversations.remove((int)c.getId());
    }

    /*
     * this updates the changes in lists across the app
     */
    public void invokeDataSourceChanged() {
        for (OnDataSourceChangeListener listener : dataSourceChangedListenrs) {
            listener.DataSourceChanged();
        }
    }

    /*
     * this method save all of my data to the shared preference
     */
    public void saveMyUser(User myUser) {
        SharedPreferences prefs = context.getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(appConst.REGISTRATION_ID,myUser.getRegistrationId());
        editor.putString(appConst.MY_MAIL, myUser.getMail());
        editor.putString(appConst.MY_NAME, myUser.getFullName());
        editor.putString(appConst.MY_IMAGE, myUser.getImageUrl());
        editor.apply();
    }

    /*
     * get the user from the shared preference
     */
    public User getMyUser() {
        final SharedPreferences prefs = context.getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
        String regId = prefs.getString(appConst.REGISTRATION_ID,"");
        String mail = prefs.getString(appConst.MY_MAIL,"");
        String name = prefs.getString(appConst.MY_NAME,"");
        String imgUrl = prefs.getString(appConst.MY_IMAGE, "");
        User myUser = new User();
        myUser.setMail(mail);
        myUser.setRegistrationId(regId);
        myUser.setFullName(name);
        myUser.setImageUrl(imgUrl);
        return myUser;
    }

    /*
     * this method is being used to get all the message in a specific conversation
     * by using the mail of my friend
     */
    public List<Message> getMessages(String friendMail){
        try {
            mDao.open();
            List<Message> ml = mDao.getMessage(friendMail);
            cDao.close();
            return ml;
        }
        catch (Exception e) {
            // in case of error, return empty list.
            Log.e(TAG, e.getMessage());
            return new ArrayList<>();
        }
    }

    /*
     * to get a single message from the database
     * in case for future update
     */
    public Message getMessage(long mId) {
        try {
            mDao.open();
            Message m = mDao.getOneMessage(mId);
            mDao.close();
            return m;
        }
        catch (SQLException e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    /*
     * method that handle the log out by deleting all the tables
     * and deleting all the data of the user from the shared preference
     */
    public void logoutUser() {
        try {
            cDao.open();
            cDao.clearTable();
            cDao.close();
            mDao.open();
            mDao.clearAllMessages();
            mDao.close();
        }
        catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
        conversations.clear();
        SharedPreferences prefs = context.getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        prefs = context.getSharedPreferences(BgPickerActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
        editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    /*
     * receives a square bitmap and returns a circle bitmap
     */
    public Bitmap createCircleBitmap(Bitmap bitmapimg) {
        Bitmap output = Bitmap.createBitmap(bitmapimg.getWidth(),
                bitmapimg.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmapimg.getWidth(),
                bitmapimg.getHeight());
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmapimg.getWidth() / 2,
                bitmapimg.getHeight() / 2, bitmapimg.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmapimg, rect, rect, paint);
        return output;
    }

    /*
     * add a new message by calling the database handler to add it to the database
     */
    public long addMassage(Message msg){
        try {
            mDao.open();
            long mid = mDao.addMessage(msg);
            mDao.close();
            invokeDataSourceChanged();
            return mid;
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
            return -1;
        }
    }

    /*
     * getting the user image from the cache
     * or from the url given by the server
     */
    public Bitmap getImage(final String userMail, final String imageUrl, final ApplicationCallback<Bitmap> callback) {
        final AroundMeApplication application = (AroundMeApplication)context.getApplicationContext();
        if (application.getImagesCache().get(userMail) != null) {
            if (callback != null) {
                callback.done(application.getImagesCache().get(userMail), null);
            }
            return null;
        }
        if (imageUrl == null || imageUrl.isEmpty()) {
            if (application.getImagesCache().get(appConst.KEY_DEFAULT_USER_IMAGE) == null) {
                application.getImagesCache().put(appConst.KEY_DEFAULT_USER_IMAGE, BitmapFactory
                        .decodeResource(context.getResources(),
                                R.drawable.default_user));
            }

            if (callback != null) {
                callback.done(application.getImagesCache().get(appConst.KEY_DEFAULT_USER_IMAGE),
                        null);
            }
            return null;
        }

        new AsyncTask<String, Void, Bitmap>() {
            protected Bitmap doInBackground(String... urls) {
                String urldisplay = urls[0];
                // I want larger image.
                String largeImageUrl = urldisplay.replace("sz=50", "sz=150");
                Bitmap mIcon11 = null;
                try {
                    InputStream in = new java.net.URL(largeImageUrl)
                            .openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                }
                return mIcon11;
            }

            protected void onPostExecute(Bitmap result) {
                application.getImagesCache().put(userMail, result);
                if (callback != null) {
                    callback.done(application.getImagesCache().get(userMail), null);
                }

            }
        }.execute(imageUrl);
        return null;
    }

    /*
     * get the url from the user to get his image
     */
    public String getUserImageUrl(String mail,int whatToGet) {
        UserAroundMeCollection aroundMeCollection = getAllUsers(getMyUser().getMail());
        for (UserAroundMe user : aroundMeCollection.getItems()) {
            if (user.getMail().equals(mail)) {
                if (whatToGet == 0) {
                    return user.getImageUrl();
                }
                else if (whatToGet == 1) {
                    return user.getDisplayName();
                }
            }
        }
        return null;
    }

}
