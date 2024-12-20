package com.example.projectmobile;

import static com.mapbox.maps.plugin.animation.CameraAnimationsUtils.getCamera;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.addOnMapClickListener;
import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;
import static com.mapbox.navigation.base.extensions.RouteOptionsExtensions.applyDefaultNavigationOptions;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.Bearing;
import com.mapbox.api.directions.v5.models.RouteOptions;
import com.mapbox.bindgen.Expected;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.EdgeInsets;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImplKt;
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.maps.plugin.gestures.OnMapClickListener;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentConstants;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.generated.LocationComponentSettings;
import com.mapbox.navigation.base.options.NavigationOptions;
import com.mapbox.navigation.base.route.NavigationRoute;
import com.mapbox.navigation.base.route.NavigationRouterCallback;
import com.mapbox.navigation.base.route.RouterFailure;
import com.mapbox.navigation.base.route.RouterOrigin;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.directions.session.RoutesObserver;
import com.mapbox.navigation.core.directions.session.RoutesUpdatedResult;
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp;
import com.mapbox.navigation.core.trip.session.LocationMatcherResult;
import com.mapbox.navigation.core.trip.session.LocationObserver;
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer;
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView;
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineError;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources;
import com.mapbox.navigation.ui.maps.route.line.model.RouteSetValue;
import com.mapbox.search.autocomplete.PlaceAutocomplete;
import com.mapbox.search.autocomplete.PlaceAutocompleteSuggestion;
import com.mapbox.search.ui.adapter.autocomplete.PlaceAutocompleteUiAdapter;
import com.mapbox.search.ui.view.CommonSearchViewConfiguration;
import com.mapbox.search.ui.view.SearchResultsView;
import com.mapbox.turf.TurfMeasurement;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.Collections;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.jvm.functions.Function1;

public class MapActivity extends AppCompatActivity implements SensorEventListener {
    MapView mapview;
    MaterialButton setRoute;
    FloatingActionButton fab, fabadd, fabClear;
    ExtendedFloatingActionButton fabspeed;
    Point point;
    DatabaseReference reference = null;
    SharePothole location;
    private PointAnnotationManager pointAnnotationManager1;
    private PointAnnotationManager pointAnnotationManager;

    private final NavigationLocationProvider navigationLocationProvider = new NavigationLocationProvider();
    private MapboxRouteLineView routeLineView;
    private MapboxRouteLineApi routeLineApi;

    private String permissionGranted;
    private final Set<String> warnedPotholes = new HashSet<>();

    private List<Point> routePoints = new ArrayList<>();

    private boolean isNavigationStopped = false;

    boolean isDestinationSet = false;

