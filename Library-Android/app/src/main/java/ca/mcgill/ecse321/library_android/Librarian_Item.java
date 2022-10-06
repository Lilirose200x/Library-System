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

import cz.msebera.android.httpclient.Header;

/**
 * librarian item management page control
 */
public class Librarian_Item extends AppCompatActivity {

    //link with UI units
    private TableLayout librarianItems;
    private String error = null;
    private TextView ItemName;
    private TextView ItemCategory;
    private TextView ItemId;

    /**
     * default functions
     *
     * @param savedInstanceState state
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.librarian_item);

        refreshErrorMessage();
        ItemName = findViewById(R.id.newItem_name);
        ItemCategory = findViewById(R.id.newItem_Category);
        ItemId = findViewById(R.id.ItemId);
        librarianItems = findViewById(R.id.librarian_ItemTable);

        librarianItems.setStretchAllColumns(true);
        addAllItemsToTable();
        findViewById(R.id.addItem).setOnClickListener(view -> {
            createItem(ItemName.getText().toString(), ItemCategory.getText().toString());
            ItemName.setText("");
            ItemCategory.setText("");
        });
        findViewById(R.id.ItemDeletButton).setOnClickListener(v -> {
            deleteItem(ItemId.getText().toString());
            librarianItems.removeAllViews();
            addAllItemsToTable();
            ItemId.setText("");
        });
    }

    /**
     * Create an item with the input parameters
     * @param ItemName     new item name
     * @param ItemCategory new item category
     */
    private void createItem(String ItemName, String ItemCategory) {
        error = "";
        RequestParams requestParams = new RequestParams();
        requestParams.add("name", ItemName);
        requestParams.add("itemCategory", ItemCategory);
        HttpUtils.post("items/createItem", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                error = "";
                refreshErrorMessage();
                showSnackBar("Success!");
                refreshTable();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                error += "Fail to create an item";
                refreshErrorMessage();
            }
        });
    }

    /**
     * Delete item by Id
     *
     * @param Id item id
     */
    private void deleteItem(String Id) {
        error = "";
        RequestParams requestParams = new RequestParams();
        requestParams.add("id", Id);
        HttpUtils.delete("items/deleteItem", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                error = "";
                refreshErrorMessage();
                librarianItems = findViewById(R.id.librarian_ItemTable);
                refreshTable();
                showSnackBar("Item Deleted");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                error = "   Fail to Delete an item";
                refreshErrorMessage();
                showSnackBar("Fail to Delete an item");
            }
        });
    }

    /**
     * refresh the error message
     */
    private void refreshErrorMessage() {
        // set the error message
        TextView tvError = findViewById(R.id.error2);

        tvError.setText(error);

        if (error == null || error.length() == 0) {
            tvError.setVisibility(View.GONE);
        } else {
            tvError.setVisibility(View.VISIBLE);
        }
    }

    /**
     * browse all the items
     */
    private void addAllItemsToTable() {
        error = "";
        HttpUtils.get("items/itemList", new RequestParams(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                int numEvent = response.length();
                for (int i = 0; i < numEvent; i++) {
                    try {
                        JSONObject item = response.getJSONObject(i);
                        addItemToTable(item);
                    } catch (Exception e) {
                        error = e.getMessage();
                    }
                }
                refreshErrorMessage();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                error = "Fail to load item info!";
                refreshErrorMessage();
            }
        });

    }

    /**
     * add the event DTO in the response to the show list
     *
     * @param item item
     */
    private void addItemToTable(JSONObject item) {
        error = "";
        final String id, name, itemCategory;
        try {
            id = item.getString("id");
            name = item.getString("name");
            itemCategory = item.getString("itemCategory");
        } catch (JSONException e) {
            error += e.getMessage();
            refreshErrorMessage();
            return;
        }
        TableRow row = TableLayoutUtils.initializeRow(Librarian_Item.this, v -> {

        });
        librarianItems.addView(row);
        LinearLayout rowVerticalLayout = new LinearLayout(Librarian_Item.this);
        rowVerticalLayout.setOrientation(LinearLayout.VERTICAL);
        row.addView(rowVerticalLayout);

        LinearLayout subRow1 = new LinearLayout(Librarian_Item.this);
        subRow1.setOrientation(LinearLayout.HORIZONTAL);
        rowVerticalLayout.addView(subRow1);

        LinearLayout subRow2 = new LinearLayout(Librarian_Item.this);
        subRow2.setOrientation(LinearLayout.HORIZONTAL);
        rowVerticalLayout.addView(subRow2);

        TextView nameView = new TextView(Librarian_Item.this);
        nameView.setTextAppearance(getApplicationContext(), R.style.Base_TextAppearance_AppCompat_Small);
        // Set TextView style -> https://www.tutorialspoint.com/how-to-change-a-textview-style-at-runtime-in-android
        nameView.setText(name);
        subRow1.addView(nameView);

        TextView idView = new TextView(Librarian_Item.this);
        idView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        // weight 1 to fill the remaining space thus right-aligning status
        // -> https://stackoverflow.com/questions/4305564/android-layout-right-align
        idView.setText(id);
        subRow2.addView(idView);

        TextView itemCategoryView = new TextView(Librarian_Item.this);
        itemCategoryView.setText(itemCategory);
        subRow2.addView(itemCategoryView);
    }


    /**
     * Show snack bar of some content
     *
     * @param content content you want to show in the snake bar
     */
    public void showSnackBar(String content) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.error2), content, Snackbar.LENGTH_LONG);
        snackbar.setAction("OK", v -> {

        });
        snackbar.show();
    }

    private void refreshTable() {
        librarianItems.removeAllViews();
        addAllItemsToTable();
    }


}
