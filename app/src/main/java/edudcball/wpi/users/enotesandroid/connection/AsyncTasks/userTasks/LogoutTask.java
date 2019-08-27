package edudcball.wpi.users.enotesandroid.connection.AsyncTasks.userTasks;

import android.util.Log;

import edudcball.wpi.users.enotesandroid.Callback;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.HttpConnectionTask;

/**
 * A task for loggin out of a user account
 * Override the onPostExecute method to activate after completion
 */

public class LogoutTask extends HttpConnectionTask {

    private Callback<String> callback;

    public LogoutTask(Callback<String> callback){
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

            // connect to server
            connect("/logout", true, false, "POST");

            // read the reponse
            String resp = readResponse();

            // disconnect
            connection.disconnect();

            return resp;

        }catch(Exception e){
            Log.d("MYAPP", "ERROR WHEN LOGGING OUT: " + e.toString());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        callback.run(result);
    }
}
