package group.sw.spos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import group.sw.spos.database.DatabaseHelper;
import group.sw.spos.database.LoginData;
import group.sw.spos.database.UserData;
import group.sw.spos.database.WareHouses;
import group.sw.spos.lib.TokenManager;
import group.sw.spos.lib.WareHouseItemsList;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener{
    private ImageButton menuButton;
    private LinearLayout countLinearLayout;
    private LinearLayout billLinearLayout;
    private String selectWarehouse;
    private DrawerLayout drawer;
    private  ActionBarDrawerToggle toggle;
    private NavigationView nv;
    private TextView navigationShopNameView, navigationUserNameView, shopNameView, userNameView, loginDateView;
    private DatabaseHelper db;
    private String shopName, userName, loginDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set multi language start

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
        //set multi language end

        setContentView(R.layout.activity_dashboard);
        LoginData loginData = db.getLoginData();
        if(loginData !=null){
            shopName = loginData.getShopName();
            userName = getResources().getString(R.string.welcome) + " " +loginData.getUserName();
            loginDate = loginData.getLoginDate();
        }

        final Toolbar app_bar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(app_bar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        shopNameView = (TextView) findViewById(R.id.shop_name1);
        userNameView =(TextView) findViewById(R.id.user_name1);
        loginDateView = (TextView) findViewById(R.id.login_date);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        nv = (NavigationView) findViewById(R.id.nav_view);
        View headerView = nv.getHeaderView(0);
        navigationShopNameView = (TextView) headerView.findViewById(R.id.shop_name);
        navigationUserNameView =(TextView) headerView.findViewById(R.id.user_name);

        navigationShopNameView.setText(shopName);
        navigationUserNameView.setText(userName);
        shopNameView.setText(shopName);
        userNameView.setText(userName);
        loginDateView.setText(loginDate);


        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(id == R.id.language){
                    Intent languageIntent = new Intent(DashboardActivity.this, LanguageActivity.class);
                    startActivity(languageIntent);
                } else if(id == R.id.logout){
                    drawer.closeDrawer(nv);
                    new AlertDialog.Builder(DashboardActivity.this)
                        .setMessage(R.string.logout_text)
                        .setPositiveButton(R.string.logout_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                TokenManager.getInstance(getApplicationContext()).clear();
                                Intent loginIntent = new Intent(DashboardActivity.this, LoginActivity.class);
                                loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(loginIntent);
                            }
                        })
                        .setNegativeButton(R.string.logout_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
                }
              return true;
            }
        });




        menuButton = (ImageButton) findViewById(R.id.menu_button);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(nv);
            }
        });
        countLinearLayout = (LinearLayout) findViewById(R.id.count);
        billLinearLayout = (LinearLayout) findViewById(R.id.bill);
        countLinearLayout.setEnabled(true);
        billLinearLayout.setEnabled(true);

        List<String> mWareHouses = new ArrayList<String>();
        List<WareHouses> wareHousesList = db.getAllHouses();
        if(wareHousesList != null && !wareHousesList.isEmpty()){
            for(WareHouses warehouse : wareHousesList){
                mWareHouses.add(warehouse.getName());
            }
        }

        final CharSequence[] availableWareHouses = mWareHouses.toArray(new String[mWareHouses.size()]);

        countLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DashboardActivity.this);
                dialogBuilder.setTitle(Html.fromHtml(getResources().getString(R.string.choose_warehouse)));
                dialogBuilder.setItems(availableWareHouses, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectWarehouse = availableWareHouses[which].toString();
                        WareHouses wareHouse  = db.getWareHouse(selectWarehouse);
                        if(wareHouse !=null){
                            Integer month = Calendar.getInstance().get(Calendar.MONTH);
                            month = month+ 1;
                            String month_label = String.valueOf(month);
                            if(month<10){
                                month_label = "0"+String.valueOf(month);
                            }
                            TokenManager.getInstance(DashboardActivity.this).clearBillNo();
                            String BillNumber = "SC"+ Calendar.getInstance().get(Calendar.YEAR)+month_label+Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+ Calendar.getInstance().get(Calendar.HOUR_OF_DAY)+ Calendar.getInstance().get(Calendar.MINUTE)+ Calendar.getInstance().get(Calendar.SECOND);
                            String date = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(new Date());
                            String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                            db.insertUpdateBill(BillNumber, date, "Pending", wareHouse.getName(), wareHouse.getWarehouse(), dateTime);
                            TokenManager.getInstance(DashboardActivity.this).setBillNo(BillNumber);
                            Intent countScanIntent = new Intent(DashboardActivity.this, CountScanActivity.class);
                            startActivity(countScanIntent);
                        }
                    }
                }).show();
            }
        });

        billLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent billIntent = new Intent(DashboardActivity.this, BillListActivity.class);
                startActivity(billIntent);
            }
        });
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.menu_button){
//            drawer.openDrawer(nv);
//            showPopupWindow(v);
        }
    }

//    void showPopupWindow(View view) {
//        PopupMenu popup = new PopupMenu(DashboardActivity.this, view, Gravity.RIGHT);
//        try {
//            Field[] fields = popup.getClass().getDeclaredFields();
//            for (Field field : fields) {
//                if ("mPopup".equals(field.getName())) {
//                    field.setAccessible(true);
//                    Object menuPopupHelper = field.get(popup);
//                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
//                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
//                    setForceIcons.invoke(menuPopupHelper, true);
//                    break;
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        popup.getMenuInflater().inflate(R.menu.right_menu, popup.getMenu());
//        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//
//            public boolean onMenuItemClick(MenuItem item) {
//                if(item.getItemId() == R.id.language){
//                    Intent newIntent = new Intent(DashboardActivity.this, LanguageActivity.class);
//                    startActivity(newIntent);
//                    Toast.makeText(getApplicationContext(), "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
//                } else {
//                    new AlertDialog.Builder(DashboardActivity.this)
//                            .setMessage(R.string.logout_text)
//                            .setPositiveButton(R.string.logout_ok, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                }
//                            })
//                            .setNegativeButton(R.string.logout_cancel, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//
//                                }
//                            }).show();
//                    Toast.makeText(getApplicationContext(), "You Clicked : " + item.getTitle(), Toast.LENGTH_SHORT).show();
//                }
//
//                return true;
//            }
//        });
//        popup.show();
//    }

}
