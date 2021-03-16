package com.stephenmorgandevelopment.thelinuxmanual.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.stephenmorgandevelopment.thelinuxmanual.CommandInfoFragment;
import com.stephenmorgandevelopment.thelinuxmanual.CommandLookupFragment;
import com.stephenmorgandevelopment.thelinuxmanual.distros.Ubuntu;
import com.stephenmorgandevelopment.thelinuxmanual.models.Command;
import com.stephenmorgandevelopment.thelinuxmanual.models.SimpleCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PrimaryPagerAdapter extends FragmentStatePagerAdapter {
    private List<Map<String, String>> dataList;
    private List<SimpleCommand> commands;

    private CommandLookupFragment lookupFragment;

    public PrimaryPagerAdapter(FragmentManager fm, CommandLookupFragment lookupFragment) {
        super(fm, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.lookupFragment = lookupFragment;

        dataList = new ArrayList<>();
    }

    public void addPage(Map<String, String> data) {
        dataList.add(data);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if(position == 0) {
            return lookupFragment;
        }

        CommandInfoFragment infoFragment = CommandInfoFragment.getInstance();
        infoFragment.setInfo(dataList.get(position - 1));

        return infoFragment;
    }

    @Override
    public int getCount() {
        return dataList.size() + 1;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if(position == 0) {
            return "Search";
        }

        return dataList.get(position - 1).get("NAME");
    }
}
