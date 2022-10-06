package ca.mcgill.ecse321.library_android;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * librarian event control
 */
public class Librarian_Event extends AppCompatActivity {
    private TableLayout eventTable;
    private String error = "";

    /**
     * bind default functions
     *
     * @param savedInstanceState state
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.librarian_event);

        refreshErrorMessage();

        eventTable = findViewById(R.id.librarian_EventTable);
        eventTable.setStretchAllColumns(true);
        addAllEventsToTable();
    }


    /**
     * browse all the events
     */
    private void addAllEventsToTable() {
        error = "";
        HttpUtils.get("events/eventList", new RequestParams(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                int numEvent = response.length();
                for (int i = 0; i < numEvent; i++) {
                    try {
                        JSONObject event = response.getJSONObject(i);
                        addEventToTable(event);
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
     * add the event DTO in the response to the show list
     *
     * @param event event
     */
    @SuppressLint("SetTextI18n")
    private void addEventToTable(JSONObject event) {
        error = "";
        //get event DTO data
        final String id, name, start, end;
        try {
            id = event.getString("id");
            name = event.getString("name");
            start = event.getJSONObject("timeSlotDto").getString("startDate") + " " + event.getJSONObject("timeSlotDto").getString("startTime");
            end = event.getJSONObject("timeSlotDto").getString("endDate") + " " + event.getJSONObject("timeSlotDto").getString("endTime");
        } catch (JSONException e) {
            error += e.getMessage();
            refreshErrorMessage();
            return;
        }

        TableRow row = TableLayoutUtils.initializeRow(Librarian_Event.this, v -> {

        });
        eventTable.addView(row);


        LinearLayout rowVerticalLayout = new LinearLayout(Librarian_Event.this);
        rowVerticalLayout.setOrientation(LinearLayout.VERTICAL);
        row.addView(rowVerticalLayout);

        LinearLayout subRow1 = new LinearLayout(Librarian_Event.this);
        subRow1.setOrientation(LinearLayout.HORIZONTAL);
        rowVerticalLayout.addView(subRow1);

        LinearLayout subRow2 = new LinearLayout(Librarian_Event.this);
        subRow2.setOrientation(LinearLayout.HORIZONTAL);
        rowVerticalLayout.addView(subRow2);

        TextView nameView = new TextView(Librarian_Event.this);
        nameView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        // Set TextView style -> https://www.tutorialspoint.com/how-to-change-a-textview-style-at-runtime-in-android
        nameView.setText("Event name: " + name);
        subRow1.addView(nameView);

        TextView startView = new TextView(Librarian_Event.this);
        startView.setText("start: " + start);
        subRow1.addView(startView);

        TextView idView = new TextView(Librarian_Event.this);
        idView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        // weight 1 to fill the remaining space thus right-aligning status
        // -> https://stackoverflow.com/questions/4305564/android-layout-right-align
        idView.setText("Event id: " + id);
        subRow2.addView(idView);

        TextView endView = new TextView(Librarian_Event.this);
        endView.setText("end: " + end);
        subRow2.addView(endView);
    }

    /**
     * Create a new create when pressing the button
     *
     * @param v view bound to this method(button)
     */
    public void createEvent(View v) {
        error = "";
        RequestParams requestParams = new RequestParams();
        final TextView name = findViewById(R.id.newEvent_name);
        final TextView startDate = findViewById(R.id.newEvent_StartDate);
        final TextView startTime = findViewById(R.id.newEvent_StartTime);
        final TextView endDate = findViewById(R.id.newEvent_EndDate);
        final TextView endTime = findViewById(R.id.newEvent_EndTime);
        requestParams.add("name", name.getText().toString());
        requestParams.add("startDate", startDate.getText().toString());
        requestParams.add("startTime", startTime.getText().toString());
        requestParams.add("endDate", endDate.getText().toString());
        requestParams.add("endTime", endTime.getText().toString());

        //      Send post request
        HttpUtils.post("events/createEvent", requestParams, new JsonHttpResponseHandler() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                error = "";
                refreshErrorMessage();
                refreshTable();
                showSnackBar("Success!");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showSnackBar("Fail!");
            }
        });
    }

    /**
     * cancel the item reservation
     * @param view view which contains the id of the event
     */
    public void deleteEvent(View view) {
        final TextView id = findViewById(R.id.deleteEvent_id);
        RequestParams requestParams = new RequestParams();
        requestParams.put("id", Integer.valueOf(id.getText().toString()));
        HttpUtils.delete("events/deleteEvent", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                showSnackBar("Success to delete");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (statusCode == 200) {
                    showSnackBar("Success to delete");
                } else {
                    showSnackBar("Fail to delete");
                }
            }
        });
        refreshTable();
    }

    /**
     * refresh the error message
     */
    private void refreshErrorMessage() {
        TextView msgTextView = findViewById(R.id.error);
        msgTextView.setText(error);
    }

    /**
     * Show snack bar of some content
     *
     * @param content content you want to show in the snake bar
     */
    public void showSnackBar(String content) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.error), content, Snackbar.LENGTH_LONG);
        snackbar.setAction("OK", view -> {

        });
        snackbar.show();
    }


    /**
     * Refresh the event table layout
     */
    private void refreshTable() {
        eventTable.removeAllViews();
        addAllEventsToTable();
    }


}
