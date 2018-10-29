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
import im.getsocial.demo.dialog.action_dialog.ActionDialog;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialException;
import im.getsocial.sdk.activities.ActivitiesQuery;
import im.getsocial.sdk.activities.ActivityPost;
import im.getsocial.sdk.ui.AvatarClickListener;
import im.getsocial.sdk.ui.GetSocialUi;
import im.getsocial.sdk.ui.MentionClickListener;
import im.getsocial.sdk.ui.TagClickListener;
import im.getsocial.sdk.ui.UiAction;
import im.getsocial.sdk.ui.UiActionListener;
import im.getsocial.sdk.ui.ViewStateListener;
import im.getsocial.sdk.ui.activities.ActionButtonListener;
import im.getsocial.sdk.usermanagement.PublicUser;

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
				new MenuItem.Builder("My Global Activity Feed")
						.withAction(new OpenMyGlobalFeedAction())
						.build(),
				new MenuItem.Builder("My Friends Global Feed")
						.withAction(new OpenMyFriendsGlobalFeedAction())
						.build(),
				new MenuItem.Builder("My Custom Activity Feed")
						.withAction(new OpenMyCustomFeedAction(CUSTOM_FEED_NAME))
						.build(),
				navigationListItem("Post Activity", PostActivityFragment.class),
				new MenuItem.Builder("Open Activity Details From Global Feed")
						.withAction(new OpenActivityDetailsAction(true))
						.build(),
				new MenuItem.Builder("Open Activity Details From Global Feed(without feed view)")
						.withAction(new OpenActivityDetailsAction(false))
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

	private void openGlobalFeedForTag(String tag) {
		GetSocialUi.createGlobalActivityFeedView()
				.setWindowTitle(String.format("Search #%s", tag))
				.setFilterByTags(tag)
				.setReadOnly(true)
				.show();
	}

	private void getUserAndShowActionDialog(String mention) {
		if (mention.equals(MentionClickListener.APP_SHORTCUT)) {
			Toast.makeText(getContext(), "Application mention clicked.", Toast.LENGTH_SHORT).show();
			return;
		}
		GetSocial.getUserById(mention, new Callback<PublicUser>() {
			@Override
			public void onSuccess(PublicUser user) {
				showUserActionDialog(user);
			}

			@Override
			public void onFailure(GetSocialException exception) {
				_log.logErrorAndToast("Failed to get user: " + exception.getMessage());
			}
		});
	}

	private void showUserActionDialog(final PublicUser user) {
		ActionDialog actionDialog = new ActionDialog(getContext());

		if (isCurrentUser(user)) {
			actionDialog.addAction(new ActionDialog.Action("Show User Feed") {
				@Override
				public void execute() {
					showGlobalFeedForCurrentUser();
				}
			});
			actionDialog.show();
		} else {
			actionDialog.setTitle("User " + user.getDisplayName());
			actionDialog.addAction(new ActionDialog.Action("Show User Feed") {
				@Override
				public void execute() {
					showGlobalFeedForOtherUser(user);
				}
			});
			checkIfFriendsAndShowDialog(user, actionDialog);
		}
	}

	private void showGlobalFeedForCurrentUser() {
		showGlobalFeedForUser(GetSocial.User.getId(), "My Global Feed");
	}

	private void showGlobalFeedForOtherUser(PublicUser user) {
		showGlobalFeedForUser(user.getId(), user.getDisplayName() + " Global Feed");
	}

	private void showGlobalFeedForUser(String id, String title) {
		GetSocialUi.createGlobalActivityFeedView()
				.setFilterByUser(id)
				.setWindowTitle(title)
				.setReadOnly(true)
				.setButtonActionListener(new ActionButtonListener() {
					@Override
					public void onButtonClicked(String action, ActivityPost post) {
						Toast.makeText(getContext(), "Activity Feed button clicked, action: " + action, Toast.LENGTH_SHORT).show();
					}
				})
				.show();
	}

	private boolean isCurrentUser(PublicUser user) {
		return user.getId().equals(GetSocial.User.getId());
	}

	private void checkIfFriendsAndShowDialog(final PublicUser user, final ActionDialog actionDialog) {
		GetSocial.User.isFriend(user.getId(), new Callback<Boolean>() {
			@Override
			public void onSuccess(Boolean isFriend) {
				if (isFriend) {
					actionDialog.addAction(new ActionDialog.Action("Remove from Friends") {
						@Override
						public void execute() {
							removeFriend(user);
						}
					});
				} else {
					actionDialog.addAction(new ActionDialog.Action("Add to Friends") {
						@Override
						public void execute() {
							addFriend(user);
						}
					});
				}
				actionDialog.show();
			}

			@Override
			public void onFailure(GetSocialException exception) {
				_log.logErrorAndToast("Failed to check if friend: " + exception.getMessage());
			}
		});
	}

	private void addFriend(final PublicUser user) {
		GetSocial.User.addFriend(user.getId(), new Callback<Integer>() {
			@Override
			public void onSuccess(Integer integer) {
				Toast.makeText(getContext(), user.getDisplayName() + " is now your friend!", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFailure(GetSocialException exception) {
				_log.logErrorAndToast("Failed to add friend: " + exception.getMessage());
			}
		});
	}

	private void removeFriend(final PublicUser user) {
		GetSocial.User.removeFriend(user.getId(), new Callback<Integer>() {
			@Override
			public void onSuccess(Integer integer) {
				Toast.makeText(getContext(), user.getDisplayName() + " is not your friend anymore!", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFailure(GetSocialException exception) {
				_log.logErrorAndToast("Failed to remove friend: " + exception.getMessage());
			}
		});
	}

	private class OpenGlobalFeedAction implements MenuItem.Action {

		@Override
		public void execute() {
			GetSocialUi.createGlobalActivityFeedView()
					.setButtonActionListener(new ActionButtonListener() {
						@Override
						public void onButtonClicked(String action, ActivityPost post) {
							if ("CloseAndRestore".equalsIgnoreCase(action)) {
								GetSocialUi.closeView(true);
								new AlertDialog.Builder(getContext())
										.setPositiveButton("Restore View", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialogInterface, int i) {
												GetSocialUi.restoreView();
											}
										})
										.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {

											}
										}).show();
							}
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
					.setAvatarClickListener(new AvatarClickListener() {
						@Override
						public void onAvatarClicked(PublicUser user) {
							showUserActionDialog(user);
						}
					})
					.setMentionClickListener(new MentionClickListener() {
						@Override
						public void onMentionClicked(String mention) {
							getUserAndShowActionDialog(mention);
						}
					})
					.setTagClickListener(new TagClickListener() {
						@Override
						public void onTagClicked(String tag) {
							openGlobalFeedForTag(tag);
						}
					})
					.show();
		}
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

	private class OpenMyGlobalFeedAction implements MenuItem.Action {

		@Override
		public void execute() {
			showGlobalFeedForCurrentUser();
		}
	}

	private class OpenMyFriendsGlobalFeedAction implements MenuItem.Action {

		@Override
		public void execute() {
			GetSocialUi.createGlobalActivityFeedView()
					.setButtonActionListener(new ActionButtonListener() {
						@Override
						public void onButtonClicked(String action, ActivityPost post) {
							Toast.makeText(getContext(), "Activity Feed button clicked, action: " + action, Toast.LENGTH_SHORT).show();
						}
					})
					.setShowFriendsFeed(true)
					.show();
		}
	}

	private class OpenMyCustomFeedAction implements MenuItem.Action {

		private final String _feed;

		OpenMyCustomFeedAction(String feed) {
			_feed = feed;
		}

		@Override
		public void execute() {
			GetSocialUi.createActivityFeedView(_feed)
					.setFilterByUser(GetSocial.User.getId())
					.setWindowTitle("My Custom Feed")
					.setReadOnly(false)
					.setButtonActionListener(new ActionButtonListener() {
						@Override
						public void onButtonClicked(String action, ActivityPost post) {
							Toast.makeText(getContext(), "Activity Feed button clicked, action: " + action, Toast.LENGTH_SHORT).show();
						}
					})
					.show();
		}
	}

	private class OpenActivityDetailsAction implements MenuItem.Action {

		private final boolean _showFeed;

		public OpenActivityDetailsAction(boolean showFeed) {
			_showFeed = showFeed;
		}

		@Override
		public void execute() {
			GetSocial.getActivities(ActivitiesQuery.postsForGlobalFeed().withLimit(5), new Callback<List<ActivityPost>>() {
				@Override
				public void onSuccess(final List<ActivityPost> activityPosts) {
					if (activityPosts.isEmpty()) {
						Toast.makeText(getContext(), "No activities in global feed", Toast.LENGTH_SHORT).show();
						return;
					}
					final String[] activityContents = new String[activityPosts.size()];
					for (int i = 0; i < activityContents.length; i++) {
						activityContents[i] = activityPosts.get(i).getText();
					}
					new AlertDialog.Builder(getContext())
							.setItems(activityContents, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
									String activityId = activityPosts.get(which).getId();
									GetSocialUi.createActivityDetailsView(activityId)
											.setViewStateListener(new ViewStateListener() {
												@Override
												public void onOpen() {
													Toast.makeText(getContext(), "Activity details view opened", Toast.LENGTH_SHORT).show();
												}

												@Override
												public void onClose() {
													Toast.makeText(getContext(), "Activity details view closed", Toast.LENGTH_SHORT).show();
												}
											})
											.setButtonActionListener(new ActionButtonListener() {
												@Override
												public void onButtonClicked(String action, ActivityPost post) {
													Toast.makeText(getContext(), "Activity action button pressed: " + action, Toast.LENGTH_SHORT).show();
												}
											})
											.setWindowTitle("Activity Details")
											.setUiActionListener(new UiActionListener() {
												@Override
												public void onUiAction(UiAction action, UiAction.Pending pendingAction) {
													Toast.makeText(getContext(), "Action done: " + action.name(), Toast.LENGTH_SHORT).show();
													pendingAction.proceed();
												}
											})
											.setShowActivityFeedView(_showFeed)
											.show();
								}
							}).show();
				}

				@Override
				public void onFailure(GetSocialException exception) {
					_log.logErrorAndToast("Failed to load activities, error: " + exception.getMessage());
				}
			});
		}
	}
}
