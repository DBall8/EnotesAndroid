package edudcball.wpi.users.enotesandroid.AsyncTasks;

import android.util.Log;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import edudcball.wpi.users.enotesandroid.NoteManager;

/**
 * Created by Owner on 1/4/2018.
 */

public abstract class LoginTask extends ENotesTask {

    @Override
    protected String doInBackground(String... vals) {
        try{
            connect("/login", true, true, "POST");

            JSONObject msg = new JSONObject();
            msg.put("username", vals[0]);
            msg.put("password", vals[1]);
            msg.put("stayLoggedIn", false);


            // write the message
            writeMessage(msg.toString());

            saveCookies();

            String resp = readResponse();

            connection.disconnect();

            return resp;

        }catch(Exception e){
            Log.d("MYAPP", "ERROR WHEN LOGGIN IN: " + e.toString());
            return null;
        }
    }
}
