package AsyncTasks;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Owner on 1/5/2018.
 */

public abstract class RetrieveNotesTask extends AsyncTask<String, Integer, String> {

    private static String baseUrl = "http://stickybusiness.herokuapp.com/api?sessionID=";

    @Override
    protected String doInBackground(String... sesionIDs) {
        try{
            URL url = new URL(baseUrl + sesionIDs[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(false);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");

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
