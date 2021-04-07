package com.stephenmorgandevelopment.thelinuxmanual.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.stephenmorgandevelopment.thelinuxmanual.ui.CommandInfoFragment;
import com.stephenmorgandevelopment.thelinuxmanual.ui.CommandLookupFragment;
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
            Log.i(TAG, "Adding from addAllPages(): " + command.getShortName());
            idList.add(command.getId());
            titleList.add(command.getShortName());
        }

        notifyDataSetChanged();
    }

    public void removePage(long id) {
        int idx = idList.indexOf(id);

        if(idx != -1) {
            Log.i(TAG, "Removing " + titleList.get(idx) + "from PagerAdapter.");
            String title = titleList.remove(idx);
            long removedId = idList.remove(idx);
            Log.i(TAG, "Successfully removed: " + removedId + " - " + title);
            return;
        }

        for(int i = 0; i < idList.size(); i++) {
            if(idList.get(i) == id) {
                Log.i(TAG, "Removing " + titleList.get(i) + "from PagerAdapter.");
                long removedId = idList.remove(i);
                String title = titleList.remove(i);
                Log.i(TAG, "primitive long did not match Long: " + removedId + " - " + title);
                return;
            }
        }
    }

    public void clearAll() {
        idList.clear();
        titleList.clear();
        notifyDataSetChanged();
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

        return lookupFragment;
    }

    private CommandLookupFragment getLookupFragmentSingleton() {
        if(lookupFragment == null) {
            lookupFragment = CommandLookupFragment.newInstance();
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

    public CharSequence getPageTitle(int position) {
        if(position == 0) {
            return "Search";
        }
        return titleList.get(position - 1);
    }

}
