package group.sw.spos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.DialogInterface;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import group.sw.spos.database.DatabaseHelper;
import group.sw.spos.database.Products;
import group.sw.spos.database.UserData;
import group.sw.spos.lib.TokenManager;

public class CountQuantityActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView goodNameView, unitView;
    private EditText quantityView;
    private Button saveAndContinueButton, retryButton;
    private ImageButton uploadButton;
    private String goodCode, language;
    private DatabaseHelper db;
    private Products goodItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_quantity);
        Toolbar app_bar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(app_bar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_left_forward);

        db = new DatabaseHelper(this);
        uploadButton = (ImageButton) findViewById(R.id.upload_button);
        quantityView = (EditText) findViewById(R.id.quantity);
        goodNameView = (TextView) findViewById(R.id.good_name);
        unitView = (TextView) findViewById(R.id.unit);
        quantityView.requestFocus();
        saveAndContinueButton = (Button) findViewById(R.id.saveAndContinueBtn);
        retryButton = (Button) findViewById(R.id.retryBtn);
        saveAndContinueButton.setOnClickListener(this);
        retryButton.setOnClickListener(this);
        uploadButton.setEnabled(true);
        uploadButton.setOnClickListener(this);

        if(savedInstanceState  == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                goodCode = "";
            }else{
                goodCode= extras.getString("goodCode");
            }
        } else {
            goodCode = (String) savedInstanceState.getSerializable("goodCode");
        }
        Log.d("good_code", goodCode);
        UserData languageUserData = db.getUserData("language");
        if(languageUserData != null){
            language = languageUserData.getValue();
        } else if(TokenManager.getInstance(this).getLanguage() != null){
            language = TokenManager.getInstance(this).getLanguage();
        } else {
            language = "en";
        }
        if(goodCode != ""){
            goodItem = db.getProduct(language, goodCode);
            goodNameView.setText(goodItem.getName());
            unitView.setText(goodItem.getUnit());
        } else {
            Intent countSearchActivity = new Intent(CountQuantityActivity.this, CountScanActivity.class);
            startActivity(countSearchActivity);
        }

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.retryBtn){
            Intent countScanIntent = new Intent(this, CountScanActivity.class);
            startActivity(countScanIntent);
        } else if(v.getId() == R.id.saveAndContinueBtn){
            saveAndContinueButton.setEnabled(false);
            if(check_validation()) {
                String BillNo = TokenManager.getInstance(this).getBillNo();
                int quantity = Integer.parseInt(quantityView.getText().toString());
                String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                db.insertBillProduct(BillNo, goodCode, quantity, goodItem.getUnit(), dateTime);
                Intent countScanIntent = new Intent(this, CountScanActivity.class);
                startActivity(countScanIntent);
            } else {
                saveAndContinueButton.setEnabled(true);
            }

        } else if(v.getId() == R.id.upload_button){
            new AlertDialog.Builder(this)
                .setMessage(R.string.count_upload_message)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String BillNo = TokenManager.getInstance(CountQuantityActivity.this).getBillNo();
                        String quantityString = quantityView.getText().toString();
                        if (!TextUtils.isEmpty(quantityString)) {
                            int quantity = Integer.parseInt(quantityString);
                            String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                            db.insertBillProduct(BillNo, goodCode, quantity, goodItem.getUnit(), dateTime);
                        }
                        db.updateBill(TokenManager.getInstance(CountQuantityActivity.this).getBillNo(), "Local");
                        TokenManager.getInstance(CountQuantityActivity.this).clearBillNo();
                        Intent newIntent = new Intent( CountQuantityActivity.this, BillListActivity.class);
                        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(newIntent);
                    }
                })
                .setNegativeButton(R.string.logout_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    public boolean check_validation() {
        quantityView.setError(null);
        boolean valid = true;
        View focusView = null;
        String quantity = quantityView.getText().toString();

        if (TextUtils.isEmpty(quantity)) {
            quantityView.setError(getString(R.string.error_field_required));
            focusView = quantityView;
            valid = false;
        }

        if (!valid) {
            focusView.requestFocus();
        }
        return valid;
    }
}
