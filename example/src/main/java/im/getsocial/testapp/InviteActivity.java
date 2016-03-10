/*
 *    	Copyright 2015-2016 GetSocial B.V.
 *
 *	Licensed under the Apache License, Version 2.0 (the "License");
 *	you may not use this file except in compliance with the License.
 *	You may obtain a copy of the License at
 *
 *    	http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *	See the License for the specific language governing permissions and
 *	limitations under the License.
 */

package im.getsocial.testapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.HashMap;

import im.getsocial.sdk.core.GetSocial;

public class InviteActivity extends AppCompatActivity implements View.OnLongClickListener
{
	public static final String EXTRA_SUBJECT = "extra_subject";
	public static final String EXTRA_TEXT = "extra_text";
	public static final String EXTRA_BUNDLE = "extra_bundle";
	public static final String EXTRA_IMAGE = "extra_image";
	private static final int REQUEST_PICK_IMAGE_ACTIVITY = 1984;

	private ImageView imageView;
	private String imageUriString;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_invite);

		if(getSupportActionBar() != null)
		{
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setTitle(getString(R.string.label_title_custom_smart_invite));
		}

		EditText editText = (EditText) findViewById(R.id.subject);
		editText.setOnLongClickListener(this);

		editText = (EditText) findViewById(R.id.text);
		editText.setOnLongClickListener(this);

		imageView = (ImageView) findViewById(R.id.image);
		imageView.setOnClickListener(new View.OnClickListener()
		                             {
			                             @Override
			                             public void onClick(View v)
			                             {
				                             pickImageFromDevice();
			                             }
		                             }
		);
	}

	private void pickImageFromDevice()
	{
		Intent imagePickerIntent = new Intent(Intent.ACTION_PICK);
		imagePickerIntent.setType("image/*");
		startActivityForResult(imagePickerIntent, REQUEST_PICK_IMAGE_ACTIVITY);
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

		if(imageUriString != null && !TextUtils.isEmpty(imageUriString))
		{
			intent.putExtra(EXTRA_IMAGE, imageUriString);
		}

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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode == REQUEST_PICK_IMAGE_ACTIVITY)
		{
			if(resultCode == RESULT_OK)
			{
				handlePickedImage(data.getData());
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void handlePickedImage(Uri imageUri)
	{
		imageUriString = imageUri.toString();
		imageView.setImageURI(imageUri);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case android.R.id.home:
				setResult(RESULT_CANCELED, null);
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
