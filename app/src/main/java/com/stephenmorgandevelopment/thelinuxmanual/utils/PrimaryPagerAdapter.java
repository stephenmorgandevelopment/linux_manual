package com.stephenmorgandevelopment.thelinuxmanual.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.stephenmorgandevelopment.thelinuxmanual.CommandInfoFragment;
import com.stephenmorgandevelopment.thelinuxmanual.CommandLookupFragment;
import com.stephenmorgandevelopment.thelinuxmanual.models.Command;

import java.util.ArrayList;
import java.util.List;

public class PrimaryPagerAdapter extends FragmentStatePagerAdapter {
    private final List<Command> dataList = new ArrayList<>();
    private final CommandLookupFragment lookupFragment;

    private static final String INFO_KEY_NAME = "NAME";

    public PrimaryPagerAdapter(FragmentManager fm, CommandLookupFragment lookupFragment) {
        super(fm, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.lookupFragment = lookupFragment;
    }

    public void addPage(Command command) {
        dataList.add(command);
    }

    public void addAllPages(List<Command> commands) {
        dataList.clear();
        dataList.addAll(commands);
        notifyDataSetChanged();
    }

    public void removePage(Command command) {
        if(!dataList.remove(command)) {
            for(Command cmd : dataList) {
                if(cmd.getId() == command.getId()) {
                    dataList.remove(cmd);
                    break;
                }
            }
        }
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if(position == 0) {
            return lookupFragment;
        }

        if(dataList.size() > 0) {
            Command cmd = dataList.get(position - 1);
            CommandInfoFragment infoFragment =
                    CommandInfoFragment.getInstance(cmd);
            return infoFragment;
        }

        return lookupFragment;
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

        String regex = "(/W/s)";
        String name = dataList.get(position - 1)
                .getData().get(INFO_KEY_NAME);

        return name.substring(0, name.indexOf(" "));
    }


}
