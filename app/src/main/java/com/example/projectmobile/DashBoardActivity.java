package com.example.projectmobile;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import de.hdodenhof.circleimageview.CircleImageView;

public class DashBoardActivity extends AppCompatActivity {
    private BarChart barChart;
    private PieChart pieChartLeft;
    private LineChart linechart;
    TextView todayCountValue, chooseCountValue, monthlyCountValue;
    private DatabaseReference mDatabase;

    TextView txt_name;
    CircleImageView avatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dash_board);

        avatar = findViewById(R.id.avatar);
        txt_name = findViewById(R.id.name);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.dashboard);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.home){
                    Intent intent = new Intent(DashBoardActivity.this, MapActivity.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.user){
                    Intent intent = new Intent(DashBoardActivity.this, UpdateProfileActivity.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.setting){
                    Intent intent = new Intent(DashBoardActivity.this, SettingActivity.class);
                    startActivity(intent);
                    finish();
                }
                return false;
            }
        });

        barChart = findViewById(R.id.barChart);
        pieChartLeft = findViewById(R.id.pieChartLeft);
        linechart = findViewById(R.id.lineChart);
        todayCountValue = findViewById(R.id.todayCount);
        todayCountValue.setTooltipText(getString(R.string.today_count));

        chooseCountValue = findViewById(R.id.chooseCount);

        monthlyCountValue = findViewById(R.id.monthlyCount);
        monthlyCountValue.setTooltipText(getString(R.string.monthly_count));

        mDatabase = FirebaseDatabase.getInstance().getReference("sharedPothole");
        getPotholesData();
        setupLineCharts();
        fetchDataForLast7Days();
        chooseCountValue.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String id = user.getUid();

            DatePickerDialog datePicker = new DatePickerDialog(DashBoardActivity.this, (view, selectedYear, selectedMonth, selectedDay) -> {
                // Định dạng ngày thành dd/MM/yyyy
                String selectedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                // Truy vấn ổ gà theo ngày từ thư mục sharedPothole
                mDatabase.orderByChild("date").equalTo(selectedDate).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                int count = 0;
                                for (DataSnapshot data : snapshot.getChildren()) {
                                    String id1 = data.child("id").getValue(String.class);
                                    if (id1.equals(id)) {
                                        count++;
                                    }
                                }
                                chooseCountValue.setText(String.valueOf(count));
                                chooseCountValue.setTooltipText(getString(R.string.choose_count) + selectedDate + getString(R.string.is) + count);
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                chooseCountValue.setText(getString(R.string.error_fetching_data));
                            }
                        });
            }, year, month, day);
            datePicker.show();
        });
    }

    private void fetchDataForLast7Days() {
        Calendar calendar = Calendar.getInstance();
        long currentTime = calendar.getTimeInMillis();
        long oneDayInMillis = 24 * 60 * 60 * 1000;

        // Danh sách BarEntry cho Stacked BarChart
        ArrayList<BarEntry> stackedEntries = new ArrayList<>();

        AtomicInteger daysFetched = new AtomicInteger(0); // Biến đếm số ngày đã lấy dữ liệu

        // Chạy qua 7 ngày
        for (int i = 6; i >= 0; i--) {
            final int dayIndex = 6 - i; // Để ngày cuối cùng là dayIndex = 0
            long dayStartTime = currentTime - (i * oneDayInMillis); // Tính thời gian bắt đầu cho mỗi ngày
            String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date(dayStartTime));

            mDatabase.orderByChild("date").equalTo(date).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String id = user.getUid();
                    int countLight = 0, countMedium = 0, countHeavy = 0;

                    for (DataSnapshot data : snapshot.getChildren()) {
                        String severity = data.child("severity").getValue(String.class);
                        String id1 = data.child("id").getValue(String.class);
                        if (severity != null && id1.equals(id)) {
                            switch (severity) {
                                case "Nhẹ":
                                    countLight++;
                                    break;
                                case "Vừa":
                                    countMedium++;
                                    break;
                                case "Nặng":
                                    countHeavy++;
                                    break;
                            }
                        }
                    }

                    // Thêm dữ liệu cho từng ngày vào danh sách Stacked Bar
                    stackedEntries.add(new BarEntry(dayIndex, new float[]{countLight, countMedium, countHeavy}));

                    // Kiểm tra số lượng ngày đã lấy dữ liệu
                    if (daysFetched.incrementAndGet() == 7) {
                        setupChart(stackedEntries); // Gọi setupChart với dữ liệu mới
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e("Firebase", "Error fetching data: " + error.getMessage());
                }
            });
        }
    }

    private void setupChart(ArrayList<BarEntry> stackedEntries) {
        // Kiểm tra số lượng entries
        if (stackedEntries.size() != 7) {
            Log.e("ChartData", "Số lượng entries không đúng: " + stackedEntries.size());
            return;
        }

        // Tạo danh sách nhãn ngày tháng
        ArrayList<String> labels = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());

        // Lùi lại 7 ngày và tạo nhãn cho trục X
        for (int i = 6; i >= 0; i--) {
            calendar.setTimeInMillis(System.currentTimeMillis() - i * 24 * 60 * 60 * 1000);
            labels.add(sdf.format(calendar.getTime()));
        }

        // Tạo BarDataSet với các giá trị stack
        BarDataSet stackedDataSet = new BarDataSet(stackedEntries, getString(R.string.potholebylevel));
        stackedDataSet.setColors(
                Color.parseColor("#4CAF50"), // Màu cho Nhẹ
                Color.parseColor("#2196F3"), // Màu cho Vừa
                Color.parseColor("#F44336")  // Màu cho Nặng
        );
        stackedDataSet.setStackLabels(new String[]{getString(R.string.nhe), getString(R.string.vua), getString(R.string.nang)}); // Nhãn các mức độ
        stackedDataSet.setValueTextColor(Color.BLACK);
        stackedDataSet.setValueTextSize(10f);
        stackedDataSet.setDrawValues(false);

        // Tạo BarData và gán vào BarChart
        BarData barData = new BarData(stackedDataSet);
        barData.setBarWidth(0.8f); // Độ rộng cột (giá trị từ 0.0 -> 1.0)

        barChart.setData(barData);

        // Tùy chỉnh trục X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f); // Mỗi giá trị ứng với một nhãn
        xAxis.setGranularityEnabled(true);
        xAxis.setPosition(XAxis.XAxisPosition.TOP); // Trục X bên trên
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels)); // Hiển thị nhãn ngày
        xAxis.setDrawGridLines(false); // Tắt lưới trục X

        // Tùy chỉnh trục Y
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setGranularity(1f); // Giá trị tăng tối thiểu
        leftAxis.setGranularityEnabled(true);

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setGranularity(1f); // Giá trị tăng tối thiểu
        rightAxis.setGranularityEnabled(true);

        // Tùy chỉnh biểu đồ
        barChart.getDescription().setText(getString(R.string.potholelast7days));
        barChart.getDescription().setTextSize(12f);
        barChart.getLegend().setEnabled(true); // Hiển thị chú thích
        barChart.invalidate(); // Cập nhật lại biểu đồ

        // Thêm sự kiện khi chọn giá trị trên biểu đồ
        barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e instanceof BarEntry) {
                    BarEntry barEntry = (BarEntry) e;

                    // Lấy giá trị stack tương ứng
                    float[] stackValues = barEntry.getYVals();
                    if (stackValues != null && h.getStackIndex() >= 0 && h.getStackIndex() < stackValues.length) {
                        // Lấy giá trị của stack được chọn
                        float selectedValue = stackValues[h.getStackIndex()];
                        String stackLabel = stackedDataSet.getStackLabels()[h.getStackIndex()];

                        // Hiển thị thông báo giá trị
                        String dateLabel = getString(R.string.date_label);
                        String message = String.format("%s: %.0f (%s: %s)", stackLabel, selectedValue, dateLabel, labels.get((int) barEntry.getX()));
                        Toast.makeText(barChart.getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onNothingSelected() {
                // Không làm gì khi không chọn gì
            }
        });
    }

    private void setupLineCharts() {
        // Khởi tạo các danh sách để lưu trữ dữ liệu cho từng mức độ
        ArrayList<Entry> nheData = new ArrayList<>();
        ArrayList<Entry> vuaData = new ArrayList<>();
        ArrayList<Entry> nangData = new ArrayList<>();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String id = user.getUid();
                // Đặt lại dữ liệu cũ mỗi khi có thay đổi trong cơ sở dữ liệu
                nheData.clear();
                vuaData.clear();
                nangData.clear();

                // Tạo mảng để đếm số lượng ổ gà theo từng tháng cho các mức độ
                int[] countNhe = new int[12];
                int[] countVua = new int[12];
                int[] countNang = new int[12];

                // Tính số lượng ổ gà theo từng tháng và mức độ
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String severity = snapshot.child("severity").getValue(String.class);
                    String date = snapshot.child("date").getValue(String.class);
                    String id1 = snapshot.child("id").getValue(String.class);
                    // Chuyển chuỗi ngày thành tháng trong năm
                    String[] dateParts = date.split("/");
                    int month = Integer.parseInt(dateParts[1]) - 1; // Cộng 1 để match với index của tháng

                    // Đếm số lượng ổ gà theo mức độ
                    if (severity != null && id1.equals(id)) {
                        if (severity.equals("Nhẹ")) {
                            countNhe[month]++;
                        } else if (severity.equals("Vừa")) {
                            countVua[month]++;
                        } else if (severity.equals("Nặng")) {
                            countNang[month]++;
                        }
                    }
                }

                // Tạo Entry cho từng mức độ ổ gà theo tháng
                for (int i = 0; i < 12; i++) {
                    nheData.add(new Entry(i, countNhe[i]));
                    vuaData.add(new Entry(i, countVua[i]));
                    nangData.add(new Entry(i, countNang[i]));
                }

                // Tạo LineDataSet cho từng mức độ ổ gà
                LineDataSet nheLineDataSet = new LineDataSet(nheData, getString(R.string.nhe));
                LineDataSet vuaLineDataSet = new LineDataSet(vuaData, getString(R.string.vua));
                LineDataSet nangLineDataSet = new LineDataSet(nangData, getString(R.string.nang));

                // Cấu hình LineDataSet
                nheLineDataSet.setColor(0xFF4CAF50);
                nheLineDataSet.setDrawValues(false);
                vuaLineDataSet.setColor(0xFF2196F3);
                vuaLineDataSet.setDrawValues(false);
                nangLineDataSet.setColor(0xFFF44336);
                nangLineDataSet.setDrawValues(false);

                // Tạo LineData
                LineData lineData = new LineData(nheLineDataSet, vuaLineDataSet, nangLineDataSet);

                // Cập nhật biểu đồ
                LineChart lineChart = findViewById(R.id.lineChart);
                lineChart.setData(lineData);

                XAxis xAxis = lineChart.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"}));
                xAxis.setGranularity(1f); // Đảm bảo rằng chỉ có 1 giá trị cho mỗi tháng
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                YAxis leftYAxis = lineChart.getAxisLeft();
                leftYAxis.setGranularity(1f); // Đặt độ phân giải cho trục Y bên trái
                leftYAxis.setGranularityEnabled(true); // Bật độ phân giải

                YAxis rightYAxis = lineChart.getAxisRight();
                rightYAxis.setGranularity(1f); // Đặt độ phân giải cho trục Y bên phải
                rightYAxis.setGranularityEnabled(true); // Bật độ phân giải

                lineChart.getDescription().setText(getString(R.string.potholelevelpermonth));

                lineChart.invalidate(); // Refresh biểu đồ
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }

    private void getPotholesData() {
        // Lấy dữ liệu từ Firebase
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String id = user.getUid();
                int todayPotholesCount = 0;
                int monthlyPotholesCount = 0;
                int lightCount = 0, mediumCount = 0, heavyCount = 0;
                Calendar calendar = Calendar.getInstance();
                int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
                int currentMonth = calendar.get(Calendar.MONTH) + 1;
                int currentYear = calendar.get(Calendar.YEAR);

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String date = snapshot.child("date").getValue(String.class);
                    String id1 = snapshot.child("id").getValue(String.class);
                    String severity = snapshot.child("severity").getValue(String.class);
                    String[] dateParts = date.split("/");
                    int potholeDay = Integer.parseInt(dateParts[0]);
                    int potholeMonth = Integer.parseInt(dateParts[1]);
                    int potholeYear = Integer.parseInt(dateParts[2]);

                    if (potholeYear == currentYear && potholeMonth == currentMonth && potholeDay == currentDay && id1.equals(id)) {
                        todayPotholesCount++;
                    }

                    if (potholeYear == currentYear && potholeMonth == currentMonth && id1.equals(id)) {
                        monthlyPotholesCount++;
                    }

                    if (id1.equals(id)) {
                        if (severity.equals("Nhẹ")) {
                            lightCount++;
                        } else if (severity.equals("Vừa")) {
                            mediumCount++;
                        } else if (severity.equals("Nặng")) {
                            heavyCount++;
                        }
                        int totalCount = lightCount + mediumCount + heavyCount;

                        showPieChart(lightCount, mediumCount, heavyCount, totalCount);
                    }
                }

                todayCountValue.setText(String.valueOf(todayPotholesCount));

                monthlyCountValue.setText(String.valueOf(monthlyPotholesCount));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Xử lý lỗi khi đọc dữ liệu từ Firebase
            }
        });
    }
    private void showPieChart(int lightCount, int mediumCount, int heavyCount, int totalCount) {
        // Tạo danh sách các PieEntry với tỷ lệ phần trăm
        ArrayList<PieEntry> entries = new ArrayList<>();

        if (totalCount > 0) {
            // Tính phần trăm cho mỗi severity
            float lightPercentage = (lightCount / (float) totalCount) * 100;
            float mediumPercentage = (mediumCount / (float) totalCount) * 100;
            float heavyPercentage = (heavyCount / (float) totalCount) * 100;

            // Thêm PieEntry với phần trăm
            if (lightCount > 0) {
                entries.add(new PieEntry(lightPercentage, getString(R.string.nhe)));
            }
            if (mediumCount > 0) {
                entries.add(new PieEntry(mediumPercentage, getString(R.string.vua)));
            }
            if (heavyCount > 0) {
                entries.add(new PieEntry(heavyPercentage, getString(R.string.nang)));
            }
        }

        // Tạo PieDataSet và thêm màu sắc cho các phần
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{0xFF4CAF50, 0xFF2196F3, 0xFFF44336});  // Màu sắc của các mức độ
        dataSet.setDrawValues(false);

        // Tạo PieData từ PieDataSet
        PieData pieData = new PieData(dataSet);

        pieChartLeft.setData(pieData);

        // Cấu hình mô tả biểu đồ và hiển thị phần trăm
        pieChartLeft.getDescription().setText(getString(R.string.potholeleveldetect));
        pieChartLeft.setDrawEntryLabels(true);  // Hiển thị nhãn
        pieChartLeft.setUsePercentValues(true); // Sử dụng phần trăm
        pieChartLeft.invalidate();  // Cập nhật lại biểu đồ

        pieChartLeft.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                if (e instanceof PieEntry) {
                    PieEntry entry = (PieEntry) e;
                    String label = entry.getLabel(); // Lấy nhãn (Nhẹ, Vừa, Nặng)
                    float value = entry.getValue(); // Lấy phần trăm
                    String percentText = String.format("%s: %.1f%%", label, value);

                    // Hiển thị phần trăm ở giữa biểu đồ hoặc thông qua Toast
                    pieChartLeft.setCenterText(percentText); // Hiển thị ở giữa
                    // Toast.makeText(getApplicationContext(), percentText, Toast.LENGTH_SHORT).show(); // (Tùy chọn)
                }
            }

            @Override
            public void onNothingSelected() {
                pieChartLeft.setCenterText(""); // Xóa text khi không chọn gì
            }
        });
    }

    private void showUserInformation(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null){
            return;
        }
        //String email = user.getEmail();
        //txt_Email.setText(email);
        String name = user.getDisplayName();
        if (name == null){
            txt_name.setVisibility(View.GONE);
        } else {
            txt_name.setVisibility(View.VISIBLE);
            txt_name.setText(name);
        }

        Uri photo = user.getPhotoUrl();
        Glide.with(this).load(photo).error(R.drawable.outline_account_circle_24).into(avatar);
    }
}