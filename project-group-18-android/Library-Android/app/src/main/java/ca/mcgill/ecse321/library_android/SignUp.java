package ca.mcgill.ecse321.library_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.*;

import cz.msebera.android.httpclient.Header;

/**
 * the user sign up page control
 */
public class SignUp extends AppCompatActivity {
    private String error = null;

    /**
     * when the page is generated, bind with some default functions.
     * @param savedInstanceState state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        refreshErrorMessage();

        //button that allow the user to go back to the home page
        Button returnHomePage = findViewById(R.id.returnHomePage);
        returnHomePage.setOnClickListener(view -> startActivity(new Intent(SignUp.this,MainActivity.class)));
    }

    /**
     * refresh the error message text field
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
     * when the user click the sign up button, try to sign up.
     * @param view view
     */
    public void signUp(View view){
        String name = String.valueOf(((TextView)findViewById(R.id.editTextName)).getText());
        String address = String.valueOf(((TextView)findViewById(R.id.editTextAddress)).getText());
        String username = String.valueOf(((TextView)findViewById(R.id.editTextUserName)).getText());
        String password = String.valueOf(((TextView)findViewById(R.id.editTextPassword)).getText());
        String confirmPassword = String.valueOf(((TextView)findViewById(R.id.editTextRepeatPassword)).getText());
        String email = String.valueOf(((TextView)findViewById(R.id.editTextEmailAddress)).getText());
        //check the input format
        if (checkInputs(name,address,username,password,confirmPassword,email)){
            //inputs are valid now, so send request to sign up an user.
            sendSignUpRequest(name,address,username,password,email);
        }

    }

    /**
     * check whether the sign up filled in information are in the correct format
     * @param name user's name
     * @param address user's address
     * @param username user's username
     * @param password user's password
     * @param confirmPassword the input confirm password
     * @param email user's email address
     * @return return true if all inputs are in the right format, otherwise false
     */
    private boolean checkInputs(String name, String address,String username,String password,String confirmPassword,String email){

        //check name
        Pattern namePattern = Pattern.compile("^[a-zA-Z\\s]{3,}$",Pattern.CASE_INSENSITIVE);
        Matcher nameMatcher = namePattern.matcher(name);
        if(!nameMatcher.find()){
            new AlertDialog.Builder(this)
                    .setMessage("Please enter your full name!")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Alright", (dialog, id) -> dialog.cancel())
                    .show();
            return false;
        }

        //check address
        if(address==null || address.equals("") || address.trim().equals("")){
            new AlertDialog.Builder(this)
                    .setMessage("Address cannot be empty!")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Alright", (dialog, id) -> dialog.cancel())
                    .show();
            return false;
        }

        //check username
        Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9_-]{6,16}$", Pattern.CASE_INSENSITIVE);
        Matcher usernameMatcher = usernamePattern.matcher(username);
        if(!usernameMatcher.find()){
            new AlertDialog.Builder(this)
                    .setMessage("Username should be a 6-16 long characters consisted of letters and digits")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Alright", (dialog, id) -> dialog.cancel())
                    .show();
            return false;
        }

        //check password pattern
        Pattern passwordPattern = Pattern.compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,18}$", Pattern.CASE_INSENSITIVE);
        Matcher passwordMatcher = passwordPattern.matcher(password);
        if(!passwordMatcher.find()){
            new AlertDialog.Builder(this)
                    .setMessage("Password should be a 8-18 long characters consist of letters and digits")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Alright", (dialog, id) -> dialog.cancel())
                    .show();
            return false;
        }

        //check confirm password
        if (!password.equals(confirmPassword)){
            new AlertDialog.Builder(this)
                    .setMessage("Password and confirm password should be identical")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Alright", (dialog, id) -> dialog.cancel())
                    .show();
            return false;
        }

        //check email
        Pattern emailPattern = Pattern.compile("^([A-Za-z0-9_\\-.])+@([A-Za-z0-9_\\-.])+\\.([A-Za-z]{2,4})$", Pattern.CASE_INSENSITIVE);
        Matcher emailMatcher = emailPattern.matcher(email);
        if(!emailMatcher.find()){
            new AlertDialog.Builder(this)
                    .setMessage("Please enter a valid email address!")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Alright", (dialog, id) -> dialog.cancel())
                    .show();
            return false;
        }

        return true;
    }

    /**
     * send the sign up request to the backend
     * @param name user's name
     * @param address user's address
     * @param username user's username
     * @param password user's password
     * @param email user's email address
     */
    private void sendSignUpRequest(String name, String address,String username,String password,String email){
        error = "";
        RequestParams requestParams = new RequestParams();
        requestParams.add("name", name);
        requestParams.add("address",address);
        requestParams.add("isLocal", String.valueOf(true));

        HttpUtils.post("users/createUser", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //create user account successfully, try create online account
                try {
                    //send with responding uid parameter
                    sendCreateOnlineAccountRequest(response.getInt("id"),username, password,email);

                } catch (JSONException e) {
                    error = "fail to sign up! Check your input information!";
                }
                refreshErrorMessage();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                error = "fail to sign up! Check your input information!";
                refreshErrorMessage();
            }
        });
    }

    /**
     * send the sign up online account request
     * @param uid user id
     * @param username username
     * @param password user's password
     * @param email users' email address
     */
    private void sendCreateOnlineAccountRequest(int uid, String username, String password, String email){
        error = "";
        RequestParams requestParams = new RequestParams();
        requestParams.put("uid",uid);
        requestParams.add("username", username);
        requestParams.add("password", password);
        requestParams.add("email", email);

        HttpUtils.put("users/updateOnlineAccount", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                showSnackBar("Sign Up Successfully!");
                // to the login page
                startActivity(new Intent(SignUp.this,Login.class));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                error = "Fail to sign up! The username has been registered";
                refreshErrorMessage();
            }
        });


    }

    /**
     * Show snack bar of some content
     * @param content content you want to show in the snake bar
     */
    public void showSnackBar(String content){
        Snackbar snackbar=Snackbar.make(findViewById(R.id.error),content,Snackbar.LENGTH_LONG);
        snackbar.setAction("OK", v -> {

        });
        snackbar.show();
    }

}