package edudcball.wpi.users.enotesandroid.data;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.Callback;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.LoadUserDataTask;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.userTasks.LoginTask;
import edudcball.wpi.users.enotesandroid.connection.ConnectionManager;
import edudcball.wpi.users.enotesandroid.data.classes.Note;
import edudcball.wpi.users.enotesandroid.data.classes.Page;
import edudcball.wpi.users.enotesandroid.exceptions.CustomException;

public class UserManager {

    private String username = "";
    private PageManager pageManager;
    private ConnectionManager connectionManager;

    public UserManager(){}

    public boolean loadUser() throws CustomException {
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
            pageManager = new PageManager(pageArr, noteArr);

            connectionManager = new ConnectionManager(username);

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

    private void sendError(CustomException exception)
    {
        // TODO handle errors here
        assert(false);
    }

    // PUBLIC METHODS ------------------------------------------------------------------------------

    public PageManager getPageManager(){
        return pageManager;
    }

    // Singleton

    private static class SingletonHelper{
        private final static UserManager _userManager = new UserManager();
    }

    public static UserManager getInstance(){ return SingletonHelper._userManager; }
}
