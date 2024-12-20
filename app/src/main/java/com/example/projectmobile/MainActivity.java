package com.example.projectmobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    ImageView img_Google;
    Button btn_Signin, btn_Signup;
    ProgressBar progressBar;
    Timer timer;
    int count;
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth auth;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

            btn_Signin = findViewById(R.id.btn_Signin);
            btn_Signup = findViewById(R.id.btn_Signup);
            img_Google = findViewById(R.id.img_Google);
            progressBar = findViewById(R.id.progressBar);

            //Google
            auth = FirebaseAuth.getInstance();
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            googleSignInClient = GoogleSignIn.getClient(this, gso);

            btn_Signin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    setProgressBar();

                    Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                    startActivity(intent);
                }
            });

            btn_Signup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    setProgressBar();

                    Intent intent2 = new Intent(MainActivity.this, SignUpActivity.class);
                    startActivity(intent2);
                }
            });

            img_Google.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage(getString(R.string.please_wait));
                    progressDialog.show();
                    signin();
                }
            });
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
                Toast.makeText(MainActivity.this, getString(R.string.login_fail), Toast.LENGTH_SHORT).show();
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
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(intent);
                            finishAffinity();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, getString(R.string.login_fail), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    void setProgressBar() {

        progressBar.setVisibility(View.VISIBLE);

        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                count++;
                progressBar.setProgress(count);
                if(count == 100) {
                    timer.cancel();
                }
            }
        };
        timer.schedule(timerTask, 0, 100);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.GONE);
        }
    }
}