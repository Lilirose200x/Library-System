package ca.mcgill.ecse321.library_android;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * user home page control
 */
public class User_Homepage extends AppCompatActivity {
    private int uid = -1;

    /**
     * bind default functions when visit the user home page
     * @param savedInstanceState state
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_homepage);

        //get the passed parameter
        uid = getIntent().getIntExtra("uid",-1);

        //        Set button which goes to view item page
        findViewById(R.id.goToUserItemButton).setOnClickListener(v -> {
            Intent intent = new Intent(User_Homepage.this, User_Item.class);
            intent.putExtra("uid", uid);
            startActivity(intent);
        });
        //        Set button which goes to view event page
        findViewById(R.id.goToUserEventButton).setOnClickListener(v -> {
            Intent intent = new Intent(User_Homepage.this, User_Event.class);
            intent.putExtra("uid", uid);
            startActivity(intent);
        });

        //        Set button which goes to view my item page
        findViewById(R.id.goToUserMyItemButton).setOnClickListener(v -> {
            Intent intent = new Intent(User_Homepage.this, User_MyItem.class);
            intent.putExtra("uid", uid);
            startActivity(intent);
        });
        //        Set button which goes to view my event page
        findViewById(R.id.goToUserMyEventButton).setOnClickListener(v -> {
            Intent intent = new Intent(User_Homepage.this, User_MyEvent.class);
            intent.putExtra("uid", uid);
            startActivity(intent);
        });
        //        Set button which goes to update account page
        findViewById(R.id.goToUserUpdateAccountButton).setOnClickListener(v -> {
            Intent intent = new Intent(User_Homepage.this, User_UpdateAccount.class);
            intent.putExtra("uid", uid);
            startActivity(intent);
        });

    }


}
