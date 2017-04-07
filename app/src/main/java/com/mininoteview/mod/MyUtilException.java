package com.mininoteview.mod;

class MyUtilException extends Exception
{
	private static final long serialVersionUID = 1L;
	private int code;

	MyUtilException(int code, String message)
	{
		super(message);
		this.code = code;
	}

	int getCode()
	{
		return code;
	}
}
