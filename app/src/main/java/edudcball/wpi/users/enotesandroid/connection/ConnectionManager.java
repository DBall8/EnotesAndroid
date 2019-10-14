package edudcball.wpi.users.enotesandroid.connection;

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.Timer;

import edudcball.wpi.users.enotesandroid.activities.MainActivity;

public class ConnectionManager {

    private final static int UPDATE_PERIOD_MS = 5000;

    private SocketConnection socketConnection = null;
    private CookieManager cookies ; // manages cookies
    private Timer updateDaemonTimer = null;

    public ConnectionManager(){
        cookies = new CookieManager();
    }

    public void connectSocket(String username){
        socketConnection = new SocketConnection(username);
    }

    public void addCookie(String cookie){
        cookies.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
    }

    public CookieStore getCookies(){ return cookies.getCookieStore(); }

    public void resetCookies(){
        cookies.getCookieStore().removeAll();
    }

    public void startUpdateDaemon(MainActivity mainActivity){
//        updateDaemonTimer = new Timer();
//        updateDaemonTimer.schedule(new UpdateDaemon(mainActivity), 0, UPDATE_PERIOD_MS);
    }

    public void stopUpdateDaemon(){
        updateDaemonTimer.cancel();
    }

    public void startSocket(String username){
        if (socketConnection == null){
            socketConnection = new SocketConnection(username);
        }
    }

    public void stopSocket(){
        socketConnection.disconnect();
        socketConnection = null;
    }
}
