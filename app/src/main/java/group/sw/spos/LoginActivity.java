package group.sw.spos;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
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
import group.sw.spos.lib.AccessToken;
import group.sw.spos.lib.ApiClient;
import group.sw.spos.lib.GoodItemsList;
import group.sw.spos.lib.TokenManager;
import group.sw.spos.lib.WareHouseItemsList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity  implements View.OnClickListener{

    private DatabaseHelper db;
    private EditText userNameView, passwordView;
    private Button loginButton;
    private View progressLayoutView, progressView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseHelper(this);
        UserData languageData = db.getUserData("language");
        if(languageData != null){
            Configuration conf = getResources().getConfiguration();

            String language = "en";
            String databaseLanguage = languageData.getValue();
            if(databaseLanguage.equals("zh-cn")){
//                language = "zh;
//                conf.locale = new Locale("true", "zh", "CN");
                conf.locale = Locale.SIMPLIFIED_CHINESE;
            } else if(databaseLanguage.equals("km")){
                conf.locale = new Locale(databaseLanguage);
            } else if(databaseLanguage.equals("en")){
                conf.locale = new Locale(databaseLanguage);
            }
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            Resources resources = new Resources(getAssets(), metrics, conf);
        }


        setContentView(R.layout.activity_login);



        progressLayoutView = (FrameLayout) findViewById(R.id.progress_bar_layout);
        progressView = (ProgressBar) progressLayoutView.findViewById(R.id.progress_bar);
        loginButton = (Button) findViewById(R.id.login_in_button);
        userNameView = (EditText) findViewById(R.id.user_name);
        passwordView = (EditText) findViewById(R.id.password);
        loginButton.setEnabled(true);
        loginButton.setOnClickListener(this);


    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.login_in_button){
            loginButton.setEnabled(false);
            if (check_validation()) {
                String userName = userNameView.getText().toString();
                String password = passwordView.getText().toString();
                if(!TextUtils.isEmpty(userName) &&  !TextUtils.isEmpty(password)){
                    processLogin();
                } else {
                    loginButton.setEnabled(true);
                    Toast.makeText(this, R.string.invalid_request, Toast.LENGTH_SHORT).show();
                }
            } else {
                loginButton.setEnabled(true);
            }
        }
    }

    public void processLogin(){
        showProgress(true);
        String userName = userNameView.getText().toString();
        String password = passwordView.getText().toString();
        UserData sposAddressData = db.getUserData("sposAddress");
        if(sposAddressData != null){
            String sposAddress = sposAddressData.getValue();
            String shopNo = db.getUserData("shopNo").getValue();
            String lastUpdateTime = db.getUserData("lastUpdateTime").getValue();

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("user", userName);
            jsonObject.addProperty("password", password);
            jsonObject.addProperty("shopNo", shopNo);
            jsonObject.addProperty("lastUpdateTime", lastUpdateTime);

            Call<AccessToken> mServiceLogin = ApiClient.getInstance(sposAddress).getApi().login(jsonObject);
            mServiceLogin.enqueue(new Callback<AccessToken>() {
                @Override
                public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                    showProgress(false);

                    try {
                        if(response.isSuccessful()){
                            AccessToken mServiceAccessToken = response.body();
                            int code = response.body().code;
                            if(code == 0) {
                                TokenManager.getInstance(LoginActivity.this).saveToken(mServiceAccessToken);
                                db.removePendingBillsAndProducts();
                                List<GoodItemsList> goodItemsLists = response.body().goodItems;
                                if(goodItemsLists != null && !goodItemsLists.isEmpty()){
                                    db.deleteAllGoodItems();
                                    for (GoodItemsList goodItemsList : goodItemsLists) {
                                        db.insertGoodItem(goodItemsList.getGoodname(), goodItemsList.getGoodcode(), goodItemsList.getUnit(), goodItemsList.getLanguage());
                                    }
                                }
                                String lastDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                                db.insertUserData("lastUpdateTime", lastDate);

                                List<WareHouseItemsList> wareHouseItemsLists = mServiceAccessToken.warehouseItems;
                                if(wareHouseItemsLists != null && !wareHouseItemsLists.isEmpty()){
                                    db.deleteAllWareHouse();
                                    for (WareHouseItemsList wareHouseItemsList : wareHouseItemsLists) {
                                        db.insertWareHouse(wareHouseItemsList.getWarehouse(), wareHouseItemsList.getName());
                                    }
                                }
                                String date = new SimpleDateFormat("MM/dd/yyyyy HH:mm:ss", Locale.getDefault()).format(new Date());
                                db.insertUpdateLoginData(mServiceAccessToken.shopName, userName, date);
                                loginButton.setEnabled(true);
                                Intent dashboardIntent = new Intent(LoginActivity.this, DashboardActivity.class);
                                dashboardIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(dashboardIntent);

                            } else {
                                Toast.makeText(getApplicationContext(), mServiceAccessToken.message, Toast.LENGTH_LONG).show();
                                loginButton.setEnabled(true);
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
                    }catch (IOException e) {
                        Toast.makeText(getApplicationContext(), R.string.failure_wrong_format, Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<AccessToken> call, Throwable t) {
                    showProgress(false);
                    call.cancel();
                    Toast.makeText(getApplicationContext(), R.string.failure_error, Toast.LENGTH_LONG).show();
                }
            });

        } else {
            showProgress(false);
            loginButton.setEnabled(true);
            Toast.makeText(this, R.string.invalid_request, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean check_validation() {
        userNameView.setError(null);
        passwordView.setError(null);
        boolean valid = true;
        View focusView = null;
        String userName = userNameView.getText().toString();
        String password = passwordView.getText().toString();

        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_password_can_not_be_empty));
            focusView = passwordView;
            valid = false;
        }

        if (TextUtils.isEmpty(userName)) {
            userNameView.setError(getString(R.string.error_user_name_can_not_be_empty));
            focusView = userNameView;
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
