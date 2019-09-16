package edudcball.wpi.users.enotesandroid.connection.AsyncTasks.userTasks;

import android.util.Log;

import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.Callback;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.HttpConnectionTask;

/**
 * A task for logging in to a user account
 * Override the onPostExecute method to activate after completion
 */

public class ChangePasswordTask extends HttpConnectionTask {

    private String oldPassword, newPassword;
    private Callback<String> callback;

    public ChangePasswordTask(String oldPassword, String newPassword, Callback<String> callback){
        this.callback = callback;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
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
            connect("/changepassword", true, true, "POST");

            // build the message
            JSONObject msg = new JSONObject();
            msg.put("oldpassword", this.oldPassword);
            msg.put("newpassword", this.newPassword);

            // write the message
            writeMessage(msg.toString());

            // read response
            String resp = readResponse();

            // disconnect
            connection.disconnect();

            this.oldPassword = null;
            this.newPassword = null;

            return resp;

        }catch(Exception e){
            Log.d("MYAPP", "ERROR WHEN UPDATING SETTINGS IN: " + e.toString());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        callback.run(result);
    }
}
