package AsyncTasks;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import edudcball.wpi.users.enotesandroid.Note;

/**
 * Created by Owner on 1/7/2018.
 */

public abstract class UpdateNoteTask extends AsyncTask<String, Integer, String> {

    private static String baseUrl = "http://stickybusiness.herokuapp.com/api";

    private Note n;
    private String sessionID;

    public UpdateNoteTask(String sessionID, Note n) {
        this.n = n;
        this.sessionID = sessionID;
    }

    @Override
    protected String doInBackground(String... vals) {
        try{
            URL url = new URL(baseUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("PUT");

            JSONObject msg = new JSONObject();
            msg.put("sessionID", sessionID);
            msg.put("tag", n.getTag());
            msg.put("newcontent", n.getContent());
            msg.put("newx", n.getX());
            msg.put("newy", n.getY());
            msg.put("newW", n.getW());
            msg.put("newH", n.getH());
            msg.put("newZ", n.getZ());
            msg.put("newColors", n.getColors());


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
