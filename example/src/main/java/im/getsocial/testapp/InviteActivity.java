/*
 * Published under the MIT License (MIT)
 * Copyright: (c) 2015 GetSocial B.V.
 */

package im.getsocial.testapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import java.util.HashMap;

import im.getsocial.sdk.core.GetSocial;

public class InviteActivity extends AppCompatActivity implements View.OnLongClickListener
{
	public static final String EXTRA_SUBJECT = "extra_subject";
	public static final String EXTRA_TEXT = "extra_text";
	public static final String EXTRA_BUNDLE = "extra_bundle";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_invite);

		EditText editText = (EditText) findViewById(R.id.subject);
		editText.setOnLongClickListener(this);

		editText = (EditText) findViewById(R.id.text);
		editText.setOnLongClickListener(this);
	}

	public void onButtonClick(View view)
	{
		String text = ((EditText) findViewById(R.id.text)).getText().toString();

		if(text.isEmpty() || text.contains(GetSocial.PLACEHOLDER_APP_INVITE_URL))
		{
			setResultAndFinish();
		}
		else
		{
			new AlertDialog.Builder(this)
					.setTitle(android.R.string.dialog_alert_title)
					.setMessage("No placeholder for URL found in text, would you like to continue anyway?\nWithout placeholder the invite URL will not be visible.")
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
							{
								@Override
								public void onClick(DialogInterface dialogInterface, int which)
								{
									setResultAndFinish();
								}
							}
					)
					.setNegativeButton(android.R.string.no, null)
					.show();
		}
	}

	private void setResultAndFinish()
	{
		Intent intent = new Intent();

		EditText editText = (EditText) findViewById(R.id.subject);
		intent.putExtra(EXTRA_SUBJECT, editText.getText().toString());

		editText = (EditText) findViewById(R.id.text);
		intent.putExtra(EXTRA_TEXT, editText.getText().toString());

		intent.putExtra(EXTRA_BUNDLE, buildBundle());

		setResult(RESULT_OK, intent);

		finish();
	}

	private HashMap<String, String> buildBundle()
	{
		int[] keys = new int[] {R.id.key1, R.id.key2, R.id.key3};
		int[] values = new int[] {R.id.value1, R.id.value2, R.id.value3};

		HashMap<String, String> referralData = new HashMap<String, String>();

		for(int i = 0; i < keys.length; i++)
		{
			EditText editText1 = (EditText) findViewById(keys[i]);
			EditText editText2 = (EditText) findViewById(values[i]);

			if(!TextUtils.isEmpty(editText1.getText()) && !TextUtils.isEmpty(editText2.getText()))
			{
				referralData.put(editText1.getText().toString(), editText2.getText().toString());
			}
		}

		return referralData;
	}

	@Override
	public boolean onLongClick(final View view)
	{
		final String[] options = {GetSocial.PLACEHOLDER_APP_INVITE_URL,
				GetSocial.PLACEHOLDER_APP_NAME,
				GetSocial.PLACEHOLDER_USER_DISPLAY_NAME,
				GetSocial.PLACEHOLDER_APP_ICON_URL,
				GetSocial.PLACEHOLDER_APP_PACKAGE_NAME};

		AlertDialog.Builder builder = new AlertDialog.Builder(InviteActivity.this);
		builder.setCancelable(true);
		builder.setItems(options, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						EditText editText = (EditText) view;
						editText.getText().insert(editText.getSelectionStart(), options[which]);
					}
				}
		);

		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();

		return true;
	}
}
