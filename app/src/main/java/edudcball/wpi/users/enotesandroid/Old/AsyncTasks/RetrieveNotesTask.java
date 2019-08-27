package edudcball.wpi.users.enotesandroid.Old.AsyncTasks;

import android.util.Log;

/**
 * A task for retrieving all notes created by the user
 * Override the onPostExecute method to activate after completion
 */

public abstract class RetrieveNotesTask extends HttpConnectionTask {


    /**
     * Runs on task execution
     * @param vals unused
     * @return
     */
    @Override
    protected String doInBackground(String... vals) {
        try{
            // connect to the server
            connect(apiURL, true, false, "GET");

            // read the response
            String resp = readResponse();

            // disconnect from the server
            connection.disconnect();

            return resp;

        }catch(Exception e){
            Log.d("MYAPP", "ERROR WHEN RETRIEVING NOTES: " + e.toString());
            return null;
        }
    }
}
