package edudcball.wpi.users.enotesandroid;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Owner on 1/6/2018.
 */

public abstract class HttpRequest extends AsyncTask<String, Integer, String>{

    private static String baseUrl = "http://stickybusiness.herokuapp.com/";

    @Override
    protected String doInBackground(String... paths) {
        try{
            URL url = new URL(baseUrl + paths[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");

            JSONObject msg = new JSONObject();
            msg.put("username", "Damon");
            msg.put("password", "shush");
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

    protected abstract void onPostExecute(String result);
}
