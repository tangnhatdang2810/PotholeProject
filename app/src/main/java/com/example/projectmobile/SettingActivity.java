package com.example.projectmobile;

import androidx.appcompat.app.ActionBar;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.Locale;

public class SettingActivity extends AppCompatActivity {
    TextView tvemail, tvname, tvsignout, tvupdate, tvchangeLanguage;
    GoogleSignInClient googleSignInClient;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);
        tvsignout = findViewById(R.id.signout);
        tvupdate = findViewById(R.id.updateprofile);
        tvemail = findViewById(R.id.tvemail);
        tvname = findViewById(R.id.tvname);
        imageView = findViewById(R.id.imgprofile);
        tvchangeLanguage = findViewById(R.id.changeLanguage);
        showUserInformation();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        tvsignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    for (UserInfo profile : user.getProviderData()) {
                        String providerId = profile.getProviderId();
                        if (providerId.equals("google.com")) {
                            // Người dùng đăng nhập bằng Google
                            signOutGoogle();
                        } else if (providerId.equals("password")) {
                            // Người dùng đăng nhập bằng Email/Password
                            signOutEmailPassword();
                        } else if (providerId.equals("facebook.com")) {
                            signOutFacebook();
                        }
                    }
                }

            }
        });

        tvupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, UpdateProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        tvchangeLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangeLanguageDialog();
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.setting);
        // Gắn sự kiện khi click vào các item
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.dashboard){
                    Intent intent = new Intent(SettingActivity.this, DashBoardActivity.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.user){
                    Intent intent = new Intent(SettingActivity.this, UpdateProfileActivity.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.home){
                    Intent intent = new Intent(SettingActivity.this, MapActivity.class);
                    startActivity(intent);
                    finish();
                }
                return false;
            }
        });
    }

    private void showUserInformation(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null){
            return;
        }
        String email = user.getEmail();
        tvemail.setText(email);
        String name = user.getDisplayName();
        if (name == null){
            tvname.setVisibility(View.GONE);
        } else {
            tvname.setVisibility(View.VISIBLE);
            tvname.setText(name);
        }

        Uri photo = user.getPhotoUrl();
        Glide.with(this).load(photo).error(R.drawable.user).into(imageView);
    }

    private void signOutGoogle() {
        googleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Đăng xuất thành công
                FirebaseAuth.getInstance().signOut();
                // Điều hướng về màn hình đăng nhập
                Intent intent = new Intent(SettingActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void signOutEmailPassword() {
        FirebaseAuth.getInstance().signOut();
        // Điều hướng về màn hình đăng nhập
        Intent intent = new Intent(SettingActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    private void signOutFacebook() {
        com.facebook.login.LoginManager.getInstance().logOut();
        FirebaseAuth.getInstance().signOut();
        // Điều hướng về màn hình đăng nhập
        Intent intent = new Intent(SettingActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    private void showChangeLanguageDialog(){
        final String[] listItems = {"Tiếng Việt", "Français", "English"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(SettingActivity.this);
        mBuilder.setTitle("Choose Language...");
        mBuilder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0){
                    setLocale("vi");
                    recreate();
                } else if (i == 1){
                    setLocale("fr");
                    recreate();
                } else if (i == 2){
                    setLocale("en");
                    recreate();
                }

                dialogInterface.dismiss();
            }
        });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", lang);
        editor.apply();
    }

    public void loadLocale(){
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "");
        setLocale(language);
    }
}