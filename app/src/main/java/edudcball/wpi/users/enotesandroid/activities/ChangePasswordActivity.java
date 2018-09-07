package edudcball.wpi.users.enotesandroid.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import edudcball.wpi.users.enotesandroid.AsyncTasks.userTasks.ChangePasswordTask;
import edudcball.wpi.users.enotesandroid.NoteManager.NoteManager;
import edudcball.wpi.users.enotesandroid.R;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextView messageView;
    private Button applyButton, backButton;

    /**
     * Called when the dialog is opened
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // load visuals
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_change_password);

        applyButton = findViewById(R.id.changePasswordButton);
        backButton = findViewById(R.id.closeButton);
        messageView = findViewById(R.id.messageText);

        final EditText oldPassField = findViewById(R.id.oldPasswordField);
        final EditText newPassField = findViewById(R.id.newPasswordField);
        final EditText confirmPassField = findViewById(R.id.confirmNewPasswordField);

        final Activity activity = this;

        // set apply button action
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                hideMessage();

                String oldPass = oldPassField.getText().toString();
                String newPass = newPassField.getText().toString();
                String confirmPass = confirmPassField.getText().toString();

                if(oldPass == null || oldPass.equals("")){
                    displayMessage("Please enter your old password.");
                }
                else if(newPass == null || newPass.equals("")){
                    displayMessage("Please enter your new password.");
                }
                else if(confirmPass == null || newPass.equals("")){
                    displayMessage("Please confirm your new password.");
                }
                else if(!newPass.equals(confirmPass)){
                    displayMessage("New passwords do not match!");
                }
                else{
                    new ChangePasswordTask(oldPass, newPass){
                        @Override
                        protected void onPostExecute(String result) {
                            if(result == null){
                                NoteManager.sessionExpired(activity, "Problem communicating with server. Please try again later.");
                                return;
                            }

                            try{
                                // Convert response to a JSON object
                                JSONObject obj = new JSONObject(result);
                                // If successful flag received, save the session id on the phone for next time
                                if(obj.getBoolean("successful")){
                                    displayMessage("Password updated!");
                                }
                                else{
                                    displayMessage("Incorrect password.");
                                }
                            }
                            catch(Exception e){
                                Log.d("MYAPP", "Failed to parse JSON");
                                NoteManager.sessionExpired(activity, "Problem communicating with server. Please try again later.");
                            }
                        }
                    }.execute();
                }
            }
        });

        // have the cancel button simply close the dialog
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void displayMessage(String message){
        if(messageView == null) return;
        messageView.setText(message);
        messageView.setVisibility(View.VISIBLE);
    }

    private void hideMessage(){
        if(messageView.getVisibility() == View.GONE) return;
        messageView.setText("");
        messageView.setVisibility(View.GONE);
    }
}
