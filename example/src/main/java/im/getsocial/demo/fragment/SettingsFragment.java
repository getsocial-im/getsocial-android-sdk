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

import android.app.AlertDialog;
import android.content.DialogInterface;
import im.getsocial.demo.R;
import im.getsocial.demo.adapter.MenuItem;
import im.getsocial.demo.adapter.TextGenerator;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.consts.LanguageCodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SettingsFragment extends BaseListFragment {

	public SettingsFragment() {
	}

	@Override
	protected List<MenuItem> createListData() {
		List<MenuItem> listData = new ArrayList<>();

		listData.add(new MenuItem.Builder("Change Language")
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						changeLanguage();
					}
				})
				.withSubtitle(new TextGenerator() {
					@Override
					public String generateText() {
						return String.format("Current language: %s", LanguageCodes.all().get(GetSocial.getLanguage()));
					}
				})
				.build());

		return listData;
	}

	//region Presenter
	private void changeLanguage() {
		final Map<String, String> all = LanguageCodes.all();

		final String[] providers = all.values().toArray(new String[all.size()]);
		final String[] codes = all.keySet().toArray(new String[all.size()]);

		new AlertDialog.Builder(getContext())
				.setTitle(R.string.select_language)
				.setNegativeButton(android.R.string.cancel, null)
				.setItems(providers,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								GetSocial.setLanguage(codes[which]);
								invalidateList();
							}
						}
				)
				.create()
				.show();
	}

	@Override
	public String getTitle() {
		return "Settings";
	}

	@Override
	public String getFragmentTag() {
		return "settings";
	}

	//endregion
}
