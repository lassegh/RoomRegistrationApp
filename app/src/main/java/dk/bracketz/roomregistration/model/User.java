package dk.bracketz.roomregistration.model;

public class User {

    private static User single_instance = null;

    private User(){

    }

    public boolean isSomeoneLoggedIn(){
        return !email.isEmpty() && !password.isEmpty();
    }

    public boolean login(String email, String password){
        this.email = email;
        this.password = password;
        return true;
    }

    public boolean logout(){
        email = null;
        password = null;
        return true;
    }

    public static User getInstance(){
        if (single_instance == null)
            single_instance = new User();
        return single_instance;
    }

    public String email;
    public String password;

}
