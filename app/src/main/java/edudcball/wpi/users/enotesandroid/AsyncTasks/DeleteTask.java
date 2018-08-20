package edudcball.wpi.users.enotesandroid.AsyncTasks;

import android.util.Log;

import org.json.JSONObject;

/**
 * A task for sending a note deleting to the server
 * Override the onPostExecute method to activate after completion
 */

public abstract class DeleteTask extends HttpConnectionTask {

    private String tag; // the tag of the note to be deleted

    public DeleteTask(String tag) {
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
}
