package edudcball.wpi.users.enotesandroid.AsyncTasks;

import android.text.TextUtils;
import android.util.Log;

import java.io.DataInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import edudcball.wpi.users.enotesandroid.NoteManager;

/**
 * Created by Owner on 1/5/2018.
 */

public abstract class RetrieveNotesTask extends ENotesTask {


    @Override
    protected String doInBackground(String... vals) {
        try{
            connect(apiURL, true, false, "GET");

            String resp = readResponse();

            connection.disconnect();

            return resp;

        }catch(Exception e){
            Log.d("MYAPP", "ERROR WHEN RETRIEVING NOTES: " + e.toString());
            return null;
        }
    }
}
