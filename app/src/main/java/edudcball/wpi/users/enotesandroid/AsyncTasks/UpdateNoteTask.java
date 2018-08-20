package edudcball.wpi.users.enotesandroid.AsyncTasks;

import android.util.Log;

import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.Note;

/**
 * A task for updating the server of the new state of a note
 * Override the onPostExecute method to activate after completion
 */

public abstract class UpdateNoteTask extends HttpConnectionTask {

    private Note n; // the note to update the server about

    public UpdateNoteTask(Note n) {
        this.n = n;
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
            connect(apiURL, true, true, "PUT");

            // create the message from the note
            JSONObject msg = n.toJSON(false);

            // write the message
            writeMessage(msg.toString());

            // read the response
            String resp = readResponse();

            // disconnect
            connection.disconnect();

            return resp;

        }catch(Exception e){
            Log.d("MYAPP", "ERROR WHEN UPDATING NOTE: " + e.toString());
            return null;
        }
    }

}
