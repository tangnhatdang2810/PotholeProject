package com.example.projectmobile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {
    Button btnsignup;
    ImageView btnback;
    EditText etmail, etpw, etcpw;
    TextView signin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        btnsignup = findViewById(R.id.btn_Signup2);
        btnback = findViewById(R.id.btn_Back3);
        etmail = findViewById(R.id.edt_Email);
        etpw = findViewById(R.id.edt_Password);
        etcpw = findViewById(R.id.edt_ConfirmPass);
        signin = findViewById(R.id.signin_new);

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this ,SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btnsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mail = etmail.getText().toString().trim();
                String pw = etpw.getText().toString().trim();
                String cpw = etcpw.getText().toString().trim();
                if (mail.isEmpty() || pw.isEmpty() || cpw.isEmpty()){
                    Toast.makeText(SignUpActivity.this, getString(R.string.empty_fields), Toast.LENGTH_SHORT).show();
                } else if (!pw.equals(cpw)){
                    Toast.makeText(SignUpActivity.this, getString(R.string.not_match), Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    auth.createUserWithEmailAndPassword(mail, pw)
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(SignUpActivity.this, MapActivity.class);
                                        startActivity(intent);
                                        finishAffinity();
                                    } else {
                                        Toast.makeText(SignUpActivity.this, getString(R.string.error_occurred),Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}