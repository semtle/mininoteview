package com.mininoteview.mod;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

class FileListAdapter extends ArrayAdapter<String>
{

	private LayoutInflater mInflater;
	private float mFontSize;
	private int mImageDim;

	FileListAdapter(Context context, List<String> objects)
	{
		super(context, 0, objects);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mFontSize = Config.getFontSizeOnList();
		final float scale = getContext().getResources().getDisplayMetrics().density;
		mImageDim = (int) (mFontSize * scale * 4.0f / 3.0f + 0.5f);

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view = convertView;

		if(convertView == null)
		{
			view = mInflater.inflate(R.layout.file_row_with_icon, parent, false);
		}

		String name = this.getItem(position);
		if(name != null)
		{
			TextView mFileName = (TextView) view.findViewById(R.id.TextView);
			mFileName.setText(name);
			mFileName.setTextSize(TypedValue.COMPLEX_UNIT_SP, mFontSize);

			ImageView mIcon = (ImageView) view.findViewById(R.id.ImageView);

			if(name.startsWith("/"))
			{
				mIcon.setImageResource(R.drawable.folder01);
			}
			else if(name.endsWith(".txt"))
			{
				mIcon.setImageResource(R.drawable.textfile01);
			}
			else if(name.endsWith(".chi"))
			{
				mIcon.setImageResource(R.drawable.tombofile01);
			}
			else if(name.endsWith(".."))
			{
				mIcon.setImageResource(R.drawable.updir01);
			}
			else
			{
				mIcon.setImageResource(R.drawable.otherfile02);
			}

			mIcon.setLayoutParams(new LinearLayout.LayoutParams(mImageDim, mImageDim));

		}
		return view;
	}


}
