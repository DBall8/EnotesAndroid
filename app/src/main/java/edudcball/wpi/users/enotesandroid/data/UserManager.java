package edudcball.wpi.users.enotesandroid.data;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.Callback;
import edudcball.wpi.users.enotesandroid.activities.LoginActivity;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.LoadUserDataTask;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.userTasks.ChangePasswordTask;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.userTasks.CreateUserTask;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.userTasks.LoginTask;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.userTasks.LogoutTask;
import edudcball.wpi.users.enotesandroid.connection.ConnectionManager;
import edudcball.wpi.users.enotesandroid.exceptions.CustomException;

import static android.content.Context.MODE_PRIVATE;

public class UserManager {

    private final static String STORED_USERNAME = "username";
    private final static String STORED_PASSWORD = "password";

    private String username = "";
    private PageManager pageManager;
    private ConnectionManager connectionManager;
    private boolean isUserSignedIn = false;

    public UserManager(){
        pageManager = new PageManager();
        connectionManager = new ConnectionManager();
    }

    public void logIn(final String username, final String password, final Context context, final Callback<String> callback){

        new LoginTask(username, password, new Callback<String>(){
            @Override
            public void run(String param){

                if (param == null){
                    callback.run(param);
                    return;
                }

                // Quick check to see if login was successful before passing on
                try {
                    // Convert response to a JSON object
                    JSONObject obj = new JSONObject(param);
                    // If successful flag received, save the login info for next time
                    if (obj.getBoolean("successful")) {
                        saveLoginInfo(username, password, context);
                        isUserSignedIn = true;
                    }
                }
                catch (JSONException e)
                {
                    // Do nothing, let login activity handle this
                }
                finally
                {
                    callback.run(param);
                }
            }
        }).execute();
    }

    public void logIn(final String username, final String password, final Callback<String> callback){

        new LoginTask(username, password, new Callback<String>(){
            @Override
            public void run(String param){

                if (param == null){
                    callback.run(param);
                    return;
                }

                // Quick check to see if login was successful before passing on
                try {
                    // Convert response to a JSON object
                    JSONObject obj = new JSONObject(param);
                    // If successful flag received, save the login info for next time
                    if (obj.getBoolean("successful")) {
                        isUserSignedIn = true;
                    }
                }
                catch (JSONException e)
                {
                    // Do nothing, let login activity handle this
                }
                finally
                {
                    callback.run(param);
                }
            }
        }).execute();
    }

    public void newUser(final String username, final String password, final Context context, final Callback<String> callback){
        new CreateUserTask(username, password, new Callback<String>(){
            @Override
            public void run(String param){

                // Quick check to see if login was successful before passing on
                try {
                    // Convert response to a JSON object
                    JSONObject obj = new JSONObject(param);
                    // If successful flag received, save the login info for next time
                    if (!obj.getBoolean("userAlreadyExists")) {
                        saveLoginInfo(username, password, context);
                        isUserSignedIn = true;
                    }
                }
                catch (JSONException e)
                {
                    // Do nothing, let login activity handle this
                }
                finally
                {
                    callback.run(param);
                }
            }
        }).execute();
    }

    public void logOut(Context context, Callback<String> callback){
        clearLoginInfo(context);
        connectionManager.resetCookies();
        isUserSignedIn = false;

        new LogoutTask(callback).execute();
    }

    public void changePassword(String oldPassword, final String newPassword, final Context context, final Callback<String> callback){

        if(!isUserSignedIn) return;

        new ChangePasswordTask(oldPassword, newPassword, new Callback<String>() {
            @Override
            public void run(String param) {
                try {
                    // Quick check if the password update went through
                    JSONObject obj = new JSONObject(param);
                    if (obj.getBoolean("successful")) {
                        SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
                        sp.edit().putString("password", newPassword);
                        sp.edit().commit();
                    }
                }
                catch (JSONException e)
                {
                    // Do nothing, le activity handle error
                }
                finally {
                    callback.run(param);
                }
            }
        });

    }

    public boolean loadUser(){
        isUserSignedIn = true;
        new LoadUserDataTask(new Callback<String>() {
            @Override
            public void run(String param) {
                loadUserData(param);
            }
        }).execute();
        return true;
    }

    public boolean loadUserData(String serverResponse){
        try{
            JSONObject obj = new JSONObject(serverResponse);
            if(obj.getBoolean("sessionExpired")){
                Log.d("MYAPP", "Session expired");
                sendError(new CustomException("Error when contacting server. Please try again later.", "Server session expired"));
                return false;
            }

            // store the user's username
            username = obj.getString("username");

            JSONArray pageArr = obj.getJSONArray("notePages");
            JSONArray noteArr = obj.getJSONArray("notes");
            pageManager.load(pageArr, noteArr);

            connectionManager.connectSocket(username);

            return true;
        }
        catch(CustomException e){
            sendError(e);
        }
        catch(Exception e){
            Log.d("MYAPP", "Unable to form response JSON for get notes");
            Log.d("MYAPP", "LOAD NOTES FAILED: " + e.getMessage());
            sendError(new CustomException("Error when contacting server. Please try again later.", e.getMessage()));
            return false;
        }
        return true;
    }

    private void saveLoginInfo(String username, String password, Context context){
        SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(STORED_USERNAME, username);
        editor.putString(STORED_PASSWORD, password);
        editor.commit();
    }

    private void clearLoginInfo(Context context){
        SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(STORED_USERNAME);
        editor.remove(STORED_PASSWORD);
        editor.commit();
    }

    public void attemptSavedLogin(Context context, Callback<String> callback){
        if (isUserSignedIn) return;

        SharedPreferences sp = context.getSharedPreferences("Login", MODE_PRIVATE);
        String username = sp.getString(STORED_USERNAME, null);
        String password = sp.getString(STORED_PASSWORD, null);

        if (username != null && password != null){
            logIn(username, password, callback);
        }
    }

    private void sendError(CustomException exception)
    {
        // TODO handle errors here
        assert(false);
    }

    // PUBLIC METHODS ------------------------------------------------------------------------------

    public PageManager getPageManager(){
        return pageManager;
    }

    public ConnectionManager getConnectionManager(){ return connectionManager; }

    public boolean isUserSignedIn(){ return isUserSignedIn; }

    // Singleton

    private static class SingletonHelper{
        private final static UserManager _userManager = new UserManager();
    }

    public static UserManager getInstance(){ return SingletonHelper._userManager; }
}
