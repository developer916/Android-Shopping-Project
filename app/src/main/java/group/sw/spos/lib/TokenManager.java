package group.sw.spos.lib;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;

public class TokenManager {

    private static final String SHARED_PREF_NAME = "my_shared_preff";

    private static TokenManager mInstance;
    private Context mCtx;


    private TokenManager(Context mCtx) {
        this.mCtx = mCtx;
    }

    public static synchronized TokenManager getInstance(Context mCtx) {
        if (mInstance == null) {
            mInstance = new TokenManager(mCtx);
        }
        return mInstance;
    }

    public void saveToken(AccessToken token) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Token", token.getToken());
        editor.apply();
    }

    public boolean isLoggedIn() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String savedToken = sharedPreferences.getString("Token", null);
        if (savedToken != null) {
            return true;
        } else {
            return false;
        }
    }

    public AccessToken getToken() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String savedToken = sharedPreferences.getString("Token", null);
        AccessToken token = new AccessToken();
        token.setToken(savedToken);
        return token;
    }

    public void clear() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public String getLanguage() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String savedLanguage = sharedPreferences.getString("Language", "English");
        return savedLanguage;
    }

    public void setLanguage(String language) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Language", language);
        editor.apply();
    }

    public void clearLanguage() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Language", null);
        editor.apply();
    }

    public void setBillNo(String billNo){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("BillNo", billNo);
        editor.apply();
    }

    public String getBillNo(){
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String savedBillNo = sharedPreferences.getString("BillNo", null);
        return savedBillNo;
    }

    public void clearBillNo() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("BillNo", null);
        editor.apply();
    }
}
