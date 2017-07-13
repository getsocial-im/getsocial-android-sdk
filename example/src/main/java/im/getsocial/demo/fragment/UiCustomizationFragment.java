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

import android.content.pm.ActivityInfo;
import android.text.TextUtils;
import im.getsocial.demo.adapter.MenuItem;
import im.getsocial.sdk.ui.GetSocialUi;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class UiCustomizationFragment extends BaseListFragment {

	public static final String UI_CONFIGURATION_NAME_KEY = "config_name";

	@Override
	protected List<MenuItem> createListData() {
		return Arrays.asList(
				new MenuItem.Builder("Default UI")
						.withAction(new MenuItem.Action() {
							@Override
							public void execute() {
								loadDefaultConfiguration();
							}
						}).build(),
				new MenuItem.Builder("Default UI Landscape")
						.withAction(new MenuItem.Action() {
							@Override
							public void execute() {
								GetSocialUi.loadConfiguration(getContext(), "getsocial/ui-landscape.json");
								updateUiConfigurationName("Default Landscape");
							}
						}).build()
		);
	}

	private void loadDefaultConfiguration() {
		if (GetSocialUi.loadDefaultConfiguration(getContext())) {
			getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			updateUiConfigurationName(null);
		}
	}

	protected void updateUiConfigurationName(@Nullable String name) {
		_activityListener.putSessionValue(UI_CONFIGURATION_NAME_KEY, name);
		_activityListener.invalidateUi();
	}

	@Override
	public String getTitle() {
		String savedName = _activityListener.getSessionValue(UI_CONFIGURATION_NAME_KEY);
		if (TextUtils.isEmpty(savedName)) {
			return "UI Config";
		} else {
			return "UI Config(" + savedName + ")";
		}
	}

	@Override
	public String getFragmentTag() {
		return "uiconfig";
	}
}
