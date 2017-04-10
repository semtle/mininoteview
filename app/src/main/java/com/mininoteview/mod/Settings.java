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
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Settings extends PreferenceActivity
{


	//private static final int SELECT_DIR_ACTIVITY = 1;
	//private PreferenceScreen mInitDirScreen;
	private ListPreference mPasswordTimerListScreen;
	private ListPreference mCharsetListScreen;
	private ListPreference mLinebreakListScreen;
	private PreferenceScreen mFontSizePref;
	private ListPreference mTypefaceListScreen;
	private PreferenceScreen mFontSizeOnListPref;
	//private PreferenceScreen mDefaultFolderNamePref;
	//private String initDir = Environment.getExternalStorageDirectory().getPath();
	private float fontSize = 18; //maybe default is 18 sp?
	private float fontSizeOnList = 24; //maybe default is 18 sp?
	//private myTemplateText defaultFolderName;
	//
	private boolean mBackKeyDown = false;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		Config.update(this);
		//initDir = Config.getInitDirName();
		fontSizeOnList = Config.getFontSizeOnList();
		//defaultFolderName = Config.getDefaultFolderName();

		String PWTimer = Config.getPWTimer();
		String charsetName = Config.getCharsetName();
		//Boolean syncTitleFlag = Config.getSyncTitleFlag();
		Boolean showButtonsFlag = Config.getShowButtonsFlag();
		//Boolean viewerModeFlag = Config.getViewerModeFlag();
		fontSize = Config.getFontSize();
		String lineBreak = Config.getLineBreak();
		Boolean autoSaveFlag = Config.getAutoSaveFlag();
		String typeface = Config.getTypeface();
		Boolean noTitleBarFlag = Config.getNoTitleBarFlag();

		//===========================
		//initDirScreen
		//===========================
		/*
		CharSequence csScreenPref3 = getText(R.string.prefInitDirKey);
		mInitDirScreen = (PreferenceScreen) findPreference(csScreenPref3);
		mInitDirScreen.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			//            @Override
			public boolean onPreferenceClick(Preference pref)
			{
				return onPreferenceClick_setInitDir();
			}
		});
		mInitDirScreen.setSummary(getText(R.string.prefInitDirSummary) + ": " + initDir);
		*/

		//===========================
		//Password Timer
		//===========================
		CharSequence listScreen = getText(R.string.prefPWResetTimerKey);
		mPasswordTimerListScreen = (ListPreference) findPreference(listScreen);
//        mPasswordTimerListScreen.setSummary(PWTimer + " minites");
		mPasswordTimerListScreen.setSummary(PWTimer + " " + getText(R.string.prefPWResetTimerSummary));
		mPasswordTimerListScreen.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			//@Override
			public boolean onPreferenceChange(Preference pref, Object val)
			{
				String newval = val.toString();
				mPasswordTimerListScreen.setSummary(newval + " " + getText(R.string.prefPWResetTimerSummary));

				return true;
			}
		});

		//===========================
		//PreferenceList(charset)
		//===========================
		CharSequence charsetListScreen = getText(R.string.prefCharsetNameKey);
		mCharsetListScreen = (ListPreference) findPreference(charsetListScreen);
		mCharsetListScreen.setSummary(charsetName);
		mCharsetListScreen.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			//@Override
			public boolean onPreferenceChange(Preference pref, Object val)
			{
				String newval = val.toString();
				mCharsetListScreen.setSummary(newval);

				return true;
			}
		});

		//===========================
		//PreferenceList(linebreak)
		//===========================
		CharSequence linebreakListScreen = getText(R.string.prefLineBreakCodeKey);
		mLinebreakListScreen = (ListPreference) findPreference(linebreakListScreen);
		final CharSequence[] entries = mLinebreakListScreen.getEntries();
		final CharSequence[] entryValues = mLinebreakListScreen.getEntryValues();


		String currentEntry = lineBreak;
		for(int i = 0; i < entryValues.length; i++)
		{
			if(entryValues[i].toString().equals(lineBreak))
			{
				currentEntry = entries[i].toString();
				break;
			}
		}
		mLinebreakListScreen.setSummary(currentEntry);

		mLinebreakListScreen.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			//@Override
			public boolean onPreferenceChange(Preference pref, Object val)
			{
//        		String newval = val.toString();
//        		mLinebreakListScreen.setSummary(newval);

				String newval = val.toString();
				String newEntry = newval;
				for(int i = 0; i < entryValues.length; i++)
				{
					if(entryValues[i].toString().equals(newval))
					{
						newEntry = entries[i].toString();
						break;
					}
				}
				mLinebreakListScreen.setSummary(newEntry);

				return true;
			}
		});


		//===========================
		// SyncTitle
		//===========================
		//CharSequence syncTitleKey = getText(R.string.prefSyncTitleKey);
		//CheckBoxPreference syncTitleCheckBox = (CheckBoxPreference) findPreference(syncTitleKey);
