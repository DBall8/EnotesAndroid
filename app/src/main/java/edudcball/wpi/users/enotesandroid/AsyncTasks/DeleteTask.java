package edudcball.wpi.users.enotesandroid.AsyncTasks;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import edudcball.wpi.users.enotesandroid.NoteManager;

/**
 * Created by Owner on 1/11/2018.
 */

public abstract class DeleteTask extends ENotesTask {

    private String tag;

    public DeleteTask(String tag) {
        this.tag = tag;
    }

    @Override
    protected String doInBackground(String... vals) {
        try{

            connect(apiURL, true, true, "DELETE");

            JSONObject msg = new JSONObject();
            msg.put("tag", tag);

            // write the message
            writeMessage(msg.toString());

            String resp = readResponse();

            connection.disconnect();

            return resp;

        }catch(Exception e){
            Log.d("MYAPP", "ERROR WHEN DELETING NOTE:  " + e.toString());
            return null;
        }
    }
}
