package dk.bracketz.roomregistration;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dk.bracketz.roomregistration.adapter.ReservationAdapter;
import dk.bracketz.roomregistration.model.Reservation;
import dk.bracketz.roomregistration.restconsuming.ApiUtils;
import dk.bracketz.roomregistration.restconsuming.ModelService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Instances for datePickers
    EditText fromDatePicker;
    EditText toDatePicker;
    String myFormat = "dd/MM/yy";
    SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMAN);
    final Calendar fromCalendar = Calendar.getInstance();
    Calendar toCalendar = Calendar.getInstance();

    // Instances for recyclerView
    ArrayList<Reservation> reservations = new ArrayList<Reservation>();
    ReservationAdapter adapter;
    RecyclerView recyclerView;

    // Toolbar instance
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Room RO-D3.07");
        setSupportActionBar(toolbar);


        // Floating action button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // From Date Picker init
        fromDatePicker = (EditText) findViewById(R.id.mainInputFromDateEditText);
        fromDatePicker.setText(sdf.format(new Date()));
        fromDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(MainActivity.this, fromDate, fromCalendar
                        .get(Calendar.YEAR), fromCalendar.get(Calendar.MONTH),
                        fromCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // To date picker init
        toDatePicker = (EditText) findViewById(R.id.mainInputToDateEditText);
        final Date toDate = new Date();
        toCalendar.setTime(toDate);
        toCalendar.add(Calendar.DATE,7);
        toDatePicker.setText(sdf.format(toCalendar.getTime()));
        toDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(MainActivity.this, toDatePickerListener, toCalendar
                        .get(Calendar.YEAR), toCalendar.get(Calendar.MONTH),
                        toCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // Init swipe to refresh
        SwipeRefreshLayout refreshLayout = findViewById(R.id.mainSwiperefresh);
        refreshLayout.setOnRefreshListener(() -> {
            getAndShowReservations();
            refreshLayout.setRefreshing(false);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getAndShowReservations();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // navigation bar buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_select_room) {
            // Create intent - go to select room activity
            Intent intent = new Intent(this, RoomActivity.class);
            startActivityForResult(intent,42); // Denne linie bruges hvis man forventer at få data retur. 42 er tilfældigt
            // TODO get roomId on the way back

            return true;
        }

        if (id== R.id.action_log_in){
            // TODO create intent - go to login page
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // From date dialog
    DatePickerDialog.OnDateSetListener fromDate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            fromCalendar.set(Calendar.YEAR, year);
            fromCalendar.set(Calendar.MONTH, monthOfYear);
            fromCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            toCalendar.set(Calendar.YEAR, year);
            toCalendar.set(Calendar.MONTH, monthOfYear);
            toCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }
    };

    // from date is set
    private void updateLabel() {
        fromDatePicker.setText(sdf.format(fromCalendar.getTime()));
    }

    // To date dialog
    DatePickerDialog.OnDateSetListener toDatePickerListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            fromCalendar.set(Calendar.YEAR, year);
            fromCalendar.set(Calendar.MONTH, monthOfYear);
            fromCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            toCalendar.set(Calendar.YEAR, year);
            toCalendar.set(Calendar.MONTH, monthOfYear);
            toCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateToDateLabel();
        }

    };

    // to date is set
    private void updateToDateLabel() {
        toDatePicker.setText(sdf.format(toCalendar.getTime()));
    }

    @Override
    public void onClick(View v) {

    }

    private void getAndShowReservations() {
        ModelService modelStoreService = ApiUtils.getBookStoreService();
        Call<List<Reservation>> getAllReservations = modelStoreService.getAllReservations();
        getAllReservations.enqueue(new Callback<List<Reservation>>() {
            @Override
            public void onResponse(Call<List<Reservation>> call, Response<List<Reservation>> response) {
                if (response.isSuccessful()) {
                    Log.d("response",response.body().toString());
                    populateRecyclerView(response.body());

                } else {
                    String message = "Problem " + response.code() + " " + response.message();
                }
            }

            @Override
            public void onFailure(Call<List<Reservation>> call, Throwable t) {

            }
        });
    }

    private void populateRecyclerView(List<Reservation> allReservations) {
        RecyclerView recyclerView = findViewById(R.id.mainRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReservationAdapter(allReservations);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener((view, position, item) -> {
            Log.d("tag","");
        });
    }


    // TODO Recieves data from go back intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Request code er en int for undersiden.
        // result code er den status kode, der bliver sendt med, når undersiden sender retur. (setResult metoden.)
        if (data != null){
            super.onActivityResult(requestCode, resultCode, data);
            int shoeSize = data.getIntExtra("SkoStr",-1); // Der kræves default value da int ikke har null
            Log.d("myTag",""+shoeSize);
        }
    }
}
