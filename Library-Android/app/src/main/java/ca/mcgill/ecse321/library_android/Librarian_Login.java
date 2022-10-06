package ca.mcgill.ecse321.library_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class Librarian_Login extends AppCompatActivity {
    private TextView librarianID =null;
    private String error =null;
    /**
     * bind default functions to UI components
     * @param savedInstanceState state
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.librarian_login);
        refreshErrorMessage();
        //        Set button where librarians login
        librarianID=findViewById(R.id.librarian_login_id);
        findViewById(R.id.librarian_loginButton).setOnClickListener(view -> login(librarianID.getText().toString()));
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
     * This method check the input of the id and call the request method
     * @param Id librarian id
     */
    public void login(String Id){
        if (checkInput(Id)){
            sendLibrarianLoginRequest(Id);
        }
    }

    /**
     * This method send the request to the backend.
     * @param id librarian id
     */
    private void sendLibrarianLoginRequest(String id) {
        error = "";
        RequestParams requestParams = new RequestParams();
        requestParams.add("id", id);
        HttpUtils.get("librarians/getLibrarianById", requestParams, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Intent intent = new Intent(Librarian_Login.this, Librarian_Homepage.class);
                    intent.putExtra("librarianId", response.getInt("id"));
                    error="";
                    refreshErrorMessage();
                    startActivity(intent);
                }catch (JSONException e){
                    error = e.getMessage();
                    error +="    Fail to login";
                    refreshErrorMessage();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    error += errorResponse.get("error").toString();
                    error +="    Fail to login";
                } catch (JSONException e) {
                    error += e.getMessage();
                    error +="    Fail to login";
                }
                refreshErrorMessage();
            }
        });
    }

    /**
     * This method check the input and return true when there is input in the id
     * @param id input login id
     * @return if the input id is empty, return false, otherwise true
     */
    private boolean checkInput(String id) {
        if(id.equals("")){
            new AlertDialog.Builder(this)
                    .setMessage("Please input an id")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Alright", (dialog, id1) -> dialog.cancel())
                    .show();
            return false;
        }
        return true;
    }
}
