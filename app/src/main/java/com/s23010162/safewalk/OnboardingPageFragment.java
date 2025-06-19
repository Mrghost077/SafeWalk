package com.s23010162.safewalk;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class OnboardingPageFragment extends Fragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_IMAGE = "image";

    private String title;
    private String description;
    private int imageResource;

    public static OnboardingPageFragment newInstance(String title, String description, int imageResource) {
        OnboardingPageFragment fragment = new OnboardingPageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESCRIPTION, description);
        args.putInt(ARG_IMAGE, imageResource);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
            description = getArguments().getString(ARG_DESCRIPTION);
            imageResource = getArguments().getInt(ARG_IMAGE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_onboarding_page, container, false);

        ImageView appLogo = view.findViewById(R.id.app_logo);
        TextView appTitle = view.findViewById(R.id.app_title);
        TextView descriptionText = view.findViewById(R.id.description_text);

        // Set the data
        appLogo.setImageResource(imageResource);
        appTitle.setText(title);
        descriptionText.setText(description);

        return view;
    }
}