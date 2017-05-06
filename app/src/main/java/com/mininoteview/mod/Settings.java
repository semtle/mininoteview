package com.mininoteview.mod;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Settings extends PreferenceActivity
{
	private PreferenceScreen mFontSizePref;
	private ListPreference mTypefaceListScreen;
	private PreferenceScreen mFontSizeOnListPref;
	private float fontSize = 18;
	private float fontSizeOnList = 24;
	private boolean mBackKeyDown = false;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		Config.update(this);
		fontSizeOnList = Config.getFontSizeOnList();

		Boolean showButtonsFlag = Config.getShowButtonsFlag();
		fontSize = Config.getFontSize();
		String typeface = Config.getTypeface();
		Boolean noTitleBarFlag = Config.getNoTitleBarFlag();


		CharSequence showButtonsKey = getText(R.string.prefShowButtonsKey);
		CheckBoxPreference showButtonsCheckBox = (CheckBoxPreference) findPreference(showButtonsKey);

		showButtonsCheckBox.setTitle(R.string.prefShowButtons);

		showButtonsCheckBox.setSummaryOn(R.string.prefShowButtonsSummaryOn);
		showButtonsCheckBox.setSummaryOff(R.string.prefShowButtonsSummaryOff);

		showButtonsCheckBox.setChecked(showButtonsFlag);

		CharSequence fontSizeKey = getText(R.string.prefFontSizeKey);
		mFontSizePref = (PreferenceScreen) findPreference(fontSizeKey);
		mFontSizePref.setSummary(getText(R.string.prefFontSizeSummary) + ": " + Float.toString(fontSize) + " sp");

		mFontSizePref.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			public boolean onPreferenceClick(Preference pref)
			{
				return onPreferenceClick_setFontSize();
			}
		});

		CharSequence typefaceListScreen = getText(R.string.prefTypefaceKey);
		mTypefaceListScreen = (ListPreference) findPreference(typefaceListScreen);
		final CharSequence[] tf_entries = mTypefaceListScreen.getEntries();
		final CharSequence[] tf_entryValues = mTypefaceListScreen.getEntryValues();


		String tf_currentEntry = typeface;
		for(int i = 0; i < tf_entryValues.length; i++)
		{
			if(tf_entryValues[i].toString().equals(typeface))
			{
				tf_currentEntry = tf_entries[i].toString();
				break;
			}
		}
		mTypefaceListScreen.setSummary(tf_currentEntry);

		mTypefaceListScreen.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference pref, Object val)
			{
				String newval = val.toString();
				String newEntry = newval;
				for(int i = 0; i < tf_entryValues.length; i++)
				{
					if(tf_entryValues[i].toString().equals(newval))
					{
						newEntry = tf_entries[i].toString();
						break;
					}
				}
				mTypefaceListScreen.setSummary(newEntry);

				return true;
			}
		});

		CharSequence noTitleBarKey = getText(R.string.prefNoTitleBarKey);
		CheckBoxPreference mNoTitleBarCheckBox = (CheckBoxPreference) findPreference(noTitleBarKey);

		mNoTitleBarCheckBox.setTitle(R.string.prefNoTitleBar);

		mNoTitleBarCheckBox.setChecked(noTitleBarFlag);

		CharSequence fontSizeOnListKey = getText(R.string.prefFontSizeOnListKey);
		mFontSizeOnListPref = (PreferenceScreen) findPreference(fontSizeOnListKey);
		mFontSizeOnListPref.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			public boolean onPreferenceClick(Preference pref)
			{
				return onPreferenceClick_setFontSizeOnList();
			}
		});
		mFontSizeOnListPref.setSummary(getText(R.string.prefFontSizeOnListSummary) + ": " + Float.toString(fontSizeOnList) + " sp");

	}

	private boolean onPreferenceClick_setFontSize()
	{
		LayoutInflater inflater = LayoutInflater.from(this);
		final View setFontSizeView = inflater.inflate(R.layout.set_fontsize,
				(ViewGroup) findViewById(android.R.id.content), false);
		//final View setFontSizeView = inflater.inflate(R.layout.set_fontsize, null);
		final TextView textview = (TextView) setFontSizeView.findViewById(R.id.dialog_textview);

		float current_size = fontSize;
		final float offset = 5;
		final SeekBar seekBar = (SeekBar) setFontSizeView.findViewById(R.id.seekbar);
		seekBar.setMax(43); // max is 43 + 5. "5" is offset
		seekBar.setProgress((int) (current_size - offset));

		final String sampletext = getString(R.string.prefFontSizeSampleText);
		textview.setText(sampletext + ": " + Float.toString(current_size));
		textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, current_size);

		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			float size;
			public void onStartTrackingTouch(SeekBar seekBar)
			{
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch)
			{
				size = progress + offset;
				textview.setText(sampletext + ": " + Float.toString(size));
				textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
			}

			public void onStopTrackingTouch(SeekBar seekBar)
			{
			}
		});


		final AlertDialog alertDialog = new AlertDialog.Builder(this)
				.setTitle(R.string.prefFontSize)
				.setCancelable(true)
				.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{

						float size = seekBar.getProgress() + offset;
						setFontSize(size);

					}
				}).setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
					}
				})
				.setView(setFontSizeView)
				.create();


		alertDialog.show();


		return true;
	}

	private void setFontSize(float size)
	{
		fontSize = size;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		Editor ed = sp.edit();
		ed.putFloat(getString(R.string.prefFontSizeKey), fontSize);
		ed.apply();

		mFontSizePref.setSummary(getText(R.string.prefFontSizeSummary) + ": " + Float.toString(fontSize) + " sp");

	}

	private boolean onPreferenceClick_setFontSizeOnList()
	{
		LayoutInflater inflater = LayoutInflater.from(this);

		final View setFontSizeView = inflater.inflate(R.layout.set_fontsize,
				(ViewGroup) findViewById(android.R.id.content), false);
		//final View setFontSizeView = inflater.inflate(R.layout.set_fontsize, null);
		final TextView textview = (TextView) setFontSizeView.findViewById(R.id.dialog_textview);

		float current_size = fontSizeOnList;
		final float offset = 5;
		final SeekBar seekBar = (SeekBar) setFontSizeView.findViewById(R.id.seekbar);
		seekBar.setMax(43); // max is 43 + 5. "5" is offset
		seekBar.setProgress((int) (current_size - offset));

		final String sampletext = getString(R.string.prefFontSizeOnListSampleText);
		textview.setText(sampletext + ": " + Float.toString(current_size));
		textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, current_size);

		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			float size;
			public void onStartTrackingTouch(SeekBar seekBar)
			{
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch)
			{
				size = progress + offset;
				textview.setText(sampletext + ": " + Float.toString(size));
				textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
			}

			public void onStopTrackingTouch(SeekBar seekBar)
			{
			}
		});


		final AlertDialog alertDialog = new AlertDialog.Builder(this)
				.setTitle(R.string.prefFontSizeOnList)
				.setCancelable(true)
				.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{

						float size = seekBar.getProgress() + offset;
						setFontSizeOnList(size);

					}
				}).setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
					}
				})
				.setView(setFontSizeView)
				.create();


		alertDialog.show();


		return true;
	}

	private void setFontSizeOnList(float size)
	{
		fontSizeOnList = size;

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		Editor ed = sp.edit();
		ed.putFloat(getString(R.string.prefFontSizeOnListKey), fontSizeOnList);
		ed.apply();

		mFontSizeOnListPref.setSummary(getText(R.string.prefFontSizeOnListSummary) + ": " + Float.toString(fontSizeOnList) + " sp");

	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		if(event.getAction() == KeyEvent.ACTION_DOWN)
		{
			switch(event.getKeyCode())
			{
				case KeyEvent.KEYCODE_BACK:
					mBackKeyDown = true;
					return true;
				default:
					mBackKeyDown = false;
					break;
			}
		}

		if(event.getAction() == KeyEvent.ACTION_UP)
		{
			switch(event.getKeyCode())
			{
				case KeyEvent.KEYCODE_BACK:
					if(mBackKeyDown)
					{
						mBackKeyDown = false;
						finish();
					}
					return true;

				default:
					mBackKeyDown = false;
					break;
			}
		}
		return super.dispatchKeyEvent(event);
	}
}
