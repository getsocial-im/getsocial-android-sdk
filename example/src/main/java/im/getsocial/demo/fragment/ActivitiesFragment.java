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
import android.widget.Toast;

import im.getsocial.demo.Utils;
import im.getsocial.demo.adapter.MenuItem;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.ErrorCode;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.actions.Action;
import im.getsocial.sdk.actions.ActionListener;
import im.getsocial.sdk.common.PagingQuery;
import im.getsocial.sdk.communities.ActivitiesQuery;
import im.getsocial.sdk.communities.GetSocialActivity;
import im.getsocial.sdk.communities.PollStatus;
import im.getsocial.sdk.communities.UserId;
import im.getsocial.sdk.media.MediaAttachment;
import im.getsocial.sdk.ui.CustomErrorMessageProvider;
import im.getsocial.sdk.ui.UiAction;
import im.getsocial.sdk.ui.ViewStateListener;
import im.getsocial.sdk.ui.communities.ActivityDetailsViewBuilder;
import im.getsocial.sdk.ui.communities.ActivityFeedViewBuilder;

import java.util.Arrays;
import java.util.List;

public class ActivitiesFragment extends BaseListFragment implements ActionListener {

	private static final List<UiAction> FORBIDDEN_FOR_ANONYMOUS = Arrays.asList(UiAction.LIKE_ACTIVITY, UiAction.LIKE_COMMENT, UiAction.POST_ACTIVITY, UiAction.POST_COMMENT);

