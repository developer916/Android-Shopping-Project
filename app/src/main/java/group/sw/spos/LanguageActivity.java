package group.sw.spos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioGroup;

import group.sw.spos.database.DatabaseHelper;
import group.sw.spos.database.LoginData;
import group.sw.spos.database.UserData;
import group.sw.spos.lib.TokenManager;

public class LanguageActivity extends AppCompatActivity {
    private RadioGroup languageRadioGroup;
    private int mCheckedId = -1;
    private String selectLanguage;
    private boolean isChecking = true;
    private DatabaseHelper db;
    private String selectedLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        db = new DatabaseHelper(this);

        languageRadioGroup = (RadioGroup) findViewById(R.id.language_radio_group);

//        UserData userDataLanguage = db.getUserData("language");
//        if (userDataLanguage != null) {
//            selectedLanguage = userDataLanguage.getValue();
//
//            if(selectedLanguage.equals("English")){
//                languageRadioGroup.check(R.id.english);
//                mCheckedId = R.id.english;
//            } else if(selectedLanguage.equals("Chinese")){
//                languageRadioGroup.check(R.id.chinese);
//                mCheckedId = R.id.chinese;
//            } else if(selectedLanguage.equals("Khmer")){
//                languageRadioGroup.check(R.id.khmer);
//                mCheckedId = R.id.khmer;
//            }
//            selectLanguage = selectedLanguage;
//        } else {
//            languageRadioGroup.check(R.id.english);
//            mCheckedId = R.id.english;
//            selectLanguage = "English";
//        }

        languageRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId != -1 && isChecking) {
                    isChecking = false;
                    mCheckedId = checkedId;

                    if(mCheckedId != -1) {
                        if(mCheckedId == R.id.english){
//                            selectLanguage = "English";
                            selectLanguage="en";
                        } else if(mCheckedId == R.id.chinese){
//                            selectLanguage = "Chinese";
                            selectLanguage="zh-cn";
                        } else if(mCheckedId == R.id.khmer){
//                            selectLanguage = "Khmer";
                            selectLanguage = "km";
                        }
                        db.insertUserData("language", selectLanguage);
                        TokenManager.getInstance(LanguageActivity.this).setLanguage(selectedLanguage);
                        if (TokenManager.getInstance(LanguageActivity.this).isLoggedIn() == true) {
                            LoginData loginData = db.getLoginData();
                            if(loginData !=null){
                                Intent dashboardIntent = new Intent(LanguageActivity.this, DashboardActivity.class);
                                dashboardIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(dashboardIntent);
                            }else {
                                Intent redirectIntent = new Intent(LanguageActivity.this, LoginActivity.class);
                                redirectIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(redirectIntent);
                            }
                        } else {
                            Intent redirectIntent = new Intent(LanguageActivity.this, LoginActivity.class);
                            redirectIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(redirectIntent);
                        }


                    }
                }
                isChecking = true;
            }
        });
    }
}
