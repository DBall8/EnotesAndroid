package edudcball.wpi.users.enotesandroid.AsyncTasks;

import android.os.AsyncTask;


public abstract class ENotesTask extends AsyncTask<String, Integer, String> {

    protected static final String baseURL = "https://enotes.site";//"http://10.0.2.2:8080";//
    protected static final String apiURL = baseURL + "/api";
    protected static final String COOKIES_HEADER = "Set-Cookie";

}
