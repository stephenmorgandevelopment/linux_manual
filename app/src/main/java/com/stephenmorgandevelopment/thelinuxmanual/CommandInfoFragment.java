package com.stephenmorgandevelopment.thelinuxmanual;

import android.content.Context;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;

import com.stephenmorgandevelopment.thelinuxmanual.distros.Ubuntu;
import com.stephenmorgandevelopment.thelinuxmanual.network.HttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommandInfoFragment extends Fragment {
    public static final String TAG = CommandInfoFragment.class.getSimpleName();
    private CommandInfoFragment instance;

    private LinearLayout scrollContainer;
    private Map<String, String> infoMap;
    private boolean firstRun = false;

    public static CommandInfoFragment getInstance() {
//        if(instance == null) {
//            instance = new CommandInfoFragment();
//        }
        return new CommandInfoFragment();
    }

    public void setInfo(Map<String, String> infoMap) {
        this.infoMap = new ArrayMap<>();
        this.infoMap.putAll(infoMap);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.command_info_fragment, null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scrollContainer = view.findViewById(R.id.scrollContainer);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firstRun = true;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(firstRun) {
            addTextBubble("NAME", infoMap.remove("NAME"));
            addTextBubble("DESCRIPTION", infoMap.remove("DESCRIPTION"));

            Set<String> keys = infoMap.keySet();
            for(String key : keys) {
                addTextBubble(key, infoMap.get(key));
            }
            firstRun = false;
        }
    }



    private void addTextBubble(String header, String description) {
        View view = (ViewGroup) ((LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.text_bubble, null);
//        View view = (ViewGroup) ((LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.text_bubble, scrollContainer, true);
        ((TextView)view.findViewById(R.id.headerText)).setText(header);
        ((TextView)view.findViewById(R.id.descriptionText)).setText(description);
        scrollContainer.addView(view);
    }

}
