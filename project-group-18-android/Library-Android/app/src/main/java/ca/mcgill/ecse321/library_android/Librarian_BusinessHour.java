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

import com.google.android.material.snackbar.Snackbar;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * manage the business hour page control
 */
public class Librarian_BusinessHour extends AppCompatActivity {
    private TextView dayOfWeek = null;
    private TextView startTime = null;
    private TextView endTime = null;
    private String error = null;
    private TableLayout businessHourTable;
    private int librarianId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.librarian_businesshour);

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

        dayOfWeek = (TextView) findViewById(R.id.newBusinessHour_DayOfWeek);
        startTime = (TextView) findViewById(R.id.newBusinessHour_StartTime);
        endTime = (TextView) findViewById(R.id.newBusinessHour_EndTime);

        findViewById(R.id.create_businessHour).setOnClickListener(v -> sendCreateBusinessHourRequest(dayOfWeek.getText().toString(), startTime.getText().toString(), endTime.getText().toString()));

        findViewById(R.id.update_buessinessHour).setOnClickListener(v -> sendUpdateBusinessHourRequest(dayOfWeek.getText().toString(), startTime.getText().toString(), endTime.getText().toString()));

        businessHourTable = findViewById(R.id.librarian_businessHourListTable);
        businessHourTable.setStretchAllColumns(true);
        addAllBusinessHourToTable();
    }

    /**
     * Add all table business hours to the table layout
     */
    private void addAllBusinessHourToTable() {
        error = "";
        HttpUtils.get("/businessHours/businessHourList", new RequestParams(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                int numBusinessHour = response.length();
                for (int i = 0; i < numBusinessHour; i++) {
                    try {
                        JSONObject businessHour = response.getJSONObject(i);
                        addBusinessHourToTable(businessHour);
                    } catch (Exception e) {
                        error = e.getMessage();
                    }
                }
                refreshErrorMessage();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                error ="Fail ot load current business hour!";
                refreshErrorMessage();
            }
        });

    }

    /**
     * Add a business hour to the table
     *
     * @param businessHour JSON object of the business hour
     */
    @SuppressLint("SetTextI18n")
    private void addBusinessHourToTable(JSONObject businessHour) {
        error = "";
        final String id, dayOfWeek, startTime, endTime;
        try {
            id = businessHour.getString("id");
            dayOfWeek = businessHour.getString("dayOfWeek");
            startTime = businessHour.getString("startTime");
            endTime = businessHour.getString("endTime");
        } catch (JSONException e) {
            error += e.getMessage();
            refreshErrorMessage();
            return;
        }

        TableRow row = TableLayoutUtils.initializeRow(Librarian_BusinessHour.this, v -> {

        });
        businessHourTable.addView(row);

        LinearLayout rowVerticalLayout = new LinearLayout(Librarian_BusinessHour.this);
        rowVerticalLayout.setOrientation(LinearLayout.VERTICAL);
        row.addView(rowVerticalLayout);

        LinearLayout subRow1 = new LinearLayout(Librarian_BusinessHour.this);
        subRow1.setOrientation(LinearLayout.HORIZONTAL);
        rowVerticalLayout.addView(subRow1);

        LinearLayout subRow2 = new LinearLayout(Librarian_BusinessHour.this);
        subRow2.setOrientation(LinearLayout.HORIZONTAL);
        rowVerticalLayout.addView(subRow2);

        TextView nameView = new TextView(Librarian_BusinessHour.this);
        nameView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        // Set TextView style -> https://www.tutorialspoint.com/how-to-change-a-textview-style-at-runtime-in-android
        nameView.setText("Day of Week: " + dayOfWeek);
        subRow1.addView(nameView);

        TextView startView = new TextView(Librarian_BusinessHour.this);
        startView.setText("Start Time: " + startTime);
        subRow1.addView(startView);

        TextView idView = new TextView(Librarian_BusinessHour.this);
        idView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        // weight 1 to fill the remaining space thus right-aligning status
        // -> https://stackoverflow.com/questions/4305564/android-layout-right-align
        idView.setText("BusinessHour id: " + id);
        subRow2.addView(idView);

        TextView endView = new TextView(Librarian_BusinessHour.this);
        endView.setText("End Time: " + endTime);
        subRow2.addView(endView);
    }

    /**
     * Send a http request of creating business hour
     *
     * @param dayOfWeek day of week
     * @param startTime start time
     * @param endTime   end time
     */
    private void sendCreateBusinessHourRequest(String dayOfWeek, String startTime, String endTime) {
        error = "";
        RequestParams requestParams = new RequestParams();
        requestParams.add("dayOfWeek", dayOfWeek);
        requestParams.add("startTime", startTime);
        requestParams.add("endTime", endTime);
        HttpUtils.post("/businessHours/createBusinessHour", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                refreshTable();
                showSnackBar("Create Business Hour Successfully!");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showSnackBar("Fail to create!");
            }
        });
    }

    /**
     * Send a http request of updating business hour
     *
     * @param dayOfWeek new day of week
     * @param startTime new start time
     * @param endTime   new end time
     */
    private void sendUpdateBusinessHourRequest(String dayOfWeek, String startTime, String endTime) {
        error = "";
        RequestParams requestParams = new RequestParams();
        requestParams.add("dayOfWeek", dayOfWeek);
        requestParams.add("startTime", startTime);
        requestParams.add("endTime", endTime);
        HttpUtils.put("/businessHours/updateBusinessHourTime", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                refreshTable();
                showSnackBar("Update Successfully!");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showSnackBar("Fail to Update!");
            }
        });
    }

    /**
     * Refresh error message
     */
    private void refreshErrorMessage() {
        TextView msgTextView = findViewById(R.id.error);
        msgTextView.setText(error);
    }

    /**
     * Refresh the business hour table layout
     */
    private void refreshTable() {
        businessHourTable.removeAllViews();
        addAllBusinessHourToTable();
    }

    /**
     * Show snack bar of some content
     *
     * @param content content you want to show in the snake bar
     */
    public void showSnackBar(String content) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.error), content, Snackbar.LENGTH_LONG);
        snackbar.setAction("OK", v -> {

        });
        snackbar.show();
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
                            new Intent(Librarian_BusinessHour.this, Librarian_Homepage.class).putExtra("librarianId", librarianId));
                })
                .show();
    }
}
