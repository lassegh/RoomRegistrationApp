package dk.bracketz.roomregistration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import java.util.concurrent.Executor;

import dk.bracketz.roomregistration.model.User;

public class LoginActivity extends AppCompatActivity {

    Boolean stayLoggedIn;
    Switch toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login");

        // Shows backbutton in toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toggle = (Switch) findViewById(R.id.loginStayInSwitch);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                stayLoggedIn = !stayLoggedIn;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        stayLoggedIn = User.getInstance().stayLoggedIn;
        toggle.setChecked(stayLoggedIn);
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



    public void onLogin(View view) {
        EditText emailEdit = (EditText)findViewById(R.id.loginEnterMail);
        String email = emailEdit.getText().toString();
        EditText passwordEdit = (EditText)findViewById(R.id.loginEnterPassword);
        String password = passwordEdit.getText().toString();

        // log in to firebase
        User.getInstance().mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("MyTag", "signInWithEmail:success");
                            User.getInstance().login(User.getInstance().mAuth.getCurrentUser(),stayLoggedIn);
                            finish();
                            Toast toast = Toast.makeText(getApplicationContext(),"Logged in successfully.",Toast.LENGTH_LONG);
                            toast.show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("MyTag", "signInWithEmail:failure", task.getException());
                            Toast toast = Toast.makeText(getApplicationContext(),"Login failed. Try again.",Toast.LENGTH_LONG);
                            toast.show();
                        }

                    }
                });
    }
}
