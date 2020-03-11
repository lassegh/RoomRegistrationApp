package dk.bracketz.roomregistration.model;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.sql.Struct;
import java.util.concurrent.Executor;

import dk.bracketz.roomregistration.MainActivity;

public class User {

    private static User single_instance = null;

    // Firebase auth
    public FirebaseAuth mAuth;
    public FirebaseUser firebaseUser;

    private User(){
        stayLoggedIn = false;

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is signed in (non-null) and update UI accordingly.
        firebaseUser = mAuth.getCurrentUser();
    }

    public boolean isSomeoneLoggedIn(){
        if (firebaseUser != null){
            return true;
        }
        return false;
    }

    public void login(FirebaseUser user, Boolean stayLoggedIn){
        firebaseUser = user;
        this.stayLoggedIn = stayLoggedIn;
    }

    public boolean logout(){
        // log out from firebase
        firebaseUser = null;
        if(firebaseUser== null)return true;
        return false;
    }

    public static User getInstance(){
        if (single_instance == null)
            single_instance = new User();
        return single_instance;
    }

    public Boolean stayLoggedIn;
}
