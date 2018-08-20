package edudcball.wpi.users.enotesandroid.AsyncTasks;

import android.util.Log;

/**
 * A task for loggin out of a user account
 * Override the onPostExecute method to activate after completion
 */

public abstract class LogoutTask extends HttpConnectionTask {

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
}
