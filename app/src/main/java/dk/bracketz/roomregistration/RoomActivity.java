package dk.bracketz.roomregistration;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toolbar;

public class RoomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Select Room");
        setContentView(R.layout.activity_room);

        // Shows backbutton in toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    // TODO On list click action - sender data med intent retur
    public void GoBackMethod(View view) {
        Intent intent = new Intent(); // Denne intent bruges til at sende data med tilbage i stakken
        intent.putExtra("SkoStr",42);
        setResult(RESULT_OK,intent); // Denne linie tilføjer intent til 'finish'
        finish();
    }
}
