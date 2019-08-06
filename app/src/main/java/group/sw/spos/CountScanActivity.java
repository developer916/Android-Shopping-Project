package group.sw.spos;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import group.sw.spos.database.BillProducts;
import group.sw.spos.database.DatabaseHelper;
import group.sw.spos.database.Products;
import group.sw.spos.database.UserData;
import group.sw.spos.lib.BillProductList;
import group.sw.spos.lib.TokenManager;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class CountScanActivity extends AppCompatActivity implements View.OnClickListener, ZXingScannerView.ResultHandler {

    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private static final int PICK_FROM_GALLERY = 1958;
    private ZXingScannerView qrCodeScanner;
    private String rawQrCodeData;
    private static CountScanActivity instance;
    private DatabaseHelper db;
    private Products goodItem;
    private String language;
    private ImageButton uploadButton;
    private int countBillProducts =0;

    public static CountScanActivity instance() {
        if (instance != null)
            return instance;
        throw new RuntimeException("Count scan activity  is not instantiated yet");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_scan);
        db = new DatabaseHelper(this);
        Toolbar app_bar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(app_bar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_left_forward);
        uploadButton = (ImageButton) findViewById(R.id.upload_button);
        uploadButton.setEnabled(true);
        uploadButton.setOnClickListener(this);

        UserData languageUserData = db.getUserData("language");
        if(languageUserData != null){
            language = languageUserData.getValue();
        } else {
            language = "en";
        }
        String billNo = TokenManager.getInstance(CountScanActivity.this).getBillNo();
        ArrayList<BillProductList> billProductList = db.getBillProducts(billNo, language);
        if(billProductList.size() >0 ){
            uploadButton.setVisibility(View.VISIBLE);
            countBillProducts = 1;
        } else {
            uploadButton.setVisibility(View.GONE);
            countBillProducts = 0;
        }
        qrCodeScanner = (ZXingScannerView) findViewById(R.id.qrCodeScanner);

        setScannerProperties();
        rawQrCodeData = null;

        instance = this;
    }

    private void setScannerProperties() {
        List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
        formats.add(BarcodeFormat.QR_CODE);
        formats.add(BarcodeFormat.UPC_A);
        formats.add(BarcodeFormat.UPC_E);
        formats.add(BarcodeFormat.EAN_13);
        formats.add(BarcodeFormat.EAN_8);
        formats.add(BarcodeFormat.RSS_14);
        formats.add(BarcodeFormat.CODE_39);
        formats.add(BarcodeFormat.CODE_93);
        formats.add(BarcodeFormat.CODE_128);
        formats.add(BarcodeFormat.ITF);
        formats.add(BarcodeFormat.CODABAR);
        formats.add(BarcodeFormat.DATA_MATRIX);
        formats.add(BarcodeFormat.PDF_417);


        qrCodeScanner.setFormats(formats);
        qrCodeScanner.setAutoFocus(true);
        if (Build.MANUFACTURER.equalsIgnoreCase("HUAWEI")) {
            qrCodeScanner.setAspectTolerance(0.5f);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (TokenManager.getInstance(this).isLoggedIn() == false) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            qrCodeScanner.setResultHandler(this); // Register ourselves as a handler for scan results.
            qrCodeScanner.startCamera();          // Start camera on resume
        } else {
            requestCameraPermission();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        qrCodeScanner.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        rawQrCodeData = rawResult.getText();
        Log.d("string", rawQrCodeData);
        if (rawQrCodeData == null) {
            Toast.makeText(this, R.string.scan_error_invalid_qr_code, Toast.LENGTH_SHORT).show();
            resume_scanning();
        } else {
            processScanning();
        }
    }

    /* ==================================== */
    // Handles the requesting of the camera permission.
    private void requestCameraPermission() {

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);

    }


    public void processScanning() {
        goodItem = db.getProduct(language, rawQrCodeData);
        if(goodItem != null){
            Intent countQuantityActivity = new Intent(CountScanActivity.this, CountQuantityActivity.class);
            countQuantityActivity.putExtra("goodCode", rawQrCodeData);
            startActivity(countQuantityActivity);
        } else {
            Toast.makeText(getApplicationContext(), R.string.error_scan, Toast.LENGTH_LONG).show();
            resume_scanning();
        }

    }

    public void resume_scanning() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                qrCodeScanner.resumeCameraPreview(CountScanActivity.this);
            }
        }, 2000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // we have permission, so create the camerasource
            qrCodeScanner.setResultHandler(CountScanActivity.this); // Register ourselves as a handler for scan results.
            qrCodeScanner.startCamera();          // Start camera on resume
            return;
        }

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(Html.fromHtml(getResources().getString(R.string.app_name)))
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.logout_ok, listener)
                .show();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(countBillProducts == 0){
            Intent dashboardIntent = new Intent(this, DashboardActivity.class);
            startActivity(dashboardIntent);
        } else {
            new AlertDialog.Builder(this)
                .setMessage(R.string.redirect_dashboard_message)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent newIntent = new Intent( CountScanActivity.this, DashboardActivity.class);
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
    public void onClick(View v) {
        if(v.getId() == R.id.upload_button){
            new AlertDialog.Builder(this)
                .setMessage(R.string.count_upload_message)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.updateBill(TokenManager.getInstance(CountScanActivity.this).getBillNo(), "Local");
                        TokenManager.getInstance(CountScanActivity.this).clearBillNo();
                        Intent newIntent = new Intent( CountScanActivity.this, BillListActivity.class);
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
}
