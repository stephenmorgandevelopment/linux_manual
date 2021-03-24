package com.stephenmorgandevelopment.thelinuxmanual.utils;

import android.util.Log;
import android.view.ViewGroup;

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
    private static final String TAG = "PrimaryPagerAdapter";
    private static final List<String> titleList = new ArrayList<>();
//    private final List<Command> dataList = new ArrayList<>();
    private final List<Long> idList = new ArrayList<>();
    private CommandLookupFragment lookupFragment;

    private static final String INFO_KEY_NAME = "NAME";

    public PrimaryPagerAdapter(FragmentManager fm, CommandLookupFragment lookupFragment) {
        super(fm, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.lookupFragment = lookupFragment;
    }

//    public void addPage(Command command) {
//        dataList.add(command);
//    }
    public void addPage(long id, String title) {
        String name = title.substring(0, title.indexOf(" "));

        if(name.contains(",")) {
            name = name.substring(0, name.indexOf(","));
        }

        titleList.add(name);
        idList.add(id);
    }

//    public void addAllPages(List<Command> commands) {
//        dataList.clear();
//        dataList.addAll(commands);
//        notifyDataSetChanged();
//    }

    public void addAllPages(List<Command> commands) {
        idList.clear();
        titleList.clear();
        for(Command command : commands) {
            String title = command.getData().get(INFO_KEY_NAME);
            String name = title.substring(0, title.indexOf(" "));

            if(name.contains(",")) {
                name = name.substring(0, name.indexOf(","));
            }

            idList.add(command.getId());
            titleList.add(command.getData().get(INFO_KEY_NAME));
        }
        notifyDataSetChanged();
    }

//    public void removePage(Command command) {
//        if(!dataList.remove(command)) {
//            for(Command cmd : dataList) {
//                if(cmd.getId() == command.getId()) {
//                    dataList.remove(cmd);
//                    break;
//                }
//            }
//        }
//    }

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


//        if(!idList.remove(id)) {
//            for(Long mId : idList) {
//                if(mId == id) {
//                    idList.remove(mId);
//                    Log.i(TAG, "primitive long did not match Long - Caught issue.");
//                    break;
//                }
//            }
//        }
    }

    @Override
    public void startUpdate(@NonNull ViewGroup container) {
        super.startUpdate(container);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        return super.instantiateItem(container, position);


    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.destroyItem(container, position, object);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if(position == 0) {
            if(lookupFragment == null) {
                lookupFragment = new CommandLookupFragment();
            }
            return lookupFragment;
        }

        if(idList.size() > 0) {
//            Command cmd = dataList.get(position - 1);
//            CommandInfoFragment infoFragment =
//                    CommandInfoFragment.getInstance(cmd);
            return CommandInfoFragment.newInstance(idList.get(position - 1));
        }

        return lookupFragment;
    }

    @Override
    public int getCount() {
        return idList.size() + 1;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if(position == 0) {
            return "Search";
        }

        return titleList.get(position - 1);

//        String name = titleList.get(position - 1);
//        name = name.substring(0, name.indexOf(" "));
//
//        return !name.contains(",")
//                ? name : name.substring(0, name.indexOf(","));

//        String regex = "(/W/s)";

//        String name = dataList.get(position - 1)
//                .getData().get(INFO_KEY_NAME);

//        return name.substring(0, name.indexOf(" "));
    }


}