//        syncTitleCheckBox = new CheckBoxPreference(this);

//        syncTitleCheckBox.setKey(getString(R.string.prefSyncTitleKey));
		//syncTitleCheckBox.setTitle(R.string.prefSyncTitle);

		//syncTitleCheckBox.setSummaryOn(R.string.prefSyncTitleSummaryOn);
		//syncTitleCheckBox.setSummaryOff(R.string.prefSyncTitleSummaryOff);

		//syncTitleCheckBox.setChecked(syncTitleFlag);


		//===========================
		//show bottom bar(close/menu button)
		//===========================
		CharSequence showButtonsKey = getText(R.string.prefShowButtonsKey);
		CheckBoxPreference showButtonsCheckBox = (CheckBoxPreference) findPreference(showButtonsKey);

//        ShowBarOnEditBoxCheckBox.setKey(getString(R.string.prefShowBarOnEditBoxKey));
		showButtonsCheckBox.setTitle(R.string.prefShowButtons);

		showButtonsCheckBox.setSummaryOn(R.string.prefShowButtonsSummaryOn);
		showButtonsCheckBox.setSummaryOff(R.string.prefShowButtonsSummaryOff);

		showButtonsCheckBox.setChecked(showButtonsFlag);

		//===========================
		//viewer mode
		//===========================
		//CharSequence viewerModeKey = getText(R.string.prefViewerModeKey);
		//CheckBoxPreference viewerModeCheckBox = (CheckBoxPreference) findPreference(viewerModeKey);

//        ShowBarOnEditBoxCheckBox.setKey(getString(R.string.prefShowBarOnEditBoxKey));
		// Title
		//viewerModeCheckBox.setTitle(R.string.prefViewerMode);
		//viewerModeCheckBox.setSummary(R.string.prefViewerModeSummary);
		//viewerModeCheckBox.setChecked(viewerModeFlag);


		//===========================
		//Font Size
		//===========================
		CharSequence fontSizeKey = getText(R.string.prefFontSizeKey);
		mFontSizePref = (PreferenceScreen) findPreference(fontSizeKey);
		mFontSizePref.setSummary(getText(R.string.prefFontSizeSummary) + ": " + Float.toString(fontSize) + " sp");

		//Log.d("FontSize ",getText(R.string.prefFontSizeSummary)+ Float.toString(fontSize) + " sp");

		mFontSizePref.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			//            @Override
			public boolean onPreferenceClick(Preference pref)
			{
				return onPreferenceClick_setFontSize();
			}
		});


		//===========================
		//Auto save mode
		//===========================
		CharSequence autoSaveKey = getText(R.string.prefAutoSaveKey);
		CheckBoxPreference mAutoSaveCheckBox = (CheckBoxPreference) findPreference(autoSaveKey);

