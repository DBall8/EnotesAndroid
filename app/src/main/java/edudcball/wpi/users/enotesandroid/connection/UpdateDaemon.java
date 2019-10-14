package edudcball.wpi.users.enotesandroid.connection;

import android.util.Log;

import java.util.TimerTask;

import edudcball.wpi.users.enotesandroid.Callback;
import edudcball.wpi.users.enotesandroid.activities.EnotesActivity;
import edudcball.wpi.users.enotesandroid.data.UserManager;

public class UpdateDaemon extends TimerTask {

    private UserManager userManager = UserManager.getInstance();
    private EnotesActivity callingActivity;

    public UpdateDaemon(EnotesActivity callingActivity){
        this.callingActivity = callingActivity;
    }

    @Override
    public void run() {
        userManager.getPageManager().update(new Callback<Boolean>() {
            @Override
            public void run(Boolean successful) {
            if(!successful){
                callingActivity.showErrorAndLogout("Error communicating with server.");
            }
            }
        });
    }
}
