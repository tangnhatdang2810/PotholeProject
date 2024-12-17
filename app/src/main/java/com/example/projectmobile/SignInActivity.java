package com.example.projectmobile;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import java.util.Arrays;


public class SignInActivity extends AppCompatActivity {
    Button btnsignin;
    EditText etmail, etpw;
    TextView tvfgpw, tvsignup;
    ImageView imggle;
    CallbackManager callbackManager;
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth auth;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        btnsignin = findViewById(R.id.signInButton);
        etmail = findViewById(R.id.etmail);
        etpw = findViewById(R.id.edpw);
        imggle = findViewById(R.id.img_Google);
        tvfgpw = findViewById(R.id.forgotPassword);
        tvsignup = findViewById(R.id.txt_Signup_new);

        //Google
        auth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnsignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = new ProgressDialog(SignInActivity.this);
                progressDialog.setMessage(getString(R.string.please_wait));
                progressDialog.show();
                String mail = etmail.getText().toString().trim();
                String password = etpw.getText().toString().trim();
                if (mail.isEmpty() || password.isEmpty()){
                    progressDialog.dismiss();
                    Toast.makeText(SignInActivity.this, getString(R.string.email_password), Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    auth.signInWithEmailAndPassword(mail, password)
                            .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        progressDialog.dismiss();
                                        Intent intent = new Intent(SignInActivity.this, MapActivity.class);
                                        startActivity(intent);
                                        finishAffinity();
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(SignInActivity.this, getString(R.string.login_fail), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        tvsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        tvfgpw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                String emailAddress = etmail.getText().toString().trim();
                if (emailAddress.isEmpty()){
                    Toast.makeText(SignInActivity.this, getString(R.string.email_empty), Toast.LENGTH_SHORT).show();
                    return;
                }
                auth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignInActivity.this, getString(R.string.email_sent), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SignInActivity.this, getString(R.string.email_fail), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        imggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = new ProgressDialog(SignInActivity.this);
                progressDialog.setMessage(getString(R.string.please_wait));
                progressDialog.show();
                signin();
            }
        });
    }

    private void signin(){
        Intent signinIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signinIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(SignInActivity.this, getString(R.string.login_fail), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuth(String idToken){
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            progressDialog.dismiss();
                            Intent intent = new Intent(SignInActivity.this, MapActivity.class);
                            startActivity(intent);
                            finishAffinity();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(SignInActivity.this, getString(R.string.login_fail), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}