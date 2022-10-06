package ca.mcgill.ecse321.library_android;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;

/**
 * user update account page control
 */
public class User_UpdateAccount extends AppCompatActivity {
    private int uid = -1;

    /**
     * bind default functions when the page is created
     * @param savedInstanceState state
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_update_account);
        //get the passed parameter
        uid = getIntent().getIntExtra("uid",-1);
        //initialize UI by the current info
        refreshCurrentAccountInfo();
    }

    /**
     * refresh the current account info to show the latest current account info
     */
    private void refreshCurrentAccountInfo(){
        HttpUtils.get("/users/getUserById?uid="+uid, new RequestParams(), new JsonHttpResponseHandler() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try{
                    ((TextView)findViewById(R.id.currentAddress)).setText("Current address:\n"+response.get("address"));
                    ((TextView)findViewById(R.id.currentUsername)).setText("Current username:\n"+response.getJSONObject("onlineAccountDto").get("username"));
                    ((TextView)findViewById(R.id.currentEmail)).setText("Current username:\n"+response.getJSONObject("onlineAccountDto").get("email"));
                }catch (JSONException e){
                    showSnackBar("Fail to get current account info");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showSnackBar("Fail to get current account info");
            }
        });
    }

    /**
     * update address
     * @param view view
     */
    public void updateAddress(View view) {
        String newAddress = String.valueOf(((TextView)findViewById(R.id.newAddress)).getText());
        //check whether the input format is valid
        if (newAddress.equals("")||newAddress.trim().equals("")){
            new AlertDialog.Builder(this)
                    .setMessage("Address cannot be empty!")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Alright", (dialog, id) -> dialog.cancel())
                    .show();
            return;
        }

        //send the request
        RequestParams requestParams = new RequestParams();
        requestParams.put("uid",uid);
        requestParams.add("address",newAddress);
        HttpUtils.put("/users/updateAddress", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //tell user register condition
                showSnackBar("update address successfully!");
                ((TextView)findViewById(R.id.newAddress)).setText("");
                refreshCurrentAccountInfo();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showSnackBar("update address failed!");
            }
        });
    }

    /**
     * update username
     * @param view view
     */
    public void updateUsername(View view) {
        String newUsername = String.valueOf(((TextView)findViewById(R.id.newUsername)).getText());
        if(checkUsername(newUsername)){
            //send request
            RequestParams requestParams = new RequestParams();
            requestParams.put("uid",uid);
            requestParams.add("username",newUsername);
            HttpUtils.put("/users/updateOnlineAccountUsername", requestParams, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    //tell user register condition
                    showSnackBar("update username successfully!");
                    ((TextView)findViewById(R.id.newUsername)).setText("");
                    refreshCurrentAccountInfo();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    showSnackBar("update username failed! The username has been registered by others!");
                }
            });
        }
    }

    /**
     * update the email address
     * @param view view
     */
    public void updateEmail(View view) {
        String newEmail = String.valueOf(((TextView)findViewById(R.id.newEmail)).getText());
        //check the input email format
        if(checkEmail(newEmail)){
            //send request
            RequestParams requestParams = new RequestParams();
            requestParams.put("uid",uid);
            requestParams.add("email",newEmail);
            HttpUtils.put("/users/updateOnlineAccountEmail", requestParams, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    //tell user register condition
                    showSnackBar("update email successfully!");
                    ((TextView)findViewById(R.id.newEmail)).setText("");
                    refreshCurrentAccountInfo();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    showSnackBar("update email failed!");
                }
            });
        }
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

    /**
     * check whether the input username is in the right format
     * @param username new input username
     * @return true if in the right format, otherwise false.
     */
    private boolean checkUsername(String username){
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
        return true;
    }

    /**
     * check the input email format
     * @param email the input update email
     * @return true if the email is in the right format, otherwise false
     */
    private boolean checkEmail(String email){
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
}