//        ShowBarOnEditBoxCheckBox.setKey(getString(R.string.prefShowBarOnEditBoxKey));
		mAutoSaveCheckBox.setTitle(R.string.prefAutoSave);

		mAutoSaveCheckBox.setChecked(autoSaveFlag);

		//===========================
		//PreferenceList(Typeface)
		//===========================
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
			//@Override
			public boolean onPreferenceChange(Preference pref, Object val)
			{
//        		String newval = val.toString();
//        		mLinebreakListScreen.setSummary(newval);

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

		//===========================
		//No Title Bar mode
		//===========================
		CharSequence noTitleBarKey = getText(R.string.prefNoTitleBarKey);
		CheckBoxPreference mNoTitleBarCheckBox = (CheckBoxPreference) findPreference(noTitleBarKey);

		mNoTitleBarCheckBox.setTitle(R.string.prefNoTitleBar);

		mNoTitleBarCheckBox.setChecked(noTitleBarFlag);


		//===========================
		//Font Size On List View
		//===========================
		CharSequence fontSizeOnListKey = getText(R.string.prefFontSizeOnListKey);
		mFontSizeOnListPref = (PreferenceScreen) findPreference(fontSizeOnListKey);
		mFontSizeOnListPref.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			//            @Override
			public boolean onPreferenceClick(Preference pref)
			{
				return onPreferenceClick_setFontSizeOnList();
			}
		});
		mFontSizeOnListPref.setSummary(getText(R.string.prefFontSizeOnListSummary) + ": " + Float.toString(fontSizeOnList) + " sp");


		/*
		//===========================
		//default folder name
		//===========================
		CharSequence defaultFolderNameKey = getText(R.string.prefDefaultFolderNameKey);
		mDefaultFolderNamePref = (PreferenceScreen) findPreference(defaultFolderNameKey);
		mDefaultFolderNamePref.setSummary(getText(R.string.prefDefaultFolderNameSummary) + " " + defaultFolderName.getText());

		mDefaultFolderNamePref.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			//            @Override
			public boolean onPreferenceClick(Preference pref)
			{
				return onPreferenceClick_setDefaultFolderName();
			}
		});
		*/

	}

	private boolean onPreferenceClick_setFontSize()
	{
		LayoutInflater inflater = LayoutInflater.from(this);
		final View setFontSizeView = inflater.inflate(R.layout.set_fontsize, null);
		final TextView textview = (TextView) setFontSizeView.findViewById(R.id.dialog_textview);

		float current_size = fontSize;
		final float offset = 5;
		final SeekBar seekBar = (SeekBar) setFontSizeView.findViewById(R.id.seekbar);
		seekBar.setMax(43);//max is 43 + 5. "5" is offset
		seekBar.setProgress((int) (current_size - offset));

		final String sampletext = getString(R.string.prefFontSizeSampleText);
		textview.setText(sampletext + ": " + Float.toString(current_size));
		textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, current_size);

		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			float size;

			//@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
				//Log.v("onStartTrackingTouch()",String.valueOf(seekBar.getProgress()));
			}

			//@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch)
			{
//                Log.v("onProgressChanged()", String.valueOf(progress) + ", " + String.valueOf(fromTouch));
				size = progress + offset;
				textview.setText(sampletext + ": " + Float.toString(size));
				textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
			}

			//@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
//                Log.v("onStopTrackingTouch()",String.valueOf(seekBar.getProgress()));
			}
		});


		final AlertDialog alertDialog = new AlertDialog.Builder(this)
				.setTitle(R.string.prefFontSize)
				.setCancelable(true)
				.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener()
				{
					//		    @Override
					public void onClick(DialogInterface dialog, int which)
					{

						float size = seekBar.getProgress() + offset;
						setFontSize(size);

					}
				}).setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						//Do Nothing
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
		ed.commit();

		mFontSizePref.setSummary(getText(R.string.prefFontSizeSummary) + ": " + Float.toString(fontSize) + " sp");

	}

	private boolean onPreferenceClick_setFontSizeOnList()
	{
		LayoutInflater inflater = LayoutInflater.from(this);

		final View setFontSizeView = inflater.inflate(R.layout.set_fontsize, null);
		final TextView textview = (TextView) setFontSizeView.findViewById(R.id.dialog_textview);

		float current_size = fontSizeOnList;
		final float offset = 5;
		final SeekBar seekBar = (SeekBar) setFontSizeView.findViewById(R.id.seekbar);
		seekBar.setMax(43);//max is 43 + 5. "5" is offset
		seekBar.setProgress((int) (current_size - offset));

		final String sampletext = getString(R.string.prefFontSizeOnListSampleText);
		textview.setText(sampletext + ": " + Float.toString(current_size));
		textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, current_size);

		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			float size;

			//@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
				//Log.v("onStartTrackingTouch()",String.valueOf(seekBar.getProgress()));
			}

			//@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch)
			{
//                Log.v("onProgressChanged()", String.valueOf(progress) + ", " + String.valueOf(fromTouch));
				size = progress + offset;
				textview.setText(sampletext + ": " + Float.toString(size));
				textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
			}

			//@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
//                Log.v("onStopTrackingTouch()",String.valueOf(seekBar.getProgress()));
			}
		});


		final AlertDialog alertDialog = new AlertDialog.Builder(this)
				.setTitle(R.string.prefFontSizeOnList)
				.setCancelable(true)
				.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener()
				{
					//		    @Override
					public void onClick(DialogInterface dialog, int which)
					{

						float size = seekBar.getProgress() + offset;
						setFontSizeOnList(size);

					}
				}).setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						//Do Nothing
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
		ed.commit();

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
					//back key　ACTION_UP
					mBackKeyDown = true;
					return true;
				//break;
				default:
					mBackKeyDown = false;
					break;
			}
		}

		if(event.getAction() == KeyEvent.ACTION_UP)
		{
			switch(event.getKeyCode())
			{
				case KeyEvent.KEYCODE_BACK: // BACK KEY
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
