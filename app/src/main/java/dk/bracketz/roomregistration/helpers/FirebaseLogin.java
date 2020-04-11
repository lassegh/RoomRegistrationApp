package dk.bracketz.roomregistration.helpers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseLogin {

    private static FirebaseLogin single_instance = null;

    // Firebase auth
    public final FirebaseAuth mAuth;
    public FirebaseUser firebaseUser;

    private FirebaseLogin(){
        stayLoggedIn = false;

        mAuth = FirebaseAuth.getInstance();
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
    }

    public void checkUserChoice(){
        if (!stayLoggedIn)logout();
    }

    public static FirebaseLogin getInstance(){
        if (single_instance == null)
            single_instance = new FirebaseLogin();
        return single_instance;
    }

    public Boolean stayLoggedIn;
}
