package group.sw.spos;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import group.sw.spos.database.DatabaseHelper;
import group.sw.spos.database.UserData;
import group.sw.spos.lib.BillList;
import group.sw.spos.lib.BillProductList;
import group.sw.spos.lib.BillProductListAdapter;
import group.sw.spos.lib.BillUploadProductListAdapter;

public class BillUploadedDetailActivity extends AppCompatActivity {

    private String billNo, language;
    private BillList bill;
    private DatabaseHelper db;
    private TextView billNoView, billDateView, billStatusView;
    private ArrayList<BillProductList> billProducts = new ArrayList<BillProductList>();
    private ArrayList<BillProductList> changeBillProducts = new ArrayList<BillProductList>();
    private BillUploadProductListAdapter adapter;
    private EditText searchGoodView;
    private ListView goodListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_uploaded_detail);

        db = new DatabaseHelper(this);

        Toolbar app_bar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(app_bar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_left_forward);

        searchGoodView =(EditText) findViewById(R.id.search_good);
        searchGoodView.clearFocus();
        goodListView = (ListView) findViewById(R.id.good_list);
        billNoView = (TextView) findViewById(R.id.bill_no);
        billDateView = (TextView) findViewById(R.id.bill_date);
        billStatusView = (TextView) findViewById(R.id.bill_status);

        UserData languageData = db.getUserData("language");

        if(savedInstanceState  == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                billNo = "";
            }else{
                billNo= extras.getString("billNo");
            }
        } else {
            billNo = (String) savedInstanceState.getSerializable("billNo");
        }

        bill = db.getBillFromNo(billNo);
        if(bill != null){
            billNoView.setText(bill.getNo());
            billDateView.setText(bill.getDatTime());
            billStatusView.setText(bill.getStatus());
        }

        if(languageData != null){
            language = languageData.getValue();
            billProducts = db.getBillProducts(billNo, language);
        }

        adapter = new BillUploadProductListAdapter(BillUploadedDetailActivity.this, billProducts);
        goodListView.setTextFilterEnabled(true);
        goodListView.setAdapter(adapter);


        searchGoodView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
