package edudcball.wpi.users.enotesandroid.connection.AsyncTasks.userTasks;

import android.util.Log;

import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.Callback;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.HttpConnectionTask;

/**
 * A task for logging in to a user account
 * Override the onPostExecute method to activate after completion
 */

public class UpdateSettingsTask extends HttpConnectionTask {

    private String dFont, dColor;
    private int dFontSize;
    private Callback<String> callback;

    public UpdateSettingsTask(String dFont, int dFontSize, String dColor, Callback<String> callback){
        this.callback = callback;
        this.dFont = dFont;
        this.dFontSize = dFontSize;
        this.dColor = dColor;
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
            connect("/user", true, true, "POST");

            // build the message
            JSONObject msg = new JSONObject();
            msg.put("dfont", dFont);
            msg.put("dfontsize", dFontSize);
            msg.put("dcolor", dColor);

            // write the message
            writeMessage(msg.toString());

            // read response
            String resp = readResponse();

            // disconnect
            connection.disconnect();

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
