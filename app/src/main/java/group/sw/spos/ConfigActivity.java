package group.sw.spos;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import group.sw.spos.database.DatabaseHelper;
import group.sw.spos.database.UserData;
import group.sw.spos.lib.ApiClient;
import group.sw.spos.lib.GoodItemsList;
import group.sw.spos.response.ConfigResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfigActivity extends AppCompatActivity implements View.OnClickListener{

    private Button continueButton;
    private DatabaseHelper db;
    private EditText sposAddressView, merchantNumberView;
    private View progressLayoutView, progressView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        db = new DatabaseHelper(this);

        progressLayoutView = (FrameLayout) findViewById(R.id.progress_bar_layout);
        progressView = (ProgressBar) progressLayoutView.findViewById(R.id.progress_bar);
        sposAddressView = (EditText) findViewById(R.id.spos_address);
        merchantNumberView = (EditText) findViewById(R.id.merchant_number);
        continueButton = (Button) findViewById(R.id.continue_button);
        continueButton.setEnabled(true);
        continueButton.setOnClickListener(this);

        UserData sposAddress = db.getUserData("sposAddress");
        UserData shopNo = db.getUserData("shopNo");

        if(sposAddress != null && shopNo!= null) {
            Intent loginIntent = new Intent(ConfigActivity.this, LoginActivity.class);
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.continue_button){
            continueButton.setEnabled(false);
            if (check_validation()) {
                String sposAddress = sposAddressView.getText().toString();
                String merchantNumber = merchantNumberView.getText().toString();
                if(!TextUtils.isEmpty(sposAddress) &&  !TextUtils.isEmpty(merchantNumber)){
                    processConfig();
                } else {
                    continueButton.setEnabled(true);
                    Toast.makeText(this, R.string.invalid_request, Toast.LENGTH_SHORT).show();
                }
            } else {
                continueButton.setEnabled(true);
            }

        }
    }

    String removeLastSlash(String url){
        if(url.endsWith("/")) {
            return url.substring(0, url.lastIndexOf("/"));
        } else {
            return url;
        }
    }
    public void processConfig(){
        showProgress(true);
        String sposAddress = sposAddressView.getText().toString();
        String merchantNumber = merchantNumberView.getText().toString();
        String address = this.removeLastSlash(sposAddress);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("sposAddress", address);
        jsonObject.addProperty("shopNo", merchantNumber);

        Call<ConfigResponse> mServiceConfigResponse = ApiClient.getInstance(address).getApi().config(jsonObject);

        mServiceConfigResponse.enqueue(new Callback<ConfigResponse>() {
            @Override
            public void onResponse(Call<ConfigResponse> call, Response<ConfigResponse> response) {
                showProgress(false);
                try {
                    if(response.isSuccessful()){
                        ConfigResponse mGeneralResponse = response.body();
                        int code = response.body().code;
                        if(code == 0){
                            db.deleteAllGoodItems();
                            List<GoodItemsList> goodItemsLists = response.body().goodItems;
                            if(goodItemsLists != null && !goodItemsLists.isEmpty()) {
                                for (GoodItemsList goodItemsList : goodItemsLists) {
                                    db.insertGoodItem(goodItemsList.getGoodname(), goodItemsList.getGoodcode(), goodItemsList.getUnit(), goodItemsList.getLanguage());
                                }
                            }
                            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                            db.insertUserData("lastUpdateTime", date);
                            db.insertUserData("sposAddress", address);
                            db.insertUserData("shopNo", merchantNumber);
                            continueButton.setEnabled(true);
                            Intent languageIntent = new Intent(ConfigActivity.this, LanguageActivity.class);
                            languageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(languageIntent);
                        } else {
                            Toast.makeText(getApplicationContext(), mGeneralResponse.message, Toast.LENGTH_LONG).show();
                            continueButton.setEnabled(true);
                        }
                        continueButton.setEnabled(true);
                    }else {
                        continueButton.setEnabled(true);
                        String s = response.errorBody().string();
                        try {
                            JSONObject jsonObject = new JSONObject(s);
                            Toast.makeText(getApplicationContext(), jsonObject.getString("msg"), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), R.string.failure_wrong_format, Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    continueButton.setEnabled(true);
                    Toast.makeText(getApplicationContext(), R.string.failure_wrong_format, Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ConfigResponse> call, Throwable t) {
                showProgress(false);
                continueButton.setEnabled(true);
                call.cancel();
                Toast.makeText(getApplicationContext(), R.string.failure_error, Toast.LENGTH_LONG).show();
            }
        });

    }

    public boolean check_validation() {
        sposAddressView.setError(null);
        merchantNumberView.setError(null);
        boolean valid = true;
        View focusView = null;
        String sposAddress = sposAddressView.getText().toString();
        String merchantNumber = merchantNumberView.getText().toString();

        if (TextUtils.isEmpty(merchantNumber)) {
            merchantNumberView.setError(getString(R.string.error_merchant_number_can_not_be_empty));
            focusView = merchantNumberView;
            valid = false;
        }

        if(!Patterns.WEB_URL.matcher(sposAddress).matches()){
            sposAddressView.setError(getString(R.string.invalid_url));
            focusView = sposAddressView;
            valid = false;
        }

        if (TextUtils.isEmpty(sposAddress)) {
            sposAddressView.setError(getString(R.string.error_spos_address_can_not_be_empty));
            focusView = sposAddressView;
            valid = false;
        }

        if (!valid) {
            focusView.requestFocus();
        }
        return valid;
    }


    /**
     * Shows the progress UI and hides the login form.
     */
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
