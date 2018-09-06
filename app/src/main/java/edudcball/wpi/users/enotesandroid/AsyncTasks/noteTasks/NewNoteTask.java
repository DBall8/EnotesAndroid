package edudcball.wpi.users.enotesandroid.AsyncTasks.noteTasks;

import android.util.Log;

import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.AsyncTasks.HttpConnectionTask;
import edudcball.wpi.users.enotesandroid.objects.Note;

/**
 * A task for sending a new note event to the server
 * Override the onPostExecute method to activate after completion
 */

public abstract class NewNoteTask extends HttpConnectionTask {

    private Note n; // the new note being added

    public NewNoteTask(Note n) {
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
            connect(apiURL, true, true, "POST");

            // create the message from the note
            JSONObject msg = n.toJSON();

            // write the message
            writeMessage(msg.toString());

            // get the response
            String resp = readResponse();

            // disconnect
            connection.disconnect();

            return resp;

        }catch(Exception e){
            Log.d("MYAPP", "ERROR WHEN CREATING A NEW NOTE: " + e.toString());
            return null;
        }
    }
}
