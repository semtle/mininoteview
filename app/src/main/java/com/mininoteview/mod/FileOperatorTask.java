package com.mininoteview.mod;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

import java.io.File;

class FileOperatorTask extends AsyncTask<String, Integer, Boolean>
{

	static String DELETE_FILE = "DELETE";
	static String COPY_FILE = "COPY";
	static String MOVE_FILE = "MOVE";
	private ProgressDialog mProgressDialog;
	private MainActivity mActivity;
	private boolean errorOccured = false;
	private String errorMessage = "";
	private String mResultMessage = "";
	private Handler handler;


	FileOperatorTask(MainActivity activity)
	{
		mActivity = activity;
	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		mProgressDialog = new ProgressDialog(mActivity);

		mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			public void onCancel(DialogInterface dialog)
			{
				taskCancel();
			}
		});

		mProgressDialog.setMessage(mActivity.getText(R.string.notify_now_in_progress));
		mProgressDialog.show();
		errorOccured = false;

		handler = new Handler();
	}

	@Override
	protected Boolean doInBackground(String... params)
	{

		boolean result = false;

		String operationType;
		String srcFilePath = "";
		String dstFilePath = "";


		if(params.length > 0)
		{
			operationType = params[0];
		}
		else
		{
			return false;
		}
		if(params.length > 1) srcFilePath = params[1];
		if(params.length > 2) dstFilePath = params[2];

		if(operationType.equals(DELETE_FILE))
		{
			result = deleteFile(new File(srcFilePath));
		}
		else if(operationType.equals(MOVE_FILE))
		{
			File srcFile = new File(srcFilePath);
			File dstFile = new File(dstFilePath);

			if(srcFile.isDirectory() && dstFilePath.startsWith(srcFilePath + "/"))
			{
				errorOccured = true;
				errorMessage = mActivity.getString(R.string.error_file_cannot_copy) + "\n" + srcFilePath + " -> " + dstFilePath + ".";
				return false;
			}
			result = moveFile(srcFile, dstFile);
		}
		else if(operationType.equals(COPY_FILE))
		{
			File srcFile = new File(srcFilePath);
			File dstFile = new File(dstFilePath);

			if(srcFile.isDirectory() && dstFilePath.startsWith(srcFilePath + "/"))
			{
				errorOccured = true;
				errorMessage = mActivity.getString(R.string.error_file_cannot_copy) + "\n" + srcFilePath + " -> " + dstFilePath + ".";
				return false;
			}
			result = copyFile(srcFile, dstFile);
		}
		return result;
	}

	@Override
	protected void onPostExecute(Boolean result)
	{
		super.onPostExecute(result);
		if(mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();

		if(result)
		{
			if(!mResultMessage.equals(""))
				Toast.makeText(mActivity, mResultMessage, Toast.LENGTH_SHORT).show();
		}
		else
		{
			if(errorOccured)
			{
				if(!mActivity.isFinishing()) MyUtil.showMessage(errorMessage, mActivity);
			}
			else
			{
				if(!mActivity.isFinishing())
					MyUtil.showMessage(mActivity.getString(R.string.error_operation_failed), mActivity);
			}

		}

		mActivity.refreshDir();

	}


	private boolean deleteFile(File file)
	{
		if(isCancelled())
		{
			errorOccured = true;
			errorMessage = "operation cancelled.";
			return false;
		}

		try
		{
			if(file.isDirectory())
			{
				String[] children = file.list();
				for(String child : children)
				{
					boolean success = deleteFile(new File(file, child));
					if(!success)
					{
						return false;
					}
				}
			}

			postMessage("Deleting " + file.getAbsolutePath());
			return file.delete();
		}
		catch(Exception e)
		{
			errorOccured = true;
			errorMessage = file.getAbsolutePath() + ":\n" + e.toString() + "\n";

		}
		return false;
	}

	private boolean moveFile(File srcFile, File dstFile)
	{
		if(dstFile.exists())
		{
			errorOccured = true;
			errorMessage = mActivity.getString(R.string.error_file_already_exists) + ": " + dstFile.getName();
			return false;
		}
		if(srcFile.renameTo(dstFile))
		{
			mResultMessage = mActivity.getString(R.string.toast_move_file) + ": " + dstFile.getAbsolutePath();
			return true;
		}


		try
		{
			return transferFile(srcFile, dstFile) && deleteFile(srcFile);

		}
		catch(Exception e)
		{
			errorOccured = true;
			errorMessage = e.toString();
		}
		return false;
	}


	private boolean copyFile(File srcFile, File dstFile)
	{
		try
		{
			return transferFile(srcFile, dstFile);

		}
		catch(Exception e)
		{
			errorOccured = true;
			errorMessage = e.toString();
		}
		return false;
	}

	private boolean transferFile(File srcFile, File dstFile) throws Exception
	{
		if(isCancelled())
		{
			errorOccured = true;
			errorMessage = "operation cancelled.";
			return false;
		}

		if(!srcFile.canRead())
		{
			errorOccured = true;
			errorMessage = mActivity.getString(R.string.error_file_cannot_read) + ": " + dstFile.getName();
			return false;


		}
		else if(srcFile.isDirectory())
		{

			if(!dstFile.exists())
			{
				if(!dstFile.mkdirs())
					return false;
			}
			else if(!dstFile.isDirectory())
			{
				errorOccured = true;
				errorMessage = mActivity.getString(R.string.error_file_already_exists) + ": " + dstFile.getName();
				return false;
			}

			String[] children = srcFile.list();
			for(String child : children)
			{
				boolean success1 = transferFile(new File(srcFile, child), new File(dstFile, child));
				if(!success1) return false;
			}
			return true;
		}
		else
		{
			if(dstFile.exists())
			{
				errorOccured = true;
				errorMessage = mActivity.getString(R.string.error_file_already_exists) + ": " + dstFile.getName();
				return false;
			}
			else
			{
				postMessage("Copying " + srcFile.getAbsolutePath());
				MyUtil.fileCopy(srcFile, dstFile);
				return true;
			}
		}
	}


	private void postMessage(final String msg)
	{
		handler.post(new Runnable()
		{
			public void run()
			{
				mProgressDialog.setMessage(msg);

			}
		});
	}


	@Override
	protected void onCancelled()
	{
		if(mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.dismiss();
		Toast.makeText(mActivity, "Canceling...", Toast.LENGTH_SHORT).show();
		mActivity.refreshDir();
		super.onCancelled();
	}

	private void taskCancel()
	{
		cancel(true);
	}
}