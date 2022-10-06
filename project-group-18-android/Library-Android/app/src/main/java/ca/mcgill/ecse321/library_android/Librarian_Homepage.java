package ca.mcgill.ecse321.library_android;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * librarian home page control
 */
public class Librarian_Homepage extends AppCompatActivity {

    /**
     * bind default functions
     * @param savedInstanceState state
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.librarian_homepage);

        //get the passed parameter
        int librarianId = getIntent().getIntExtra("librarianId",-1);

        //        Set button which goes to item management page
        findViewById(R.id.goToLibrarianItemButton).setOnClickListener(
                view -> startActivity(new Intent(Librarian_Homepage.this, Librarian_Item.class)));

        //        Set button which goes to event management page
        findViewById(R.id.goToLibrarianEventButton).setOnClickListener(
                view -> startActivity(new Intent(Librarian_Homepage.this, Librarian_Event.class)));

        //        Set button which goes to business hour management page
        findViewById(R.id.goToLibrarianBusinessHourButton).setOnClickListener(
//                view -> startActivity(new Intent(Librarian_Homepage.this, Librarian_BusinessHour.class))
                view -> startActivity(
                        new Intent(Librarian_Homepage.this, Librarian_BusinessHour.class).putExtra("librarianId", librarianId))
        );

        //        Set button which goes to employment(librarian) management page
        findViewById(R.id.goToLibrarianEmploymentButton).setOnClickListener(
//                view -> startActivity(new Intent(Librarian_Homepage.this, Librarian_Employment.class)));
                view -> startActivity(
                        new Intent(Librarian_Homepage.this, Librarian_Employment.class).putExtra("librarianId", librarianId))
        );
    }

}
