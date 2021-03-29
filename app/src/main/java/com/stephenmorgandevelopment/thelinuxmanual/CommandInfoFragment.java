package com.stephenmorgandevelopment.thelinuxmanual;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.stephenmorgandevelopment.thelinuxmanual.models.Command;
import com.stephenmorgandevelopment.thelinuxmanual.models.TextSearchResult;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommandInfoFragment extends Fragment {
    public static final String TAG = CommandInfoFragment.class.getSimpleName();
    private Command command;

    private LinearLayout scrollContainer;
    private ScrollView rootScrollView;
    private List<String> jumpToList;

    private ConstraintLayout searchBar;
    private EditText searchEditText;
    private ImageButton searchBarButton;

    private ConstraintLayout searchControlBar;
    private TextView searchTextDisplay;
    private TextView numberOfTextMatches;
    private Button prevSearchButton;
    private Button nextSearchButton;

    private MainActivityViewModel viewModel;

    private static final String KEY_ID = "id";
    private static final String INFO_KEY_NAME = Helpers.getApplicationContext().getString(R.string.info_key_name);
    private static final String INFO_KEY_SYNOPSIS = Helpers.getApplicationContext().getString(R.string.info_key_synopsis);
    private static final String INFO_KEY_EXAMPLE = Helpers.getApplicationContext().getString(R.string.info_key_example);
    private static final String INFO_KEY_EXAMPLES = Helpers.getApplicationContext().getString(R.string.info_key_examples);
    private static final String INFO_KEY_OPTIONS = Helpers.getApplicationContext().getString(R.string.info_key_options);
    private static final String INFO_KEY_DESCRIPTION = Helpers.getApplicationContext().getString(R.string.info_key_description);

    public static CommandInfoFragment newInstance(long id) {
        Bundle args = new Bundle();
        args.putLong(KEY_ID, id);

        CommandInfoFragment fragment = new CommandInfoFragment();
        fragment.setArguments(args);
        return fragment;
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
        rootScrollView = view.findViewById(R.id.rootScrollView);

        searchBar = view.findViewById(R.id.textSearchBox);
        searchEditText = view.findViewById(R.id.searchEditText);
        searchBarButton = view.findViewById(R.id.searchBarButton);

        searchControlBar = view.findViewById(R.id.searchControlBar);
        searchTextDisplay = view.findViewById(R.id.searchTextDisplay);
        numberOfTextMatches = view.findViewById(R.id.numberOfTextMatches);
        prevSearchButton = view.findViewById(R.id.prevSearchButton);
        nextSearchButton = view.findViewById(R.id.nextSearchButton);

        long id = getArguments().getLong(KEY_ID);
        command = viewModel.getCommandFromListById(id);

        if(command != null) {
            buildOutput();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);

        setHasOptionsMenu(true);

        jumpToList = new ArrayList<>();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.info_dropdown, menu);

        Menu dropDownMenu = menu.findItem(R.id.dropDown).getSubMenu();

        if (dropDownMenu != null) {
            for (String title : jumpToList) {
                dropDownMenu.add(R.id.jumpTo, Menu.NONE, Menu.NONE, title);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.closeButton) {
            ((MainActivity) requireActivity()).removePage(command);
            return true;
        }

        if(item.getItemId() == R.id.searchButton) {
            toggleSearchBarDisplay();
        }

        if (jumpToList.contains(item.getTitle().toString())) {
            View v = scrollContainer.findViewWithTag(item.getTitle());
            rootScrollView.scrollTo(0, v.getTop() - 12);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOptionsMenuClosed(@NonNull Menu menu) {
        super.onOptionsMenuClosed(menu);
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(command == null) {
            long id = getArguments().getLong(KEY_ID);
            command = viewModel.getCommandFromListById(id);
        }

        ((AppCompatActivity)requireActivity()).getSupportActionBar().setTitle(command.getShortName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    View.OnClickListener onClickSearchBarButton = v -> {
        for(Map.Entry<String, String> entry : command.getData().entrySet()) {
            getTextMatches(entry.getValue());
        }
    };

    View.OnClickListener onClickPrevButton = v -> {

    };

    View.OnClickListener onClickNextButton = v -> {

    };

    private TextSearchResult getTextMatches(String searchMeForMatches) {
        final String query = searchEditText.getText().toString();

        if(searchMeForMatches.contains(query)) {

        }

        return null;
    }

    private void toggleSearchBarDisplay() {
        if(searchBar.getVisibility() == View.GONE) {
            searchBar.setVisibility(View.VISIBLE);
        } else {
            searchBar.setVisibility(View.GONE);
        }
    }

    private void addTextBubble(String header, String description) {
        ViewGroup view = getInflatedBubbleView();
        view.setTag(header);

        ((TextView) view.findViewById(R.id.headerText)).setText(header);
        ((TextView) view.findViewById(R.id.descriptionText)).setText(description);

        scrollContainer.addView(view);
        scrollContainer.addView(getDivider());

        jumpToList.add(header);
    }

    private ViewGroup getInflatedBubbleView() {
        LayoutInflater inflater = (LayoutInflater) requireContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        return  (ViewGroup) inflater.inflate(R.layout.text_bubble, null);
    }

    private View getDivider() {
        View divider = new View(getContext());
        divider.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 16));
        divider.setBackgroundColor(Color.TRANSPARENT);
        return divider;
    }

    private void buildOutput() {
        Map<String, String> infoMap = new LinkedHashMap<>(command.getData());

        scrollContainer.addView(getDivider());

        addTextBubble(INFO_KEY_NAME, infoMap.remove(INFO_KEY_NAME));

        if (infoMap.containsKey(INFO_KEY_SYNOPSIS)) {
            addTextBubble(INFO_KEY_SYNOPSIS, infoMap.remove(INFO_KEY_SYNOPSIS));
        }

        if (infoMap.containsKey(INFO_KEY_EXAMPLE)) {
            addTextBubble(INFO_KEY_EXAMPLE, infoMap.remove(INFO_KEY_EXAMPLE));
        }

        if (infoMap.containsKey(INFO_KEY_EXAMPLES)) {
            addTextBubble(INFO_KEY_EXAMPLES, infoMap.remove(INFO_KEY_EXAMPLES));
        }

        if (infoMap.containsKey(INFO_KEY_OPTIONS)) {
            addTextBubble(INFO_KEY_OPTIONS, infoMap.remove(INFO_KEY_OPTIONS));
        }

        if (infoMap.containsKey(INFO_KEY_DESCRIPTION)) {
            addTextBubble(INFO_KEY_DESCRIPTION, infoMap.remove(INFO_KEY_DESCRIPTION));
        }

        Set<String> keys = infoMap.keySet();
        for (String key : keys) {
            addTextBubble(key, infoMap.get(key));
        }

        scrollContainer.requestLayout();
        scrollContainer.invalidate();

        infoMap.clear();
        infoMap = null;
    }
}
