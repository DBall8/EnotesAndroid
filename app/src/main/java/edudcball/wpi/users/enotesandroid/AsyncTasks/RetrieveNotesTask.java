package edudcball.wpi.users.enotesandroid.AsyncTasks;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.DataInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import edudcball.wpi.users.enotesandroid.NetInfo;
import edudcball.wpi.users.enotesandroid.NoteManager;

/**
 * Created by Owner on 1/5/2018.
 */

public abstract class RetrieveNotesTask extends AsyncTask<String, Integer, String> {


    @Override
    protected String doInBackground(String... vals) {
        try{
            URL url = new URL(NetInfo.apiURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(false);
            connection.setDoInput(true);
            if(NoteManager.cookies.getCookieStore().getCookies().size() > 0){
                connection.setRequestProperty("Cookie", TextUtils.join(";", NoteManager.cookies.getCookieStore().getCookies()));
            }
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
