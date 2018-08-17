package edudcball.wpi.users.enotesandroid.AsyncTasks;

import android.util.Log;

import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.Note;

/**
 * Created by Owner on 1/7/2018.
 */

public abstract class UpdateNoteTask extends HttpConnectionTask {

    private Note n;

    public UpdateNoteTask(Note n) {
        this.n = n;
    }

    @Override
    protected String doInBackground(String... vals) {
        try{
            connect(apiURL, true, true, "PUT");

            JSONObject msg = n.toJSON(false);

            // write the message
            writeMessage(msg.toString());

            String resp = readResponse();

            connection.disconnect();

            return resp;

        }catch(Exception e){
            Log.d("MYAPP", "ERROR WHEN UPDATING NOTE: " + e.toString());
            return null;
        }
    }

}
