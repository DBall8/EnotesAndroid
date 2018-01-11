package AsyncTasks;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import edudcball.wpi.users.enotesandroid.LoginActivity;

/**
 * Created by Owner on 1/4/2018.
 */

public abstract class LoginTask extends AsyncTask<String, Integer, String> {

    private static String urlStr = "http://stickybusiness.herokuapp.com/login";

    private LoginActivity parent; // temporary

    public LoginTask(LoginActivity parent){
        this.parent = parent;
    }

    @Override
    protected String doInBackground(String... vals) {
        try{
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");

            JSONObject msg = new JSONObject();
            msg.put("username", vals[0]);
            msg.put("password", vals[1]);
            msg.put("stayLoggedIn", false);


            // write the message
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.writeBytes(msg.toString());
            out.close();

            DataInputStream in = new DataInputStream(connection.getInputStream());
            String input;
            String response = "";
            while ((input = in.readLine()) != null) {
                //input = input.replace("null", "");
                response += input;
            }
            in.close();
            Log.d("MYAPP", response);

            connection.disconnect();

            return response;

        }catch(Exception e){
            Log.d("MYAPP", "UH OH " + e.toString());
            return null;
        }
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    protected abstract void onPostExecute(String result);
}
