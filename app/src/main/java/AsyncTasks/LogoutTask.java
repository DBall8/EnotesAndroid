package AsyncTasks;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import edudcball.wpi.users.enotesandroid.LoginActivity;
import edudcball.wpi.users.enotesandroid.NoteManager;

/**
 * Created by Owner on 1/16/2018.
 */

public abstract class LogoutTask extends AsyncTask<String, Integer, String> {

    static final String COOKIES_HEADER = "Set-Cookie";

    private static String urlStr = "http://stickybusiness.herokuapp.com/";


    @Override
    protected String doInBackground(String... vals) {
        try{
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(false);
            connection.setDoInput(true);
            if(NoteManager.cookies.getCookieStore().getCookies().size() > 0){
                connection.setRequestProperty("Cookie", TextUtils.join(";", NoteManager.cookies.getCookieStore().getCookies()));
            }
            connection.setRequestMethod("POST");

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
