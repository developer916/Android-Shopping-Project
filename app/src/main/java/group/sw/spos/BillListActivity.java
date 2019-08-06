package group.sw.spos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import group.sw.spos.database.Bills;
import group.sw.spos.database.DatabaseHelper;
import group.sw.spos.lib.BillList;
import group.sw.spos.lib.BillListAdapter;
import group.sw.spos.lib.TokenManager;

public class BillListActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView billSearch;
    private BillListAdapter adapter;
    private ArrayList<BillList> mBills = new ArrayList<>();
    private DatabaseHelper db;
    private ListView billListView;
    private String startDate, endDate;
    private ImageButton deleteButton, clearButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_list);
        db = new DatabaseHelper(this);
        Toolbar app_bar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(app_bar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_left_forward);

        billSearch = (TextView) findViewById(R.id.bill_search_readonly);
        billListView = (ListView) findViewById(R.id.bill_list);
        deleteButton = (ImageButton) findViewById(R.id.delete_button);
        clearButton = (ImageButton) findViewById(R.id.bill_search_readonly_clear_button);
        billSearch.setEnabled(true);
        deleteButton.setEnabled(true);
        clearButton.setEnabled(true);
        deleteButton.setOnClickListener(this);
        billSearch.setOnClickListener(this);
        clearButton.setOnClickListener(this);
        mBills = db.getAllBills();

        adapter = new BillListAdapter(BillListActivity.this, mBills);
        billListView.setTextFilterEnabled(true);
        billListView.setAdapter(adapter);

        billListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BillList item = mBills.get(position);
                if(item.getStatus().equals("Local")){
                    Intent billItemIntent = new Intent(BillListActivity.this, BillDetailActivity.class);
                    billItemIntent.putExtra("billNo", item.getNo());
                    startActivity(billItemIntent);
                } else if(item.getStatus().equals("Uploaded")){
                    Intent billItemIntent = new Intent(BillListActivity.this, BillUploadedDetailActivity.class);
                    billItemIntent.putExtra("billNo", item.getNo());
                    startActivity(billItemIntent);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.delete_button){
            new AlertDialog.Builder(BillListActivity.this)
                .setMessage(R.string.confirm_message_delete_local_data)
                .setPositiveButton(R.string.logout_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.removeBillAndBillProducts();
                        mBills = db.getAllBills();
                        adapter.updateRecords(mBills);
                        adapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton(R.string.logout_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
        } else if(v.getId() == R.id.bill_search_readonly){
            Intent searchIntent = new Intent(this, BillSearchActivity.class);
            startActivityForResult(searchIntent, 2);
        } else if(v.getId() == R.id.bill_search_readonly_clear_button){
            String search = "";
            billSearch.setText(search);
            mBills = db.getAllBills();
            adapter.updateRecords(mBills);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            String search = "";
            startDate =data.getStringExtra("startDate");
            endDate = data.getStringExtra("endDate");
            if(!startDate.equals("") && !endDate.equals("")){
                search = startDate+"~"+endDate;
                billSearch.setText(search);
                String startDateConvert = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(startDate));
                String endDateConvert = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(endDate));

                mBills = db.getSearchBills(startDateConvert, endDateConvert);

            } else {
                mBills = db.getAllBills();
            }
            adapter.updateRecords(mBills);

            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Intent dashboardIntent = new Intent(this, DashboardActivity.class);
        startActivity(dashboardIntent);
    }
}
