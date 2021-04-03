package com.stephenmorgandevelopment.thelinuxmanual.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.models.Command;
import com.stephenmorgandevelopment.thelinuxmanual.models.SingleTextMatch;
import com.stephenmorgandevelopment.thelinuxmanual.models.TextSearchResult;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Helpers;
import com.stephenmorgandevelopment.thelinuxmanual.viewmodels.CommandInfoViewModel;
import com.stephenmorgandevelopment.thelinuxmanual.viewmodels.MainActivityViewModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CommandInfoFragment extends Fragment {
    public static final String TAG = CommandInfoFragment.class.getSimpleName();

    private MainActivityViewModel viewModel;
    private CommandInfoViewModel infoModel;

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
        Command command = viewModel.getCommandFromListById(id);
        infoModel.init(command);

        buildOutput(command.getData());

        searchBarButton.setOnClickListener(onClickSearchBarButton);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);
        infoModel = new ViewModelProvider(this).get(CommandInfoViewModel.class);

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
            ((MainActivity) requireActivity()).removePage(infoModel.getId());
            return true;
        }

        if (item.getItemId() == R.id.searchButton) {
            toggleSearchBarDisplay();
        }

        if (jumpToList.contains(item.getTitle().toString())) {
            jumpTo(item.getTitle().toString());
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

//        if (command == null) {
//            long id = getArguments().getLong(KEY_ID);
//            command = viewModel.getCommandFromListById(id);
//        }

        ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(infoModel.getShortName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    View.OnClickListener onClickSearchBarButton = v -> {
        if (searchEditText.getText().length() >= 2) {
            infoModel.searchTextFor(
                    searchEditText.getText().toString(),
                    viewModel.getCommandFromListById(infoModel.getId()));

            displaySearchResults();
        }
    };

    View.OnClickListener onClickPrevButton = v -> {



        updateCurrentMatchDisplay();
    };

    View.OnClickListener onClickNextButton = v -> {
        SingleTextMatch textMatch = infoModel.getNextMatch();

        if(textMatch == null) {
            Toast.makeText(getContext(), "No matches found.", Toast.LENGTH_SHORT).show();
            return;
        }

        jumpTo(textMatch.getSection());

//        tv.setHighlightColor(getResources().getColor(
//                R.color.ic_launcher_background,
//                requireContext().getTheme()));

        highlightCurrentMatch(textMatch);

    };

    private void jumpTo(String section) {
        View v = scrollContainer.findViewWithTag(section);
        rootScrollView.scrollTo(0, v.getTop() - 12);
    }

    private void displaySearchResults() {
        searchControlBar.setVisibility(View.VISIBLE);
        searchTextDisplay.setText(infoModel.getSearchResults().getQuery());

        updateCurrentMatchDisplay();
    }

    private void updateCurrentMatchDisplay() {
        String numberTextMatchesText = infoModel.getCurrentMatchIndex() + "/" + infoModel.getResultsCount();
        numberOfTextMatches.setText(numberTextMatchesText);
    }

    private void toggleSearchBarDisplay() {
        if (searchBar.getVisibility() == View.GONE) {
            searchBar.setVisibility(View.VISIBLE);
        } else {
            searchBar.setVisibility(View.GONE);
        }
    }

    private void highlightCurrentMatch(SingleTextMatch textMatch) {
        View bubble = scrollContainer.findViewWithTag(textMatch.getSection());
        TextView tv = bubble.findViewById(R.id.descriptionText);

        SpannableString text = SpannableString.valueOf(tv.getText());

        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(
                requireContext().getColor(R.color.ic_launcher_background));

        BackgroundColorSpan backgroundSpan = new BackgroundColorSpan(
                requireContext().getColor(R.color.design_default_color_secondary));

        int endIdx = textMatch.getIndex() + infoModel.getSearchResults().getQuery().length();

        text.setSpan(
                backgroundSpan,
                textMatch.getIndex(),
                endIdx,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        text.setSpan(
                foregroundColorSpan,
                textMatch.getIndex(),
                endIdx,
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        tv.setText(text);
    }

    private void buildOutput(Map<String, String> infoMap) {
        scrollContainer.addView(getDivider());

        Set<String> keys = infoMap.keySet();
        for (String key : keys) {
            addTextBubble(key, SpannableString.valueOf(Html.fromHtml(infoMap.get(key))));
        }

        scrollContainer.requestLayout();
        scrollContainer.invalidate();
    }

    private void addTextBubble(String header, SpannableString description) {
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

        return (ViewGroup) inflater.inflate(R.layout.text_bubble, null);
    }

    private View getDivider() {
        View divider = new View(getContext());
        divider.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 16));
        divider.setBackgroundColor(Color.TRANSPARENT);
        return divider;
    }


}
