package edudcball.wpi.users.enotesandroid.connection.AsyncTasks.pageTasks;

import android.util.Log;

import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.Old.NoteManager.NoteManager;
import edudcball.wpi.users.enotesandroid.connection.AsyncTasks.HttpConnectionTask;

/**
 * A task for sending a note deleting to the server
 * Override the onPostExecute method to activate after completion
 */

public abstract class DeletePageTask extends HttpConnectionTask {

    private String pageID; // the tag of the note to be deleted

    public DeletePageTask(String pageID) {
        this.pageID = pageID;
    }

    /**
     * Runs when the task is executed
     * @param vals unused
     * @return the response from the server as a string
     */
    @Override
    protected String doInBackground(String... vals) {
        try{
            // connect to server
            connect("/notepage", true, true, "DELETE");

            // build message
            JSONObject msg = new JSONObject();
            msg.put("pageid", pageID);
            msg.put("socketid", NoteManager.getSocketID());

            // write the message
            writeMessage(msg.toString());

            // read response
            String resp = readResponse();

            // disconnect
            connection.disconnect();

            return resp;

        }catch(Exception e){
            Log.d("MYAPP", "ERROR WHEN DELETING PAGE:  " + e.toString());
            return null;
        }
    }
}
