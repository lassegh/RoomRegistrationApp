package dk.bracketz.roomregistration.activities.login;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import dk.bracketz.roomregistration.R;
import dk.bracketz.roomregistration.helpers.FirebaseLogin;

public class LoginActivity extends AppCompatActivity {

    private Boolean stayLoggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login");

        // Shows backbutton in toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Switch toggle = findViewById(R.id.loginStayInSwitch);
        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> LoginActivity.this.onCheckedChanged(buttonView,isChecked));

        stayLoggedIn = FirebaseLogin.getInstance().stayLoggedIn;
        toggle.setChecked(stayLoggedIn);
    }

    private Toast toast;
    private void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        stayLoggedIn = !stayLoggedIn;

        if(toast!=null)toast.cancel();

        if (stayLoggedIn){
            toast = Toast.makeText(getApplicationContext(),"Stay logged in.",Toast.LENGTH_SHORT);
            toast.show();
        }
        else{
            toast = Toast.makeText(getApplicationContext(),"Log out after a reservation or deletion.",Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    // Handles backbutton in toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();

        return super.onOptionsItemSelected(item);
    }

    public void onLogin(View view) {
        EditText emailEdit = (EditText)findViewById(R.id.loginEnterMail);
        String email = emailEdit.getText().toString();
        EditText passwordEdit = (EditText)findViewById(R.id.loginEnterPassword);
        String password = passwordEdit.getText().toString();

        if (email.isEmpty() || password.isEmpty())loginFailed();
        else {
            // log in to firebase
            FirebaseLogin.getInstance().mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("MyTag", "signInWithEmail:success");
                            FirebaseLogin.getInstance().login(FirebaseLogin.getInstance().mAuth.getCurrentUser(), stayLoggedIn);
                            finish();
                            Toast toast = Toast.makeText(getApplicationContext(), "Logged in successfully.", Toast.LENGTH_LONG);

                            toast.show();
                        } else {
                            loginFailed();
                        }

                    });
        }
    }

    private void loginFailed(){
        // If sign in fails, display a message to the user.
        Log.d("MyTag", "signInWithEmail:failure");
        Toast toast = Toast.makeText(getApplicationContext(),"Login failed. Try again.",Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        EditText emailEdit = (EditText)findViewById(R.id.loginEnterMail);
        emailEdit.setText(savedInstanceState.getString("email"));

        EditText passwordEdit = (EditText)findViewById(R.id.loginEnterPassword);
        passwordEdit.setText(savedInstanceState.getString("password"));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        EditText emailEdit = (EditText)findViewById(R.id.loginEnterMail);
        outState.putString("email", emailEdit.getText().toString());

        EditText passwordEdit = (EditText)findViewById(R.id.loginEnterPassword);
        outState.putString("password", passwordEdit.getText().toString());

        super.onSaveInstanceState(outState);
    }
}
