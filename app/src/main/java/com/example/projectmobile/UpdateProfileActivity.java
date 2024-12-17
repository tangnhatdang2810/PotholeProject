package com.example.projectmobile;

import android.app.usage.StorageStats;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class UpdateProfileActivity extends AppCompatActivity {
    Button btnupdate;
    TextView tvsaveimg;
    EditText edtname, edtnpw, edtcrpw, edtcpw;
    CircleImageView imgprofile;
    ActivityResultLauncher<Intent> imagePickLauncher;
    Uri uri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_profile);
        btnupdate = findViewById(R.id.btnUpdate);
        edtname = findViewById(R.id.edtname);
        edtcrpw = findViewById(R.id.edtcrpassword);
        edtnpw = findViewById(R.id.edtpassword);
        edtcpw = findViewById(R.id.edtpasswordcon);
        imgprofile = findViewById(R.id.imgupdateprofile);
        tvsaveimg = findViewById(R.id.tvsaveimg);

        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            uri = data.getData();
                            Glide.with(UpdateProfileActivity.this).load(uri).apply(RequestOptions.circleCropTransform()).into(imgprofile);
                        }
                    }
                });

        imgprofile.setOnClickListener((view) -> {
            ImagePicker.with(UpdateProfileActivity.this).crop().compress(512).maxResultSize(512, 512)
                    .createIntent(new Function1<Intent, Unit>() {
                        @Override
                        public Unit invoke(Intent intent) {
                            imagePickLauncher.launch(intent);
                            return null;
                        }
                    });

        });

        tvsaveimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null){
                    return;
                }

                buttonUpdateImageClick();
            }
        });

        btnupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    return;
                }
                String name = edtname.getText().toString().trim();
                if (!name.isEmpty()) {
                    buttonUpdateNameClick();
                }
                String currentPw = edtcrpw.getText().toString().trim();
                String newPw = edtnpw.getText().toString().trim();
                String confirmPw = edtcpw.getText().toString().trim();
                if (!currentPw.isEmpty() || !newPw.isEmpty() || !confirmPw.isEmpty()) {
                    if (currentPw.isEmpty() || newPw.isEmpty()) {
                        Toast.makeText(UpdateProfileActivity.this, getString(R.string.nhapthieu), Toast.LENGTH_SHORT).show();
                    } else if (!newPw.equals(confirmPw)) {
                        Toast.makeText(UpdateProfileActivity.this, getString(R.string.not_match), Toast.LENGTH_SHORT).show();
                    } else {
                        buttonUpdatePasswordClick();
                    }
                }
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.user);
        // Gắn sự kiện khi click vào các item
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.dashboard){
                    Intent intent = new Intent(UpdateProfileActivity.this, DashBoardActivity.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.home){
                    Intent intent = new Intent(UpdateProfileActivity.this, MapActivity.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.setting){
                    Intent intent = new Intent(UpdateProfileActivity.this, SettingActivity.class);
                    startActivity(intent);
                    finish();
                }
                return false;
            }
        });
    }

    private void buttonUpdateNameClick() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        String name = edtname.getText().toString().trim();
        // Cập nhật thông tin người dùng
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();

        // Cập nhật thông tin người dùng
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(UpdateProfileActivity.this, getString(R.string.update_name_success), Toast.LENGTH_SHORT).show();
                            edtname.setText("");
                        } else {
                            Toast.makeText(UpdateProfileActivity.this, getString(R.string.update_name_fail), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void buttonUpdatePasswordClick(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        String crpassword = edtcrpw.getText().toString().trim();
        String newpassword = edtnpw.getText().toString().trim();
        String conpassword = edtcpw.getText().toString().trim();
        // Tạo thông tin xác thực
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), crpassword);

        // Xác thực lại người dùng
        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Nếu xác thực thành công, cập nhật mật khẩu
                            user.updatePassword(newpassword)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(UpdateProfileActivity.this, getString(R.string.update_password_success), Toast.LENGTH_SHORT).show();
                                                edtcrpw.setText("");
                                                edtnpw.setText("");
                                                edtcpw.setText("");
                                            } else {
                                                Toast.makeText(UpdateProfileActivity.this, getString(R.string.update_password_fail), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            // Nếu xác thực không thành công
                            Toast.makeText(UpdateProfileActivity.this, getString(R.string.auth_fail), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void buttonUpdateImageClick(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(UpdateProfileActivity.this, getString(R.string.update_image_success), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UpdateProfileActivity.this, getString(R.string.update_image_fail), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}