	@Override
	protected List<MenuItem> createListData() {
		return Arrays.asList(
						new MenuItem.Builder("Timeline with Handlers")
										.withAction(new OpenTimelineWithHandlers())
										.build(),
						new MenuItem.Builder("My Feed")
										.withAction(new OpenMyFeed())
										.build(),
						new MenuItem.Builder("My Feed without UI")
								.withAction(new OpenMyFeedWOUI())
								.build(),
						new MenuItem.Builder("Demo Feed")
										.withAction(new OpenTopicFeedAction("DemoFeed"))
										.build(),
						new MenuItem.Builder("Timeline")
										.withAction(new OpenTimeline())
										.build(),
						new MenuItem.Builder("Timeline without UI")
								.withAction(new OpenTimelineWOUI())
								.build(),
						new MenuItem.Builder("Open Activity Details From Timeline")
										.withAction(new OpenActivityDetailsAction(true))
										.build(),
						new MenuItem.Builder("Open Activity Details From Timeline(without feed view)")
										.withAction(new OpenActivityDetailsAction(false))
										.build(),
						MenuItem.builder("Update Last Activity")
										.withAction(new UpdateActivityAction())
										.build(),
						MenuItem.builder("All my posts everywhere")
										.withAction(new MenuItem.Action() {
											@Override
											public void execute() {
												ActivityFeedViewBuilder.create(ActivitiesQuery.everywhere().byUser(UserId.currentUser()))
																.show();
											}
										})
										.build(),
						MenuItem.builder("All my posts in all topics")
								.withAction(new MenuItem.Action() {
									@Override
									public void execute() {
										ActivityFeedViewBuilder.create(ActivitiesQuery.inAllTopics().byUser(UserId.currentUser()))
												.show();
									}
								})
								.build(),
						MenuItem.builder("All my posts in all groups")
								.withAction(new MenuItem.Action() {
									@Override
									public void execute() {
										ActivityFeedViewBuilder.create(ActivitiesQuery.inAllGroups().byUser(UserId.currentUser()))
												.show();
									}
								})
								.build(),
						MenuItem.builder("My Feed")
										.withAction(new MenuItem.Action() {
											@Override
											public void execute() {
												ActivityFeedViewBuilder.create(ActivitiesQuery.feedOf(UserId.currentUser()))
																.show();
											}
										})
										.build(),
						navigationListItem("Post to timeline", PostActivityFragment.class),
						MenuItem.builder("Create Poll in User's feed")
								.withAction(new MenuItem.Action() {
									@Override
									public void execute() {
										addContentFragment(CreatePollFragment.postToTimeline());
									}
								})
								.build(),
						navigationListItem("Reactions", FeedFragment.class),
						MenuItem.builder("Topic Activity Details (569141942483009835)")
							.withAction(new MenuItem.Action() {
								@Override
								public void execute() {
									ActivityDetailsViewBuilder.create("569141942483009835")
										.show();
							}
						})
						.build(),
						MenuItem.builder("Group Activity Details (504205474833540031)")
								.withAction(new MenuItem.Action() {
								@Override
								public void execute() {
									ActivityDetailsViewBuilder.create("504205474833540031")
											.show();
								}
						})
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

	private void openGlobalFeedForTag(final String tag) {
		ActivityFeedViewBuilder.create(ActivitiesQuery.everywhere().withTag(tag))
						.setWindowTitle(String.format("Search #%s", tag))
						.show();
	}


	private void showCurrentUserFeed() {
		ActivityFeedViewBuilder builder = ActivityFeedViewBuilder.create(ActivitiesQuery.feedOf(UserId.currentUser()))
						.setWindowTitle("My Feed")
						.setActionListener(actionListener());
		if (Utils.isCustomErrorMessageEnabled(getContext())) {
			builder.setCustomErrorMessageProvider(new CustomErrorMessageProvider() {
				@Override
				public String onError(int errorCode, String errorMessage) {
					if (errorCode == ErrorCode.ACTIVITY_REJECTED) {
						return "Be careful what you say :)";
					}
					return errorMessage;
				}
			});
		}
		builder.show();
	}

	@Override
	public void handleAction(final Action action) {
		_activityListener.dependencies().actionListener().handleAction(action);
	}

	private <T extends ActionListener> T actionListener() {
		return (T) this;
	}

	private class OpenTimelineWithHandlers implements MenuItem.Action {

		@Override
		public void execute() {
			ActivityFeedViewBuilder builder = ActivityFeedViewBuilder.create(ActivitiesQuery.timeline())
							.setActionListener(actionListener())
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
							.setUiActionListener((action, pendingAction) -> {
								final String actionDescription = action.name().replace("_", " ").toLowerCase();
								_log.logInfoAndToast("User is going to " + actionDescription);
								if (GetSocial.getCurrentUser().isAnonymous() && FORBIDDEN_FOR_ANONYMOUS.contains(action)) {
									showAuthorizeUserDialogForPendingAction(actionDescription, pendingAction);
								} else {
									pendingAction.proceed();
								}
							})
							.setAvatarClickListener(ActivitiesFragment.this::showUserActionDialog)
							.setMentionClickListener(mention -> getUserAndShowActionDialog(mention))
							.setTagClickListener(ActivitiesFragment.this::openGlobalFeedForTag);
			if (Utils.isCustomErrorMessageEnabled(getContext())) {
				builder.setCustomErrorMessageProvider(new CustomErrorMessageProvider() {
					@Override
					public String onError(int errorCode, String errorMessage) {
						if (errorCode == ErrorCode.ACTIVITY_REJECTED) {
							return "Be careful what you say :)";
						}
						return errorMessage;
					}
				});
			}
			builder.show();
		}
	}

	private class OpenTopicFeedAction implements MenuItem.Action {

		private final String _feed;

		OpenTopicFeedAction(final String feed) {
			_feed = feed;
		}

		@Override
		public void execute() {
			ActivityFeedViewBuilder builder =  ActivityFeedViewBuilder.create(ActivitiesQuery.activitiesInTopic(_feed))
							.setActionListener(actionListener())
							.setAvatarClickListener(ActivitiesFragment.this::showUserActionDialog);

							if (Utils.isCustomErrorMessageEnabled(getContext())) {
								builder.setCustomErrorMessageProvider(new CustomErrorMessageProvider() {
									@Override
									public String onError(int errorCode, String errorMessage) {
										if (errorCode == ErrorCode.ACTIVITY_REJECTED) {
											return "Be careful what you say :)";
										}
										return errorMessage;
									}
								});
							}
							builder.show();
		}
	}

	private class OpenMyFeed implements MenuItem.Action {

		@Override
		public void execute() {
			showCurrentUserFeed();
		}
	}

	private class OpenMyFeedWOUI implements MenuItem.Action {

		@Override
		public void execute() {
			addContentFragment(ActivitiesListFragment.currentUserFeed());
		}
	}

	private class OpenTimelineWOUI implements MenuItem.Action {

		@Override
		public void execute() {
			addContentFragment(ActivitiesListFragment.timeline());
		}
	}

	private class OpenTimeline implements MenuItem.Action {

		@Override
		public void execute() {
			ActivityFeedViewBuilder builder = ActivityFeedViewBuilder.create(ActivitiesQuery.timeline())
							.setActionListener(actionListener());
			if (Utils.isCustomErrorMessageEnabled(getContext())) {
				builder.setCustomErrorMessageProvider(new CustomErrorMessageProvider() {
					@Override
					public String onError(int errorCode, String errorMessage) {
						if (errorCode == ErrorCode.ACTIVITY_REJECTED) {
							return "Be careful what you say :)";
						}
						return errorMessage;
					}
				});
			}
			builder.show();
		}
	}

	private class OpenActivityDetailsAction implements MenuItem.Action {

		private final boolean _showFeed;

		OpenActivityDetailsAction(final boolean showFeed) {
			_showFeed = showFeed;
		}

		@Override
		public void execute() {
			Communities.getActivities(new PagingQuery<>(ActivitiesQuery.timeline()).withLimit(5), result -> {
				final List<GetSocialActivity> getSocialActivities = result.getEntries();
				if (getSocialActivities.isEmpty()) {
					Toast.makeText(getContext(), "No activities in timeline", Toast.LENGTH_SHORT).show();
					return;
				}
				final String[] activityContents = new String[getSocialActivities.size()];
				for (int i = 0; i < activityContents.length; i++) {
					final GetSocialActivity activity = getSocialActivities.get(i);
					if (activity.getText() != null) {
						activityContents[i] = activity.getText();
					} else if (activity.getAttachments().size() > 0) {
						final MediaAttachment attachment = activity.getAttachments().get(0);
						if (attachment.getImageUrl() != null) {
							activityContents[i] = "[IMAGE] " + attachment.getImageUrl();
						} else {
							activityContents[i] = "[VIDEO] " + attachment.getVideoUrl();
						}
					} else {
						activityContents[i] = activity.toString();
					}
				}
				new AlertDialog.Builder(getContext())
								.setItems(activityContents, (dialog, which) -> {
									dialog.dismiss();
									String activityId = getSocialActivities.get(which).getId();
									ActivityDetailsViewBuilder.create(activityId)
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
													.setActionListener(actionListener())
													.setWindowTitle("Activity Details")
													.setUiActionListener((action, pendingAction) -> {
														Toast.makeText(getContext(), "Action done: " + action.name(), Toast.LENGTH_SHORT).show();
														pendingAction.proceed();
													})
													.setShowActivityFeedView(_showFeed)
													.show();
								}).show();
			}, exception -> _log.logErrorAndToast("Failed to load activities, error: " + exception.getMessage()));
		}
	}

	private class UpdateActivityAction implements MenuItem.Action {

		@Override
		public void execute() {
			Communities.getActivities(new PagingQuery<>(ActivitiesQuery.everywhere().byUser(UserId.currentUser()).withPollStatus(PollStatus.WITHOUT_POLL)).withLimit(5), result -> {
				final List<GetSocialActivity> getSocialActivities = result.getEntries();
				if (getSocialActivities.isEmpty()) {
					Toast.makeText(getContext(), "You haven't recently posted anywhere.", Toast.LENGTH_SHORT).show();
					return;
				}
				final String[] activityContents = new String[getSocialActivities.size()];
				for (int i = 0; i < activityContents.length; i++) {
					final GetSocialActivity activity = getSocialActivities.get(i);
					if (activity.getText() != null) {
						activityContents[i] = activity.getText();
					} else if (activity.getAttachments().size() > 0) {
						final MediaAttachment attachment = activity.getAttachments().get(0);
						if (attachment.getImageUrl() != null) {
							activityContents[i] = "[IMAGE] " + attachment.getImageUrl();
						} else {
							activityContents[i] = "[VIDEO] " + attachment.getVideoUrl();
						}
					} else {
						activityContents[i] = activity.toString();
					}
				}
				new AlertDialog.Builder(getContext())
								.setItems(activityContents, (dialog, which) -> {
									dialog.dismiss();
									String activityId = getSocialActivities.get(which).getId();

									addContentFragment(PostActivityFragment.updateActivity(activityId));
								}).show();
			}, exception -> _log.logErrorAndToast("Failed to load activities, error: " + exception.getMessage()));
		}
	}
}
