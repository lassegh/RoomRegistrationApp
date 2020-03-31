package dk.bracketz.roomregistration;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
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
    private EditText fromDatePicker;
    private EditText toDatePicker;
    private final String myFormat = "dd/MM/yy";
    private final SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMAN);
    private final Calendar fromCalendar = Calendar.getInstance();
    private final Calendar toCalendar = Calendar.getInstance();

    // Toolbar instance
    private Toolbar toolbar;

    // Chosen room instance
    private final Room chosenRoom = new Room();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        chosenRoom.setName("RO-D3.07");
        chosenRoom.setId(1);
        Context thisContext = this;

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
                if (User.getInstance().isSomeoneLoggedIn()){
                    Intent intent = new Intent(thisContext, AddBookingActivity.class);
                    Bundle roomBundle = new Bundle();
                    roomBundle.putString("name",chosenRoom.getName());
                    roomBundle.putInt("id",chosenRoom.getId());
                    intent.putExtra("room",roomBundle);
                    startActivity(intent);
                }
                else{
                    Toast toast = Toast.makeText(getApplicationContext(), "Please log in to create reservation", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

        // From Date Picker init
        fromDatePicker = (EditText) findViewById(R.id.mainInputFromDateEditText);
        fromDatePicker.setText(sdf.format(new Date()));
        fromDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(MainActivity.this, setDate(fromCalendar), fromCalendar
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
                new DatePickerDialog(MainActivity.this, setDate(toCalendar), toCalendar
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // navigation bar buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_select_room) {
            // Create intent - go to select room activity
            Intent intent = new Intent(this, RoomActivity.class);
            startActivityForResult(intent,42);
            return true;
        }

        if (id== R.id.action_log_in){
            // Create intent - go to login page
            if (!User.getInstance().isSomeoneLoggedIn()){
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            }
            else{
                // Spørg bruger om han ønsker at logge ud
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Logged in as "+ User.getInstance().firebaseUser.getEmail());
                alertDialog.setMessage("Do you want to log out?");
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Yes",
                        (dialog, which) -> {
                            User.getInstance().logout();
                            dialog.dismiss();
                            Toast toast = Toast.makeText(getApplicationContext(), "You have been logged out.", Toast.LENGTH_LONG);
                            toast.show();
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

    // Date dialog
    private DatePickerDialog.OnDateSetListener setDate(Calendar cal){
        return (view, year, monthOfYear, dayOfMonth) -> {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, monthOfYear);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            fromDatePicker.setText(sdf.format(fromCalendar.getTime()));
            toDatePicker.setText(sdf.format(toCalendar.getTime()));
            getAndShowReservations(chosenRoom.getId(), toUnixTime(fromDatePicker.getText().toString()), toUnixTime(toDatePicker.getText().toString()));
        };
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
                Log.e("Error",t.getMessage());
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
        if (requestCode == 42 && data != null){
            super.onActivityResult(requestCode, resultCode, data);
            Bundle roomBundle = data.getBundleExtra("roomBundle");
            chosenRoom.setName(roomBundle.getString("name"));
            chosenRoom.setId(roomBundle.getInt("id"));
            Log.d("MyTag",chosenRoom.getName());
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        TextView fromDateTextView = (TextView) findViewById(R.id.mainInputFromDateEditText);
        fromDateTextView.setText(savedInstanceState.getString("fromDate"));

        TextView toDateTextView = (TextView) findViewById(R.id.mainInputToDateEditText);
        toDateTextView.setText(savedInstanceState.getString("toDate"));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        TextView fromDateTextView = (TextView) findViewById(R.id.mainInputFromDateEditText);
        outState.putString("fromDate", fromDateTextView.getText().toString());

        TextView toDateTextView = (TextView) findViewById(R.id.mainInputToDateEditText);
        outState.putString("toDate", toDateTextView.getText().toString());

        super.onSaveInstanceState(outState);
    }
}
