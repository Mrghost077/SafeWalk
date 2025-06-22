package com.s23010162.safewalk;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RecordingsListFragment extends Fragment {

    private RecyclerView rvRecordings;
    private RecordingsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recordings_list, container, false);

        rvRecordings = view.findViewById(R.id.rvRecordings);
        rvRecordings.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new RecordingsAdapter(new ArrayList<>());
        rvRecordings.setAdapter(adapter);

        loadRecordings();

        return view;
    }

    private void loadRecordings() {
        List<Recording> recordings = new ArrayList<>();
        Uri collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_ADDED
        };
        String selection = MediaStore.Video.Media.RELATIVE_PATH + " like ? and " + MediaStore.Video.Media.DISPLAY_NAME + " like ?";
        String[] selectionArgs = new String[]{"%Movies/SafeWalk%", "%SafeWalk-recording%"};
        String sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = requireContext().getContentResolver().query(
                collection,
                projection,
                selection,
                selectionArgs,
                sortOrder
        )) {
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
            int dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);

            while (cursor.moveToNext()) {
                String name = cursor.getString(nameColumn);
                long dateAdded = cursor.getLong(dateColumn);
                recordings.add(new Recording(name, dateAdded));
            }
        }

        adapter.setRecordings(recordings);
    }
} 