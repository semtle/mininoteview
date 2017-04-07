package com.mininoteview.mod;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


class MyUtil
{
	static int folderFirstCompare(String object1, String object2, int sortorder)
	{
		if(object1.equals(".."))
			return -1;
		if(object2.equals(".."))
			return 1;
		boolean obj1isDir = object1.startsWith("/");
		boolean obj2isDir = object2.startsWith("/");
		if(obj1isDir == obj2isDir)
		{
			return object1.compareTo(object2) * sortorder;
		}
		else if(obj1isDir)
		{
			return -sortorder;
		}
		else
		{
			return sortorder;
		}
	}

	static String upOneLevel(String DirPath)
	{
		int i = DirPath.lastIndexOf("/");
		if(i <= 0)
		{
			return "/";
		}
		else
			return DirPath.substring(0, i);
	}

	static List<String> fillList(File dir, final int sortOrder)
	{
		String DirPath = dir.getAbsolutePath();
		File[] files = dir.listFiles();
		if(files == null)
		{
			return null;
		}

		List<String> items = new ArrayList<String>();

		if(!DirPath.equals("/"))
		{
			items.add("..");
		}

		for(File file : files)
		{
			String pname = (file.isDirectory() ? "/" : "") + file.getName();
			items.add(pname);
		}

		Collections.sort(items, new Comparator<String>()
		{
			public int compare(String object1, String object2)
			{
				return folderFirstCompare(object1, object2, sortOrder);
			}
		});
		return items;
	}

	static List<String> fillList(String DirPath)
	{
		return fillList(new File(DirPath), 1);
	}

