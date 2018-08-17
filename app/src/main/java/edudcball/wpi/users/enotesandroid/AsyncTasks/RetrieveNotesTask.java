package edudcball.wpi.users.enotesandroid.AsyncTasks;

import android.util.Log;

/**
 * Created by Owner on 1/5/2018.
 */

public abstract class RetrieveNotesTask extends HttpConnectionTask {


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
