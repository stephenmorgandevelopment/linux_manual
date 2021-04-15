package com.stephenmorgandevelopment.thelinuxmanual.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.text.LineBreaker;
import android.os.Bundle;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ParagraphStyle;
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

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.models.Command;
import com.stephenmorgandevelopment.thelinuxmanual.models.SingleTextMatch;
import com.stephenmorgandevelopment.thelinuxmanual.utils.HtmlNewlinePreserver;
import com.stephenmorgandevelopment.thelinuxmanual.viewmodels.CommandInfoViewModel;
import com.stephenmorgandevelopment.thelinuxmanual.viewmodels.MainActivityViewModel;

import java.util.ArrayList;
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

	private ConstraintLayout searchControlBar;
	private TextView searchTextDisplay;
	private TextView numberOfTextMatches;

	private LayoutInflater inflater;

	private static final String KEY_ID = "id";

	public static CommandInfoFragment newInstance(long id) {
		Bundle args = new Bundle();
		args.putLong(KEY_ID, id);

		CommandInfoFragment fragment = new CommandInfoFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		viewModel = new ViewModelProvider(requireActivity()).get(MainActivityViewModel.class);
		infoModel = new ViewModelProvider(this).get(CommandInfoViewModel.class);

		setHasOptionsMenu(true);

		jumpToList = new ArrayList<>();
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

		inflater  = (LayoutInflater) requireContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		scrollContainer = view.findViewById(R.id.scrollContainer);
		rootScrollView = view.findViewById(R.id.rootScrollView);

		long id = requireArguments().getLong(KEY_ID);
		Command command = viewModel.getCommandFromListById(id);

		if(command != null) {
			infoModel.init(command);
			buildOutput(command.getData());
		}

		initSearchUi(view);
		initSearchButtons(view);

		requireActivity().getOnBackPressedDispatcher().addCallback(onBackPressedCallback);
	}

	private void initSearchUi(View view) {
		searchBar = view.findViewById(R.id.textSearchBox);
		searchEditText = view.findViewById(R.id.searchEditText);

		searchControlBar = view.findViewById(R.id.searchControlBar);
		searchTextDisplay = view.findViewById(R.id.searchTextDisplay);
		numberOfTextMatches = view.findViewById(R.id.numberOfTextMatches);
	}

	private void initSearchButtons(View view) {
		ImageButton searchBarButton = view.findViewById(R.id.searchBarButton);
		Button prevSearchButton = view.findViewById(R.id.prevSearchButton);
		Button nextSearchButton = view.findViewById(R.id.nextSearchButton);

		searchBarButton.setOnClickListener(onClickSearchBarButton);
		nextSearchButton.setOnClickListener(onClickNextButton);
		prevSearchButton.setOnClickListener(onClickPrevButton);
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
		inflater.inflate(R.menu.command_info_menu, menu);

		Menu dropDownMenu = menu.findItem(R.id.dropDown).getSubMenu();

		for (String title : jumpToList) {
			dropDownMenu.add(R.id.jumpTo, Menu.NONE, Menu.NONE, title);
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
			return true;
		}

		if (jumpToList.contains(item.getTitle().toString())) {
			jumpToTopSection(item.getTitle().toString());
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();

		Objects.requireNonNull(
				((AppCompatActivity) requireActivity())
						.getSupportActionBar()).setTitle(infoModel.getShortName());
	}

	View.OnClickListener onClickSearchBarButton = v -> {
		String query = searchEditText.getText().toString();

		if (query.length() >= 2) {
			infoModel.searchTextFor(query, viewModel.getCommandFromListById(infoModel.getId()));

			gotoMatch(infoModel.getCurrentMatch());

			if(searchControlBar.getVisibility() == View.GONE) {
				toggleSearchResults();
			}

			updateCurrentMatchDisplay();
		}
	};

	View.OnClickListener onClickPrevButton = v -> {
		clearSpan(infoModel.getCurrentMatch());
		gotoMatch(infoModel.getPrevMatch());
	};

	View.OnClickListener onClickNextButton = v -> {
		clearSpan(infoModel.getCurrentMatch());
		gotoMatch(infoModel.getNextMatch());
	};

	private void gotoMatch(SingleTextMatch textMatch) {
		if (textMatch == null) {
			Toast.makeText(getContext(), "No matches found.", Toast.LENGTH_SHORT).show();
			return;
		}

		jumpToTextMatch(textMatch);
		highlightCurrentMatch(textMatch);
		updateCurrentMatchDisplay();
	}

	private void jumpToTopSection(String section) {
		View v = scrollContainer.findViewWithTag(section);
		rootScrollView.scrollTo(0, v.getTop() - 12);
	}

	private void jumpToTextMatch(SingleTextMatch textMatch) {
		View v = scrollContainer.findViewWithTag(textMatch.getSection());
		int sectionTop = v.getTop() - 12;

		Layout layout = ((TextView)v.findViewById(R.id.descriptionText)).getLayout();
		int line = layout.getLineForOffset(textMatch.getIndex());
		int lineTop = layout.getLineTop(line) - 64;

		rootScrollView.scrollTo(0, sectionTop + lineTop);
	}

	private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
		@Override
		public void handleOnBackPressed() {
			if(searchBar.getVisibility() == View.VISIBLE) {
				toggleSearchBarDisplay();
				clearSpan(infoModel.getCurrentMatch());
			} else {
				setEnabled(false);
				Objects.requireNonNull(getActivity()).onBackPressed();
			}
		}
	};

	private void clearSpan(SingleTextMatch textMatch) {
		View v = scrollContainer.findViewWithTag(textMatch.getSection());
		TextView tv = ((TextView)v.findViewById(R.id.descriptionText));

		SpannableString text = (SpannableString) tv.getText();
		text.removeSpan(SingleTextMatch.backgroundSpan);
		text.removeSpan(SingleTextMatch.foregroundColorSpan);
	}

	private void toggleSearchResults() {
		if(searchControlBar.getVisibility() == View.GONE) {
			searchControlBar.setVisibility(View.VISIBLE);
		} else {
			searchControlBar.setVisibility(View.GONE);
		}

	}

	private void updateCurrentMatchDisplay() {
		searchTextDisplay.setText(infoModel.getSearchResults().getQuery());

		String numberTextMatchesText = infoModel.getCurrentMatchIndex() + "/" + infoModel.getResultsCount();
		numberOfTextMatches.setText(numberTextMatchesText);
	}

	private void toggleSearchBarDisplay() {
		if (searchBar.getVisibility() == View.GONE) {
			searchBar.setVisibility(View.VISIBLE);
			if(infoModel.hasSearchResults()) {
				searchControlBar.setVisibility(View.VISIBLE);
			}
		} else {
			searchBar.setVisibility(View.GONE);
			searchControlBar.setVisibility(View.GONE);
		}
	}

	private void highlightCurrentMatch(SingleTextMatch textMatch) {
		View bubble = scrollContainer.findViewWithTag(textMatch.getSection());
		TextView tv = bubble.findViewById(R.id.descriptionText);

		SpannableStringBuilder text = (SpannableStringBuilder) tv.getText();

		text.setSpan(
				SingleTextMatch.backgroundSpan,
				textMatch.getIndex(),
				infoModel.calcEndIndex(textMatch.getIndex()),
				SpannableString.SPAN_INCLUSIVE_INCLUSIVE);

		text.setSpan(
				SingleTextMatch.foregroundColorSpan,
				textMatch.getIndex(),
				infoModel.calcEndIndex(textMatch.getIndex()),
				SpannableString.SPAN_INCLUSIVE_INCLUSIVE);

		tv.setText(text, TextView.BufferType.SPANNABLE);
		tv.bringPointIntoView(textMatch.getIndex());
	}

	private void buildOutput(Map<String, String> infoMap) {
		scrollContainer.addView(getDivider());

		Set<String> keys = infoMap.keySet();
		for (String key : keys) {
			addTextBubble(key, infoMap.get(key));
		}

		scrollContainer.requestLayout();
		scrollContainer.invalidate();
	}

	private void addTextBubble(String header, String description) {
		ViewGroup view = getInflatedBubbleView();
		view.setTag(header);

		//TODO Replace with span parsed from HtmlNewlinePreserver
//		SpannableString span = SpannableString.valueOf(Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY));
		SpannableStringBuilder spannableStringBuilder = SpannableStringBuilder.valueOf(Html.fromHtml(description, Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH));
//		SpannableString span = HtmlNewlinePreserver.parse(description);

		((TextView) view.findViewById(R.id.headerText)).setText(header);

		TextView descriptionView = ((TextView) view.findViewById(R.id.descriptionText));
		descriptionView.setSpannableFactory(spannableFactory);
		descriptionView.setText(spannableStringBuilder, TextView.BufferType.SPANNABLE);


		scrollContainer.addView(view);
		scrollContainer.addView(getDivider());

		jumpToList.add(header);
	}

	private ViewGroup getInflatedBubbleView() {
		return (ViewGroup) inflater.inflate(R.layout.text_bubble, scrollContainer, false);
	}

	private View getDivider() {
		View divider = new View(getContext());
		divider.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 16));
		divider.setBackgroundColor(Color.TRANSPARENT);
		return divider;
	}

	private final Spannable.Factory spannableFactory = new Spannable.Factory() {
		@Override
		public Spannable newSpannable(CharSequence source) {
			return (Spannable) source;
		}
	};
}
