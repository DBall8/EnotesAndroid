package edudcball.wpi.users.enotesandroid.connection.AsyncTasks;

import android.util.Log;

import edudcball.wpi.users.enotesandroid.Callback;

/**
 * A task for retrieving all notes created by the user
 * Override the onPostExecute method to activate after completion
 */

public class LoadUserDataTask extends HttpConnectionTask{

    private Callback<String> callback;

    public LoadUserDataTask(Callback<String> callback){
        this.callback = callback;
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

    @Override
    protected void onPostExecute(String result) {
        callback.run(result);
    }
}
