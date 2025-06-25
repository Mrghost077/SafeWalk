package com.s23010162.safewalk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecordFragment extends Fragment {

    private static final String TAG = "RecordFragment";
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

    private PreviewView previewView;
    private TextView tvTimer;
    private com.google.android.material.floatingactionbutton.FloatingActionButton btnRecord;

    private ExecutorService cameraExecutor;
    private VideoCapture videoCapture;
    private ProcessCameraProvider cameraProvider;

    private boolean isRecording = false;
    private Handler timerHandler = new Handler(Looper.getMainLooper());
    private long startTime = 0L;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 500);
        }
    };

    private Recording currentRecording;
    private AppDatabase appDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        previewView = view.findViewById(R.id.previewView);
        tvTimer = view.findViewById(R.id.tvTimer);
        btnRecord = view.findViewById(R.id.btnRecord);

        cameraExecutor = Executors.newSingleThreadExecutor();
        appDatabase = AppDatabase.getDatabase(requireContext());

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        btnRecord.setOnClickListener(v -> {
            if (isRecording) {
                stopRecording();
            } else {
                startRecording();
            }
        });

        view.findViewById(R.id.btnRecordingsList).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Recordings List - Coming Soon", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraExecutor = Executors.newSingleThreadExecutor();
        appDatabase = AppDatabase.getDatabase(requireContext());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                videoCapture = new VideoCapture.Builder()
                    .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    @SuppressLint("MissingPermission")
    private void startRecording() {
        if (videoCapture == null) {
            Log.e(TAG, "VideoCapture is not initialized.");
            return;
        }

        isRecording = true;
        btnRecord.setImageResource(R.drawable.ic_stop);

        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);

        String name = "SafeWalk-recording-" +
                new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US)
                        .format(System.currentTimeMillis());

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/SafeWalk");

        VideoCapture.OutputFileOptions outputOptions = new VideoCapture.OutputFileOptions
                .Builder(requireContext().getContentResolver(),
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                contentValues).build();

        videoCapture.startRecording(outputOptions, cameraExecutor, new VideoCapture.OnVideoSavedCallback() {
            @Override
            public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Video Saved: " + name, Toast.LENGTH_SHORT).show());
                Log.d(TAG, "Video saved: " + outputFileResults.getSavedUri());
                saveRecordingToDatabase(outputFileResults.getSavedUri().getPath());
            }

            @Override
            public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                isRecording = false;
                btnRecord.setImageResource(R.drawable.ic_record);
                timerHandler.removeCallbacks(timerRunnable);
                tvTimer.setText("00:00");
                Log.e(TAG, "Video capture error: " + message, cause);
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Error saving video: " + message, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void stopRecording() {
        if (videoCapture != null) {
            videoCapture.stopRecording();
            isRecording = false;
            btnRecord.setImageResource(R.drawable.ic_record);
            timerHandler.removeCallbacks(timerRunnable);
            tvTimer.setText("00:00");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(requireContext(),
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show();
                // Optionally, navigate back or disable functionality
            }
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void saveRecordingToDatabase(String filePath) {
        Recording newRecording = new Recording();
        newRecording.filePath = filePath;
        newRecording.timestamp = System.currentTimeMillis();

        AppExecutors.getInstance().diskIO().execute(() -> {
            appDatabase.recordingDao().insert(newRecording);
            Log.d(TAG, "Recording saved to database: " + filePath);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }
} 