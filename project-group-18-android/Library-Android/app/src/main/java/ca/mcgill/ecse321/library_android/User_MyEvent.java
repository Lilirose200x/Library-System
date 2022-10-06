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
 * user my event page control
 */
public class User_MyEvent extends AppCompatActivity {
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
        setContentView(R.layout.user_my_event);

        //get the passed parameter
        uid = getIntent().getIntExtra("uid", -1);
        error = "";
        refreshErrorMessage();

        eventTable = findViewById(R.id.userMyEventTable);
        eventTable.setStretchAllColumns(true);
        addAllEventsToTable();
    }

    /**
     * browse all the events
     */
    private void addAllEventsToTable() {
        error = "";
        refreshErrorMessage();

        HttpUtils.get("eventRegistrations/getEventsByPerson?pid=" + uid, new RequestParams(), new JsonHttpResponseHandler() {
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
        System.out.println("add event to table");
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

        TableRow row = TableLayoutUtils.initializeRow(User_MyEvent.this, v -> {

        });
        eventTable.addView(row);


        LinearLayout rowVerticalLayout = new LinearLayout(User_MyEvent.this);
        rowVerticalLayout.setOrientation(LinearLayout.VERTICAL);
        row.addView(rowVerticalLayout);

        LinearLayout subRow1 = new LinearLayout(User_MyEvent.this);
        subRow1.setOrientation(LinearLayout.HORIZONTAL);
        rowVerticalLayout.addView(subRow1);

        LinearLayout subRow2 = new LinearLayout(User_MyEvent.this);
        subRow2.setOrientation(LinearLayout.HORIZONTAL);
        rowVerticalLayout.addView(subRow2);

        TextView nameView = new TextView(User_MyEvent.this);
        nameView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        // Set TextView style -> https://www.tutorialspoint.com/how-to-change-a-textview-style-at-runtime-in-android
        nameView.setText("Event name: " + name);
        subRow1.addView(nameView);

        TextView startView = new TextView(User_MyEvent.this);
        startView.setText("start: " + start);
        subRow1.addView(startView);

        TextView idView = new TextView(User_MyEvent.this);
        idView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        // weight 1 to fill the remaining space thus right-aligning status
        // -> https://stackoverflow.com/questions/4305564/android-layout-right-align
        idView.setText("Event id: " + id);
        subRow2.addView(idView);

        TextView endView = new TextView(User_MyEvent.this);
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
     * unregister the event when the unregister button is clicked
     *
     * @param view view
     */
    public void unRegisterEvent(View view) {
        int eventId=0;
        //get the input eid
        if(String.valueOf(((TextView) findViewById(R.id.myEventIdInput)).getText()).length()>0){
            eventId = Integer.parseInt(String.valueOf(((TextView) findViewById(R.id.myEventIdInput)).getText()));
        }else{
            showSnackBar("Input id cannot be empty!");
            return;
        }


        //now the input event is valid, so can send request to unregister event
        sendUnregisterEventRequest(uid, eventId);

    }

    /**
     * send unregister event request to the backend
     *
     * @param uid current user id
     * @param eid unregister event id
     */
    private void sendUnregisterEventRequest(int uid, int eid) {
        RequestParams requestParams = new RequestParams();
        requestParams.put("pid", uid);
        requestParams.put("eid", eid);
        HttpUtils.delete("/eventRegistrations/cancel", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                error = "";
                refreshErrorMessage();
                //tell user unregister condition
                showSnackBar("unregister successfully");
                //refresh the my event list
                refreshTable();
                ((TextView) findViewById(R.id.myEventIdInput)).setText("");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (statusCode == 200) {
                    showSnackBar("unregister successfully!");
                    refreshTable();
                } else {
                    showSnackBar("unregister failed!");
                }
            }
        });
    }

    /**
     * after some update operation, the showing data should change to the latest form
     */
    private void refreshTable() {
        eventTable.removeAllViews();
        addAllEventsToTable();
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