	static byte[] readTextFile(String strFilePath) throws Exception
	{
		BufferedInputStream b_ins = null;

		try
		{
			File file = new File(strFilePath);
			byte[] buff = new byte[(int) file.length()];
			FileInputStream f_ins = new FileInputStream(strFilePath);

			b_ins = new BufferedInputStream(f_ins);
			b_ins.read(buff, 0, buff.length);

			return buff;
			//			return new String(buff);
		}
		finally
		{
			if(b_ins != null)
			{
				try
				{
					b_ins.close();
					//b_ins = null;
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	static boolean isChiFile(File aFile)
	{
		if(!aFile.exists() || (aFile.length() < 32) || (aFile.length() % 8 != 0))
			return false;
		byte[] buff = new byte[4];
		try
		{
			FileInputStream f_ins = new FileInputStream(aFile);
			f_ins.read(buff, 0, 4);
			f_ins.close();
			String BFHeader = new String(buff, 0, 4);
			return BFHeader.equals("BF01");
		}
		catch(Exception e)
		{
			return false;
		}
	}

	static boolean isChiData(byte[] data)
	{
		if(data == null || (data.length < 32) || (data.length % 8 != 0))
			return false;
		String BFHeader = new String(data, 0, 4);
		return BFHeader.equals("BF01");
	}

	// テキストファイル書込処理
	static void writeTextFile(String strFilePath, byte[] text) throws Exception
	{
		BufferedOutputStream bw = null;

		try
		{
			FileOutputStream fw = new FileOutputStream(strFilePath);
			bw = new BufferedOutputStream(fw);
			bw.write(text, 0, text.length);
		}
		finally
		{
			if(bw != null)
			{
				try
				{
					bw.close();
					//bw = null;
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}


	static byte[] decrypt(byte[] data, byte[] passDigest) throws Exception
	{

		int length = data.length;

		/*
		if(length - 32 < 0)
			throw new MyUtilException(R.string.util_error_invalid_filesize1, "Invalid file");
		if(length % 8 != 0)
			throw new MyUtilException(R.string.util_error_invalid_filesize2, "Invalid file size (not mutiples of 8)");


		String BFHeader = new String(data, 0, 4);
		if(!BFHeader.equals("BF01"))
			throw new MyUtilException(R.string.util_error_invalid_fileheader, "This File is NOT BF01");
		*/
		if(!isChiData(data))
			throw new MyUtilException(R.string.util_error_invalid_fileheader, "Not a valid chi file");

		int BFSize = 0;
		int k = 1;
		for(int i = 4; i < 8; i++)
		{
			int size = data[i];
			if(size < 0)
				size = 256 + size;

			BFSize = BFSize + size * k;
			k = k * 256;
		}

		if(passDigest == null)
			throw new MyUtilException(R.string.util_error_passdigest_is_null, "password input error");


//////////////////////////////////////////////////
//		 CryptManagerによる暗号化ファイルのフォーマット
//		 The format of the container is:
//		 0-3  : BF01(4 bytes)
//		 4-7  : data length (include randum area + md5sum)(4 bytes)
//		 8-15 :* random data(8 bytes)
//		16-31 :* md5sum of plain text(16 bytes)
//		32-   :* data


		byte[] dec = new byte[length - 8];
		byte[] iv = new byte[8];

		SecretKeySpec key = new SecretKeySpec(passDigest, "Blowfish");
		IvParameterSpec ivps = new IvParameterSpec(iv);
		Cipher cbfish = Cipher.getInstance("BLOWFISH/CBC/NoPadding");
		cbfish.init(Cipher.DECRYPT_MODE, key, ivps);
		cbfish.doFinal(data, 8, length - 8, dec, 0);

		byte[] orgMd5 = new byte[16];
		System.arraycopy(dec, 8, orgMd5, 0, 16);

		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update(dec, 24, BFSize);
		byte[] dataMd5 = md5.digest();

		if(!MessageDigest.isEqual(orgMd5, dataMd5))
		{
			throw new MyUtilException(R.string.util_error_md5_check_sum_error, "Password is not correct.");
		}

		byte[] dec_data = new byte[BFSize];//return用buffer
		System.arraycopy(dec, 24, dec_data, 0, BFSize);

		return dec_data;
	}


	static byte[] encrypt(byte[] data, byte[] passDigest) throws Exception
	{


		int enc_size;
		if(data.length % 8 == 0)
		{
			enc_size = data.length + 32;
		}
		else
		{
			enc_size = (data.length / 8 + 1) * 8 + 32; //8の倍数＋32ヘッダサイズ
		}

		byte[] savedata = new byte[enc_size];


//////////////////////////////////////////////////
//		 CryptManagerによる暗号化ファイルのフォーマット
//		 The format of the container is:
//		 0-3  : BF01(4 bytes)
//		 4-7  : data length (include randum area + md5sum)(4 bytes)
//		 8-15 :* random data(8 bytes)
//		16-31 :* md5sum of plain text(16 bytes)
//		32-   :* data


//		 0-3  : BF01(4 bytes)
		System.arraycopy("BF01".getBytes(), 0, savedata, 0, 4);


//		 4-7  : data length (include randum area + md5sum)(4 bytes)

		int l = data.length;
		savedata[7] = (byte) ((l >>> 24) & 0xFF);
		savedata[6] = (byte) ((l >>> 16) & 0xFF);
		savedata[5] = (byte) ((l >>> 8) & 0xFF);
		savedata[4] = (byte) ((l) & 0xFF);


		// データ部からdigestを計算
		//MD5 md5 = new MD5();
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update(data, 0, data.length);
		byte[] dataMd5 = md5.digest();

		byte[] tmpdata = new byte[enc_size - 8];//最初の8byteを除く部分のbuffer


		System.arraycopy(dataMd5, 0, tmpdata, 8, 16);//md5を格納　16-31(tmpdataの8から)
		System.arraycopy(data, 0, tmpdata, 24, data.length);

//		printHex(tmpdata,tmpdata.length);
//		System.out.println("=======================");

//		 8-15 :* random data(8 bytes)
		Random rand = new Random();
		for(int i = 0; i < 8; i++)
		{
			tmpdata[i] = (byte) rand.nextInt(256);
		}


		//byte[] dec = new byte[length - 8];
		byte[] iv = new byte[8];

		SecretKeySpec key = new SecretKeySpec(passDigest, "Blowfish");
		IvParameterSpec ivps = new IvParameterSpec(iv);
		Cipher cbfish = Cipher.getInstance("BLOWFISH/CBC/NoPadding");
		cbfish.init(Cipher.ENCRYPT_MODE, key, ivps);
		cbfish.doFinal(tmpdata, 0, tmpdata.length, savedata, 8);
		return savedata;
	}


	// ディレクトリ作成処理
	static boolean createDir(File dir) throws Exception
	{
		if(dir.exists())
		{
//				throw new IOException("Folder/File already exists.");
			throw new MyUtilException(R.string.util_error_file_already_exists, "Folder/File already exists.");
		}
		else
		{
			return dir.mkdirs();    //make folders
		}


	}

	// ファイル名変更処理
	static boolean renameFile(File srcFile, File dstFile) throws Exception
	{
		if(dstFile.exists())
		{
//				throw new IOException("Folder/File already exists: " + dstFile.getName());
			throw new MyUtilException(R.string.util_error_file_already_exists, "Folder/File already exists.");
		}
		else
		{
			return srcFile.renameTo(dstFile);
		}


	}


	// file copy using NIO Channel
	static void fileCopy(File source, File target) throws Exception
	{
		FileChannel in = null;
		FileChannel out = null;

		FileInputStream inStream = null;
		FileOutputStream outStream = null;

		try
		{
			inStream = new FileInputStream(source);
			outStream = new FileOutputStream(target);

			in = inStream.getChannel();
			out = outStream.getChannel();

			in.transferTo(0, in.size(), out);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			throw e;
		}
		finally
		{
			close(inStream);
			close(in);
			close(outStream);
			close(out);
		}
	}

	private static void close(Closeable closable) throws Exception
	{
		if(closable != null)
		{
			try
			{
				closable.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
				throw e;
			}
		}
	}


	static void showMessage(String msg, Activity activity)
	{
		new AlertDialog.Builder(activity)
				.setMessage(msg)
				.setNeutralButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener()
				{
					// この中に"OK"時の処理をいれる。
					public void onClick(DialogInterface dialog, int whichButton)
					{
					}
				})
				.show();
	}
}
