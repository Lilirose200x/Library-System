package ca.mcgill.ecse321.library_android;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import cz.msebera.android.httpclient.Header;

/**
 * The index page control
 */
public class MainActivity extends AppCompatActivity {

    //number of init times
    private int count = 0;

    /**
     * bind default functions
     * @param savedInstanceState state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //        Set button which goes to librarian login page
        findViewById(R.id.librarian_login).setOnClickListener(v -> startActivity(new Intent(MainActivity.this,Librarian_Login.class)));
        //        Set button which goes to user login page
        findViewById(R.id.user_login).setOnClickListener(v -> startActivity(new Intent(MainActivity.this,Login.class)));
        //        set button which goes to user sign up page
        findViewById(R.id.user_signUp).setOnClickListener(v -> startActivity(new Intent(MainActivity.this,SignUp.class)));

        //only init the library one time
        if(count==0){
            RequestParams requestParams= new RequestParams();
            HttpUtils.get("/init", requestParams, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, String responseString) {
                    count++;
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    //sometimes 200 status code will enter the onFailure
                    if (statusCode==200){
                        count++;
                    }
                }
            });
        }
    }










}