    //Sensor Accelerometer
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Button confirmButton;
    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];
    private static final float BUMP_THRESHOLD = 5.0f;
    private static final float STATIC_THRESHOLD = 0.5f;
    private float lastAcceleration = 0;
    private float maxAccelerationInSession = 0;
    private boolean isBumpSessionActive = false;
    private boolean isDeviceStatic = true; // Cờ trạng thái tĩnh của thiết bị
    private long lastStaticCheckTime = 0;
    private long bumpSessionStartTime = 0;
    private static final long SESSION_DURATION = 2000; // 2 giây để kết thúc một phiên phát hiện ổ gà
    private static final long STATIC_CHECK_DURATION = 500; // 500ms để kiểm tra trạng thái tĩnh
    private boolean isFirstReading = true;

    private final LocationObserver locationObserver = new LocationObserver() {
        @Override
        public void onNewRawLocation(@NonNull Location location) {
            // Cập nhật vị trí mới nếu cần
        }

        @Override
        public void onNewLocationMatcherResult(@NonNull LocationMatcherResult locationMatcherResult) {
            Location location = locationMatcherResult.getEnhancedLocation();
            navigationLocationProvider.changePosition(location, locationMatcherResult.getKeyPoints(), null, null);

            // Lấy vị trí hiện tại của người dùng
            double userLatitude = location.getLatitude();
            double userLongitude = location.getLongitude();
            float speed = location.getSpeed();

            // Kiểm tra khoảng cách với các ổ gà
            checkProximityToPotholes(userLatitude, userLongitude);

            updateSpeedOnFAB(speed);

            if (focusLocation) {
                updateCamera(Point.fromLngLat(location.getLongitude(), location.getLatitude()), (double) location.getBearing());
                point = Point.fromLngLat(location.getLongitude(), location.getLatitude());
            }
        }
    };

    private final RoutesObserver routesObserver = new RoutesObserver() {
        @Override
        public void onRoutesChanged(@NonNull RoutesUpdatedResult routesUpdatedResult) {
            routeLineApi.setNavigationRoutes(routesUpdatedResult.getNavigationRoutes(), new MapboxNavigationConsumer<Expected<RouteLineError, RouteSetValue>>() {
                @Override
                public void accept(Expected<RouteLineError, RouteSetValue> routeLineErrorRouteSetValueExpected) {
                    mapview.getMapboxMap().getStyle(style -> {
                        if (style != null) {
                            routeLineView.renderRouteDrawData(style, routeLineErrorRouteSetValueExpected);
                        }
                    });
                }
            });
        }
    };

    boolean focusLocation = true;
    private MapboxNavigation mapboxNavigation;
    private void updateCamera(Point point, Double bearing) {
        MapAnimationOptions animationOptions = new MapAnimationOptions.Builder().duration(1500L).build();
        CameraOptions cameraOptions = new CameraOptions.Builder().center(point).zoom(16.5)
                .padding(new EdgeInsets(0.0, 0.0, 0.0, 0.0)).bearing(bearing).build();

        getCamera(mapview).easeTo(cameraOptions, animationOptions);
    }

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            if (result) {
                // Quyền đã được cấp, kích hoạt các tính năng liên quan
                if (Manifest.permission.ACCESS_FINE_LOCATION.equals(permissionGranted)) {
                    startLocationUpdates();
                } else if (Manifest.permission.POST_NOTIFICATIONS.equals(permissionGranted)) {
                    enableNotifications();
                }
                recreate();
            } else {

            }
        }
    });

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapboxNavigation.startTripSession();
            fab.hide();
            LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapview);
            locationComponentPlugin.setEnabled(true);
            locationComponentPlugin.setLocationProvider(navigationLocationProvider);
        }
    }

    private void enableNotifications() {
        // Đây là nơi cấu hình cho thông báo nếu cần
        Toast.makeText(this, getString(R.string.notienable), Toast.LENGTH_SHORT).show();
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            startLocationUpdates(); // Nếu quyền đã cấp, kích hoạt ngay lập tức
        }
    }

    private final OnMoveListener onMoveListener = new OnMoveListener() {
        @Override
        public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {
            focusLocation = false;
            getGestures(mapview).removeOnMoveListener(this);
            fab.show();
        }

        @Override
        public boolean onMove(@NonNull MoveGestureDetector moveGestureDetector) {
            return false;
        }

        @Override
        public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {
        }
    };

    private void checkProximityToPotholes(double userLatitude, double userLongitude) {
        FirebaseDatabase.getInstance().getReference().child("sharedPothole").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SharePothole pothole = dataSnapshot.getValue(SharePothole.class);
                    if (pothole != null) {
                        String potholeId = dataSnapshot.getKey();

                        double potholeLatitude = pothole.getLatitude();
                        double potholeLongitude = pothole.getLongitude();

                        if (routePoints.isEmpty()) {
                            // Không có route
                            float distanceToUser = calculateDistance(userLatitude, userLongitude, potholeLatitude, potholeLongitude);
                            if (!warnedPotholes.contains(potholeId) && distanceToUser < 50) {
                                triggerPotholeAlert(pothole);
                                warnedPotholes.add(potholeId);
                            } else if (warnedPotholes.contains(potholeId) && distanceToUser > 60) {
                                warnedPotholes.remove(potholeId);
                            }
                        } else {
                            // Có route, kiểm tra ổ gà gần route
                            boolean isPotholeOnRoute = false;
                            for (Point routePoint : routePoints) {
                                float distanceToRoutePoint = calculateDistance(routePoint.latitude(), routePoint.longitude(), potholeLatitude, potholeLongitude);
                                if (distanceToRoutePoint <= 2) {
                                    isPotholeOnRoute = true;
                                    break;
                                }
                            }

                            if (isPotholeOnRoute) {
                                float distanceToUser = calculateDistance(userLatitude, userLongitude, potholeLatitude, potholeLongitude);
                                if (!warnedPotholes.contains(potholeId) && distanceToUser < 50) {
                                    triggerPotholeAlert(pothole);
                                    warnedPotholes.add(potholeId);
                                } else if (warnedPotholes.contains(potholeId) && distanceToUser > 60) {
                                    warnedPotholes.remove(potholeId);
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi lấy dữ liệu ổ gà", error.toException());
            }
        });
    }

    // Tính khoảng cách giữa hai tọa độ (mét)
    private float calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Bán kính trái đất (km)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (float) (R * c * 1000); // Trả về khoảng cách theo mét
    }

    private void triggerPotholeAlert(SharePothole pothole) {
        // Rung điện thoại
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(2000, 255));
        }

        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.alert);
        mediaPlayer.start();

        // Hiển thị thông báo
        Toast.makeText(this, getString(R.string.potholewarning), Toast.LENGTH_LONG).show();

        mediaPlayer.setOnCompletionListener(mp -> {
            mediaPlayer.release();
        });
    }

    //Search
    private PlaceAutocomplete placeAutocomplete;
    private SearchResultsView searchResultsView;
    private PlaceAutocompleteUiAdapter placeAutocompleteUiAdapter;
    private TextInputEditText searchET;
    private Boolean ignoreNextQueryUpdate = false;

    private Handler handler = new Handler();
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);

        //Sensor Accelerometer
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //Search
        placeAutocomplete = PlaceAutocomplete.create(getString(R.string.mapbox_access_token));
        searchET = findViewById(R.id.searchET);
        searchResultsView = findViewById(R.id.searchResultsView);
        searchResultsView.initialize(new SearchResultsView.Configuration(new CommonSearchViewConfiguration()));
        placeAutocompleteUiAdapter = new PlaceAutocompleteUiAdapter(searchResultsView, placeAutocomplete, LocationEngineProvider.getBestLocationEngine(MapActivity.this));

        MapboxRouteLineOptions options = new MapboxRouteLineOptions.Builder(this).withRouteLineResources(new RouteLineResources.Builder().build())
                .withRouteLineBelowLayerId(LocationComponentConstants.LOCATION_INDICATOR_LAYER).build();
        routeLineView = new MapboxRouteLineView(options);
        routeLineApi = new MapboxRouteLineApi(options);

        NavigationOptions navigationOptions = new NavigationOptions.Builder(this).accessToken(getString(R.string.mapbox_access_token)).build();

        MapboxNavigationApp.setup(navigationOptions);
        mapboxNavigation = new MapboxNavigation(navigationOptions);

        mapboxNavigation.registerRoutesObserver(routesObserver);
        mapboxNavigation.registerLocationObserver(locationObserver);

        //Maker Pothole
        FirebaseApp.initializeApp(this);
        location = new SharePothole();

        mapview = findViewById(R.id.mapView);
        fabClear = findViewById(R.id.fabClear);
        fabClear.hide();
        fabadd = findViewById(R.id.fabadd);
        fabspeed = findViewById(R.id.fabspeed);
        fab = findViewById(R.id.fab);
        fab.hide();
        setRoute = findViewById(R.id.setRoute);

        checkPermissions();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                activityResultLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            activityResultLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
        } else {
            mapboxNavigation.startTripSession();
        }

        LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapview);
        getGestures(mapview).addOnMoveListener(onMoveListener);

        mapview.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                mapview.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(16.5).build());
                locationComponentPlugin.setEnabled(true);
                locationComponentPlugin.setLocationProvider(navigationLocationProvider);
                getGestures(mapview).addOnMoveListener(onMoveListener);

                locationComponentPlugin.updateSettings(new Function1<LocationComponentSettings, Unit>() {
                    @Override
                    public Unit invoke(LocationComponentSettings locationComponentSettings) {
                        locationComponentSettings.setEnabled(true);
                        locationComponentSettings.setPulsingEnabled(true);
                        return null;
                    }
                });

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.warning1);
                Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.location);
                Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.warning2);
                Bitmap bitmap3 = BitmapFactory.decodeResource(getResources(), R.drawable.warning3);
                AnnotationPlugin annotationPlugin = AnnotationPluginImplKt.getAnnotations(mapview);
                pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, new AnnotationConfig());
                pointAnnotationManager1 = PointAnnotationManagerKt.createPointAnnotationManager(annotationPlugin, new AnnotationConfig());

                addOnMapClickListener(mapview.getMapboxMap(), new OnMapClickListener() {
                    @Override
                    public boolean onMapClick(@NonNull Point point) {
                        pointAnnotationManager1.deleteAll();
                        PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions().withTextAnchor(TextAnchor.CENTER).withIconImage(bitmap1)
                                .withPoint(point);
                        pointAnnotationManager1.create(pointAnnotationOptions);
                        fabClear.show();
                        isDestinationSet = true;

                        setRoute.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (isDestinationSet){
                                    fetchRoute(point);
                                } else {
                                    Toast.makeText(MapActivity.this, getString(R.string.plserlocatinmap), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        return true;
                    }
                });

                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        focusLocation = true;
                        getGestures(mapview).addOnMoveListener(onMoveListener);
                        fab.hide();
                    }
                });

                FirebaseDatabase.getInstance().getReference().child("sharedPothole").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pointAnnotationManager.deleteAll();
                        snapshot.getChildren().forEach(new Consumer<DataSnapshot>() {
                            @Override
                            public void accept(DataSnapshot dataSnapshot) {
                                SharePothole location1 = dataSnapshot.getValue(SharePothole.class);
                                if (location1 != null && !location1.getId().equals(MapActivity.this.location.getId())) {
                                    if (bitmap != null && bitmap2 != null && bitmap3 != null) {
                                        String severity = location1.getSeverity();
                                        Bitmap selectedBitmap = null;
                                        if ("Nhẹ".equals(severity)) {
                                            selectedBitmap = bitmap;  // Sử dụng bitmap cho mức độ nhẹ
                                        } else if ("Vừa".equals(severity)) {
                                            selectedBitmap = bitmap2;  // Sử dụng bitmap cho mức độ vừa
                                        } else if ("Nặng".equals(severity)) {
                                            selectedBitmap = bitmap3;  // Sử dụng bitmap cho mức độ nặng
                                        }
                                        if (selectedBitmap != null) {
                                            // Tạo annotation cho ổ gà
                                            PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                                                    .withTextAnchor(TextAnchor.CENTER)
                                                    .withIconImage(selectedBitmap)
                                                    .withPoint(Point.fromLngLat(location1.getLongitude(), location1.getLatitude()));

                                            pointAnnotationManager.create(pointAnnotationOptions);
                                        } else {
                                            Toast.makeText(MapActivity.this, getString(R.string.errornotfoundbitmap), Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(MapActivity.this, getString(R.string.errorbitmapnotinitialize), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });

                        pointAnnotationManager.addClickListener(new OnPointAnnotationClickListener() {
                            @Override
                            public boolean onAnnotationClick(@NonNull PointAnnotation pointAnnotation) {
                                snapshot.getChildren().forEach(new Consumer<DataSnapshot>() {
                                    @Override
                                    public void accept(DataSnapshot dataSnapshot) {
                                        SharePothole location1 = dataSnapshot.getValue(SharePothole.class);
                                        // Kiểm tra vị trí của marker và dữ liệu trong Firebase có trùng khớp không
                                        if (location1 != null && pointAnnotation.getPoint().longitude() == location1.getLongitude() && pointAnnotation.getPoint().latitude() == location1.getLatitude()) {
                                            String severityLocalized = "";
                                            switch (location1.getSeverity()) {
                                                case "Nhẹ":
                                                    severityLocalized = getString(R.string.nhe);
                                                    break;
                                                case "Vừa":
                                                    severityLocalized = getString(R.string.vua);
                                                    break;
                                                case "Nặng":
                                                    severityLocalized = getString(R.string.nang);
                                                    break;
                                            }

                                            String info = getString(R.string.contributorname) + location1.getName() + "\n" +
                                                    getString(R.string.location) + location1.getLatitude() + ", " + location1.getLongitude() + "\n" +
                                                    getString(R.string.contributiondate) + location1.getDate() + "\n" +
                                                    getString(R.string.severity) + severityLocalized;

                                            // Tạo Dialog để hiển thị thông tin
                                            new AlertDialog.Builder(MapActivity.this)
                                                    .setTitle(getString(R.string.potholeinfo))
                                                    .setMessage(info)  // Hiển thị thông tin ổ gà
                                                    .setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();  // Đóng Dialog khi nhấn "Đóng"
                                                        }
                                                    })
                                                    .setCancelable(true)  // Cho phép đóng Dialog khi nhấn ngoài vùng dialog
                                                    .show();
                                        }
                                    }
                                });
                                return true;
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }

                });

                fabadd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user == null) {
                            return;
                        }
                        String id = user.getUid();
                        String name = user.getDisplayName();
                        String currentDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

                        String[] severityOptions = {getString(R.string.nhe), getString(R.string.vua), getString(R.string.nang)};
                        final String[] selectedSeverity = new String[1];  // Sử dụng mảng để giữ giá trị

                        // Tạo dialog để người dùng chọn mức độ
                        new AlertDialog.Builder(MapActivity.this)
                                .setTitle(getString(R.string.selectpotholelevel))
                                .setSingleChoiceItems(severityOptions, 0, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Lưu mức độ người dùng chọn vào selectedSeverity
                                        selectedSeverity[0] = severityOptions[which];
                                        if (selectedSeverity[0].equals("Mild") || selectedSeverity[0].equals("Léger")) {
                                            selectedSeverity[0] = "Nhẹ";
                                        } else if (selectedSeverity[0].equals("Moderate") || selectedSeverity[0].equals("Modéré")) {
                                            selectedSeverity[0] = "Vừa";
                                        } else if (selectedSeverity[0].equals("Severe") || selectedSeverity[0].equals("Sévère")) {
                                            selectedSeverity[0] = "Nặng";
                                        }
                                    }
                                })
                                .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Chia sẻ vị trí và thông tin ổ gà
                                        Toast.makeText(MapActivity.this, getString(R.string.sharingpothole), Toast.LENGTH_SHORT).show();

                                        // Tạo reference mới mỗi lần nhấn nút
                                        DatabaseReference newReference = FirebaseDatabase.getInstance().getReference().child("sharedPothole").push();

                                        // Tạo thông tin ổ gà mới
                                        SharePothole location = new SharePothole();
                                        location.setId(newReference.getKey());  // Lấy key của reference mới tạo
                                        location.setId(id);
                                        location.setName(name);
                                        location.setLongitude(point.longitude());
                                        location.setLatitude(point.latitude());
                                        location.setDate(currentDate);
                                        location.setSeverity(selectedSeverity[0]);  // Sử dụng giá trị từ selectedSeverity

                                        // Lưu ổ gà vào Firebase
                                        newReference.setValue(location);
                                    }
                                })
                                .setNegativeButton(getString(R.string.cancel), null)  // Nếu người dùng không muốn chọn mức độ, có thể hủy
                                .show();

                    }
                });

                fabClear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mapboxNavigation.setNavigationRoutes(Collections.emptyList());
                        routePoints.clear();
                        fabClear.hide();
                        isDestinationSet = false;
                        pointAnnotationManager1.deleteAll();
                    }
                });

                placeAutocompleteUiAdapter.addSearchListener(new PlaceAutocompleteUiAdapter.SearchListener() {
                    @Override
                    public void onSuggestionsShown(@NonNull List<PlaceAutocompleteSuggestion> list) {

                    }

                    @Override
                    public void onSuggestionSelected(@NonNull PlaceAutocompleteSuggestion placeAutocompleteSuggestion) {
                        ignoreNextQueryUpdate = true;
                        focusLocation = false;
                        searchET.setText(placeAutocompleteSuggestion.getName());
                        searchResultsView.setVisibility(View.GONE);
                        pointAnnotationManager1.deleteAll();
                        PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions().withTextAnchor(TextAnchor.CENTER).withIconImage(bitmap1)
                                .withPoint(placeAutocompleteSuggestion.getCoordinate());
                        pointAnnotationManager1.create(pointAnnotationOptions);
                        updateCamera(placeAutocompleteSuggestion.getCoordinate(), 0.0);

                        isDestinationSet = true;

                        setRoute.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (isDestinationSet) {
                                    fetchRoute(placeAutocompleteSuggestion.getCoordinate());
                                    fabClear.show();
                                } else {
                                    Toast.makeText(MapActivity.this, getString(R.string.plserlocatinmap), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onPopulateQueryClick(@NonNull PlaceAutocompleteSuggestion placeAutocompleteSuggestion) {
                        //queryEditText.setText(placeAutocompleteSuggestion.getName());
                    }

                    @Override
                    public void onError(@NonNull Exception e) {

                    }
                });
            }
        });

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // Không làm gì
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Nếu flag ignoreNextQueryUpdate đang bật, bỏ qua
                if (ignoreNextQueryUpdate) {
                    ignoreNextQueryUpdate = false;
                } else {
                    // Hủy bỏ yêu cầu trước đó nếu người dùng tiếp tục gõ
                    handler.removeCallbacks(searchRunnable);

                    // Tạo một yêu cầu tìm kiếm mới sau 500ms khi người dùng ngừng nhập
                    searchRunnable = new Runnable() {
                        @Override
                        public void run() {
                            String query = charSequence.toString();
                            // Thực hiện tìm kiếm sau khi người dùng ngừng gõ
                            placeAutocompleteUiAdapter.search(query, new Continuation<Unit>() {
                                @NonNull
                                @Override
                                public CoroutineContext getContext() {
                                    return EmptyCoroutineContext.INSTANCE;
                                }

                                @Override
                                public void resumeWith(@NonNull Object o) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            searchResultsView.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                            });
                        }
                    };

                    // Đặt lại delay 1000ms để gửi yêu cầu
                    handler.postDelayed(searchRunnable, 1000);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Không làm gì
            }
        });

        setRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MapActivity.this, getString(R.string.plserlocatinmap), Toast.LENGTH_SHORT).show();
            }
        });

        //BottomNavigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.home);
        // Gắn sự kiện khi click vào các item
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.dashboard) {
                    Intent intent = new Intent(MapActivity.this, DashBoardActivity.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.user) {
                    Intent intent = new Intent(MapActivity.this, UpdateProfileActivity.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.setting) {
                    Intent intent = new Intent(MapActivity.this, SettingActivity.class);
                    startActivity(intent);
                    finish();
                }
                return false;
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void fetchRoute(Point destination) {
        isNavigationStopped = false;
        LocationEngine locationEngine = LocationEngineProvider.getBestLocationEngine(MapActivity.this);
        locationEngine.getLastLocation(new LocationEngineCallback<LocationEngineResult>() {
            @Override
            public void onSuccess(LocationEngineResult result) {
                Location location = result.getLastLocation();
                setRoute.setEnabled(false);
                setRoute.setText(getString(R.string.fetchingroute));

                RouteOptions.Builder builder = RouteOptions.builder()
                        .language("vi")
                        .steps(true)
                        .profile(DirectionsCriteria.PROFILE_DRIVING);

                Point origin = Point.fromLngLat(Objects.requireNonNull(location).getLongitude(), location.getLatitude());
                builder.coordinatesList(Arrays.asList(origin, destination));
                builder.alternatives(false);
                builder.profile(DirectionsCriteria.PROFILE_DRIVING);
                builder.bearingsList(Arrays.asList(Bearing.builder().angle(location.getBearing()).degrees(45.0).build(), null));
                applyDefaultNavigationOptions(builder);

                mapboxNavigation.requestRoutes(builder.build(), new NavigationRouterCallback() {
                    @Override
                    public void onRoutesReady(@NonNull List<NavigationRoute> list, @NonNull RouterOrigin routerOrigin) {
                        NavigationRoute route = list.get(0);
                        String geometry = route.getDirectionsRoute().geometry();

                        // Lấy các điểm từ tuyến đường
                        List<Point> originalRoutePoints = LineString.fromPolyline(geometry, 6).coordinates();

                        // Làm mịn tuyến đường với khoảng cách mỗi điểm là 2m
                        routePoints = densifyRoute(originalRoutePoints, 2.0);

                        // Đếm số ổ gà trên tuyến đường
                        countPotholesOnRoute();

                        fab.performClick();
                        mapboxNavigation.setNavigationRoutes(list);
                        setRoute.setEnabled(true);
                        setRoute.setText(getString(R.string.SetRoute));

                        mapboxNavigation.registerLocationObserver(new LocationObserver() {
                            @Override
                            public void onNewRawLocation(@NonNull Location rawLocation) {
                            }

                            @Override
                            public void onNewLocationMatcherResult(@NonNull LocationMatcherResult locationMatcherResult) {
                                Location currentLocation = locationMatcherResult.getEnhancedLocation();
                                Location destinationLocation = new Location("");
                                destinationLocation.setLatitude(destination.latitude());
                                destinationLocation.setLongitude(destination.longitude());

                                float distanceToDestination = currentLocation.distanceTo(destinationLocation);  // Khoảng cách tính theo mét

                                // Kiểm tra nếu người dùng đã đến đích
                                if (distanceToDestination < 5 && !isNavigationStopped) {  // Ngưỡng 5m, chỉ thực hiện khi chưa dừng
                                    isNavigationStopped = true; // Đánh dấu đã dừng dẫn đường
                                    stopNavigation();          // Dừng dẫn đường nếu đến đích
                                }

                                if (!isUserOnRoute(currentLocation, list.get(0))) {
                                    // If user is off route, fetch a new route
                                    fetchRoute(destination);
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(@NonNull List<RouterFailure> list, @NonNull RouteOptions routeOptions) {
                        setRoute.setEnabled(true);
                        setRoute.setText(getString(R.string.SetRoute));
                        Toast.makeText(MapActivity.this, getString(R.string.routerequestfail), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCanceled(@NonNull RouteOptions routeOptions, @NonNull RouterOrigin routerOrigin) {

                    }
                });
            }

            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(MapActivity.this, getString(R.string.locationrequestfail), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isUserOnRoute(Location currentLocation, NavigationRoute route) {
        // Define acceptable deviation threshold in meters
        double deviationThreshold = 50.0;

        // Calculate distance from user's location to nearest point on the route
        Point currentPoint = Point.fromLngLat(currentLocation.getLongitude(), currentLocation.getLatitude());
        double distanceFromRoute = TurfMeasurement.distance(currentPoint, route.getRouteOptions().coordinatesList().get(0));

        // If user is more than deviation threshold away from the route, return false
        return distanceFromRoute < deviationThreshold;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapboxNavigation.onDestroy();
        mapboxNavigation.unregisterRoutesObserver(routesObserver);
        mapboxNavigation.unregisterLocationObserver(locationObserver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (isFirstReading) {
                isFirstReading = false; // Đã qua lần đọc đầu tiên
                lastAcceleration = (float) Math.sqrt(
                        event.values[0] * event.values[0] +
                                event.values[1] * event.values[1] +
                                event.values[2] * event.values[2]
                ); // Khởi tạo giá trị gia tốc
                return; // Bỏ qua xử lý lần đầu tiên
            }

            final float alpha = 0.8f;
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            linear_acceleration[0] = event.values[0] - gravity[0];
            linear_acceleration[1] = event.values[1] - gravity[1];
            linear_acceleration[2] = event.values[2] - gravity[2];

            float acceleration = (float) Math.sqrt(
                    linear_acceleration[0] * linear_acceleration[0] +
                            linear_acceleration[1] * linear_acceleration[1] +
                            linear_acceleration[2] * linear_acceleration[2]
            );

            // Kiểm tra trạng thái tĩnh
            long currentTime = System.currentTimeMillis();
            if (acceleration < STATIC_THRESHOLD) {
                // Nếu gia tốc nhỏ hơn ngưỡng STATIC_THRESHOLD, thiết bị có thể đang tĩnh
                if (currentTime - lastStaticCheckTime > STATIC_CHECK_DURATION) {
                    isDeviceStatic = true;
                }
            } else {
                // Nếu gia tốc vượt quá STATIC_THRESHOLD, thiết bị không còn tĩnh
                isDeviceStatic = false;
                lastStaticCheckTime = currentTime;
            }

            if (!isBumpSessionActive && !isDeviceStatic) {
                // Nếu không có phiên và thiết bị không tĩnh, kiểm tra dao động đột ngột
                float deltaAcceleration = Math.abs(acceleration - lastAcceleration);
                if (deltaAcceleration > BUMP_THRESHOLD) {
                    // Bắt đầu phiên phát hiện ổ gà
                    isBumpSessionActive = true;
                    bumpSessionStartTime = currentTime;
                    maxAccelerationInSession = acceleration;
                    Toast.makeText(MapActivity.this, getString(R.string.detectingpothole), Toast.LENGTH_SHORT).show();
                }
            } else if (isBumpSessionActive) {
                // Nếu có phiên, cập nhật gia tốc lớn nhất
                maxAccelerationInSession = Math.max(maxAccelerationInSession, acceleration);

                // Kiểm tra xem phiên đã kết thúc chưa
                if (currentTime - bumpSessionStartTime > SESSION_DURATION) {
                    isBumpSessionActive = false;
                    String severity = classifyBump(maxAccelerationInSession);
                    String severity1;

                    if (severity.equals("Nhẹ")){
                        severity1 = getString(R.string.nhe);
                    } else if (severity.equals("Vừa")){
                        severity1 = getString(R.string.vua);
                    } else {
                        severity1 = getString(R.string.nang);
                    }

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user == null) {
                        return;
                    }
                    String id = user.getUid();
                    String name = user.getDisplayName();
                    String currentDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

                    // Hiển thị AlertDialog
                    AlertDialog alertDialog = new AlertDialog.Builder(MapActivity.this)
                            .setTitle(getString(R.string.selectpotholelevel))
                            .setMessage(getString(R.string.detect1pothole) + severity1 + getString(R.string.wanttosaveinfo))
                            .setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Chia sẻ vị trí và thông tin ổ gà
                                    Toast.makeText(MapActivity.this, getString(R.string.sharingpothole), Toast.LENGTH_SHORT).show();

                                    // Tạo reference mới mỗi lần nhấn nút
                                    DatabaseReference newReference = FirebaseDatabase.getInstance().getReference().child("sharedPothole").push();

                                    // Tạo thông tin ổ gà mới
                                    SharePothole location = new SharePothole();
                                    location.setId(newReference.getKey());  // Lấy key của reference mới tạo
                                    location.setId(id);
                                    location.setName(name);
                                    location.setLongitude(point.longitude());
                                    location.setLatitude(point.latitude());
                                    location.setDate(currentDate);
                                    location.setSeverity(severity);  // Sử dụng giá trị từ selectedSeverity

                                    // Lưu ổ gà vào Firebase
                                    newReference.setValue(location);
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel), null) // Nếu người dùng không muốn chọn mức độ, có thể hủy
                            .show();

                    // Tạo Handler để tự động đóng AlertDialog sau 3 giây
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (alertDialog.isShowing()) {
                                alertDialog.dismiss(); // Đóng AlertDialog nếu còn hiển thị
                            }
                        }
                    }, 3000); // Thời gian trì hoãn là 3000ms (3 giây)
                }
            }
            lastAcceleration = acceleration;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Không cần xử lý
    }

    private String classifyBump(float acceleration) {
        if (acceleration < 20) {
            return "Nhẹ";
        } else if (acceleration < 40) {
            return "Vừa";
        } else {
            return "Nặng";
        }
    }

    private void stopNavigation() {
        mapboxNavigation.setNavigationRoutes(Collections.emptyList());
        routePoints.clear();
        pointAnnotationManager1.deleteAll();
        setRoute.setEnabled(true);
        Toast.makeText(MapActivity.this, getString(R.string.arrived), Toast.LENGTH_SHORT).show();
    }

    private List<Point> densifyRoute(List<Point> routePoints, double intervalMeters) {
        List<Point> densifiedPoints = new ArrayList<>();

        for (int i = 0; i < routePoints.size() - 1; i++) {
            Point start = routePoints.get(i);
            Point end = routePoints.get(i + 1);

            // Tính khoảng cách giữa hai điểm
            double distance = TurfMeasurement.distance(start, end) * 1000; // Đổi sang mét

            // Tính số lượng điểm cần thêm
            int numPointsToAdd = (int) (distance / intervalMeters);

            // Nội suy các điểm
            for (int j = 0; j <= numPointsToAdd; j++) {
                double fraction = (double) j / numPointsToAdd;
                Point interpolatedPoint = interpolate(start, end, fraction);
                densifiedPoints.add(interpolatedPoint);
            }
        }

        // Thêm điểm cuối cùng của tuyến đường
        densifiedPoints.add(routePoints.get(routePoints.size() - 1));

        return densifiedPoints;
    }


    private Point interpolate(Point start, Point end, double fraction) {
        double lat = start.latitude() + (end.latitude() - start.latitude()) * fraction;
        double lng = start.longitude() + (end.longitude() - start.longitude()) * fraction;
        return Point.fromLngLat(lng, lat);
    }

    private void updateSpeedOnFAB(float speed) {
        // Chuyển đổi tốc độ từ m/s sang km/h và làm tròn thành số nguyên
        int speedInKmh = Math.round(speed * 3.6f); // Làm tròn thành số nguyên

        // Cập nhật tốc độ lên FAB dưới dạng số nguyên
        fabspeed.setText(String.format("%d km/h", speedInKmh)); // Đặt tốc độ lên FAB
    }

    private void countPotholesOnRoute() {
        FirebaseDatabase.getInstance().getReference().child("sharedPothole").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int potholeCount = 0;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    SharePothole pothole = dataSnapshot.getValue(SharePothole.class);
                    if (pothole != null) {
                        double potholeLatitude = pothole.getLatitude();
                        double potholeLongitude = pothole.getLongitude();

                        // Kiểm tra xem ổ gà có gần tuyến đường không
                        for (Point routePoint : routePoints) {
                            float distanceToRoutePoint = calculateDistance(routePoint.latitude(), routePoint.longitude(), potholeLatitude, potholeLongitude);
                            if (distanceToRoutePoint <= 2) { // 2 mét là khoảng cách chấp nhận
                                potholeCount++;
                                break;
                            }
                        }
                    }
                }

                // Hiển thị thông báo
                Toast.makeText(MapActivity.this, getString(R.string.numberpotholeonroute) + potholeCount, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi lấy dữ liệu ổ gà", error.toException());
            }
        });
    }

}