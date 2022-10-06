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
 * user event page control
 */
public class User_Event extends AppCompatActivity {
    private String error = "";
    private TableLayout eventTable;
    private int uid = -1;

    /**
     * when the event page is loaded, bind default functions
     *
     * @param savedInstanceState state
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_event);

        //get the passed parameter
        uid = getIntent().getIntExtra("uid", -1);
        error = "";
        refreshErrorMessage();

        eventTable = findViewById(R.id.user_eventTable);
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

        TableRow row = TableLayoutUtils.initializeRow(User_Event.this, v -> {

        });
        eventTable.addView(row);

        LinearLayout rowVerticalLayout = new LinearLayout(User_Event.this);
        rowVerticalLayout.setOrientation(LinearLayout.VERTICAL);
        row.addView(rowVerticalLayout);

        LinearLayout subRow1 = new LinearLayout(User_Event.this);
        subRow1.setOrientation(LinearLayout.HORIZONTAL);
        rowVerticalLayout.addView(subRow1);

        LinearLayout subRow2 = new LinearLayout(User_Event.this);
        subRow2.setOrientation(LinearLayout.HORIZONTAL);
        rowVerticalLayout.addView(subRow2);

        TextView nameView = new TextView(User_Event.this);
        nameView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        // Set TextView style -> https://www.tutorialspoint.com/how-to-change-a-textview-style-at-runtime-in-android
        nameView.setText("Event name: " + name);
        subRow1.addView(nameView);

        TextView startView = new TextView(User_Event.this);
        startView.setText("start: " + start);
        subRow1.addView(startView);

        TextView idView = new TextView(User_Event.this);
        idView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        // weight 1 to fill the remaining space thus right-aligning status
        // -> https://stackoverflow.com/questions/4305564/android-layout-right-align
        idView.setText("Event id: " + id);
        subRow2.addView(idView);

        TextView endView = new TextView(User_Event.this);
        endView.setText("end: " + end);
        subRow2.addView(endView);
    }

    /**
     * refresh the error message
     */
    private void refreshErrorMessage() {
        TextView msgTextView = findViewById(R.id.error);
        msgTextView.setText(error);
    }

    /**
     * register the event when the register button is clicked
     *
     * @param view view
     */
    public void registerEvent(View view) {
        //get the input eid
        int eventId = 0;
        if (String.valueOf(((TextView) findViewById(R.id.eventIdInput)).getText()).length()>0) {
            eventId = Integer.parseInt(String.valueOf(((TextView) findViewById(R.id.eventIdInput)).getText()));
        } else {
            showSnackBar("Input id cannot be empty!");
            return;
        }


        //now the input event is valid, so can send request to register event
        sendRegisterEventRequest(uid, eventId);
    }

    /**
     * send register event request to the backend
     *
     * @param uid current user id
     * @param eid register event id
     */
    private void sendRegisterEventRequest(int uid, int eid) {
        RequestParams requestParams = new RequestParams();
        requestParams.put("pid", uid);
        requestParams.put("eid", eid);
        HttpUtils.post("/eventRegistrations/attend", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                error = "";
                refreshErrorMessage();
                //tell user register condition
                showSnackBar("register successfully!");
                ((TextView) findViewById(R.id.eventIdInput)).setText("");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showSnackBar("register failed!");
            }
        });
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


}
