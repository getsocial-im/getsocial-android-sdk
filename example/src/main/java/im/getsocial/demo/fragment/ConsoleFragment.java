/*
*    	Copyright 2015-2017 GetSocial B.V.
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

package im.getsocial.demo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.getsocial.demo.R;
import im.getsocial.demo.utils.CompatibilityUtils;
import im.getsocial.demo.utils.Console;

import java.text.DateFormat;
import java.util.Date;

public class ConsoleFragment extends BaseFragment {

	private ViewContainer _viewContainer;

	public ConsoleFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_console, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		_viewContainer = new ViewContainer(view);

		setHasOptionsMenu(true);

		populateConsoleContent();

		scrollToBottom();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.menu_console, menu);
		MenuItem item = menu.findItem(R.id.action_shareConsole);
		ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
		shareActionProvider.setShareIntent(getDefaultShareIntent());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_cleanConsole:
				Console.clear();
				populateConsoleContent();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public String getTitle() {
		return "Console";
	}

	private Intent getDefaultShareIntent() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain")
				.putExtra(Intent.EXTRA_SUBJECT, getShareSubject())
				.putExtra(Intent.EXTRA_TEXT, getShareText());

		return intent;
	}

	private String getShareSubject() {
		return String.format("[%s] GetSocial Android Demo Logs", DateFormat.getDateTimeInstance().format(new Date()));
	}

	private String getShareText() {
		return "LOGS:\n" + CompatibilityUtils.fromHtml(Console.getFormattedOutput());
	}

	private void populateConsoleContent() {
		_viewContainer._consoleTextView.setText(CompatibilityUtils.fromHtml(Console.getFormattedOutput()));
	}

	private void scrollToBottom() {
		_viewContainer._scrollView.post(
				new Runnable() {
					public void run() {
						_viewContainer._scrollView.smoothScrollTo(0, _viewContainer._consoleTextView.getBottom());
					}
				}
		);
	}

	@Override
	public String getFragmentTag() {
		return "console";
	}

	static class ViewContainer {

		@BindView(R.id.console_textView)
		TextView _consoleTextView;
		@BindView(R.id.console_scrollView)
		ScrollView _scrollView;

		ViewContainer(View view) {
			ButterKnife.bind(this, view);
		}
	}
}
