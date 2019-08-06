package group.sw.spos;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import group.sw.spos.database.BillProducts;
import group.sw.spos.database.Bills;
import group.sw.spos.database.DatabaseHelper;
import group.sw.spos.database.UserData;
import group.sw.spos.lib.ApiClient;
import group.sw.spos.lib.BillList;
import group.sw.spos.lib.BillProductList;
import group.sw.spos.lib.BillProductListAdapter;
import group.sw.spos.lib.GoodItemsList;
import group.sw.spos.lib.TokenManager;
import group.sw.spos.response.UploadBillResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BillDetailActivity extends AppCompatActivity implements View.OnClickListener{

    private String billNo, language;
    private BillList bill;
    private DatabaseHelper db;
    private TextView billNoView, billDateView, billStatusView;
    private ArrayList<BillProductList> billProducts = new ArrayList<BillProductList>();
    private ArrayList<BillProductList> changeBillProducts = new ArrayList<BillProductList>();
    private BillProductListAdapter adapter;
    private EditText searchGoodView;
    private ListView goodListView;
    private LinearLayout trashLinearlayoutView;
    private ImageButton trashButtonView, uploadButtonView;
    private View progressLayoutView, progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_detail);
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
        trashLinearlayoutView = (LinearLayout) findViewById(R.id.trush_linearlayout);
        trashButtonView = (ImageButton) findViewById(R.id.trash_button);
        uploadButtonView = (ImageButton) findViewById(R.id.upload_button);
        billNoView = (TextView) findViewById(R.id.bill_no);
        billDateView = (TextView) findViewById(R.id.bill_date);
        billStatusView = (TextView) findViewById(R.id.bill_status);
        progressLayoutView = (FrameLayout) findViewById(R.id.progress_bar_layout);
        progressView = (ProgressBar) progressLayoutView.findViewById(R.id.progress_bar);
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

        adapter = new BillProductListAdapter(BillDetailActivity.this, billProducts);
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


        goodListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                long viewId = view.getId();
                if(viewId == R.id.edit_button){
                    if(id !=0){
                        BillProductList  billProduct = db.getBillProduct(id);
                        int positionInt = position;
                        final Dialog dialog = new Dialog(BillDetailActivity.this);
                        dialog.setContentView(R.layout.modify_quantity_form);
                        final TextView dialogGoodsName = (TextView) dialog.findViewById(R.id.modal_good_name);
                        final TextView dialogGoodsCode = (TextView) dialog.findViewById(R.id.modal_good_code);
                        final EditText dialogQuantity = (EditText) dialog.findViewById(R.id.modal_quantity);
                        dialogQuantity.setText(String.valueOf(billProduct.getQuantity()));
                        dialogGoodsCode.setText(billProduct.getCode());
                        dialogGoodsName.setText(billProduct.getName());

                        TextView btnModify = (TextView) dialog.findViewById(R.id.modal_modify);
                        TextView btnCancel = (TextView) dialog.findViewById(R.id.modal_cancel);
                        int pos = dialogQuantity.getText().length();
                        dialogQuantity.setSelection(pos);

                        dialog.show();

                        btnModify.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int goodQuantity;
                                String goodQuantityString = dialogQuantity.getText().toString();
                                try{
                                    goodQuantity =Integer.parseInt(dialogQuantity.getText().toString()) ;
                                } catch (Exception e){
                                    goodQuantity = 0;
                                }
                                if (!TextUtils.isEmpty(goodQuantityString)) {
                                    db.updateBillProduct(billProduct.getId(), goodQuantity);
                                    changeBillProducts = db.getBillProducts(billNo, language);
                                    for(int i=0; i<changeBillProducts.size(); i++){
                                        for(int j =0; j<billProducts.size(); j++){
                                            if(changeBillProducts.get(i).getId() == billProducts.get(j).getId()){
                                                changeBillProducts.get(i).setIsclicked(billProducts.get(j).isIsclicked());
                                                break;
                                            }
                                        }
                                    }
                                    String searchGoodViewString = searchGoodView.getText().toString();
                                    if(!TextUtils.isEmpty(searchGoodViewString)){
                                        searchGoodView.getText().clear();
                                        adapter.updateRecords(changeBillProducts);
                                        searchGoodView.setText(searchGoodViewString);
                                        int pos = searchGoodView.getText().length();
                                        searchGoodView.setSelection(pos);
                                    } else {
                                        adapter.updateRecords(changeBillProducts);
                                    }
                                    billProducts= changeBillProducts;
                                    dialog.hide();
                                }
                            }
                        });

                        btnCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.hide();
                            }
                        });
                    }


                } else if(viewId == R.id.checkbox){
                    int size = billProducts.size();
                    changeBillProducts = new ArrayList<BillProductList>();
                    int checkValue = 0;
                    if(id !=0){
                        for(int i=0;i<size;i++)
                        {
                            BillProductList billProduct = billProducts.get(i);
                            if(billProduct.getId() == id){
                                boolean value =billProduct.isIsclicked();
                                if (value){
                                    billProduct.setIsclicked(false);
                                } else{
                                    billProduct.setIsclicked(true);
                                }
                            }
                            if(billProduct.isIsclicked()){
                                checkValue = checkValue + 1;
                            }
                        }

                        String searchGoodViewString = searchGoodView.getText().toString();
                        if(!TextUtils.isEmpty(searchGoodViewString)){
                            searchGoodView.getText().clear();
                            adapter.updateRecords(billProducts);
                            searchGoodView.setText(searchGoodViewString);
                            int pos = searchGoodView.getText().length();
                            searchGoodView.setSelection(pos);
                        } else {
                            adapter.updateRecords(billProducts);
                        }
                    }


                    if(checkValue >0 ){
                        trashLinearlayoutView.setVisibility(View.VISIBLE);
                    } else {
                        trashLinearlayoutView.setVisibility(View.GONE);
                    }
                }
            }
        });
        trashButtonView.setEnabled(true);
        trashButtonView.setOnClickListener(this);
        uploadButtonView.setEnabled(true);
        uploadButtonView.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.trash_button){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(BillDetailActivity.this);
            alertDialog.setMessage(R.string.remove_selected_goods)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int size = billProducts.size();
                        for(int i=0;i<size;i++){
                            BillProductList billProduct = billProducts.get(i);
                            if(billProduct.isIsclicked()){
                                db.deleteBillProduct(billProduct);
                            }
                        }
                        billProducts = db.getBillProducts(billNo, language);
                        String searchGoodViewString = searchGoodView.getText().toString();
                        if(!TextUtils.isEmpty(searchGoodViewString)){
                            searchGoodView.getText().clear();
                            adapter.updateRecords(billProducts);
                            searchGoodView.setText(searchGoodViewString);
                            int pos = searchGoodView.getText().length();
                            searchGoodView.setSelection(pos);
                        } else {
                            adapter.updateRecords(billProducts);
                        }
                        trashLinearlayoutView.setVisibility(View.GONE);
                        dialog.cancel();
                    }
                })
                .setNegativeButton(R.string.logout_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
            AlertDialog alert = alertDialog.create();
            alert.show();

        } else if(v.getId() == R.id.upload_button){
            AlertDialog.Builder alertDialogUpload = new AlertDialog.Builder(BillDetailActivity.this);
            alertDialogUpload.setMessage(R.string.upload_confirm_bill)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        uploadBill();
                    }
                })
                .setNegativeButton(R.string.logout_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
            AlertDialog alert = alertDialogUpload.create();
            alert.show();
        }
    }


    public void uploadBill(){
        uploadButtonView.setEnabled(false);
        showProgress(true);
        UserData sposAddressData = db.getUserData("sposAddress");
        if(sposAddressData != null){
            String sposAddress = sposAddressData.getValue();
            String shopNo = db.getUserData("shopNo").getValue();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("shopNo", shopNo);
            jsonObject.addProperty("token", TokenManager.getInstance(this).getToken().getToken());
            jsonObject.addProperty("billNo", billNo);
            jsonObject.addProperty("warehouse", bill.getWarehouse());

            billProducts = db.getBillProducts(billNo, language);
            int size = billProducts.size();
            JsonArray bills =   new JsonArray();
            for (int i=0; i<size ; i++){
                BillProductList billProduct = billProducts.get(i);
                JsonObject billJson = new JsonObject();
                billJson.addProperty("goodCode", billProduct.getCode());
                billJson.addProperty("quantity", billProduct.getQuantity());
                billJson.addProperty("unit", billProduct.getUnit());
                billJson.addProperty("timestamp", billProduct.getCreateDate());
                bills.add(billJson);
            }
            jsonObject.add("bills", bills);

            Log.d("string", jsonObject.toString());

            Call<UploadBillResponse> mServiceUploadBill = ApiClient.getInstance(sposAddress).getApi().uploadBill(jsonObject);
            mServiceUploadBill.enqueue(new Callback<UploadBillResponse>() {
                @Override
                public void onResponse(Call<UploadBillResponse> call, Response<UploadBillResponse> response) {
                    showProgress(false);
                    try {
                        if (response.isSuccessful()) {
                            UploadBillResponse  mServiceResponse = response.body();
                            int code = mServiceResponse.getCode();
                            if(code == 0) {
                                Toast.makeText(getApplicationContext(), mServiceResponse.getMessage(), Toast.LENGTH_LONG).show();
                                db.updateBill(billNo,"Uploaded");
                                uploadButtonView.setEnabled(true);
                                Intent newIntent = new Intent(BillDetailActivity.this, BillListActivity.class);
                                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(newIntent);
                            } else {
                                Toast.makeText(getApplicationContext(), mServiceResponse.getMessage(), Toast.LENGTH_LONG).show();
                                uploadButtonView.setEnabled(true);
                            }
                        } else {
                            String s = response.errorBody().string();
                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                Toast.makeText(getApplicationContext(), jsonObject.getString("msg"), Toast.LENGTH_LONG).show();
                            } catch (JSONException e) {
                                Toast.makeText(getApplicationContext(), R.string.failure_wrong_format, Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    }catch (IOException e){
                        Toast.makeText(getApplicationContext(), R.string.failure_wrong_format, Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<UploadBillResponse> call, Throwable t) {
                    showProgress(false);
                    call.cancel();
                    Toast.makeText(getApplicationContext(), R.string.failure_error, Toast.LENGTH_LONG).show();
                }
            });


        } else {
            showProgress(false);
            uploadButtonView.setEnabled(true);
            Toast.makeText(this, R.string.invalid_request, Toast.LENGTH_SHORT).show();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            progressLayoutView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            progressLayoutView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
