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

package im.getsocial.demo.adapter;

public class MenuItem {

	private final String _title;
	private String _subtitle;
	private Action _action;
	private EnabledCheck _enabledCheck;
	private TextGenerator _subtitleTextGenerator;

	private MenuItem(String title) {
		_title = title;
		_subtitle = null;
		_action = null;
		_enabledCheck = null;
		_subtitleTextGenerator = null;
	}

	private MenuItem(String title, String subtitle, TextGenerator subtitleTextGenerator, Action action, EnabledCheck enabledCheck) {
		_title = title;
		_subtitle = subtitle;
		_subtitleTextGenerator = subtitleTextGenerator;
		_action = action;
		_enabledCheck = enabledCheck;
	}

	public String getTitle() {
		return _title;
	}

	public String getSubtitle() {
		if (_subtitle != null) {
			return _subtitle;
		}
		if (_subtitleTextGenerator != null) {
			return _subtitleTextGenerator.generateText();
		}
		return null;
	}

	public boolean hasSubtitle() {
		return _subtitle != null || _subtitleTextGenerator != null;
	}

	public Action getAction() {
		return _action;
	}

	public EnabledCheck getEnabledCheck() {
		return _enabledCheck;
	}

	public interface Action {
		void execute();
	}

	public static class Builder {
		private final MenuItem _data;

		public Builder(String title) {
			_data = new MenuItem(title);
		}

		public Builder withSubtitle(String subtitle) {
			_data._subtitle = subtitle;
			return this;
		}

		public Builder withSubtitle(TextGenerator textGenerator) {
			_data._subtitleTextGenerator = textGenerator;
			return this;
		}

		public Builder withAction(Action action) {
			_data._action = action;
			return this;
		}

		public Builder withEnabledCheck(EnabledCheck enabledCheck) {
			_data._enabledCheck = enabledCheck;
			return this;
		}

		public MenuItem build() {
			return new MenuItem(_data._title, _data._subtitle, _data._subtitleTextGenerator, _data._action, _data._enabledCheck);
		}
	}
}
