package dk.bracketz.roomregistration.model;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class User {

    private static User single_instance = null;

    // Firebase auth
    public final FirebaseAuth mAuth;
    public FirebaseUser firebaseUser;

    private User(){
        stayLoggedIn = false;

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Check if user is signed in (non-null) and update UI accordingly.
        firebaseUser = mAuth.getCurrentUser();
    }

    public boolean isSomeoneLoggedIn(){
        return firebaseUser != null;
    }

    public void login(FirebaseUser user, Boolean stayLoggedIn){
        firebaseUser = user;
        this.stayLoggedIn = stayLoggedIn;
    }

    public void logout(){
        // log out from firebase
        mAuth.signOut();
        firebaseUser = null;

        // TODO delete user credentials from device
    }

    public void checkUserChoice(){
        if (!stayLoggedIn)logout();
    }

    public static User getInstance(){
        if (single_instance == null)
            single_instance = new User();
        return single_instance;
    }

    public Boolean stayLoggedIn;
}
