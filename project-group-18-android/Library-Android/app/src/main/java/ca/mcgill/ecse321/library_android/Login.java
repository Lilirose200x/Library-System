package ca.mcgill.ecse321.library_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * manage the user login page
 */
public class Login extends AppCompatActivity {
    private String error = null;

    /**
     * bind default functions to UI components
     * @param savedInstanceState state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        refreshErrorMessage();

        findViewById(R.id.goToSignUp).setOnClickListener(view -> startActivity(new Intent(Login.this,SignUp.class)));
    }

    /**
     * refresh the error message
     */
    private void refreshErrorMessage() {
        // set the error message
        TextView tvError = findViewById(R.id.error);
        tvError.setText(error);

        if (error == null || error.length() == 0) {
            tvError.setVisibility(View.GONE);
        } else {
            tvError.setVisibility(View.VISIBLE);
        }
    }

    /**
     * this method is called when login button is clicked
     * @param view view
     */
    public void login(View view){
        String username = String.valueOf(((TextView)findViewById(R.id.editTextTextPersonName)).getText());
        String password = String.valueOf(((TextView)findViewById(R.id.editTextTextPassword)).getText());
        if (checkInput(username)){
            sendLoginRequest(username,password);
        }
    }

    /**
     * check whether the input username is empty or hasn't been registered
     * @param username input username
     * @return if the username does meet the requirement, return false, otherwise true
     */
    private boolean checkInput(String username){
        //user inputs an empty username
        if(username.equals("")){
            new AlertDialog.Builder(this)
                    .setMessage("Please input an non-empty username")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Alright", (dialog, id) -> dialog.cancel())
                    .show();
            return false;
        }
        return true;
    }

    /**
     * check whether the user can login
     * @param username username
     * @param password password
     */
    private void sendLoginRequest(String username, String password){
        error = "";
        RequestParams requestParams = new RequestParams();
        requestParams.add("username", username);
        requestParams.add("password",password);

        HttpUtils.post("/users/login", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Intent intent = new Intent(Login.this, User_Homepage.class);
                    //pass the uid parameter
                    intent.putExtra("uid", response.getInt("id"));
                    startActivity(intent);
                }catch (JSONException e){
                    error = "Login failed. Please check the input!";
                    refreshErrorMessage();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                error = "Login failed. Username or password is invalid!";
                refreshErrorMessage();
            }
        });
    }

}
