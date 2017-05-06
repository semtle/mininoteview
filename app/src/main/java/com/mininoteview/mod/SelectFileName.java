package com.mininoteview.mod;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SelectFileName extends ListActivity
{
	public static final String INTENT_DIRPATH = "DIRPATH";
	public static final String INTENT_FILENAME = "FILENAME";
	public static final String INTENT_ORG_FILENAME = "ORG_FILENAME";
	public static final String INTENT_FILEPATH = "FILEPATH";
	public static final String INTENT_FORMAT = "FORMAT";
	public static final String INTENT_MODE = "MODE";

	public static final String MODE_SAVE = "SAVE";
	public static final String MODE_COPY = "COPY";
	public static final String MODE_MOVE = "MOVE";

	public static final String FORMAT_TXT = "txt";
	public static final String FORMAT_CHI = "chi";
	public static final String FORMAT_CHS = "chs";
	public static final String FORMAT_DAT = "dat";

	private static final int MODEID_NONE = 0;
	private static final int MODEID_SAVE = 1;
	private static final int MODEID_COPY = 2;
	private static final int MODEID_MOVE = 3;
	private int modeID = MODEID_NONE;

	private final String mInitDirName = Environment.getExternalStorageDirectory().getAbsolutePath();
	private File currentDirectory = new File(mInitDirName);

	private String filename;
	private String selectedFormat = FORMAT_TXT;
	private List<String> items = null;

	private boolean mBackKeyDown = false;

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);

		setContentView(R.layout.select_file);

		Button btnOK = (Button) findViewById(R.id.btnOK);
		btnOK.setText(R.string.action_ok);

		Button btnCancel = (Button) findViewById(R.id.btnCancel);
		btnCancel.setText(R.string.action_cancel);

		TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
		final Spinner formatSpinner = (Spinner) findViewById(R.id.formatSpinner);


		EditText mEdtFileName = (EditText) findViewById(R.id.edtFileName);


		Bundle extras = getIntent().getExtras();
		if(extras != null)
		{
			filename = extras.getString(INTENT_FILEPATH);
			File aFile = new File(filename);
			currentDirectory = aFile.getParentFile();
			if(currentDirectory == null)
				currentDirectory = new File(mInitDirName);

			mEdtFileName.setText(aFile.getName());


			if(extras.getString(INTENT_MODE).equals(MODE_SAVE))
			{
				modeID = MODEID_SAVE;
				txtTitle.setText(R.string.action_save_as);

				// add entries to the spinner
				List<String> list = new ArrayList<String>();
				list.add(FORMAT_TXT);
				list.add(FORMAT_CHI);
				list.add(FORMAT_CHS);
				list.add(FORMAT_DAT);
				ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
						android.R.layout.simple_spinner_item, list);
				dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				formatSpinner.setAdapter(dataAdapter);

				selectedFormat = extras.getString(INTENT_FORMAT);
				// set selected item of the spinner
				for(int i = 0; i < formatSpinner.getCount(); i++)
				{
					if(formatSpinner.getItemAtPosition(i).equals(selectedFormat))
					{
						formatSpinner.setSelection(i);
						break;
					}
				}

				// change extention on spinner select
				formatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
				{
					@Override
					public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
					{
						selectedFormat = parent.getItemAtPosition(pos).toString();
						EditText edtFileName = (EditText) findViewById(R.id.edtFileName);
						String strFileName = edtFileName.getText().toString();

						int sel_s = edtFileName.getSelectionStart();
						int sel_e = edtFileName.getSelectionEnd();

						edtFileName.setText(MyUtil.changeFileExt(strFileName, selectedFormat));

						int l = strFileName.length();
						if(sel_s > l) sel_s = l;
						if(sel_e > l) sel_e = l;
						edtFileName.setSelection(sel_s, sel_e);
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0)
					{
						// stuff...
					}
				});
			}

			else if(extras.getString(INTENT_MODE).equals(MODE_COPY))
			{
				modeID = MODEID_COPY;
				txtTitle.setText(R.string.action_copy);
				ViewGroup selectFileLayout = (ViewGroup) findViewById(R.id.selectFile);
				selectFileLayout.removeView(formatSpinner);
			}
			else if(extras.getString(INTENT_MODE).equals(MODE_MOVE))
			{
				modeID = MODEID_MOVE;
				txtTitle.setText(R.string.action_move);
				ViewGroup selectFileLayout = (ViewGroup) findViewById(R.id.selectFile);
				selectFileLayout.removeView(formatSpinner);
			}

		}

		fillList();

		btnOK.setOnClickListener(new View.OnClickListener()
		{

			//		@Override
			public void onClick(View v)
			{
				Intent intent = new Intent();

				EditText edtFileName = (EditText) findViewById(R.id.edtFileName);
				String strFileName = edtFileName.getText().toString().replaceAll("[\\s]*$", "");


				intent.putExtra(INTENT_FILENAME, strFileName);
				intent.putExtra(INTENT_DIRPATH, currentDirectory.getAbsolutePath());
				intent.putExtra(INTENT_ORG_FILENAME, filename);

				if(modeID == MODEID_SAVE)
				{
					selectedFormat = String.valueOf(formatSpinner.getSelectedItem());
					intent.putExtra(INTENT_FORMAT, selectedFormat);
				}

				String strFilePath;
				strFilePath = currentDirectory.getAbsolutePath() + "/" + strFileName;

				intent.putExtra(INTENT_FILEPATH, strFilePath);
				setResult(RESULT_OK, intent);
				finish();
			}

		});

		btnCancel.setOnClickListener(new View.OnClickListener()
		{
			//		@Override
			public void onClick(View v)
			{
				Intent intent = new Intent();
				setResult(RESULT_CANCELED, intent);
				finish();
			}
		});

		if(modeID == MODEID_SAVE)
		{
			mEdtFileName.requestFocus();
		}

		int i = mEdtFileName.getText().toString().lastIndexOf('.');
		if(i > -1) mEdtFileName.setSelection(i);//拡張子の前にカーソルをおく
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);
		String strItem = (String) getListAdapter().getItem(position);

		if(strItem.equals(".."))
		{
			upOneLevel();
			return;
		}
		if(strItem.startsWith("/"))
		{
			File newPath;
			newPath = new File(currentDirectory.getAbsolutePath() + strItem);
			File[] files = newPath.listFiles();
			if(files == null)
			{
				Toast.makeText(this, "Unable Access...", Toast.LENGTH_SHORT).show();
				return;
			}
			currentDirectory = newPath;
			fillList();
			return;
		}

		EditText edtFileName = (EditText) findViewById(R.id.edtFileName);
		edtFileName.setText(strItem);


	}

	private void upOneLevel()
	{
		File parent = currentDirectory.getParentFile();
		if(parent != null)
			currentDirectory = parent;
		fillList();
	}

	private void fillList()
	{
		List<String> fnamelist = MyUtil.fillList(currentDirectory);

		TextView txtDirName = (TextView) findViewById(R.id.txtDirName);
		txtDirName.setText(currentDirectory.getAbsolutePath());

		if(items != null)
		{
			items.clear();
		}
		items = fnamelist;

		FileListAdapter fileList = new FileListAdapter(this, this.items);
		setListAdapter(fileList);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		if(!getListView().hasFocus())
		{
			return super.dispatchKeyEvent(event);
		}

		if(event.getAction() == KeyEvent.ACTION_DOWN)
		{
			switch(event.getKeyCode())
			{
				case KeyEvent.KEYCODE_DEL:
					return true;

				case KeyEvent.KEYCODE_BACK:
					break;

				case KeyEvent.KEYCODE_DPAD_LEFT:
					upOneLevel();
					return true;

				case KeyEvent.KEYCODE_DPAD_RIGHT:
					KeyEvent e = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER);
					return super.dispatchKeyEvent(e);

				default:
					mBackKeyDown = false;
					break;
			}
		}

		if(event.getAction() == KeyEvent.ACTION_UP)
		{
			switch(event.getKeyCode())
			{
				case KeyEvent.KEYCODE_DEL:
					upOneLevel();
					return true;

				case KeyEvent.KEYCODE_BACK:
					if(mBackKeyDown)
					{
						mBackKeyDown = false;
					}
					else
					{
						mBackKeyDown = false;
					}
					break;

				case KeyEvent.KEYCODE_DPAD_RIGHT:
					KeyEvent e = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_CENTER);
					return super.dispatchKeyEvent(e);

				default:
					mBackKeyDown = false;
					break;
			}
		}
		return super.dispatchKeyEvent(event);
	}


}
