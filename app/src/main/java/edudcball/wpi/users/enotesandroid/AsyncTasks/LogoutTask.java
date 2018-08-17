package edudcball.wpi.users.enotesandroid.AsyncTasks;

import android.util.Log;

/**
 * Created by Owner on 1/16/2018.
 */

public abstract class LogoutTask extends HttpConnectionTask {

    @Override
    protected String doInBackground(String... vals) {
        try{

            connect("", true, false, "POST");

            String resp = readResponse();

            connection.disconnect();

            return resp;

        }catch(Exception e){
            Log.d("MYAPP", "ERROR WHEN LOGGING OUT: " + e.toString());
            return null;
        }
    }
}
