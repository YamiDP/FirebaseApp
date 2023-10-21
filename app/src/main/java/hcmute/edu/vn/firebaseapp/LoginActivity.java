package hcmute.edu.vn.firebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.ktx.Firebase;

public class LoginActivity extends AppCompatActivity {

    //views

    EditText mEmailet, mPasswordlet;
    TextView notHaveAccnTv;
    Button mLoginBtn;

    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;

    //progress dialog
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //action bar and titile
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Đăng nhập");
        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //init
        mEmailet = findViewById(R.id.emailEt);
        mPasswordlet = findViewById(R.id.passwordEt);
        notHaveAccnTv = findViewById(R.id.nothave_accountTv);
        mLoginBtn = findViewById(R.id.loginBtn);

        //login button click
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input data
                String email = mEmailet.getText().toString();
                String passw = mPasswordlet.getText().toString().trim();
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //invalid email pattern set error
                    mEmailet.setError("Email không hợp lệ");
                    mEmailet.requestFocus();
                }
                else {
                    //valid email pattern
                    loginUser(email,passw);
                }

            }
        });
        // not have account textview click
        notHaveAccnTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        //init progress dialog
        progressDialog =  new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng nhập");

    }

    private void loginUser(String email, String passw) {
        //show progress dialog
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, passw).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    //Sign in success, update UI with the signed-in user's infomation
                    FirebaseUser user = mAuth.getCurrentUser();
                    //user is logged in, start LoginAcitivity
                    startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                    finish();
                } else {
                    progressDialog.dismiss();
                    //If sign in fails, display a message to the user.
                    Toast.makeText(LoginActivity.this, "Xác thực thất bại.", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //dismiss progress dialog
                progressDialog.dismiss();
                //error, get and show error message
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //go previous activity
        return super.onSupportNavigateUp();
    }
}