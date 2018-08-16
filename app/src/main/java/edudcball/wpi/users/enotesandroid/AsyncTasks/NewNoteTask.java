package edudcball.wpi.users.enotesandroid.AsyncTasks;

import android.util.Log;

import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.Note;

/**
 * Created by Owner on 1/10/2018.
 */

public abstract class NewNoteTask extends ENotesTask {

    private Note n;

    public NewNoteTask(Note n) {
        this.n = n;
    }

    @Override
    protected String doInBackground(String... vals) {
        try{

            connect(apiURL, true, true, "POST");

            JSONObject msg = n.toJSON(true);

            // write the message
            writeMessage(msg.toString());

            String resp = readResponse();

            connection.disconnect();

            return resp;

        }catch(Exception e){
            Log.d("MYAPP", "ERROR WHEN CREATING A NEW NOTE: " + e.toString());
            return null;
        }
    }
}
