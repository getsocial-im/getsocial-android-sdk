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

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import im.getsocial.sdk.core.GetSocial;
import im.getsocial.sdk.core.GetSocialBuildConfig;
import im.getsocial.testapp.ui.UiConsole;

public class ConsoleActivity extends AppCompatActivity
{
	private ShareActionProvider shareActionProvider;
	private TextView consoleTextView;
	private ScrollView scrollView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_console);
		
		consoleTextView = (TextView) findViewById(R.id.console_textView);
		scrollView = (ScrollView) findViewById(R.id.console_scrollView);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		Drawable clearIcon = getResources().getDrawable(R.drawable.ic_clear);
		Drawable wrappedClearIcon = DrawableCompat.wrap(clearIcon);
		DrawableCompat.setTint(wrappedClearIcon, getResources().getColor(R.color.icons));
		getSupportActionBar().setHomeAsUpIndicator(wrappedClearIcon);
		
		populateConsoleContent();
		
		scrollToBottom();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_console, menu);
		
		MenuItem item = menu.findItem(R.id.action_shareConsole);
		shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
		shareActionProvider.setShareIntent(getDefaultShareIntent());
		
		return true;
	}
	
	private Intent getDefaultShareIntent()
	{
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		
		String subject = String.format("[%s] GetSocial Android Test App Logs", DateFormat.getDateTimeInstance().format(new Date()));
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		
		intent.putExtra(Intent.EXTRA_TEXT, getShareText());
		
		return intent;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case android.R.id.home:
				finish();
				return true;
			
			case R.id.action_cleanConsole:
				UiConsole.clear();
				populateConsoleContent();
				return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@NonNull
	private String getShareText()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("LOGS:\n");
		sb.append(Html.fromHtml(UiConsole.getFormattedOutput()));
		
		return sb.toString();
	}
	
	private void populateConsoleContent()
	{
		consoleTextView.setText(Html.fromHtml(UiConsole.getFormattedOutput()));
	}
	
	private void scrollToBottom()
	{
		scrollView.post(
				new Runnable()
				{
					public void run()
					{
						scrollView.smoothScrollTo(0, consoleTextView.getBottom());
					}
				}
		);
	}
}
