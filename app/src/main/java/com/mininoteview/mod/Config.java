package com.mininoteview.mod;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class Config
{

	//common
	private static Boolean showButtonsFlag = true;

	//File Browser View
	private static float fontSizeOnList = 28;
	private static int fileListOrder = 1;

	//Note View
	private static Boolean noTitleBarFlag = false;
	private static float fontSize = 18;
	private static String typeface = "DEFAULT";

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

		fileListOrder = sharedPreferences.getInt(context.getString(R.string.prefFileListOrderKey), fileListOrder);

		typeface = sharedPreferences.getString(context.getString(R.string.prefTypefaceKey), typeface);

		noTitleBarFlag = sharedPreferences.getBoolean(context.getString(R.string.prefNoTitleBarKey), noTitleBarFlag);

		showButtonsFlag = sharedPreferences.getBoolean(context.getString(R.string.prefShowButtonsKey), true);

		fontSize = sharedPreferences.getFloat(context.getString(R.string.prefFontSizeKey), fontSize);
	}
}
