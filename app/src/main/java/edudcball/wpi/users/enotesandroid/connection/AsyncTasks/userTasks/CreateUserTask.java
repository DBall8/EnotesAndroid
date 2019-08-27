package edudcball.wpi.users.enotesandroid.connection.AsyncTasks.userTasks;

import android.util.Log;

import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.Callback;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.HttpConnectionTask;

/**
 * A task for creating a new user account
 * Override the onPostExecute method to activate after completion
 */

public class CreateUserTask extends HttpConnectionTask {

    private String username, password;
    private Callback<String> callback;

    public CreateUserTask(String username, String password, Callback<String> callback){
        this.username = username;
        this.password = password;
        this.callback = callback;
    }

    private CreateUserTask(){}

    /**
     * Runs when the the task is executed
     * @param vals unused
     * @return returns the string response from the server
     */
    @Override
    protected String doInBackground(String... vals) {
        try{
            // Connect to the server
            connect("/newuser", true, true, "POST");

            // Build the message
            JSONObject msg = new JSONObject();
            msg.put("username", username);
            msg.put("password", password);
            msg.put("stayLoggedIn", false);


            // write the message
            writeMessage(msg.toString());

            // Save the user session
            saveCookies();

            // read the response
            String resp = readResponse();

            // stop the connection
            connection.disconnect();

            // wipe login data just in case
            username = null;
            password = null;

            return resp;

        }catch(Exception e){
            Log.d("MYAPP", "ERROR WHEN CREATING USER: " + e.toString());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        callback.run(result);
    }
}
