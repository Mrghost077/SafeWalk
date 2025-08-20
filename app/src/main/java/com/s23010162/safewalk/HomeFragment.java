package com.s23010162.safewalk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.s23010162.safewalk.utils.Constants;
import com.s23010162.safewalk.utils.DateUtils;
import com.s23010162.safewalk.utils.PermissionUtils;
import com.s23010162.safewalk.utils.SosDialogUtils;

public class HomeFragment extends Fragment {

    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvCurrentLocation;
    private TextView tvSafetyStatusDetails;
    private AppDatabase appDatabase;
    private ExecutorService executorService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        appDatabase = AppDatabase.getDatabase(requireContext());
        executorService = Executors.newSingleThreadExecutor();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvCurrentLocation = view.findViewById(R.id.tvCurrentLocation);
        tvSafetyStatusDetails = view.findViewById(R.id.tvSafetyStatusDetails);
        Button btnEmergencySos = view.findViewById(R.id.btnEmergencySos);
        Button btnStartWalk = view.findViewById(R.id.btnStartWalk);
        Button btnQuickRecord = view.findViewById(R.id.btnQuickRecord);
        Button btnViewRecordings = view.findViewById(R.id.btnViewRecordings);
        Button btnAlertHistory = view.findViewById(R.id.btnAlertHistory);

        btnEmergencySos.setOnClickListener(v -> {
            SosDialogUtils.showSosCountdown(requireContext(), new SosDialogUtils.SosDialogCallback() {
                @Override
                public void onSosTriggered() {
                    sendSmsToEmergencyContacts();
                    Intent intent = SosDialogUtils.createAlertActivityIntent(requireActivity(), "SOS Triggered");
                    startActivity(intent);
                }

                @Override
                public void onSosCancelled() {
                    // SOS was cancelled by user
                }
            });
        });

        btnStartWalk.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_home_to_walk_mode);
        });

        btnQuickRecord.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.navigation_record);
        });

        btnViewRecordings.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_recordings_list));

        btnAlertHistory.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_history));

        checkLocationPermission();
        updateSafetyStatus();
    }

    private void checkLocationPermission() {
        if (!PermissionUtils.hasLocationPermission(requireContext())) {
            requestPermissions(PermissionUtils.getLocationPermissions(), Constants.LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (!PermissionUtils.hasLocationPermission(requireContext())) {
            tvCurrentLocation.setText(Constants.ERROR_LOCATION_PERMISSION);
            return;
        }
        
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        updateLocationUI(location);
                    } else {
                        tvCurrentLocation.setText("Getting current location...");
                        requestNewLocationData();
                    }
                })
                .addOnFailureListener(e -> {
                    tvCurrentLocation.setText("Failed to get location: " + e.getMessage());
                    Log.e("HomeFragment", "Location failed", e);
                });
    }

    private void requestNewLocationData() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(Constants.LOCATION_REQUEST_INTERVAL);
        locationRequest.setFastestInterval(Constants.LOCATION_REQUEST_FASTEST_INTERVAL);
        locationRequest.setNumUpdates(Constants.SINGLE_LOCATION_UPDATE);

        if (!PermissionUtils.hasLocationPermission(requireContext())) {
            tvCurrentLocation.setText(Constants.ERROR_LOCATION_PERMISSION);
            return;
        }
        
        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location lastLocation = locationResult.getLastLocation();
                if (lastLocation != null) {
                    updateLocationUI(lastLocation);
                } else {
                    tvCurrentLocation.setText("Location not available");
                }
                fusedLocationClient.removeLocationUpdates(this);
            }
        }, Looper.myLooper());
    }

    private void updateLocationUI(Location location) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                tvCurrentLocation.setText(address.getAddressLine(0));
            } else {
                tvCurrentLocation.setText("Location found, address not available.");
            }
        } catch (IOException e) {
            tvCurrentLocation.setText("Could not get address.");
        }
    }

    private void updateSafetyStatus() {
        executorService.execute(() -> {
            int contactCount = appDatabase.emergencyContactDao().getContactCount();
            boolean isGpsEnabled = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            requireActivity().runOnUiThread(() -> {
                String gpsStatus = isGpsEnabled ? "Connected" : "Disconnected";
                tvSafetyStatusDetails.setText(String.format("GPS: %s | Contacts: %d added", gpsStatus, contactCount));
            });
        });
    }



    private void sendSmsToEmergencyContacts() {
        if (!PermissionUtils.hasSmsPermission(requireContext())) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.SEND_SMS}, Constants.SMS_PERMISSION_REQUEST_CODE);
        } else {
            sendSmsLogic();
        }
    }

    private void sendSmsLogic() {
        if (!PermissionUtils.hasLocationPermission(requireContext())) {
            Toast.makeText(getContext(), Constants.ERROR_LOCATION_PERMISSION, Toast.LENGTH_LONG).show();
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                AppExecutors.getInstance().diskIO().execute(() -> {
                    List<EmergencyContact> contacts = AppDatabase.getDatabase(requireContext()).emergencyContactDao().getAll();
                    if (contacts.isEmpty()) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), Constants.ERROR_NO_CONTACTS, Toast.LENGTH_LONG).show());
                        return;
                    }

                    String message = "🚨 EMERGENCY SOS 🚨\nI need immediate help!\nMy location: http://maps.google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude() + "\nPlease respond immediately!";
                    SmsManager smsManager = SmsManager.getDefault();
                    final int[] sentCount = {0};

                    for (EmergencyContact contact : contacts) {
                        try {
                            ArrayList<String> parts = smsManager.divideMessage(message);
                            smsManager.sendMultipartTextMessage(contact.phoneNumber, null, parts, null, null);
                            sentCount[0]++;
                            Log.d("HomeFragment", "SOS sent to " + contact.name);
                        } catch (Exception e) {
                            requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Failed to send SOS to " + contact.name + ": " + e.getMessage(), Toast.LENGTH_LONG).show());
                            Log.e("HomeFragment", "Failed to send SMS to " + contact.name, e);
                        }
                    }

                    requireActivity().runOnUiThread(() -> {
                        if (sentCount[0] > 0) {
                            Toast.makeText(getContext(), String.format(Constants.SUCCESS_SOS_SENT, sentCount[0]), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to send any SOS messages.", Toast.LENGTH_LONG).show();
                        }
                    });
                });
            } else {
                Toast.makeText(getContext(), Constants.ERROR_LOCATION_UNAVAILABLE, Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to get location for SOS: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("HomeFragment", "Failed to get location for SOS", e);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                tvCurrentLocation.setText("Location permission denied.");
                Toast.makeText(getContext(), "Location permission is required to show current location.", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == Constants.SMS_PERMISSION_REQUEST_CODE) { // SMS permission
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "SMS permission granted. Sending SOS...", Toast.LENGTH_SHORT).show();
                sendSmsLogic();
            } else {
                Toast.makeText(getContext(), Constants.ERROR_SMS_PERMISSION, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
} 