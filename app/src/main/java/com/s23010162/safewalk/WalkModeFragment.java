package com.s23010162.safewalk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class WalkModeFragment extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private static final int SMS_PERMISSION_REQUEST_CODE = 102;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private Location lastKnownLocation;

    private MapView mapView;
    private GoogleMap googleMap;

    private TextView tvWalkStartedTime, tvDuration;
    private Button btnStopWalk, btnShareLocation, btnCheckIn, btnEmergencyFromWalk;

    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;
    private long startTime;
    private CountDownTimer sosCountDownTimer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        startTime = System.currentTimeMillis();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_walk_mode, container, false);
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvWalkStartedTime = view.findViewById(R.id.tvWalkStartedTime);
        tvDuration = view.findViewById(R.id.tvDuration);
        btnStopWalk = view.findViewById(R.id.btnWalkModeActive);
        btnShareLocation = view.findViewById(R.id.btnShareLocation);
        btnCheckIn = view.findViewById(R.id.btnCheckIn);
        btnEmergencyFromWalk = view.findViewById(R.id.btnEmergencyFromWalk);
        setupUI();
        checkLocationPermissionAndStartTracking();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    private void setupUI() {
        tvWalkStartedTime.setText(new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(startTime)));
        btnStopWalk.setOnClickListener(v -> {
            stopLocationUpdates();
            NavHostFragment.findNavController(this).navigateUp();
        });
        btnShareLocation.setOnClickListener(v -> shareLocation());
        btnCheckIn.setOnClickListener(v -> checkIn());
        btnEmergencyFromWalk.setOnClickListener(v -> showSosCountdown());
        startDurationTimer();
    }

    private void startDurationTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millis = System.currentTimeMillis() - startTime;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
                tvDuration.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void checkLocationPermissionAndStartTracking() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(30000);
        locationRequest.setFastestInterval(15000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() != null) {
                    lastKnownLocation = locationResult.getLastLocation();
                    updateMapLocation();
                    Log.d("WalkModeFragment", "Location: " + lastKnownLocation.getLatitude() + ", " + lastKnownLocation.getLongitude());
                }
            }
        };
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void updateMapLocation() {
        if (googleMap != null && lastKnownLocation != null) {
            LatLng currentLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            googleMap.clear(); // Clear previous markers
            googleMap.addMarker(new MarkerOptions().position(currentLatLng).title("My Location"));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));
        }
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
        saveWalkToDatabase();
        Toast.makeText(getContext(), "Walk Mode stopped.", Toast.LENGTH_SHORT).show();
    }

    private void saveWalkToDatabase() {
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        Walk walk = new Walk();
        walk.startTime = startTime;
        walk.endTime = endTime;
        walk.duration = duration;
        AppExecutors.getInstance().diskIO().execute(() -> {
            AppDatabase.getDatabase(requireContext()).walkDao().insert(walk);
            Log.d("WalkModeFragment", "Walk saved to database.");
        });
    }

    private void shareLocation() {
        sendSms("Just sharing my location with you from my walk: ");
    }

    private void checkIn() {
        sendSms("Just checking in, I'm safe during my walk. Current location: ");
    }

    private void sendSms(String prefix) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        } else {
            executeSendSms(prefix);
        }
    }

    private void executeSendSms(String prefix) {
        if (lastKnownLocation == null) {
            Toast.makeText(getContext(), "Location not available yet. Please wait.", Toast.LENGTH_SHORT).show();
            return;
        }
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<EmergencyContact> contacts = AppDatabase.getDatabase(requireContext()).emergencyContactDao().getAll();
            if (contacts.isEmpty()) {
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "No emergency contacts to share with.", Toast.LENGTH_SHORT).show());
                return;
            }
            String message = prefix + "http://maps.google.com/maps?q=" + lastKnownLocation.getLatitude() + "," + lastKnownLocation.getLongitude();
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(message);
            for (EmergencyContact contact : contacts) {
                try {
                    smsManager.sendMultipartTextMessage(contact.phoneNumber, null, parts, null, null);
                } catch (Exception e) {
                    Log.e("WalkModeFragment", "Failed to send SMS to " + contact.name, e);
                }
            }
            requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Message sent to " + contacts.size() + " contact(s).", Toast.LENGTH_SHORT).show());
        });
    }

    private void showSosCountdown() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_sos_countdown, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        final AlertDialog dialog = builder.create();
        dialog.show();

        final TextView tvCountdown = dialogView.findViewById(R.id.tvCountdown);
        Button btnCancelSos = dialogView.findViewById(R.id.btnCancelSos);

        sosCountDownTimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvCountdown.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                dialog.dismiss();
                // We don't call sendSms directly, we just launch the alert activity which handles it
                Intent intent = new Intent(getActivity(), AlertActivity.class);
                startActivity(intent);
                stopLocationUpdates(); // End the walk mode
            }
        }.start();

        btnCancelSos.setOnClickListener(v -> {
            sosCountDownTimer.cancel();
            dialog.dismiss();
            Toast.makeText(getContext(), "SOS Canceled", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
                if (googleMap != null) {
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    googleMap.setMyLocationEnabled(true);
                }
            } else {
                Toast.makeText(getContext(), "Location permission is required for Walk Mode.", Toast.LENGTH_LONG).show();
                NavHostFragment.findNavController(this).navigateUp();
            }
        } else if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "SMS Permission granted. You can now send messages.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "SMS Permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopLocationUpdates();
        if (googleMap != null) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            googleMap.setMyLocationEnabled(false);
        }
        if(sosCountDownTimer != null) {
            sosCountDownTimer.cancel();
        }
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
} 