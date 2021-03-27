package com.stephenmorgandevelopment.thelinuxmanual.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.stephenmorgandevelopment.thelinuxmanual.CommandInfoFragment;
import com.stephenmorgandevelopment.thelinuxmanual.CommandLookupFragment;
import com.stephenmorgandevelopment.thelinuxmanual.models.Command;

import java.util.ArrayList;
import java.util.List;

public class PrimaryPagerAdapter extends FragmentStateAdapter {
    private static final String TAG = "PrimaryPagerAdapter";
    private static final List<String> titleList = new ArrayList<>();
    private final List<Long> idList = new ArrayList<>();
    private CommandLookupFragment lookupFragment;

    private static final String INFO_KEY_NAME = "NAME";

    public PrimaryPagerAdapter(FragmentActivity activity, CommandLookupFragment lookupFragment) {
        super(activity);
        this.lookupFragment = lookupFragment;
    }

    public void addPage(long id, String shortName) {
        titleList.add(shortName);
        idList.add(id);
    }

    public void addAllPages(List<Command> commands) {
        idList.clear();
        titleList.clear();

        for(Command command : commands) {
            idList.add(command.getId());
            titleList.add(command.getShortName());
        }

        notifyDataSetChanged();
    }

    public void removePage(long id) {
        int idx = idList.indexOf(id);

        if(idx != -1) {
            titleList.remove(idx);
            idList.remove(idx);
            return;
        }

        for(int i = 0; i < idList.size(); i++) {
            if(idList.get(i) == id) {
                idList.remove(i);
                titleList.remove(i);
                Log.i(TAG, "primitive long did not match Long - Caught issue.");
                return;
            }
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position == 0) {
//            return new CommandLookupFragment();
            return getLookupFragmentSingleton();
        }

        if(idList.size() > 0) {
            return CommandInfoFragment.newInstance(idList.get(position - 1));
        }

        return null;
    }

    private CommandLookupFragment getLookupFragmentSingleton() {
        if(lookupFragment == null) {
            lookupFragment = new CommandLookupFragment();
        }
        return lookupFragment;
    }

    @Override
    public int getItemCount() {
        return idList.size() + 1;
    }

    @Override
    public long getItemId(int position) {
        return position == 0 ? -1 : idList.get(position -1);
    }

    @Override
    public boolean containsItem(long itemId) {
        return itemId == -1 || idList.contains(itemId);
    }

    //    @Nullable
//    @Override
//    public CharSequence getPageTitle(int position) {
//        if(position == 0) {
//            return "Search";
//        }
//
//        return titleList.get(position - 1);
//    }
    public CharSequence getPageTitle(int position) {
        if(position == 0) {
            return "Search";
        }
        return titleList.get(position - 1);
    }

}
