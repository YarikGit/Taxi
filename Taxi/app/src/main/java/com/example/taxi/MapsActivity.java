package com.example.taxi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.taxi.databinding.ActivityMapsBinding;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String PASSENGER_MODE = "passenger";
    private String userMode;

    private Button signOutButton, bookTaxiButton;
    private CardView menuCardView;
    private FloatingActionButton openMenuButton;

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;


    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private static final int CHECK_SETTINGS_CODE = 111;
    private static final int REQUEST_LOCATION_PERMISSION = 222;

    private FusedLocationProviderClient fusedLocationClient;
    private SettingsClient settingsClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;

    private boolean isLocationUpdateActive;

    private int searchRadius = 1;
    private boolean isDriverFound = false;
    private String nearestDriverId;
    private DatabaseReference driversGeoFire;
    private DatabaseReference nearestDriverLocation;
    private Marker driverMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);

        Intent intent = getIntent();
        if (intent != null) {
            userMode = intent.getStringExtra("userMode");
        }

        buildLocationRequest();
        buildLocationCallback();
        buildLocationSettingsRequest();

        startLocationUpdate();

        currentUser = mAuth.getCurrentUser();

        driversGeoFire = FirebaseDatabase.getInstance().getReference().child("drivers");


        signOutButton = findViewById(R.id.signOutButton);
        menuCardView = findViewById(R.id.menuCardView);
        openMenuButton = findViewById(R.id.openMenuButton);
        bookTaxiButton = findViewById(R.id.bookTaxiButton);

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        openMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMenu();
            }
        });

        bookTaxiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookTaxiButton.setText("Getting Your Taxi...");
                gettingNearestTaxi();
            }
        });

    }

    private void gettingNearestTaxi() {
        if(userMode.equals(PASSENGER_MODE)) {
            GeoFire geoFire = new GeoFire(driversGeoFire);
            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(currentLocation.getLatitude(),
                    currentLocation.getLongitude()), searchRadius);

            geoQuery.removeAllListeners();

            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    if (!isDriverFound) {
                        isDriverFound = true;
                        nearestDriverId = key;

                        getNearestDriverLocation();
                    }
                }

                @Override
                public void onKeyExited(String key) {

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {

                }

                @Override
                public void onGeoQueryReady() {

                    if (!isDriverFound) {
                        searchRadius++;
                        gettingNearestTaxi();
                    }

                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }
            });
        }else {
            bookTaxiButton.setText("You in car!");
        }
    }

    private void getNearestDriverLocation() {
        bookTaxiButton.setText("Getting your driver location...");

        nearestDriverLocation = FirebaseDatabase.getInstance().getReference()
                .child("driversGeoFire")
                .child(nearestDriverId)
                .child("l");
        nearestDriverLocation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    List<Object> driverLocationParameters = (List<Object>) snapshot.getValue();
                    double latitude = 0;
                    double longitude = 0;

                    if (driverLocationParameters.get(0) != null) {
                        latitude = Double.parseDouble(driverLocationParameters.get(0).toString());
                    }
                    if (driverLocationParameters.get(1) != null) {
                        longitude = Double.parseDouble(driverLocationParameters.get(1).toString());
                    }

                    LatLng driverLatLng = new LatLng(latitude, longitude);

                    if (driverMarker != null) {
                        driverMarker.remove();
                    }

                    Location driverLocation = new Location("");
                    driverLocation.setLatitude(latitude);
                    driverLocation.setLongitude(longitude);

                    float distanceToDriver = driverLocation.distanceTo(currentLocation);
                    bookTaxiButton.setText("Distance to driver: " + distanceToDriver);

                    driverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).
                            title("Your driver is here").alpha(0.6f));


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void openMenu() {
        if (menuCardView.getVisibility() == View.GONE) {
            menuCardView.setVisibility(View.VISIBLE);
            menuCardView.animate().alpha(1f).setDuration(300);
            openMenuButton.setRotation(90);
            openMenuButton.animate().rotation(270).setDuration(300);

        } else {
            menuCardView.setVisibility(View.GONE);
            menuCardView.animate().alpha(0f).setDuration(300);
            openMenuButton.animate().rotation(openMenuButton.getRotation() + 180).setDuration(300);
        }


    }

    private void signOut() {
        if (currentUser != null) {
            mAuth.signOut();
            DatabaseReference user;
            if (userMode.equals(PASSENGER_MODE)) {
                user = FirebaseDatabase.getInstance().getReference()
                        .child("drivers");
            } else {
                user = FirebaseDatabase.getInstance().getReference()
                        .child("drivers");
            }

            GeoFire geoFire = new GeoFire(user);
            geoFire.removeLocation(currentUser.getUid());

            Intent intent = new Intent(MapsActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
            startActivity(intent);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//         Add a marker in Sydney and move the camera
        LatLng user = new LatLng(49.1516397, 31.2849321);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(user));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(5));
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();
    }

    private void buildLocationRequest() {

        locationRequest = new LocationRequest
                .Builder(10000)
                .setMinUpdateIntervalMillis(3000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build();
    }

    private void buildLocationCallback() {

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                currentLocation = locationResult.getLastLocation();

                updateLocationUi();
            }
        };

    }

    private void startLocationUpdate() {
        isLocationUpdateActive = true;

        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        if (ActivityCompat.checkSelfPermission(MapsActivity.this,
                                android.Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(MapsActivity.this,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                        updateLocationUi();
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        int statusCode = ((ApiException) e).getStatusCode();

                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    ResolvableApiException resolvableApiException =
                                            (ResolvableApiException) e;
                                    resolvableApiException.startResolutionForResult(MapsActivity.this,
                                            CHECK_SETTINGS_CODE);
                                } catch (IntentSender.SendIntentException sie) {
                                    sie.printStackTrace();
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String message = "Adjust location settings on your device";
                                Toast.makeText(MapsActivity.this, message, Toast.LENGTH_SHORT).show();

                                isLocationUpdateActive = false;
                        }
                        updateLocationUi();

                    }
                });
    }

    private void stopLocationUpdate() {

        if (!isLocationUpdateActive) {
            return;
        }

        fusedLocationClient.removeLocationUpdates(locationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        isLocationUpdateActive = false;
                    }
                });

        isLocationUpdateActive = false;
    }

    private void updateLocationUi() {

        if (currentLocation != null) {
            LatLng userLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
            String driverUserId = currentUser.getUid();
            DatabaseReference usersGeoFire;
            DatabaseReference users;

            Log.d("oleg", userMode + " user");
            Log.d("oleg", PASSENGER_MODE + " constant");

            if (userMode.equals(PASSENGER_MODE)) {
                mMap.addMarker(new MarkerOptions().position(userLatLng).title("Passenger"));
                usersGeoFire = FirebaseDatabase.getInstance().getReference()
                        .child("passengersGeoFire");
                users = FirebaseDatabase.getInstance().getReference()
                        .child("passengers");
                users.setValue("passenger created");
            } else {
                mMap.addMarker(new MarkerOptions().position(userLatLng).title("Driver"));
                usersGeoFire = FirebaseDatabase.getInstance().getReference()
                        .child("driversGeoFire");
                users = FirebaseDatabase.getInstance().getReference()
                        .child("drivers");
                users.setValue("driver created");
            }

            GeoFire geoFire = new GeoFire(usersGeoFire);
            geoFire.setLocation(driverUserId, new GeoLocation(currentLocation.getLatitude(),
                    currentLocation.getLongitude()));
        }

    }

    private void requestLocationPermission() {

        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
        );

        if (shouldProvideRationale) {
            showSnackBar("Location permission is needed for app functionality",
                    "Ok",
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(MapsActivity.this,
                                    new String[]{
                                            android.Manifest.permission.ACCESS_FINE_LOCATION
                                    }, REQUEST_LOCATION_PERMISSION
                            );
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                    }, REQUEST_LOCATION_PERMISSION
            );
        }


    }

    private boolean checkLocationPermission() {

        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length <= 0) {
                Log.d("onRequestPermissionsResult", "Request was cancelled");

            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isLocationUpdateActive) {
                    startLocationUpdate();
                }
            } else {
                showSnackBar("Turn on location on settings", "Settings", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS);
                        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID,
                                null
                        );
                        intent.setData(uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
            }
        }
    }

    private void showSnackBar(final String mainText, final String action, View.OnClickListener listener) {

        Snackbar.make(findViewById(android.R.id.content), mainText, Snackbar.LENGTH_INDEFINITE)
                .setAction(action, listener)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHECK_SETTINGS_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.d("MainActivity", "User has agreed to change location settings");
                        startLocationUpdate();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.d("MainActivity", "User has not agreed to change location settings");
                        isLocationUpdateActive = false;
                        updateLocationUi();
                        break;
                }
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdate();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isLocationUpdateActive && checkLocationPermission()) {

            startLocationUpdate();
        } else if (!checkLocationPermission()) {
            requestLocationPermission();
        }
    }

}