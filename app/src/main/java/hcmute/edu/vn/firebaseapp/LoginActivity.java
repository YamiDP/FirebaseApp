package hcmute.edu.vn.firebaseapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.ktx.Firebase;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 100;
    GoogleSignInClient mGoogleSignInClient;

    //views

    EditText mEmailet, mPasswordlet;
    TextView notHaveAccnTv, mRecoverPassTv;
    Button mLoginBtn;
    SignInButton mGoogleLoginBtn;

//    private GoogleSignInClient googleSignInClient;
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

        //before mAuth
        //configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
//        googleSignInClient = GoogleSignIn.getClient(this,options);
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //init
        mEmailet = findViewById(R.id.emailEt);
        mPasswordlet = findViewById(R.id.passwordEt);
        notHaveAccnTv = findViewById(R.id.nothave_accountTv);
        mLoginBtn = findViewById(R.id.loginBtn);
        mRecoverPassTv = findViewById(R.id.recoverPassTv);
        mGoogleLoginBtn = findViewById(R.id.googleLoginBtn);

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
                finish();
            }
        });
        //recover password textview click
        mRecoverPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswordDialog();
            }
        });

        //handle google login btn click
        mGoogleLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = googleSignInClient.getSignInIntent();
//                startActivityForResult(intent,1234);
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        //init progress dialog
        progressDialog =  new ProgressDialog(this);


    }

    private void showRecoverPasswordDialog(){
        //Alert Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Khôi phục mật khẩu.");

        //set layout linear layout
        LinearLayout linearLayout = new LinearLayout(this);
        //views to set in dialog
        EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        /*
        set the min width of a EditView to fit a text of n 'M' letters regardless of the actual text extension and text size.
        */
        emailEt.setMinEms(16);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);

        //buttons recover
        builder.setPositiveButton("Khôi phục", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input email
                String email = emailEt.getText().toString().trim();
                beginRecovery(email);

            }
        });
        //buttons cancel
        builder.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //show dialog
        builder.create().show();
    }

    private void beginRecovery(String email){
        //show progress dialog
        progressDialog.setMessage("Đang gửi email ...");
        progressDialog.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if(task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Đã gửi email!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(LoginActivity.this, "Thất bại ...", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                //get and show proper error mesage
                Toast.makeText(LoginActivity.this,""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginUser(String email, String passw) {
        //show progress dialog
        progressDialog.setMessage("Đang đăng nhập");
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

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == 1234){
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            try{
//                GoogleSignInAccount account = task.getResult(ApiException.class);
//
//                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
//                FirebaseAuth.getInstance().signInWithCredential(credential)
//                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                            @Override
//                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                if(task.isSuccessful()){
//                                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
//                                    startActivity(intent);
//                                }else{
//                                    Toast.makeText(LoginActivity.this, task.getException().getMessage(),Toast.LENGTH_SHORT).show();;
//                                }
//                            }
//                        });
//            }catch (ApiException e){
//                e.printStackTrace();
//            }
//        }
//    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        //Result returned from launching  the Intent from GoogleSignInApi.getSignInIntent(...);
        if(requestCode==RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                //Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            }catch (ApiException e){
                //Google Sign In failed, update UI appropriately
                Toast.makeText(this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct){
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(),null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //sign in success, update UI with the signed-in user's information
                    FirebaseUser user = mAuth.getCurrentUser();
                    //show user email in toast
                    Toast.makeText(LoginActivity.this,""+user.getEmail(),Toast.LENGTH_SHORT).show();
                    //go to profile activity after logged in
                    startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                    finish();
                    //updateUI(user)
                }
                else{
                    //If sign in fails, display a message to the user
                    Toast.makeText(LoginActivity.this,"Đăng nhập thất bại...",Toast.LENGTH_SHORT).show();
                    //updateUI(null)
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //get and show error message
                Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //go previous activity
        return super.onSupportNavigateUp();
    }
}