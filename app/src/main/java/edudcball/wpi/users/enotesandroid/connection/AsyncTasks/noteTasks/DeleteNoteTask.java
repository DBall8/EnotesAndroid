package edudcball.wpi.users.enotesandroid.connection.AsyncTasks.noteTasks;

import android.util.Log;

import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.Callback;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.HttpConnectionTask;

/**
 * A task for sending a note deleting to the server
 * Override the onPostExecute method to activate after completion
 */

public class DeleteNoteTask extends HttpConnectionTask {

    private String tag; // the tag of the note to be deleted
    private Callback<String> callback;

    public DeleteNoteTask(String tag, Callback<String> callback) {
        this.callback = callback;
        this.tag = tag;
    }

    /**
     * Runs when the task is executed
     * @param vals unused
     * @return the response from the server as a string
     */
    @Override
    protected String doInBackground(String... vals) {
        try{
            // connect to server
            connect(apiURL, true, true, "DELETE");

            // build message
            JSONObject msg = new JSONObject();
            msg.put("tag", tag);

            // write the message
            writeMessage(msg.toString());

            // read response
            String resp = readResponse();

            // disconnect
            connection.disconnect();

            return resp;

        }catch(Exception e){
            Log.d("MYAPP", "ERROR WHEN DELETING NOTE:  " + e.toString());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        callback.run(result);
    }
}
