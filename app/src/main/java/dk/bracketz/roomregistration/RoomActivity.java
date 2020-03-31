package dk.bracketz.roomregistration;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

import dk.bracketz.roomregistration.adapter.RoomAdapter;
import dk.bracketz.roomregistration.model.Room;
import dk.bracketz.roomregistration.restconsuming.ApiUtils;
import dk.bracketz.roomregistration.restconsuming.ModelService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomActivity extends AppCompatActivity {

    // Instance af adapter
    private RoomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        // Shows backbutton in toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int fromTime = getIntent().getIntExtra("fromTime",-1);

        if (fromTime == -1)setTitle("Select Room");
        else setTitle("Available Rooms");

        getAndShowRooms(fromTime);
    }

    // Handles backbutton in toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) // Press Back Icon
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    // On list click action - sender data med intent retur
    public void GoBackMethod(View view) {
        Intent intent = new Intent(); // Denne intent bruges til at sende data med tilbage i stakken
        intent.putExtra("SkoStr",42);
        setResult(RESULT_OK,intent); // Denne linie tilføjer intent til 'finish'
        finish();
    }

    private void getAndShowRooms(int fromTime) {
        Call<List<Room>> getAllRooms;
        ModelService modelStoreService = ApiUtils.getReservationService();
        if(fromTime==-1){getAllRooms = modelStoreService.getAllRooms();}
        else{getAllRooms = modelStoreService.getAvailableRooms(fromTime);}
        getAllRooms.enqueue(new Callback<List<Room>>() {
            @Override
            public void onResponse(Call<List<Room>> call, Response<List<Room>> response) {
                if (response.isSuccessful()) {
                    Log.d("response",response.body().toString());
                    populateRecyclerView(response.body());

                } else {
                    String message = "Problem " + response.code() + " " + response.message();
                }
            }

            @Override
            public void onFailure(Call<List<Room>> call, Throwable t) {

            }
        });
    }

    private void populateRecyclerView(List<Room> allRooms) {
        RecyclerView recyclerView = findViewById(R.id.roomRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RoomAdapter(allRooms);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener((view, position, item) -> {

            // Creates bundle and add properties to it
            Bundle roomBundle = new Bundle();
            roomBundle.putInt("id", ((Room) item).getId());
            roomBundle.putString("name", ((Room) item).getName());

            // Creates intent to carry data on way back
            Intent intent = new Intent();
            intent.putExtra("roomBundle",roomBundle);
            Log.d("MyTag","I'm here");
            setResult(RESULT_OK,intent); // Denne linie tilføjer intent til 'finish'
            finish();
        });
    }
}
