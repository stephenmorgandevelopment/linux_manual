package com.stephenmorgandevelopment.thelinuxmanual.ui;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.stephenmorgandevelopment.thelinuxmanual.R;
import com.stephenmorgandevelopment.thelinuxmanual.utils.Preferences;

public class StyleSettingsActivity extends AppCompatActivity {
	private static final String TAG = StyleSettingsActivity.class.getSimpleName();

//	private Button primaryColorBtn, secondaryColorBtn, thirdColorBtn, accentColorBtn;
//
//	private ConstraintLayout colorPicker;
//	private EditText redVal, greenVal, blueVal;
//	private SeekBar redSeek, greenSeek, blueSeek;
//
//	private Button resetColorBtn, applyColorBtn;

	private SeekBar listFontSelector;
	private SeekBar detailsFontSelector;


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
		super.onCreate(savedInstanceState, persistentState);

		Toolbar toolbar = findViewById(R.id.settingsToolbar);
		setSupportActionBar(toolbar);
		toolbar.setNavigationOnClickListener(onNavigateBackClicked);

//		primaryColorBtn = findViewById(R.id.colorPrimaryBtn);
//		secondaryColorBtn = findViewById(R.id.colorSecondaryBtn);
//		thirdColorBtn = findViewById(R.id.colorThirdBtn);
//		accentColorBtn = findViewById(R.id.colorAccentBtn);
//
//		colorPicker = findViewById(R.id.colorPicker);
//		redVal = findViewById(R.id.redValue);
//		greenVal = findViewById(R.id.greenValue);
//		blueVal = findViewById(R.id.blueValue);
//		redSeek = findViewById(R.id.redSeek);
//		greenSeek = findViewById(R.id.greenSeek);
//		blueSeek = findViewById(R.id.blueSeek);
//
//		resetColorBtn = findViewById(R.id.colorResetBtn);
//		applyColorBtn = findViewById(R.id.colorApplyBtn);

		listFontSelector = findViewById(R.id.listFontSeek);
		detailsFontSelector = findViewById(R.id.detailsFontSeek);

		listFontSelector.setOnSeekBarChangeListener(onChangeSelector);
		detailsFontSelector.setOnSeekBarChangeListener(onChangeSelector);
	}

	SeekBar.OnSeekBarChangeListener onChangeSelector = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if(fromUser) {
//				int size = 0;
//				switch (progress) {
//					case 0: size = 14;
//					break;
//					case 1: size = 16;
//					break;
//					case 2: size = 18;
//					break;
//				}

				int size = (progress*2) + 14;
				if(seekBar.getId() == listFontSelector.getId()) {
					Preferences.setListFontSize(size);
				} else if(seekBar.getId() == detailsFontSelector.getId()) {
					Preferences.setDetailsFontSize(size);
				}
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {}
	};

	@Override
	protected void onResume() {
		super.onResume();

		listFontSelector.setProgress((Preferences.getListFontSize()-14)/2);
		detailsFontSelector.setProgress((Preferences.getDetailsFontSize()-14)/2);
	}

	View.OnClickListener onNavigateBackClicked = (navigationButton) -> {
		finish();
	};
}
