package com.Guaidaodl.Client;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * 提示信息
 */
public class ShowMessage
{
	public static void displayMessage(Context context,String message)
	{
		final int xOffset = 0;
		final int yOffset = 100;

		Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.TOP, xOffset, yOffset);
		toast.show();
	}
}
