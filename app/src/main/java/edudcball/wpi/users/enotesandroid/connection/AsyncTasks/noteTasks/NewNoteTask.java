package edudcball.wpi.users.enotesandroid.connection.AsyncTasks.noteTasks;

import android.util.Log;

import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.Callback;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.HttpConnectionTask;
import edudcball.wpi.users.enotesandroid.data.classes.Note;

/**
 * A task for sending a new note event to the server
 * Override the onPostExecute method to activate after completion
 */

public class NewNoteTask extends HttpConnectionTask {

    private Note n; // the new note being added
    private Callback<String> callback;

    public NewNoteTask(Note n, Callback<String> callback) {
        this.callback = callback;
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
             connect("/api", true, true, "POST");

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

    @Override
    protected void onPostExecute(String result) {
        callback.run(result);
    }
}
