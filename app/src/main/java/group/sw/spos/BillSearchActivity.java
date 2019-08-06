package group.sw.spos;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import group.sw.spos.lib.TokenManager;

public class BillSearchActivity extends AppCompatActivity  implements View.OnClickListener{

    private EditText startDateView, endDateView;
    private  Calendar startCalendar, endCalendar;
    private Button todayButton, weekButton, monthButton, yearButton;
    private ImageButton confirmButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_search);
        startDateView = (EditText) findViewById(R.id.start_date);
        endDateView = (EditText) findViewById(R.id.end_date);
        Toolbar app_bar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(app_bar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_filter_close);

        todayButton = (Button) findViewById(R.id.today);
        weekButton = (Button) findViewById(R.id.week);
        monthButton = (Button) findViewById(R.id.month);
        yearButton = (Button) findViewById(R.id.year);
        confirmButton = (ImageButton) findViewById(R.id.confirm_button);
        confirmButton.setEnabled(true);
        confirmButton.setOnClickListener(this);
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener start_date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                startCalendar.set(Calendar.YEAR, year);
                startCalendar.set(Calendar.MONTH, month);
                startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel("start_date");
            }
        };

        final DatePickerDialog.OnDateSetListener end_date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                endCalendar.set(Calendar.YEAR, year);
                endCalendar.set(Calendar.MONTH, month);
                endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel("end_date");
            }
        };

        startDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    new DatePickerDialog(BillSearchActivity.this, start_date, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH),
                            startCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        endDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(BillSearchActivity.this, end_date, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH),
                        endCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        todayButton.setOnClickListener(this);
        weekButton.setOnClickListener(this);
        monthButton.setOnClickListener(this);
        yearButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
         if(v.getId() == R.id.today || v.getId() == R.id.week || v.getId() == R.id.month || v.getId() == R.id.year){
            Date currentDate = new Date();
            Calendar c = Calendar.getInstance();
            String startDate="";
            c.setTime(currentDate);
            if(v.getId() == R.id.week){
                c.add(Calendar.DATE, -7);
                startDate = new SimpleDateFormat("MM/dd/yyyy").format(c.getTime());
            } else if(v.getId() == R.id.month){
                c.add(Calendar.MONTH, -1);
                startDate = new SimpleDateFormat("MM/dd/yyyy").format(c.getTime());
            } else if(v.getId() == R.id.year){
                c.add(Calendar.YEAR, -1);
                startDate = new SimpleDateFormat("MM/dd/yyyy").format(c.getTime());
            } else if(v.getId() == R.id.today){
                startDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(new Date());
            }

            String endDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(new Date());
            intent.putExtra("startDate", startDate);
            intent.putExtra("endDate", endDate);
            setResult(2, intent);
            finish();
        } else if(v.getId() == R.id.confirm_button){
            String startDate = startDateView.getText().toString();
            String endDate = endDateView.getText().toString();
            if(startDate.equals("") || endDate.equals("")){
                Toast.makeText(getApplicationContext(), R.string.please_select_start_date_and_end_date, Toast.LENGTH_LONG).show();
            } else {

                SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");

                Date date1 = null;
                try {
                    date1 = format.parse(startDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Date date2 = null;
                try {
                    date2 = format.parse(endDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (date1.compareTo(date2) <= 0) {
                    intent.putExtra("startDate", startDate);
                    intent.putExtra("endDate", endDate);
                    setResult(2, intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.please_select_correct_start_and_end_date, Toast.LENGTH_SHORT).show();
                }
            }
         }
    }

    private void updateLabel(String date) {

        String myFormat = "MM/dd/YYYY"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
        if(date.equals("start_date")){
            startDateView.setText(sdf.format(startCalendar.getTime()));
        } else {
            endDateView.setText(sdf.format(endCalendar.getTime()));
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent intent = new Intent();
        intent.putExtra("startDate", "");
        intent.putExtra("endDate", "");
        setResult(2, intent);
        finish();
    }
}
