package dk.bracketz.roomregistration;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dk.bracketz.roomregistration.adapter.ReservationAdapter;
import dk.bracketz.roomregistration.adapter.SwipeToDeleteCallback;
import dk.bracketz.roomregistration.model.Reservation;
import dk.bracketz.roomregistration.model.Room;
import dk.bracketz.roomregistration.model.User;
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

    // Toolbar instance
    Toolbar toolbar;

    // Chosen room instance
    Room chosenRoom = new Room();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        chosenRoom.setName("RO-D3.07");
        chosenRoom.setId((int)1);

        // Init User / Firebase
        User.getInstance();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
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
            getAndShowReservations(chosenRoom.getId(), toUnixTime(fromDatePicker.getText().toString()), toUnixTime(toDatePicker.getText().toString()));
            refreshLayout.setRefreshing(false);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        toolbar.setTitle(chosenRoom.getName());
        Log.d("MyTag",chosenRoom.getName());
        Log.d("MyTag",chosenRoom.getId().toString());
        getAndShowReservations(chosenRoom.getId(), toUnixTime(fromDatePicker.getText().toString()), toUnixTime(toDatePicker.getText().toString()));
    }

    private Integer toUnixTime(String timestamp) {
        try {
            if (timestamp == null) return null;
            Date dt = sdf.parse(timestamp);
            long epoch = dt.getTime();
            return (int) (epoch / 1000);
        } catch (ParseException e) {
            return null;
        }
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
            return true;
        }

        if (id== R.id.action_log_in){
            // Tcreate intent - go to login page
            if (!User.getInstance().isSomeoneLoggedIn()){
                // send til login skærm
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            }
            else{
                // Spørg bruger om han ønsker at logge ud
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Logged in as "+ User.getInstance().firebaseUser.getEmail());
                alertDialog.setMessage("Do you want to log out?");
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                User.getInstance().logout();
                                dialog.dismiss();
                                Toast toast = Toast.makeText(getApplicationContext(), "You have been logged out.", Toast.LENGTH_LONG);
                                toast.show();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // No press
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
            }
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
        getAndShowReservations(chosenRoom.getId(), toUnixTime(fromDatePicker.getText().toString()), toUnixTime(toDatePicker.getText().toString()));
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
            getAndShowReservations(chosenRoom.getId(), toUnixTime(fromDatePicker.getText().toString()), toUnixTime(toDatePicker.getText().toString()));
    }

    @Override
    public void onClick(View v) {

    }

    private void getAndShowReservations(int roomId, int fromTime, int toTime) {
        Log.d("MyTag","FromTime: "+fromTime);
        Log.d("MyTag","ToTime: "+toTime);
        ModelService modelStoreService = ApiUtils.getReservationService();
        Call<List<Reservation>> getAllReservations = modelStoreService.getAllReservations(roomId, fromTime, toTime);
        getAllReservations.enqueue(new Callback<List<Reservation>>() {
            @Override
            public void onResponse(Call<List<Reservation>> call, Response<List<Reservation>> response) {
                if (response.isSuccessful()) {
                    Log.d("response",response.body().toString());
                    populateRecyclerView(response.body());

                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "'To Date' cannot be before 'From Date'.", Toast.LENGTH_LONG);
                    toast.show();
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
        ReservationAdapter adapter = new ReservationAdapter(allReservations);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (!allReservations.isEmpty()){
            ItemTouchHelper itemTouchHelper = new
                    ItemTouchHelper(new SwipeToDeleteCallback(adapter, getApplicationContext()));
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }

        adapter.setOnItemClickListener((view, position, item) -> {
            Log.d("tag","");
        });
    }


    // Recieves data from go back intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Request code er en int for undersiden.
        // result code er den status kode, der bliver sendt med, når undersiden sender retur. (setResult metoden.)
        if (data != null){
            super.onActivityResult(requestCode, resultCode, data);
            Bundle roomBundle = data.getBundleExtra("roomBundle");
            chosenRoom.setName(roomBundle.getString("name"));
            chosenRoom.setId(roomBundle.getInt("id"));
            Log.d("MyTag",chosenRoom.getName());
        }
    }
}
