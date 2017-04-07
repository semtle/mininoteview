package com.mininoteview.mod;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;


class PasswordBox
{
	private static byte[] passDigest;
	private static long last_time;//タイマーに使う時刻格納変数
	private static long EXPIRED_TIME = 5 * 60 * 1000;//5分でタイマー満了

//	private Form inputPassword;
//	private TextField  passField;//テキストフィールド3(PASSWORD)

//	private TextBox tBox;

	public PasswordBox()
	{
		passDigest = null;//new byte[16];
		last_time = 0;
	}

	static byte[] getPassDigest()
	{
		expiredCheck();//タイマー満了していたらnullになる。
		return passDigest;
	}

	static void resetPassword()
	{
		passDigest = null;
		last_time = 0;
	}

	static void setPassword(String password)
	{
		MessageDigest md5;
		try
		{
			md5 = MessageDigest.getInstance("MD5");
		}
		catch(NoSuchAlgorithmException e)
		{
			return;
		}

		md5.update(password.getBytes());
		byte[] pass = md5.digest();
		if(passDigest == null)
		{
			passDigest = new byte[16];
		}
		System.arraycopy(pass, 0, passDigest, 0, 16);
		last_time = new Date().getTime();
	}

	// true　タイマー満了していません。last_timeをupdateしました。
	// false　タイマー満了しました。　パスワードをリセットします。
	private static boolean expiredCheck()
	{
		long now = new Date().getTime();
		if(now - last_time > EXPIRED_TIME)
		{
			resetPassword();
			return false;
		}
		else
		{
			last_time = now;
			return true;//タイマー満了していません。last_timeをupdateしました。
		}
	}

	//EXPIRED_TIMEを設定
	static void setTimerVal(int min)
	{
		EXPIRED_TIME = min * 60 * 1000;
	}

}
