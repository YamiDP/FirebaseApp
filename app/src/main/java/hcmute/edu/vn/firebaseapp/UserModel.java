package hcmute.edu.vn.firebaseapp;

public class UserModel {

    String email, password;

    public UserModel(String email, String password) {
        this.email = email;
        this.password = password;
    }
    public UserModel(){

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
