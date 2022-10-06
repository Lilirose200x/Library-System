package ca.mcgill.ecse321.library_android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * librarian employment page control
 */
public class Librarian_Employment extends AppCompatActivity {
    private String error = null;
    private TableLayout librarianTable;
    private int librarianId = -1;

    /**
     * set default functions of the page
     *
     * @param savedInstanceState state
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.librarian_employment);

        //get the passed parameter
        librarianId = getIntent().getIntExtra("librarianId", -1);

        //check whether the user is the head librarian
        HttpUtils.get("/librarians/getLibrarianById?id=" + librarianId, new RequestParams(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    boolean isHeadLibrarian = response.getBoolean("headLibrarian");
                    if (!isHeadLibrarian) {
                        //no right to visit this page
                        systemAlert("No authority!");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                systemAlert("Invalid visit");
            }
        });

        librarianTable = findViewById(R.id.librarian_list);
        librarianTable.setStretchAllColumns(true);
        addAllLibrarianToTable();
    }

    /**
     * add all librarian data to table
     */
    private void addAllLibrarianToTable() {
        error = "";
        HttpUtils.get("/librarians/librarianList", new RequestParams(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                int numLibrarian = response.length();
                for (int i = 0; i < numLibrarian; i++) {
                    try {
                        JSONObject librarian = response.getJSONObject(i);
                        addLibrarianToTable(librarian);
                    } catch (Exception e) {
                        error = e.getMessage();
                    }
                }
                refreshErrorMessage();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    error += errorResponse.get("message").toString();
                } catch (Exception e) {
                    error += e.getMessage();
                }
                refreshErrorMessage();
            }
        });

    }

    /**
     * Add librarian info to table
     *
     * @param librarian librarian dto
     */
    @SuppressLint("SetTextI18n")
    private void addLibrarianToTable(JSONObject librarian) {
        error = "";
        final String id, name, address;//,businessHour;
        JSONArray businessHour;
        try {
            id = librarian.getString("id");
            name = librarian.getString("name");
            address = librarian.getString("address");
            businessHour = librarian.getJSONArray("businessHourDtos");
        } catch (JSONException e) {
            error += e.getMessage();
            refreshErrorMessage();
            return;
        }

        TableRow row = TableLayoutUtils.initializeRow(Librarian_Employment.this, v -> {

        });
        librarianTable.addView(row);

        LinearLayout rowVerticalLayout = new LinearLayout(Librarian_Employment.this);
        rowVerticalLayout.setOrientation(LinearLayout.VERTICAL);
        row.addView(rowVerticalLayout);

        LinearLayout subRow1 = new LinearLayout(Librarian_Employment.this);
        subRow1.setOrientation(LinearLayout.HORIZONTAL);
        rowVerticalLayout.addView(subRow1);

        LinearLayout subRow2 = new LinearLayout(Librarian_Employment.this);
        subRow2.setOrientation(LinearLayout.HORIZONTAL);
        rowVerticalLayout.addView(subRow2);

        TextView nameView = new TextView(Librarian_Employment.this);
        nameView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        // Set TextView style -> https://www.tutorialspoint.com/how-to-change-a-textview-style-at-runtime-in-android
        nameView.setText("name: " + name);
        subRow1.addView(nameView);

        TextView startView = new TextView(Librarian_Employment.this);
        startView.setText("businessHour: " + businessHour);
        subRow1.addView(startView);

        TextView idView = new TextView(Librarian_Employment.this);
        idView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        // weight 1 to fill the remaining space thus right-aligning status
        // -> https://stackoverflow.com/questions/4305564/android-layout-right-align
        idView.setText("librarian id: " + id);
        subRow2.addView(idView);

        TextView endView = new TextView(Librarian_Employment.this);
        endView.setText("address: " + address);
        subRow2.addView(endView);
    }

    /**
     * refresh the error message to tell user the current condition
     */
    private void refreshErrorMessage() {
        TextView msgTextView = findViewById(R.id.error);
        msgTextView.setText(error);
    }

    /**
     * pop up a window to alert user dangerous operations
     * @param content alert content
     */
    private void systemAlert(String content) {
        new AlertDialog.Builder(this)
                .setMessage(content)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Alright", (dialog, id) -> {
                    //send to the librarian home page
                    startActivity(
                            new Intent(Librarian_Employment.this, Librarian_Homepage.class).putExtra("librarianId", librarianId));
                })
                .show();
    }

}
