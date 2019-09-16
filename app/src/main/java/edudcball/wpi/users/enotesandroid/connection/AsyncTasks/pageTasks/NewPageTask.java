package edudcball.wpi.users.enotesandroid.connection.AsyncTasks.pageTasks;

import android.util.Log;

import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.Callback;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.HttpConnectionTask;
import edudcball.wpi.users.enotesandroid.data.classes.Page;

/**
 * A task for sending a new note event to the server
 * Override the onPostExecute method to activate after completion
 */

public class NewPageTask extends HttpConnectionTask {

    private Page p; // the new note being added
    private Callback<String> callback;

    public NewPageTask(Page p, Callback<String> callback) {
        this.callback = callback;
        this.p = p;
    }

    /**
     * Runs on task execution
     * @param vals unused
     * @return
     */
    @Override
    protected String doInBackground(String... vals) {
        try{

            // connect to the server
            connect("/notepage", true, true, "POST");

            // create the message from the note
            JSONObject msg = p.toJSON();

            // write the message
            writeMessage(msg.toString());

            // get the response
            String resp = readResponse();

            // disconnect
            connection.disconnect();

            return resp;

        }catch(Exception e){
            Log.d("MYAPP", "ERROR WHEN CREATING A NEW PAGE: " + e.toString());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        callback.run(result);
    }
}
