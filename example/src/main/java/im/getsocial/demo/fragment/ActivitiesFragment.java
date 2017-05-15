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
import android.widget.Toast;
import im.getsocial.demo.adapter.MenuItem;
import im.getsocial.sdk.CompletionCallback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialException;
import im.getsocial.sdk.activities.ActivityPost;
import im.getsocial.sdk.ui.UiActionListener;
import im.getsocial.sdk.ui.GetSocialUi;
import im.getsocial.sdk.ui.UiAction;
import im.getsocial.sdk.ui.ViewStateListener;
import im.getsocial.sdk.ui.activities.ActionButtonListener;

import java.util.Arrays;
import java.util.List;

public class ActivitiesFragment extends BaseListFragment {

	public static final String CUSTOM_FEED_NAME = "DemoFeed";
	private static final List<UiAction> FORBIDDEN_FOR_ANONYMOUS = Arrays.asList(UiAction.LIKE_ACTIVITY, UiAction.LIKE_COMMENT, UiAction.POST_ACTIVITY, UiAction.POST_COMMENT);

	@Override
	protected List<MenuItem> createListData() {
		return Arrays.asList(
				new MenuItem.Builder("Global Activity Feed")
						.withAction(new OpenGlobalFeedAction())
						.build(),
				new MenuItem.Builder(String.format("Custom Activity Feed (%s)", CUSTOM_FEED_NAME))
						.withAction(new OpenCustomFeedAction(CUSTOM_FEED_NAME))
						.build(),
				new MenuItem.Builder("Post Activity")
						.withAction(new OpenPostFeedFormAction())
						.build()
		);
	}

	@Override
	public String getFragmentTag() {
		return "activities";
	}

	@Override
	public String getTitle() {
		return "Activities";
	}

	private class OpenGlobalFeedAction implements MenuItem.Action {

		@Override
		public void execute() {
			GetSocialUi.createGlobalActivityFeedView()
					.setButtonActionListener(new ActionButtonListener() {
						@Override
						public void onButtonClicked(String action, ActivityPost post) {
							Toast.makeText(getContext(), "Activity Feed button clicked, action: " + action, Toast.LENGTH_SHORT).show();
						}
					})
					.setViewStateListener(new ViewStateListener() {
						@Override
						public void onOpen() {
							_log.logInfoAndToast("Global feed was opened");
						}

						@Override
						public void onClose() {
							_log.logInfoAndToast("Global feed was closed");
						}
					})
					.setUiActionListener(new UiActionListener() {
						@Override
						public void onUiAction(UiAction action, UiAction.Pending pendingAction) {
							final String actionDescription = action.name().replace("_", " ").toLowerCase();
							_log.logInfoAndToast("User is going to " + actionDescription);
							if (GetSocial.User.isAnonymous() && FORBIDDEN_FOR_ANONYMOUS.contains(action)) {
								showAuthorizeUserDialogForPendingAction(actionDescription, pendingAction);
							} else {
								pendingAction.proceed();
							}
						}
					})
					.show();
		}
	}

	private void showAuthorizeUserDialogForPendingAction(final String actionDescription, final UiAction.Pending pendingAction) {
		final CompletionCallback completionCallback = new CompletionCallback() {
			@Override
			public void onSuccess() {
				pendingAction.proceed();
			}

			@Override
			public void onFailure(GetSocialException exception) {
				_log.logErrorAndToast("You can not " + actionDescription + " because of exception during authorization: " + exception.getMessage());
			}
		};
		new AlertDialog.Builder(getContext()).setTitle("Authorize to " + actionDescription)
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						_log.logInfoAndToast("Can not " + actionDescription + " without authorization.");
					}
				})
				.setItems(new CharSequence[]{"Facebook", "Custom"}, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case 0:
								addFacebookUserIdentity(completionCallback);
								break;
							case 1:
								addCustomUserIdentity(completionCallback);
								break;
							default:
								break;
						}
						dialog.dismiss();
					}
				})
				.show();
	}

	private class OpenCustomFeedAction implements MenuItem.Action {

		private final String _feed;

		OpenCustomFeedAction(String feed) {
			_feed = feed;
		}

		@Override
		public void execute() {
			GetSocialUi.createActivityFeedView(_feed)
					.setButtonActionListener(new ActionButtonListener() {
						@Override
						public void onButtonClicked(String action, ActivityPost post) {
							Toast.makeText(getContext(), "Activity Feed button clicked, action: " + action, Toast.LENGTH_SHORT).show();
						}
					})
					.show();
		}
	}

	private class OpenPostFeedFormAction implements MenuItem.Action {

		@Override
		public void execute() {
			addContentFragment(new PostActivityFragment());
		}
	}
}
