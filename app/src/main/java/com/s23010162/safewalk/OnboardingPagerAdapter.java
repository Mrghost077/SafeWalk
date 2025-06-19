package com.s23010162.safewalk;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class OnboardingPagerAdapter extends FragmentStateAdapter {

    private static final int NUM_PAGES = 3;

    public OnboardingPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return OnboardingPageFragment.newInstance(
                        "SafeWalk",
                        "Your personal safety companion for\nwalking alone, traveling, and\nemergency situations.",
                        R.drawable.safewalk_logo
                );
            case 1:
                return OnboardingPageFragment.newInstance(
                        "Stay Connected",
                        "Share your location with trusted\ncontacts and get help when you\nneed it most.",
                        R.drawable.safewalk_logo2
                );
            case 2:
                return OnboardingPageFragment.newInstance(
                        "Peace of Mind",
                        "Walk confidently knowing that\nhelp is just a tap away with\nour emergency features.",
                        R.drawable.safewalk_logo2
                );
            default:
                return OnboardingPageFragment.newInstance(
                        "SafeWalk",
                        "Your personal safety companion for\nwalking alone, traveling, and\nemergency situations.",
                        R.drawable.safewalk_logo2
                );
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}
