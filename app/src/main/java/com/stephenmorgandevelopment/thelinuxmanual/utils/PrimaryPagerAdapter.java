package com.stephenmorgandevelopment.thelinuxmanual.utils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;
import java.util.Map;

public class PrimaryPagerAdapter extends FragmentStatePagerAdapter {
//    private List<Map<String, String>>

    public PrimaryPagerAdapter(FragmentManager fm) {
        super(fm, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return null;
    }

    @Override
    public int getCount() {
        return count;
    }

    public void addPage() {

    }
}
