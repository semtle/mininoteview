package com.mininoteview.mod;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class Config
{

	//common
	private static Boolean showButtonsFlag = true;
	private static String PWTimer = "3";

	//File Browser View
	//private static String initDirName = Environment.getExternalStorageDirectory().getPath();
	private static float fontSizeOnList = 28;
	private static int fileListOrder = 1;

	//Note View
	private static String charsetName = "utf-8";
	private static String lineBreak = "auto";
	private static Boolean noTitleBarFlag = false;
	private static float fontSize = 18;
	private static String typeface = "DEFAULT";

	static String getPWTimer()
	{
		return PWTimer;
	}

	static String getCharsetName()
	{
		return charsetName;
	}

	static String getLineBreak()
	{
		return lineBreak;
	}

	static Boolean getShowButtonsFlag()
	{
		return showButtonsFlag;
	}

	static float getFontSize()
	{
		return fontSize;
	}

	static String getTypeface()
	{
		return typeface;
	}

	static Boolean getNoTitleBarFlag()
	{
		return noTitleBarFlag;
	}

	static int getFileListOrder()
	{
		return fileListOrder;
	}

	static float getFontSizeOnList()
	{
		return fontSizeOnList;
	}


	static void update(Context context)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		fontSizeOnList = sharedPreferences.getFloat(context.getString(R.string.prefFontSizeOnListKey), fontSizeOnList);

		//initDirName = sharedPreferences.getString(context.getString(R.string.prefInitDirKey), initDirName);

		fileListOrder = sharedPreferences.getInt(context.getString(R.string.prefFileListOrderKey), fileListOrder);

		typeface = sharedPreferences.getString(context.getString(R.string.prefTypefaceKey), typeface);

		noTitleBarFlag = sharedPreferences.getBoolean(context.getString(R.string.prefNoTitleBarKey), noTitleBarFlag);

		PWTimer = sharedPreferences.getString(context.getString(R.string.prefPWResetTimerKey), "3");

		charsetName = sharedPreferences.getString(context.getString(R.string.prefCharsetNameKey), "utf-8");

		showButtonsFlag = sharedPreferences.getBoolean(context.getString(R.string.prefShowButtonsKey), true);

		fontSize = sharedPreferences.getFloat(context.getString(R.string.prefFontSizeKey), fontSize);

		lineBreak = sharedPreferences.getString(context.getString(R.string.prefLineBreakCodeKey), "auto");
	}
}
