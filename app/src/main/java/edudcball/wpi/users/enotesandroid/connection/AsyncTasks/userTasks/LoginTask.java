package edudcball.wpi.users.enotesandroid.connection.AsyncTasks.userTasks;

import android.util.Log;

import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.Callback;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.HttpConnectionTask;

/**
 * A task for logging in to a user account
 * Override the onPostExecute method to activate after completion
 */

public class LoginTask extends HttpConnectionTask {

    private String username, password;
    private Callback<String> callback;

    public LoginTask(String username, String password, Callback<String> callback){
        this.username = username;
        this.password = password;
        this.callback = callback;
    }

    /**
     * Runs on the task's execution
     * @param vals unused
     * @return
     */
    @Override
    protected String doInBackground(String... vals) {
        try{
            // connect to the server
            connect("/login", true, true, "POST");

            // build the message
            JSONObject msg = new JSONObject();
            msg.put("username", username);
            msg.put("password", password);
            msg.put("stayLoggedIn", false);

            // write the message
            writeMessage(msg.toString());

            // save user session cookie
            saveCookies();

            // read response
            String resp = readResponse();

            // disconnect
            connection.disconnect();

            // wipe login data just in case
            username = null;
            password = null;

            return resp;

        }catch(Exception e){
            Log.d("MYAPP", "ERROR WHEN LOGGIN IN: " + e.toString());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        callback.run(result);
    }
}
