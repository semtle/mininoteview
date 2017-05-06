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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
	private static final int MAXFILESIZE = 0x400000;
	private static final String CHIHEADER = "BF01";
	private static final String CHSHEADER = "BF0S";

	private static int folderFirstCompare(String object1, String object2, int sortorder)
	{
		if(object1.equals(".."))
			return -1;
		if(object2.equals(".."))
			return 1;
		boolean obj1isDir = object1.startsWith("/");
		boolean obj2isDir = object2.startsWith("/");
		if(obj1isDir == obj2isDir)
		{
			return object1.compareToIgnoreCase(object2) * sortorder;
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

	static String changeFileExt(String fname, String ext)
	{
		int dot = fname.lastIndexOf('.');
		String res;
		if(dot >= 0)
		{
			res = fname.substring(0, dot);
		}
		else
		{
			res = fname;
		}

		return res + "." + ext;
	}

	static List<String> fillList(File dir, final int sortOrder)
	{
		File[] files = dir.listFiles();
		if(files == null)
		{
			return null;
		}

		List<String> items = new ArrayList<String>();

		if(dir.getParentFile() != null)
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

	static List<String> fillList(File dir)
	{
		return fillList(dir, 1);
	}

	//// FIXME: 19/04/17 just return null, don't throw exceptions
	static byte[] readTextFile(String strFilePath) throws Exception
	{
		BufferedInputStream b_ins = null;

		try
		{
			File file = new File(strFilePath);
			byte[] buff = new byte[(int) file.length()];
			FileInputStream f_ins = new FileInputStream(strFilePath);

			b_ins = new BufferedInputStream(f_ins);
			int l = b_ins.read(buff, 0, buff.length);
			b_ins.close();
			if(l != buff.length)
				throw new MyUtilException(R.string.error_file_cannot_read, "File cannot be read");

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

	static byte[] md5Key(byte[] password)
	{
		MessageDigest md5;
		try
		{
			md5 = MessageDigest.getInstance("MD5");
		}
		catch(NoSuchAlgorithmException e)
		{
			return null;
		}

		md5.update(password);
		return md5.digest();
	}

	//////////////////////////////////////////////////
//		 Tombo Chi file format
//		 0: header (4 bytes) must equal "BF01"
//		 4: size (4 bytes) unsigned int (LE)
//		START ENCRYPT
//		 8*: random (8 bytes)
//		16*: md5sum (16 bytes) of text
//		32*: text (size bytes)
// 		END
// 		*Encrypted with key = md5(password) & iv = 0

	// return size if valid, -1 otherwise
	static int getChiSize(byte[] data)
	{
		if(data == null || (data.length < 32) || (data.length % 8 != 0))
			return -1;
		String header = new String(data, 0, 4);
		if(!header.equals(CHIHEADER))
			return -1;
		int size = ByteBuffer.wrap(data, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
		long k = size & 0xFFFFFFFFL;
		if(k < 0 || k > MAXFILESIZE) // 4 MiB limit
			return -1;
		return size;
	}

	static int getChiSize(File aFile)
	{
		if(!aFile.exists() || (aFile.length() < 32) || (aFile.length() % 8 != 0))
			return -1;
		byte[] data = new byte[8];
		try
		{
			FileInputStream f_ins = new FileInputStream(aFile);
			int l = f_ins.read(data, 0, 8);
			f_ins.close();

			if(l != 8)
				return -1;

			String header = new String(data, 0, 4);
			if(!header.equals(CHIHEADER))
				return -1;
			int size = ByteBuffer.wrap(data, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
			long k = size & 0xFFFFFFFFL;
			if(k < 0 || k > MAXFILESIZE) // 4 MiB limit
				return -1;
			return size;
		}
		catch(Exception e)
		{
			return -1;
		}
	}

	private static int bfcrypt(byte[] in, int inOffset, byte[] out, int outOffset, int size, byte[] pass, byte[] iv, int mode)
	{
		if(iv == null)
			iv = new byte[8];
		SecretKeySpec key = new SecretKeySpec(pass, "Blowfish");
		IvParameterSpec ivps = new IvParameterSpec(iv);
		try
		{
			Cipher cbfish = Cipher.getInstance("BLOWFISH/CBC/NoPadding");
			cbfish.init(mode, key, ivps);
			return cbfish.doFinal(in, inOffset, size, out, outOffset);
		}
		catch(Exception e)
		{
			return -1;
		}
	}

	// in-place (en|de)crption
	private static int bfencrypt(byte[] data, int dataOffset, int size, byte[] pass)
	{
		return bfcrypt(data, dataOffset, data, dataOffset, size, pass, null, Cipher.ENCRYPT_MODE);
	}

	private static int bfdecrypt(byte[] data, int dataOffset, int size, byte[] pass)
	{
		return bfcrypt(data, dataOffset, data, dataOffset, size, pass, null, Cipher.DECRYPT_MODE);
	}

	private static int bfencrypt(byte[] data, int dataOffset, int size, byte[] pass, byte[] iv)
	{
		return bfcrypt(data, dataOffset, data, dataOffset, size, pass, iv, Cipher.ENCRYPT_MODE);
	}

	private static int bfdecrypt(byte[] data, int dataOffset, int size, byte[] pass, byte[] iv)
	{
		return bfcrypt(data, dataOffset, data, dataOffset, size, pass, iv, Cipher.DECRYPT_MODE);
	}

	static byte[] decryptChiData(byte[] data, byte[] passDigest) throws MyUtilException
	{

		int length = data.length;

		int size = getChiSize(data);
		if(size < 0)
			throw new MyUtilException(R.string.error_invalid_chi, "not a valid chi file");

		if(passDigest == null)
			throw new MyUtilException(R.string.error_null_passdigest, "password input error");

		int ret = bfdecrypt(data, 8, length - 8, passDigest);
		if(ret < 0)
			throw new MyUtilException(R.string.error_blowfish, "error decrypting/encrypting");

		byte[] includedMD5 = new byte[16];
		System.arraycopy(data, 16, includedMD5, 0, 16);

		byte[] computedMD5;
		try
		{
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(data, 32, size);
			computedMD5 = md5.digest();
		}
		catch(NoSuchAlgorithmException e)
		{
			throw new MyUtilException(R.string.error_unsupported, e.toString());
		}

		if(!MessageDigest.isEqual(includedMD5, computedMD5))
		{
			throw new MyUtilException(R.string.error_password, "Password is not correct.");
		}

		byte[] dec_data = new byte[size];
		System.arraycopy(data, 32, dec_data, 0, size);

		return dec_data;
	}


	static byte[] encryptChiData(byte[] data, byte[] passDigest) throws MyUtilException
	{
		int size = data.length; // data size
		int length = 32 + data.length; // file length
		if(length % 8 != 0)
		{
			// bf operates on blocks of 8 bytes
			length += 8 - (length % 8);
		}

		byte[] savedata = new byte[length];

		// 0: header (4 bytes)
		System.arraycopy(CHIHEADER.getBytes(), 0, savedata, 0, 4);

		if(length < 0 || length > MAXFILESIZE)
			throw new MyUtilException(R.string.error_file_toobig, "file is too big");

		byte[] size_ba = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(size).array();
		// 4: size (4 bytes) unsigned! int (LE)
		System.arraycopy(size_ba, 0, savedata, 4, 4);

		// 8*: random (8 bytes)
		Random rand = new Random();
		for(int i = 8; i < 16; i++)
		{
			savedata[i] = (byte) rand.nextInt(256);
		}

		byte[] dataMd5;
		try
		{
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(data, 0, size);
			dataMd5 = md5.digest();
		}
		catch(NoSuchAlgorithmException e)
		{
			throw new MyUtilException(R.string.error_unsupported, e.toString());
		}
		// 16*: md5 (16 bytes)
		System.arraycopy(dataMd5, 0, savedata, 16, 16);

		// 32*: text (size bytes)
		System.arraycopy(data, 0, savedata, 32, size);

		int ret = bfencrypt(savedata, 8, length - 8, passDigest);
		if(ret < 0)
			throw new MyUtilException(R.string.error_blowfish, "error decrypting/encrypting");

		return savedata;
	}

	static byte[] decryptChsData(byte[] data, byte[] passDigest) throws MyUtilException
	{

		int length = data.length;

		int size = getChiSize(data);
		if(size < 0)
			throw new MyUtilException(R.string.error_invalid_chi, "not a valid chi file");

		if(passDigest == null)
			throw new MyUtilException(R.string.error_null_passdigest, "password input error");


		int ret = bfdecrypt(data, 8, 8, passDigest);
		if(ret < 0)
			throw new MyUtilException(R.string.error_blowfish, "error decrypting/encrypting");

		byte[] iv = new byte[8];
		System.arraycopy(data, 8, iv, 0, 8);
		ret = bfdecrypt(data, 16, length - 16, passDigest, iv);
		if(ret < 0)
			throw new MyUtilException(R.string.error_blowfish, "error decrypting/encrypting");

		byte[] includedMD5 = new byte[16];
		System.arraycopy(data, 16, includedMD5, 0, 16);

		byte[] computedMD5;
		try
		{
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(data, 32, size);
			computedMD5 = md5.digest();
		}
		catch(NoSuchAlgorithmException e)
		{
			throw new MyUtilException(R.string.error_unsupported, e.toString());
		}

		if(!MessageDigest.isEqual(includedMD5, computedMD5))
		{
			throw new MyUtilException(R.string.error_password, "Password is not correct.");
		}

		byte[] dec_data = new byte[size];
		System.arraycopy(data, 32, dec_data, 0, size);

		return dec_data;
	}


	static byte[] encryptChsData(byte[] data, byte[] passDigest) throws MyUtilException
	{
		int size = data.length; // data size
		int length = 32 + data.length; // file length
		if(length % 8 != 0)
		{
			// bf operates on blocks of 8 bytes
			length += 8 - (length % 8);
		}

		byte[] savedata = new byte[length];

		// 0: header (4 bytes)
		System.arraycopy(CHSHEADER.getBytes(), 0, savedata, 0, 4);

		if(length < 0 || length > MAXFILESIZE)
			throw new MyUtilException(R.string.error_file_toobig, "file is too big");

		byte[] size_ba = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(size).array();
		// 4: size (4 bytes) unsigned! int (LE)
		System.arraycopy(size_ba, 0, savedata, 4, 4);

		// 8*: iv (8 bytes)

		byte[] iv = new byte[8];
		Random rand = new Random();
		for(int i = 0; i < 8; i++)
		{
			iv[i] = (byte) rand.nextInt(256);
		}

		System.arraycopy(iv, 0, savedata, 8, 8);
		int ret = bfencrypt(savedata, 8, 8, passDigest);
		if(ret < 0)
			throw new MyUtilException(R.string.error_blowfish, "error decrypting/encrypting");


		byte[] dataMd5;
		try
		{
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(data, 0, size);
			dataMd5 = md5.digest();
		}
		catch(NoSuchAlgorithmException e)
		{
			throw new MyUtilException(R.string.error_unsupported, e.toString());
		}
		// 16*: md5 (16 bytes)
		System.arraycopy(dataMd5, 0, savedata, 16, 16);

		// 32*: text (size bytes)
		System.arraycopy(data, 0, savedata, 32, size);

		ret = bfencrypt(savedata, 16, length - 16, passDigest, iv);
		if(ret < 0)
			throw new MyUtilException(R.string.error_blowfish, "error decrypting/encrypting");

		return savedata;
	}

	static boolean createDir(File dir) throws Exception
	{
		if(dir.exists())
		{
			throw new MyUtilException(R.string.error_file_exists, "Folder/File already exists.");
		}
		else
		{
			return dir.mkdirs();
		}


	}

	static boolean renameFile(File srcFile, File dstFile) throws Exception
	{
		if(dstFile.exists())
		{
			throw new MyUtilException(R.string.error_file_exists, "Folder/File already exists.");
		}
		else
		{
			return srcFile.renameTo(dstFile);
		}


	}

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
				.setNeutralButton(R.string.action_ok, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
					}
				})
				.show();
	}
}
