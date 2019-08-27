package edudcball.wpi.users.enotesandroid.connection.AsyncTasks.pageTasks;

import android.util.Log;

import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.Old.objects.NotePage;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.HttpConnectionTask;

/**
 * A task for updating the server of the new state of a note
 * Override the onPostExecute method to activate after completion
 */

public abstract class UpdatePageTask extends HttpConnectionTask {

    private NotePage p; // the note to update the server about

    public UpdatePageTask(NotePage p) {
        this.p = p;
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
            connect("/notepage", true, true, "PUT");

            // create the message from the note
            JSONObject msg = p.toJSON();

            // write the message
            writeMessage(msg.toString());

            // read the response
            String resp = readResponse();

            // disconnect
            connection.disconnect();

            return resp;

        }catch(Exception e){
            Log.d("MYAPP", "ERROR WHEN UPDATING PAGE: " + e.toString());
            return null;
        }
    }

}
