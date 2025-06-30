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

public class HomeFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvCurrentLocation;
    private TextView tvSafetyStatusDetails;
    private AppDatabase appDatabase;
    private ExecutorService executorService;
    private CountDownTimer sosCountDownTimer;

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

        btnEmergencySos.setOnClickListener(v -> {
            showSosCountdown();
        });

        btnStartWalk.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_home_to_walk_mode);
        });

        btnQuickRecord.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.navigation_record);
        });

        btnViewRecordings.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.navigation_recordings_list));

        checkLocationPermission();
        updateSafetyStatus();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            tvCurrentLocation.setText("Location permission not granted");
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
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setNumUpdates(1);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            tvCurrentLocation.setText("Location permission not granted");
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
                tvSafetyStatusDetails.setText(String.format(Locale.getDefault(), "GPS: %s | Contacts: %d added", gpsStatus, contactCount));
            });
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

        final long countdownMillis = 5000;
        final long[] timeLeft = {countdownMillis};
        final boolean[] isPinDialogOpen = {false};

        sosCountDownTimer = new CountDownTimer(timeLeft[0], 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft[0] = millisUntilFinished;
                tvCountdown.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                if (!isPinDialogOpen[0]) {
                    dialog.dismiss();
                    sendSmsToEmergencyContacts();
                    Intent intent = new Intent(getActivity(), AlertActivity.class);
                    startActivity(intent);
                }
            }
        }.start();

        btnCancelSos.setOnClickListener(v -> {
            sosCountDownTimer.cancel();
            isPinDialogOpen[0] = true;
            showPinEntryDialog(dialog, timeLeft[0]);
        });
    }

    private void showPinEntryDialog(AlertDialog sosDialog, long timeLeft) {
        AlertDialog.Builder pinBuilder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View pinView = inflater.inflate(R.layout.dialog_pin_entry, null);
        pinBuilder.setView(pinView);
        pinBuilder.setCancelable(false);
        final AlertDialog pinDialog = pinBuilder.create();
        pinDialog.show();

        EditText etPin = pinView.findViewById(R.id.etPin);
        Button btnSubmit = pinView.findViewById(R.id.btnSubmitPin);
        Button btnCancel = pinView.findViewById(R.id.btnCancelPin);

        PreferencesManager preferencesManager = new PreferencesManager(requireContext());

        btnSubmit.setOnClickListener(v -> {
            String enteredPin = etPin.getText().toString().trim();
            if (enteredPin.isEmpty()) {
                Toast.makeText(getContext(), "Please enter your PIN", Toast.LENGTH_SHORT).show();
                return;
            }
            if (enteredPin.length() != 4 || !enteredPin.matches("\\d{4}")) {
                Toast.makeText(getContext(), "PIN must be 4 digits", Toast.LENGTH_SHORT).show();
                etPin.setText("");
                return;
            }
            if (preferencesManager.verifyEmergencyPin(enteredPin)) {
                pinDialog.dismiss();
                sosDialog.dismiss();
                Toast.makeText(getContext(), "SOS Canceled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Incorrect PIN", Toast.LENGTH_SHORT).show();
                etPin.setText("");
            }
        });

        btnCancel.setOnClickListener(v -> {
            pinDialog.dismiss();
            // Resume countdown if time left
            if (timeLeft > 0) {
                showSosCountdownWithTimeLeft(sosDialog, timeLeft);
            }
        });
    }

    private void showSosCountdownWithTimeLeft(AlertDialog oldDialog, long timeLeft) {
        if (oldDialog != null && oldDialog.isShowing()) oldDialog.dismiss();
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_sos_countdown, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        final AlertDialog dialog = builder.create();
        dialog.show();
        final TextView tvCountdown = dialogView.findViewById(R.id.tvCountdown);
        Button btnCancelSos = dialogView.findViewById(R.id.btnCancelSos);
        final long[] timeLeftArr = {timeLeft};
        final boolean[] isPinDialogOpen = {false};
        sosCountDownTimer = new CountDownTimer(timeLeftArr[0], 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftArr[0] = millisUntilFinished;
                tvCountdown.setText(String.valueOf(millisUntilFinished / 1000));
            }
            @Override
            public void onFinish() {
                if (!isPinDialogOpen[0]) {
                    dialog.dismiss();
                    sendSmsToEmergencyContacts();
                    Intent intent = new Intent(getActivity(), AlertActivity.class);
                    startActivity(intent);
                }
            }
        }.start();
        btnCancelSos.setOnClickListener(v -> {
            sosCountDownTimer.cancel();
            isPinDialogOpen[0] = true;
            showPinEntryDialog(dialog, timeLeftArr[0]);
        });
    }

    private void sendSmsToEmergencyContacts() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.SEND_SMS}, 101);
        } else {
            sendSmsLogic();
        }
    }

    private void sendSmsLogic() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Location permission not granted", Toast.LENGTH_LONG).show();
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                AppExecutors.getInstance().diskIO().execute(() -> {
                    List<EmergencyContact> contacts = AppDatabase.getDatabase(requireContext()).emergencyContactDao().getAll();
                    if (contacts.isEmpty()) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "No emergency contacts found. Please add contacts in settings.", Toast.LENGTH_LONG).show());
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
                            Toast.makeText(getContext(), "Emergency SOS sent to " + sentCount[0] + " contact(s).", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to send any SOS messages.", Toast.LENGTH_LONG).show();
                        }
                    });
                });
            } else {
                Toast.makeText(getContext(), "Could not get location to send SOS. Please enable location.", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to get location for SOS: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("HomeFragment", "Failed to get location for SOS", e);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                tvCurrentLocation.setText("Location permission denied.");
                Toast.makeText(getContext(), "Location permission is required to show current location.", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == 101) { // SMS permission
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "SMS permission granted. Sending SOS...", Toast.LENGTH_SHORT).show();
                sendSmsLogic();
            } else {
                Toast.makeText(getContext(), "SMS permission denied. SOS alerts will not be sent.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        if (sosCountDownTimer != null) {
            sosCountDownTimer.cancel();
        }
    }
} 