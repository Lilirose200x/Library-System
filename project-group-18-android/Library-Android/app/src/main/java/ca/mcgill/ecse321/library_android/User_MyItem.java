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
 * usey my item page control
 */
public class User_MyItem extends AppCompatActivity {
    //    id of current login user
    private int uid = -1;
    private String error;
    private TableLayout myItemReservationTable;

    /**
     * defaul functions
     *
     * @param savedInstanceState state
     */
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_myitem);
        //TableLayout displays my items reservation
        myItemReservationTable = findViewById(R.id.user_myItemTable);
        myItemReservationTable.setStretchAllColumns(true);
        //get uid parameter
        uid = getIntent().getIntExtra("uid", -1);
        if (uid != -1) {
            addAllItemsToTable();
        }
    }

    /**
     * Add all itemReservations to the TableLayout of items in UI
     */
    private void addAllItemsToTable() {
        HttpUtils.get("itemReservations/getItemReservationList", new RequestParams(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                int numItems = response.length();
                for (int i = 0; i < numItems; i++) {
                    try {
                        JSONObject itemReservation = response.getJSONObject(i);
                        if (itemReservation.getJSONObject("personDto").getInt("id") == uid) {
                            addItemToTable(itemReservation);
                        }
                    } catch (Exception e) {
                        error = e.getMessage();
                    }
                }
                refreshMessage();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                try {
                    error += errorResponse.get("message").toString();
                } catch (Exception e) {
                    error += e.getMessage();
                }
                refreshMessage();
            }
        });

    }

    /**
     * Add an itemReservation to the table, which is defined by a row in the table
     *
     * @param itemReservation JSON object of the itemReservation we want to add to the table
     */
    @SuppressLint("SetTextI18n")
    private void addItemToTable(JSONObject itemReservation) {
        final String id, name, itemCategory, start, end, itemId;
        try {
            id = itemReservation.getString("id");
            name = itemReservation.getJSONObject("itemDto").getString("name");
            itemId = itemReservation.getJSONObject("itemDto").getString("id");
            itemCategory = itemReservation.getJSONObject("itemDto").getString("itemCategory");
            start = itemReservation.getJSONObject("timeSlotDto").getString("startDate") + " " + itemReservation.getJSONObject("timeSlotDto").getString("startTime");
            end = itemReservation.getJSONObject("timeSlotDto").getString("endDate") + " " + itemReservation.getJSONObject("timeSlotDto").getString("endTime");
        } catch (JSONException e) {
            error += e.getMessage();
            refreshMessage();
            return;
        }

        TableRow row = TableLayoutUtils.initializeRow(User_MyItem.this, v -> showDialog(id, itemId));
        myItemReservationTable.addView(row);

        LinearLayout rowVerticalLayout = new LinearLayout(User_MyItem.this);
        rowVerticalLayout.setOrientation(LinearLayout.VERTICAL);
        row.addView(rowVerticalLayout);

        LinearLayout subRow1 = new LinearLayout(User_MyItem.this);
        subRow1.setOrientation(LinearLayout.HORIZONTAL);
        rowVerticalLayout.addView(subRow1);

        LinearLayout subRow2 = new LinearLayout(User_MyItem.this);
        subRow2.setOrientation(LinearLayout.HORIZONTAL);
        rowVerticalLayout.addView(subRow2);

        LinearLayout subRow3 = new LinearLayout(User_MyItem.this);
        subRow3.setOrientation(LinearLayout.HORIZONTAL);
        rowVerticalLayout.addView(subRow3);


        TextView nameAndItemCategoryView = new TextView(User_MyItem.this);
        nameAndItemCategoryView.setTextAppearance(getApplicationContext(), R.style.Base_TextAppearance_AppCompat_Small);
        nameAndItemCategoryView.setText(itemCategory + " — " + name);
        subRow1.addView(nameAndItemCategoryView);

        TextView idView = new TextView(User_MyItem.this);
        idView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        idView.setText("Reservation Id: " + id);
        subRow2.addView(idView);

        TextView startView = new TextView(User_MyItem.this);
        startView.setText(start);
        subRow2.addView(startView);


        TextView itemIdView = new TextView(User_MyItem.this);
        itemIdView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        itemIdView.setText("Item Id: " + itemId);
        subRow3.addView(itemIdView);

        TextView endView = new TextView(User_MyItem.this);
        endView.setText(end);
        subRow3.addView(endView);
    }

    /**
     * Refresh error message
     */
    private void refreshMessage() {
        TextView msgTextView = findViewById(R.id.error);
        msgTextView.setText(error);
    }

    /**
     * Show a dialog, confirm whether to cancel
     */
    public void showDialog(String id, String itemId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //Set title
        builder.setTitle("Cancel Item Reservation");

        //Set content
        builder.setMessage("Do you want to cancel this item reservation?");

        //Set icon
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        //Whether dialog can be closed by clicking elsewhere
        builder.setCancelable(true);

        View view = View.inflate(this, R.layout.user_myitem, null);
        builder.setView(view);

        //right button
        builder.setPositiveButton("No", (dialog, which) -> {
        });

        //left button
        builder.setNegativeButton("Yes", (dialog, which) -> cancelReservation(id, itemId));

        builder.show();
    }

    /**
     * cancel the item reservation
     *
     * @param id     uid
     * @param itemId reserved item id
     */
    public void cancelReservation(String id, String itemId) {
        RequestParams requestParams = new RequestParams();
        requestParams.put("itemReservationId", Integer.valueOf(id));
        requestParams.put("itemId", Integer.valueOf(itemId));
        requestParams.put("pid", this.uid);
        HttpUtils.delete("itemReservations/cancelReservation", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                showSnackbar("Success to cancel");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (statusCode == 200) {
                    showSnackbar("Success to cancel");
                } else {
                    showSnackbar("Fail to cancel");
                }
            }
        });
        refreshTable();
    }

    /**
     * Show snack bar of some content
     *
     * @param content content you want to show in the snake bar
     */
    public void showSnackbar(String content) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.error), content, Snackbar.LENGTH_LONG);
        snackbar.setAction("OK", v -> {

        });
        snackbar.show();
    }

    /**
     * when the cancel operation is done, refresh to get the latest my items.
     */
    private void refreshTable() {
        myItemReservationTable.removeAllViews();
        addAllItemsToTable();
    }
}
