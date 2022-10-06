package ca.mcgill.ecse321.library_android;

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

import java.sql.Date;
import java.sql.Time;

import cz.msebera.android.httpclient.Header;

/**
 * user borrow item page control
 */
public class User_Item extends AppCompatActivity {
    private String error;
    private TableLayout itemTable;
    private int uid = -1;

    /**
     * default functions
     *
     * @param savedInstanceState state
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_item);
        uid = getIntent().getIntExtra("uid", -1);

        //      TableLayout displays items
        itemTable = findViewById(R.id.user_itemTable);
        itemTable.setStretchAllColumns(true);
        addAllItemsToTable();

    }

    /**
     * reserve item
     *
     * @param v view
     */
    public void reserveItem(View v) {
        error = "";
        RequestParams requestParams = new RequestParams();
        final TextView itemId = findViewById(R.id.user_reserveItem_Id);
        requestParams.put("pid", this.uid);
        requestParams.put("itemId", itemId.getText().toString());
        requestParams.put("startDate", new Date(System.currentTimeMillis()));
        requestParams.put("startTime", new Time(System.currentTimeMillis()));
        HttpUtils.post("itemReservations/checkout", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    System.out.println("Success!");
                    showSnackBar("Reserve " + response.getJSONObject("itemDto").getString("name") + " Successfully!");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                showSnackBar("Fail to reserve!");
            }
        });

    }

    /**
     * Add all items to the TableLayout of items in UI
     */
    private void addAllItemsToTable() {
        HttpUtils.get("items/itemList", new RequestParams(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                int numItems = response.length();
                for (int i = 0; i < numItems; i++) {
                    try {
                        JSONObject item = response.getJSONObject(i);
                        addItemToTable(item);
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
     * Add an item to the table, which is defined by a row in the table
     *
     * @param item JSON obeject of the item we want to add to the table
     */
    private void addItemToTable(JSONObject item) {
        final String id, name, itemCategory;
        try {
            id = item.getString("id");
            name = item.getString("name");
            itemCategory = item.getString("itemCategory");
        } catch (JSONException e) {
            error += e.getMessage();
            refreshMessage();
            return;
        }

        TableRow row = TableLayoutUtils.initializeRow(User_Item.this, v -> {

        });
        itemTable.addView(row);


        LinearLayout rowVerticalLayout = new LinearLayout(User_Item.this);
        rowVerticalLayout.setOrientation(LinearLayout.VERTICAL);
        row.addView(rowVerticalLayout);

        LinearLayout subRow1 = new LinearLayout(User_Item.this);
        subRow1.setOrientation(LinearLayout.HORIZONTAL);
        rowVerticalLayout.addView(subRow1);

        LinearLayout subRow2 = new LinearLayout(User_Item.this);
        subRow2.setOrientation(LinearLayout.HORIZONTAL);
        rowVerticalLayout.addView(subRow2);

        TextView nameView = new TextView(User_Item.this);
        nameView.setTextAppearance(getApplicationContext(), R.style.Base_TextAppearance_AppCompat_Small);
        // Set TextView style -> https://www.tutorialspoint.com/how-to-change-a-textview-style-at-runtime-in-android
        nameView.setText(name);
        subRow1.addView(nameView);

        TextView idView = new TextView(User_Item.this);
        idView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        // weight 1 to fill the remaining space thus right-aligning status
        // -> https://stackoverflow.com/questions/4305564/android-layout-right-align
        idView.setText(id);
        subRow2.addView(idView);

        TextView itemCategoryView = new TextView(User_Item.this);
        itemCategoryView.setText(itemCategory);
        subRow2.addView(itemCategoryView);
    }

    /**
     * Refresh error message
     */
    private void refreshMessage() {
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
        snackbar.setAction("OK", v -> {

        });
        snackbar.show();
    }
}
