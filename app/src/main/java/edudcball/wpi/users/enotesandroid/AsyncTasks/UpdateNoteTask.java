package edudcball.wpi.users.enotesandroid.AsyncTasks;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import edudcball.wpi.users.enotesandroid.NetInfo;
import edudcball.wpi.users.enotesandroid.Note;
import edudcball.wpi.users.enotesandroid.NoteManager;

/**
 * Created by Owner on 1/7/2018.
 */

public abstract class UpdateNoteTask extends AsyncTask<String, Integer, String> {

    private Note n;

    public UpdateNoteTask(Note n) {
        this.n = n;
    }

    @Override
    protected String doInBackground(String... vals) {
        try{
            URL url = new URL(NetInfo.apiURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            if(NoteManager.cookies.getCookieStore().getCookies().size() > 0){
                connection.setRequestProperty("Cookie", TextUtils.join(";", NoteManager.cookies.getCookieStore().getCookies()));
            }
            connection.setRequestMethod("PUT");

            JSONObject msg = new JSONObject();
            msg.put("tag", n.getTag());
            msg.put("newtitle", n.getTitle());
            msg.put("newcontent", n.getContent());
            msg.put("newx", n.getX());
            msg.put("newy", n.getY());
            msg.put("newW", n.getWidth());
            msg.put("newH", n.getHeight());
            msg.put("newFont", n.getFont());
            msg.put("newFontSize", n.getFontSize());
            msg.put("newZ", n.getZ());
            msg.put("newColors", n.getColors().toString());
            
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
