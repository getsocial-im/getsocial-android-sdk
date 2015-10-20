/*
 * Published under the MIT License (MIT)
 * Copyright: (c) 2015 GetSocial B.V.
 */

package im.getsocial.testapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import im.getsocial.sdk.chat.GetSocialChat;
import im.getsocial.sdk.core.GetSocial;
import im.getsocial.sdk.core.IdentityInfo;
import im.getsocial.sdk.core.UI.builder.SmartInviteViewBuilder;
import im.getsocial.sdk.core.UI.builder.UserListViewBuilder;
import im.getsocial.sdk.core.UserIdentity;
import im.getsocial.sdk.core.resources.Leaderboard;
import im.getsocial.sdk.core.resources.LeaderboardScore;
import im.getsocial.sdk.core.util.Log;
import im.getsocial.testapp.ui.ActionableListViewMenu;
import im.getsocial.testapp.ui.CheckboxListViewMenu;
import im.getsocial.testapp.ui.ListViewMenu;
import im.getsocial.testapp.ui.ListViewMenuAdapter;
import im.getsocial.testapp.ui.ListViewMenuItemAction;
import im.getsocial.testapp.ui.ListViewMenuItemView;
import im.getsocial.testapp.ui.OnCheckboxListViewMenuChecked;
import im.getsocial.testapp.ui.UiConsole;
import im.getsocial.testapp.ui.UserInfoDialog;
import im.getsocial.testapp.ui.UserInfoView;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity
{
	/**
	 * For development purposes you can load UI configuration json
	 * hosted somewhere on the web (e.g. Dropbox).
	 * <p>
	 * Modify this constant to avoid entering URL manually each time
	 */
	private static final String DEFAULT_CUSTOM_UI_CONFIGURATION_URL = "";
	private static final List<String> FACEBOOK_PERMISSIONS = Arrays.asList("email", "user_friends");

	private static final int REQUEST_CODE_INVITE_ACTIVITY = 1983;
	private static final String ACTIVITIES_TAG = "swamp";
	private static final String ACTIVITIES_GROUP = "world1";

	private GetSocial getSocial;
	private GetSocialChat getSocialChat;
	private CallbackManager callbackManager;

	private ListViewMenuAdapter listViewMenuAdapter;
	private Toolbar toolbar;

	protected String getSocialUiConfiguration;
	protected UserInfoView userInfoView;
	protected ListViewMenu notificationsMenu;
	protected ListViewMenu uiCustomizationMenu;
	private ActionableListViewMenu languageMenu;

	private boolean isContentModerated = false;
	private boolean isUserAvatarClickHandlerCustom = false;
	private boolean isAppAvatarClickHandlerCustom = false;
	private boolean isActivityActionHandlerCustom = false;
	private boolean isInviteButtonClickHandlerCustom = false;
	private ListViewMenu chatMenu;

	// /////////////////////////////////
	// region Activity method overrides
	// /////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Fabric.with(this, new Crashlytics());

		Log.setVerbosityLevel(Log.VERBOSE);

		initFacebook();
		initGetSocial();

		initUi();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		getSocial.onResume(this);
	}

	@Override
	protected void onPause()
	{
		getSocial.onPause();
		super.onPause();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == REQUEST_CODE_INVITE_ACTIVITY)
		{
			handleInviteActivityResult(resultCode, data);
		}
		else
		{
			callbackManager.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();

		if(id == R.id.action_console)
		{
			UiConsole.showConsoleActivity(this);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed()
	{
		if(!getSocial.handleBackButtonPressed())
		{
			if(!listViewMenuAdapter.onBackPressed())
			{
				super.onBackPressed();
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);

		getSocial.closeView();
		reloadUiConfiguration();
	}
	// endregion


	// ////////////////////////////////////////
	// region Interaction with GetSocial SDK 
	// ////////////////////////////////////////

	//region GetSocial initialization
	private void initGetSocial()
	{
		UiConsole.logInfo(getTestAppInfo());

		getSocial = GetSocial.getInstance(getApplicationContext());
		getSocialChat = GetSocialChat.getInstance();

		getSocial.init(getString(R.string.getsocial_app_key), new GetSocial.OperationObserver()
				{
					@Override
					public void onSuccess(String data)
					{
						logInfoAndToast("GetSocial initialization successful");
					}

					@Override
					public void onFailure()
					{
						logErrorAndToast("GetSocial initialization failed");
					}
				}
		);

		getSocial.registerPlugin(IdentityInfo.PROVIDER_FACEBOOK, new FacebookInvitePlugin(this, callbackManager));

		getSocial.setOnLoginRequestListener(
				new GetSocial.OnLoginRequestListener()
				{
					@Override
					public void onLoginRequest()
					{
						showLoginSelectionDialog();
					}
				}
		);

		getSocial.setOnUserAvatarClickHandler(
				new GetSocial.OnUserAvatarClickHandler()
				{
					@Override
					public boolean onUserAvatarClick(UserIdentity user, int source)
					{
						if(isUserAvatarClickHandlerCustom)
						{
							logInfoAndToast(String.format("User %s avatar clicked from source %d", user.getDisplayName(), source));
							return true; // event consumed
						}
						return false; // event should be processed by the SDK
					}
				}
		);

		getSocial.setOnAppAvatarClickHandler(
				new GetSocial.OnAppAvatarClickHandler()
				{
					@Override
					public boolean onAppAvatarClick()
					{
						if(isAppAvatarClickHandlerCustom)
						{
							logInfoAndToast("Custom handler, app avatar clicked");
							return true; // event consumed
						}
						return false; // event should be processed by the SDK
					}
				}
		);

		getSocial.setOnActivityActionClickListener(
				new GetSocial.OnActivityActionClickListener()
				{
					@Override
					public void onActivityActionClick(String action)
					{
						if(isActivityActionHandlerCustom)
						{
							logInfoAndToast("Clicked on activity action: " + action);
						}
					}
				}
		);

		getSocial.setOnInviteButtonClickListener(
				new GetSocial.OnInviteButtonClickListener()
				{
					@Override
					public boolean onInviteButtonClick()
					{
						if(isInviteButtonClickHandlerCustom)
						{
							logInfoAndToast("Custom handler, invite button clicked, processing stopped");
							return true; // event consumed
						}
						return false; // event should be processed by the SDK, default behaviour is to open invite view
					}
				}
		);

		getSocial.setOnReferralDataReceivedListener(
				new GetSocial.OnReferralDataReceivedListener()
				{
					@Override
					public void onReferralDataReceived(List<Map<String, String>> referralData)
					{
						String message = "";

						for(Map<String, String> map : referralData)
						{
							for(Map.Entry<String, String> entry : map.entrySet())
							{
								String key = entry.getKey();
								String value = entry.getValue();

								message += key + ": " + value + "\n";
							}

							message += "---";
						}

						dialogOnUiThread("ReferralDataReceived", message);
					}
				}
		);

		getSocial.setUserIdentityObserver(
				new GetSocial.UserIdentityObserver()
				{
					@Override
					public void onUserIdentityUpdated(final UserIdentity userIdentity)
					{
						runOnUiThread(
								new Runnable()
								{
									@Override
									public void run()
									{
										userInfoView.setUser(userIdentity);
										if(userIdentity == null)
										{
											logInfoAndToast("Logged out");
										}
									}
								}
						);
					}
				}
		);

		getSocial.setOnUnreadNotificationsCountChangedListener(
				new GetSocial.OnUnreadNotificationsCountChangedListener()
				{
					@Override
					public void onUnreadNotificationsCountChanged(int unreadNotificationsCount)
					{
						updatedNotificationsMenuSubtitle();
					}
				}
		);

		getSocial.setOnUserGeneratedContentListener(
				new GetSocial.OnUserGeneratedContentListener()
				{
					@Override
					public String onUserGeneratedContent(int type, String content)
					{
						if(isContentModerated)
						{
							return moderateUserGeneratedContent(type, content);
						}
						return content;
					}
				}
		);

		getSocialChat.setOnUnreadConversationsCountChangedListener(
				new GetSocialChat.OnUnreadConversationsCountChangedListener()
				{
					@Override
					public void onUnreadConversationsCountChanged(int unreadConversationsCount)
					{
						updateChatMenuSubtitle();
					}
				}
		);
	}
	//endregion

	//region Working with User Authentication
	private void logInWithFacebook()
	{
		if(!getSocial.isUserLoggedIn())
		{
			final AccessTokenTracker accessTokenTracker = new AccessTokenTracker()
			{
				@Override
				protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken)
				{
					stopTracking();

					IdentityInfo fbIdentityInfo = IdentityInfo.createWithSocialNetworkInfo(IdentityInfo.PROVIDER_FACEBOOK, newAccessToken.getToken(), newAccessToken.getUserId());

					getSocial.login(
							fbIdentityInfo,
							new GetSocial.LoginObserver()
							{
								@Override
								public void onComplete()
								{
									logInfoAndToast("Successfully logged in with Facebook");
								}

								@Override
								public void onError(Exception error)
								{
									logErrorAndToast("Failed to log in with Facebook, error: " + error.getMessage());
								}
							}
					);
				}
			};
			accessTokenTracker.startTracking();

			LoginManager.getInstance().logInWithReadPermissions(this, FACEBOOK_PERMISSIONS);
		}
		else
		{
			logWarningAndToast("User already logged in");
		}
	}

	private void logInWithGenericProvider()
	{
		if(!getSocial.isUserLoggedIn())
		{
			String userId = UserIdentityUtils.getInstallationIdWithSuffix("L", this);
			String displayName = UserIdentityUtils.getDisplayName(userId);
			String avatar = UserIdentityUtils.getAvatar(userId);

			IdentityInfo identityInfo = IdentityInfo.createWithGenericInfo(IdentityInfo.PROVIDER_GENERIC, userId, displayName, avatar);
			getSocial.login(identityInfo, new GetSocial.LoginObserver()
					{
						@Override
						public void onComplete()
						{
							logInfoAndToast("Logged in successfully with 'generic' provider");
						}

						@Override
						public void onError(Exception error)
						{
							logErrorAndToast("Failed to login with 'generic' provider, error: " + error.getMessage());
						}
					}
			);
		}
		else
		{
			logWarningAndToast("User already logged in");
		}
	}

	private void addFbUserIdentity()
	{
		if(getSocial.isUserLoggedIn())
		{
			final AccessTokenTracker accessTokenTracker = new AccessTokenTracker()
			{
				@Override
				protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken)
				{
					stopTracking();

					IdentityInfo fbIdentityInfo = IdentityInfo
							.createWithSocialNetworkInfo(IdentityInfo.PROVIDER_FACEBOOK, newAccessToken.getToken(), newAccessToken.getUserId())
							.setKeyValue(IdentityInfo.FLAG_UPDATE_AVATAR, true)
							.setKeyValue(IdentityInfo.FLAG_UPDATE_DISPLAY_NAME, true);

					getSocial.addUserIdentity(
							fbIdentityInfo,
							new GetSocial.UpdateIdentityInfoObserver()
							{
								@Override
								public void onComplete()
								{
									logInfoAndToast("Successfully added Facebook user identity");
								}

								@Override
								public void onError(Exception error)
								{
									logErrorAndToast("Failed to add Facebook user identity, error: " + error.getMessage());
								}
							}
					);
				}
			};
			accessTokenTracker.startTracking();

			LoginManager.getInstance().logInWithReadPermissions(this, FACEBOOK_PERMISSIONS);
		}
		else
		{
			logWarningAndToast("User has to be logged in to add identity");
		}
	}

	private void addCustomUserIdentity()
	{
		if(getSocial.isUserLoggedIn())
		{
			String userId = UserIdentityUtils.getInstallationIdWithSuffix("A", this);
			String displayName = UserIdentityUtils.getDisplayName(userId);
			String avatar = UserIdentityUtils.getAvatar(userId);

			IdentityInfo identityInfo = IdentityInfo.createWithGenericInfo("GetSocial", userId, displayName, avatar);
			getSocial.addUserIdentity(
					identityInfo,
					new GetSocial.UpdateIdentityInfoObserver()
					{
						@Override
						public void onComplete()
						{
							logInfoAndToast("Successfully added user identity 'custom'");
						}

						@Override
						public void onError(Exception error)
						{
							logErrorAndToast("Failed to add user identity 'custom', error: " + error.getMessage());
						}
					}
			);
		}
		else
		{
			logWarningAndToast("User has to be logged in to add identity");
		}
	}

	private void removeFbUserIdentity()
	{
		final AccessTokenTracker accessTokenTracker = new AccessTokenTracker()
		{
			@Override
			protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken)
			{
				stopTracking();

				getSocial.removeUserIdentity(IdentityInfo.PROVIDER_FACEBOOK,
						new GetSocial.UpdateIdentityInfoObserver()
						{
							@Override
							public void onComplete()
							{
								logInfoAndToast("Successfully removed Facebook user identity");
							}

							@Override
							public void onError(Exception error)
							{
								logErrorAndToast("Failed to remove Facebook user identity, error: " + error.getMessage());
							}
						}
				);
			}
		};
		accessTokenTracker.startTracking();

		LoginManager.getInstance().logOut();
	}

	private void removeUserIdentity(final String providerId)
	{
		if(getSocial.isUserLoggedIn())
		{
			getSocial.removeUserIdentity(
					providerId,
					new GetSocial.UpdateIdentityInfoObserver()
					{
						@Override
						public void onComplete()
						{
							logInfoAndToast(String.format("Successfully removed user identity '%s'", providerId));
						}

						@Override
						public void onError(Exception error)
						{
							logErrorAndToast(String.format("Failed to remove user identity '%s', error: %s", providerId, error.getMessage()));
						}
					}
			);
		}
		else
		{
			logWarningAndToast("User has to be logged in to remove identity");
		}
	}

	private void logOut()
	{
		if(getSocial.isUserLoggedIn())
		{
			getSocial.logout(
					new GetSocial.LogoutObserver()
					{
						@Override
						public void onComplete()
						{
							logInfoAndToast("Successfully logged out");
						}
					}
			);
		}
		else
		{
			logWarningAndToast("User has to be logged in to logout");
		}
	}
	//endregion

	//region Working with Activities
	private void openGlobalActivities()
	{
		getSocial.createActivitiesView().show();
	}

	private void openActivitiesFilteredByGroup()
	{
		getSocial.createActivitiesView(ACTIVITIES_GROUP, ACTIVITIES_TAG).show();
	}

	private void postActivityText()
	{
		getSocial.postActivity("Text", null, null, null, null, getPostActivityOperationObserver());
	}

	private void postActivityImage()
	{
		getSocial.postActivity(null, getActivityImage(R.drawable.activity_image), null, null, null, getPostActivityOperationObserver());
	}

	private void postActivityTextAndImage()
	{
		getSocial.postActivity("Text+Image", getActivityImage(R.drawable.activity_image), null, null, null, getPostActivityOperationObserver());
	}

	private void postActivityTextAndButton()
	{
		getSocial.postActivity("Text+Button", null, "Click here", "actionId", null, getPostActivityOperationObserver());
	}

	private void postActivityTextAndImageAndButton()
	{
		getSocial.postActivity("Text+Image+Button", getActivityImage(R.drawable.activity_image_with_action), "Click here", "actionId", null, getPostActivityOperationObserver());
	}

	private void postActivityImageAndButton()
	{
		getSocial.postActivity(null, getActivityImage(R.drawable.activity_image_with_action), "Click here", "actionId", null, getPostActivityOperationObserver());
	}

	private void postActivityImageAndAction()
	{
		getSocial.postActivity(null, getActivityImage(R.drawable.activity_image_with_button), null, "actionId", null, getPostActivityOperationObserver());
	}
	//endregion

	//region Working with Smart Invites
	private void openSmartInvites()
	{
		getSocial.createSmartInviteView().show();
	}

	private void sendCustomizedSmartInvite()
	{
		Intent intent = new Intent(MainActivity.this, InviteActivity.class);
		startActivityForResult(intent, REQUEST_CODE_INVITE_ACTIVITY);
	}
	//endregion

	//region Working with Chat
	private void openGlobalChat()
	{
		getSocialChat.createChatViewForRoomName("global").show();
	}

	private void openConversationList()
	{
		getSocialChat.createChatListView().show();
	}
	//endregion

	//region Working with Notifications Center
	private void openNotificationsCenter()
	{
		getSocial.createNotificationsView().show();
	}
	//endregion

	//region Working with Friends List
	private void openFriendsList()
	{
		getSocial.createUserListView(
				new UserListViewBuilder.UserListObserver()
				{
					@Override
					public void onUserSelected(UserIdentity user)
					{
						logInfoAndToast("Selected user: " + user.getDisplayName());
					}

					@Override
					public void onCancel()
					{
						logInfoAndToast("User selection cancelled");
					}
				}
		).setTitle("Friends").show();
	}
	//endregion

	//region Working with UI Customization
	private void loadDefaultUi()
	{
		getSocialUiConfiguration = null;
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		GetSocial.getConfiguration().clear();
	}

	private void loadCustomUiFromUrl()
	{
		final EditText urlTextView = new EditText(this);
		urlTextView.setHint("http://www.getsocial.im/ui.json");
		if(!DEFAULT_CUSTOM_UI_CONFIGURATION_URL.isEmpty())
		{
			urlTextView.setText(DEFAULT_CUSTOM_UI_CONFIGURATION_URL);
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle(R.string.ui_configuration_url);
		builder.setPositiveButton(R.string.load, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialogInterface, int i)
					{
						String url = urlTextView.getText().toString();
						if(url != null && !url.isEmpty())
						{
							getSocialUiConfiguration = url;
							GetSocial.getConfiguration().setConfiguration(url);
						}
					}
				}
		);
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setView(urlTextView);
		builder.show();
	}
	//endregion

	//region Working with Leaderboards
	private void getUserRankOnLb1()
	{
		getSocial.getLeaderboard(
				"leaderboard_one",
				new GetSocial.OnOperationResultListener<Leaderboard>()
				{
					@Override
					public void onSuccess(Leaderboard leaderboard)
					{
						logInfoAndToast(formatLeaderboardData(leaderboard));
					}

					@Override
					public void onFailure(Exception exception)
					{
						logErrorAndToast(exception);
					}
				}
		);
	}

	private void getUserRankOnLb123()
	{
		getSocial.getLeaderboards(
				Arrays.asList("leaderboard_one", "leaderboard_two", "leaderboard_three"),
				new GetSocial.OnOperationResultListener<List<Leaderboard>>()
				{
					@Override
					public void onSuccess(List<Leaderboard> leaderboards)
					{
						for(Leaderboard leaderboard : leaderboards)
						{
							logInfoAndToast(formatLeaderboardData(leaderboard));
						}
					}

					@Override
					public void onFailure(Exception exception)
					{
						logErrorAndToast(exception);
					}
				}
		);
	}

	private void getFirst5Lb()
	{
		getSocial.getLeaderboards(0, 5,
				new GetSocial.OnOperationResultListener<List<Leaderboard>>()
				{
					@Override
					public void onSuccess(List<Leaderboard> leaderboards)
					{
						for(Leaderboard leaderboard : leaderboards)
						{
							logInfoAndToast(formatLeaderboardData(leaderboard));
						}
					}

					@Override
					public void onFailure(Exception exception)
					{
						logErrorAndToast(exception);
					}
				}
		);
	}

	private void getFirst5ScoresFromLb1()
	{
		getSocial.getLeaderboardScores("leaderboard_one", 0, 5, GetSocial.SCORE_TYPE_WORLD,
				new GetSocial.OnOperationResultListener<List<LeaderboardScore>>()
				{
					@Override
					public void onSuccess(List<LeaderboardScore> leaderboardScores)
					{
						for(LeaderboardScore leaderboardScore : leaderboardScores)
						{
							logInfoAndToast(
									String.format(
											"#%d %s with: %d",
											leaderboardScore.getRank(),
											leaderboardScore.getUser().getDisplayName(),
											leaderboardScore.getValue()
									)
							);
						}
					}

					@Override
					public void onFailure(Exception exception)
					{
						logErrorAndToast(exception);
					}
				}
		);
	}

	private void submitValueToLb(final String leaderboardId)
	{
		final EditText dataTextView = new EditText(this);
		dataTextView.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
		dataTextView.setText(new Random().nextInt(1000) + "");

		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle(R.string.enter_your_score);
		builder.setPositiveButton(R.string.submit, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialogInterface, int i)
					{
						String data = dataTextView.getText().toString();
						if(data != null && !data.isEmpty())
						{
							try
							{
								final int value = Integer.valueOf(data);
								getSocial.submitLeaderboardScore(leaderboardId, value,
										new GetSocial.OnOperationResultListener<Integer>()
										{
											@Override
											public void onSuccess(Integer newRank)
											{
												logInfoAndToast(String.format("Submitted score %s to LB '%s', new rank: %s", value, leaderboardId, newRank));
											}

											@Override
											public void onFailure(Exception exception)
											{
												logErrorAndToast(String.format("Failed to submit score %s to LB '%s', error: %s", value, leaderboardId, exception.getMessage()));
											}
										}
								);
							}
							catch(Exception e)
							{
								logErrorAndToast("Failed to submit score, error: " + e.getMessage());
							}
						}
					}
				}
		);
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setView(dataTextView);
		builder.show();
	}
	//endregion

	//region Working with Cloud Save
	private void openCloudSaveDialog()
	{
		final EditText dataTextView = new EditText(this);
		dataTextView.setHint("serialized data to save");

		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle(R.string.enter_data_to_save);
		builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialogInterface, int i)
					{
						String data = dataTextView.getText().toString();
						if(data != null && !data.isEmpty())
						{
							getSocial.save(data);
						}
					}
				}
		);
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setView(dataTextView);
		builder.show();
	}

	private void getClodSaveData()
	{
		getSocial.getLastSave(
				new GetSocial.OperationObserver()
				{
					@Override
					public void onSuccess(String data)
					{
						logInfoAndToast("Loaded data: " + data);
					}

					@Override
					public void onFailure()
					{
						logErrorAndToast("Failed to load saved data");
					}
				}
		);
	}
	//endregion

	//region GetSocial Settings
	private void showLanguageSelectionDialog()
	{
		final String[] providers = {"da", "de", "en", "es", "fr", "it", "nb", "nl", "pt", "ru", "sv", "tr"};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.select_language);
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setItems(providers,
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						getSocial.setLanguage(providers[which]);
						updateLanguageMenuSubtitle();
					}
				}
		);
		builder.create().show();
	}

	private void showLoginSelectionDialog()
	{
		String[] providers = {"Facebook", "Generic provider"};

		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle(R.string.login_with);
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setItems(providers, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						switch(which)
						{
							case 0:
								logInWithFacebook();
								break;
							case 1:
								logInWithGenericProvider();
								break;
						}
					}
				}
		);
		builder.create().show();
	}
	//endregion

	//endregion


	// /////////////////////////////////
	// region Helper methods 
	// /////////////////////////////////

	private void initFacebook()
	{
		FacebookSdk.sdkInitialize(getApplicationContext());
		callbackManager = CallbackManager.Factory.create();
	}

	private void initUi()
	{
		userInfoView = (UserInfoView) findViewById(R.id.toolbar_userInfo);
		userInfoView.setUser(getSocial.getLoggedInUser());
		userInfoView.setOnClickListener(
				new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						if(getSocial.isUserLoggedIn())
						{
							FragmentManager fm = getSupportFragmentManager();
							UserInfoDialog editNameDialog = new UserInfoDialog();
							editNameDialog.show(fm, "user_info_dialog");
						}
						else
						{
							showLoginSelectionDialog();
						}
					}
				}
		);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		final ListView menuListView = (ListView) findViewById(R.id.menu_listView);
		menuListView.setOnItemClickListener(
				new AdapterView.OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id)
					{
						ListViewMenu menuItem = ((ListViewMenuItemView) view).getMenuItem();
						if(menuItem instanceof ActionableListViewMenu)
						{
							((ActionableListViewMenu) menuItem).invokeAction();
						}
						else if(menuItem instanceof CheckboxListViewMenu)
						{
							// do nothing
						}
						else
						{
							listViewMenuAdapter.setMenu(menuItem);
						}
					}
				}
		);

		ListViewMenu rootMenu = populateMenu();
		listViewMenuAdapter = new ListViewMenuAdapter(getApplicationContext(), rootMenu);
		listViewMenuAdapter.registerDataSetObserver(
				new DataSetObserver()
				{
					@Override
					public void onChanged()
					{
						updateToolbarBackButton(listViewMenuAdapter.getMenu());
					}
				}
		);
		menuListView.setAdapter(listViewMenuAdapter);

		updateToolbarBackButton(rootMenu);

		TextView versionTextView = (TextView) findViewById(R.id.version_textView);
		versionTextView.setText(getTestAppInfo());
	}

	protected String getTestAppInfo()
	{
		return String.format("GetSocial Android Test App\nSDK v%s", GetSocial.VERSION);
	}

	private void updateToolbarBackButton(ListViewMenu currentMenu)
	{
		if(listViewMenuAdapter.isRootMenu())
		{
			toolbar.setNavigationIcon(null);
			userInfoView.setVisibility(View.VISIBLE);
		}
		else
		{
			userInfoView.setVisibility(View.GONE);

			toolbar.setTitle(currentMenu.getTitle());

			Drawable navigationIcon = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
			Drawable wrappedNavigationIcon = DrawableCompat.wrap(navigationIcon);
			DrawableCompat.setTint(wrappedNavigationIcon, getResources().getColor(R.color.primary_text));

			toolbar.setNavigationIcon(navigationIcon);
			toolbar.setNavigationOnClickListener(
					new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							MainActivity.this.listViewMenuAdapter.onBackPressed();
						}
					}
			);
		}
	}

	protected ListViewMenu populateMenu()
	{
		ListViewMenu rootMenu = new ListViewMenu("Root");

		//
		//  User Authentication
		//
		ListViewMenu userAuthenticationMenu = rootMenu.addItem(new ListViewMenu("User Authentication"));
		userAuthenticationMenu.addItem(
				new ActionableListViewMenu(
						"Log in with Facebook",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								logInWithFacebook();
							}
						}
				)
		);
		userAuthenticationMenu.addItem(
				new ActionableListViewMenu(
						"Log in with Generic provider",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								logInWithGenericProvider();
							}
						}
				)
		);
		userAuthenticationMenu.addItem(
				new ActionableListViewMenu(
						"Add Facebook user identity",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								addFbUserIdentity();
							}
						}
				)
		);
		userAuthenticationMenu.addItem(
				new ActionableListViewMenu(
						"Add Custom user identity",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								addCustomUserIdentity();
							}
						}
				)
		);
		ActionableListViewMenu removeFbUserIdentityMenuItem = new ActionableListViewMenu(
				"Remove Facebook user identity",
				new ListViewMenuItemAction()
				{
					@Override
					public void execute(ListViewMenu menuItem)
					{
						removeFbUserIdentity();
					}
				}
		);
		removeFbUserIdentityMenuItem.setSubtitle("Log out from Facebook");
		userAuthenticationMenu.addItem(removeFbUserIdentityMenuItem);

		userAuthenticationMenu.addItem(
				new ActionableListViewMenu(
						"Remove Custom user identity",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								removeUserIdentity("GetSocial");
							}
						}
				)
		);
		userAuthenticationMenu.addItem(
				new ActionableListViewMenu(
						"Log out",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								logOut();
							}
						}
				)
		);


		//
		//  Activities
		//
		ListViewMenu activitiesMenu = rootMenu.addItem(new ListViewMenu("Activities"));
		activitiesMenu.addItem(
				new ActionableListViewMenu(
						"Open Global Activities",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								openGlobalActivities();
							}
						}
				)
		);
		activitiesMenu.addItem(
				new ActionableListViewMenu(
						"Open Activities Filtered by Group",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								openActivitiesFilteredByGroup();
							}
						}
				)
		);

		ListViewMenu postActivityMenu = activitiesMenu.addItem(new ListViewMenu("Post Activity"));
		postActivityMenu.addItem(
				new ActionableListViewMenu(
						"Post Text",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								postActivityText();
							}
						}
				)
		);
		postActivityMenu.addItem(
				new ActionableListViewMenu(
						"Post Image",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								postActivityImage();
							}
						}
				)
		);
		postActivityMenu.addItem(
				new ActionableListViewMenu(
						"Post Text + Image",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								postActivityTextAndImage();
							}
						}
				)
		);
		postActivityMenu.addItem(
				new ActionableListViewMenu(
						"Post Text+Button",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								postActivityTextAndButton();
							}
						}
				)
		);
		postActivityMenu.addItem(
				new ActionableListViewMenu(
						"Post Text+Image+Button",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								postActivityTextAndImageAndButton();
							}
						}
				)
		);
		postActivityMenu.addItem(
				new ActionableListViewMenu(
						"Post Image+Button",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								postActivityImageAndButton();
							}
						}
				)
		);
		postActivityMenu.addItem(
				new ActionableListViewMenu(
						"Post Image+Action",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								postActivityImageAndAction();
							}
						}
				)
		);

		//
		//  Smart Invites
		//
		ListViewMenu smartInvitesMenu = rootMenu.addItem(new ListViewMenu("Smart Invites"));
		smartInvitesMenu.addItem(
				new ActionableListViewMenu(
						"Open Smart Invites UI",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								openSmartInvites();
							}
						}
				)
		);
		smartInvitesMenu.addItem(
				new ActionableListViewMenu(
						"Send Customized Smart Invite",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								sendCustomizedSmartInvite();
							}
						}
				)
		);

		//
		//  Chat
		//
		chatMenu = rootMenu.addItem(new ListViewMenu("Chat"));
		updateChatMenuSubtitle();
		chatMenu.addItem(
				new ActionableListViewMenu(
						"Open global chat",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								openGlobalChat();
							}
						}
				)
		);
		chatMenu.addItem(
				new ActionableListViewMenu(
						"Open conversation list",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								openConversationList();
							}
						}
				)
		);


		//
		//  Notification Center
		//
		notificationsMenu = rootMenu.addItem(
				new ActionableListViewMenu(
						"Notifications Center",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								openNotificationsCenter();
							}
						}
				)
		);
		updatedNotificationsMenuSubtitle();


		//
		//  Friends List
		//
		rootMenu.addItem(new ActionableListViewMenu(
						"Friends List",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								openFriendsList();
							}
						}
				)
		);


		//
		//  UI Customization
		//
		uiCustomizationMenu = rootMenu.addItem(new ListViewMenu("UI Customization"));
		updateUiCustomizationMenuSubtitle();

		uiCustomizationMenu.addItem(
				new ActionableListViewMenu(
						"Default UI",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								loadDefaultUi();
								updateUiCustomizationMenuSubtitle();
							}
						}
				)
		);
		uiCustomizationMenu.addItem(
				new ActionableListViewMenu(
						"Load Custom UI from URL",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								loadCustomUiFromUrl();
								updateUiCustomizationMenuSubtitle();
							}
						}
				)
		);

		//
		//  Leaderboards
		//
		ListViewMenu leaderboardsMenu = rootMenu.addItem(new ListViewMenu("Leaderboards"));
		leaderboardsMenu.addItem(
				new ActionableListViewMenu(
						"Get User Rank on Leaderboard 1",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								getUserRankOnLb1();
							}
						}
				)
		);
		leaderboardsMenu.addItem(
				new ActionableListViewMenu(
						"Get User Rank on Leaderboards 1, 2, 3",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								getUserRankOnLb123();
							}
						}
				)
		);
		leaderboardsMenu.addItem(
				new ActionableListViewMenu(
						"Get first 5 Leaderboards",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								getFirst5Lb();
							}
						}
				)
		);
		leaderboardsMenu.addItem(
				new ActionableListViewMenu(
						"Get first 5 scores from Leaderboard 1",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								getFirst5ScoresFromLb1();
							}
						}
				)
		);
		leaderboardsMenu.addItem(
				new ActionableListViewMenu(
						"Submit score to Leaderboard 1",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								submitValueToLb("leaderboard_one");
							}
						}
				)
		);
		leaderboardsMenu.addItem(
				new ActionableListViewMenu(
						"Submit score to Leaderboard 2",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								submitValueToLb("leaderboard_two");
							}
						}
				)
		);


		//
		//  Cloud Save
		//
		ListViewMenu cloudSaveMenu = rootMenu.addItem(new ListViewMenu("Cloud Save"));
		cloudSaveMenu.addItem(
				new ActionableListViewMenu(
						"Save state",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								openCloudSaveDialog();
							}
						}
				)
		);
		cloudSaveMenu.addItem(
				new ActionableListViewMenu(
						"Get saved state",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								getClodSaveData();
							}
						}
				)
		);


		//
		//  Settings
		//
		ListViewMenu settingsMenu = rootMenu.addItem(new ListViewMenu("Settings"));
		languageMenu = new ActionableListViewMenu(
				"Change Language",
				new ListViewMenuItemAction()
				{
					@Override
					public void execute(ListViewMenu menuItem)
					{
						showLanguageSelectionDialog();
					}
				}
		);
		settingsMenu.addItem(languageMenu);
		updateLanguageMenuSubtitle();

		settingsMenu.addItem(
				new CheckboxListViewMenu(
						"Enable User Generated Content Handler",
						isContentModerated,
						new OnCheckboxListViewMenuChecked()
						{
							@Override
							public void onCheckChanged(boolean isChecked)
							{
								isContentModerated = isChecked;
							}
						}
				)
		);
		settingsMenu.addItem(
				new CheckboxListViewMenu(
						"User avatar click custom behaviour",
						isUserAvatarClickHandlerCustom,
						new OnCheckboxListViewMenuChecked()
						{
							@Override
							public void onCheckChanged(boolean isChecked)
							{
								isUserAvatarClickHandlerCustom = isChecked;
							}
						}
				)
		);
		settingsMenu.addItem(
				new CheckboxListViewMenu(
						"App avatar click custom behaviour",
						isAppAvatarClickHandlerCustom,
						new OnCheckboxListViewMenuChecked()
						{
							@Override
							public void onCheckChanged(boolean isChecked)
							{
								isAppAvatarClickHandlerCustom = isChecked;
							}
						}
				)
		);
		settingsMenu.addItem(
				new CheckboxListViewMenu(
						"Activity action click custom behaviour",
						isActivityActionHandlerCustom,
						new OnCheckboxListViewMenuChecked()
						{
							@Override
							public void onCheckChanged(boolean isChecked)
							{
								isActivityActionHandlerCustom = isChecked;
							}
						}
				)
		);
		settingsMenu.addItem(
				new CheckboxListViewMenu(
						"Invite button click custom behaviour",
						isInviteButtonClickHandlerCustom,
						new OnCheckboxListViewMenuChecked()
						{
							@Override
							public void onCheckChanged(boolean isChecked)
							{
								isInviteButtonClickHandlerCustom = isChecked;
							}
						}
				)
		);

		return rootMenu;
	}

	private void updateLanguageMenuSubtitle()
	{
		String subtitle = String.format("Current language: %s", getSocial.getLanguage());
		languageMenu.setSubtitle(subtitle);

		if(listViewMenuAdapter != null)
		{
			// to update Setting menu item label with new language
			listViewMenuAdapter.notifyDataSetChanged();
		}
	}

	void updateUiCustomizationMenuSubtitle()
	{
		String currentConfigName = (getSocialUiConfiguration != null && !getSocialUiConfiguration.isEmpty()) ? getSocialUiConfiguration : "default";

		String title = String.format("UI Customization (%s)", currentConfigName);
		getSupportActionBar().setTitle(title);

		String subtitle = String.format("Current UI: %s", currentConfigName);
		uiCustomizationMenu.setSubtitle(subtitle);
	}

	private void updatedNotificationsMenuSubtitle()
	{
		notificationsMenu.setSubtitle(String.format("Unread notifications: %d", getSocial.getUnreadNotificationsCount()));
	}

	private String moderateUserGeneratedContent(int type, String content)
	{
		UiConsole.logInfo(String.format("User Content (%s) was generated \"%s\"", type, content));
		return content + "(verified \uD83D\uDC6E)";
	}

	private void updateChatMenuSubtitle()
	{
		chatMenu.setSubtitle(String.format("Unread conversations: %d", getSocialChat.getUnreadConversationsCount()));
	}

	public void reloadUiConfiguration()
	{
		if(getSocialUiConfiguration != null && !getSocialUiConfiguration.isEmpty())
		{
			getSocial.getConfiguration().setConfiguration(getSocialUiConfiguration);
		}
		else
		{
			getSocial.getConfiguration().clear();
		}
	}

	private void handleInviteActivityResult(int resultCode, Intent data)
	{
		if(resultCode == RESULT_OK)
		{
			SmartInviteViewBuilder smartInviteViewBuilder = getSocial.createSmartInviteView();

			if(data.hasExtra(InviteActivity.EXTRA_SUBJECT))
			{
				smartInviteViewBuilder.setSubject(data.getStringExtra(InviteActivity.EXTRA_SUBJECT));
			}

			if(data.hasExtra(InviteActivity.EXTRA_TEXT))
			{
				smartInviteViewBuilder.setText(data.getStringExtra(InviteActivity.EXTRA_TEXT));
			}

			if(data.hasExtra(InviteActivity.EXTRA_BUNDLE))
			{
				smartInviteViewBuilder.setReferralData((HashMap<String, String>) data.getSerializableExtra(InviteActivity.EXTRA_BUNDLE));
			}

			smartInviteViewBuilder.show();
		}
	}

	private GetSocial.OperationObserver getPostActivityOperationObserver()
	{
		return new GetSocial.OperationObserver()
		{
			@Override
			public void onSuccess(String data)
			{
				toast("Activity posted");
				openGlobalActivities();
			}

			@Override
			public void onFailure()
			{
				toast("Activity NOT posted (check if the user authenticated)");
			}
		};
	}

	private byte[] getActivityImage(int resId)
	{
		Drawable drawable = getResources().getDrawable(resId);
		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

		return stream.toByteArray();
	}

	private void logInfoAndToast(Object message)
	{
		Log.i(message.toString());
		UiConsole.logInfo(message.toString());
		toastOnUiThread(message.toString());
	}

	private void logWarningAndToast(Object message)
	{
		Log.w(message.toString());
		UiConsole.logWarning(message.toString());
		toastOnUiThread(message.toString());
	}

	private void logErrorAndToast(Object message)
	{
		Log.e(message.toString());
		UiConsole.logError(message.toString());
		toastOnUiThread(message.toString());
	}

	private void toastOnUiThread(final String message)
	{
		runOnUiThread(
				new Runnable()
				{
					@Override
					public void run()
					{
						toast(message);
					}
				}
		);
	}

	private void toast(String message)
	{
		Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
	}

	private String formatLeaderboardData(Leaderboard leaderboard)
	{
		if(leaderboard.getCurrentScore() == null)
		{
			return String.format(
					"You have no %s on %s",
					leaderboard.getLeaderboardMetaData().getUnits(),
					leaderboard.getLeaderboardMetaData().getName()
			);
		}
		else
		{
			return String.format(
					"You are ranked %d in %s with %d %s",
					leaderboard.getCurrentScore().getRank(),
					leaderboard.getLeaderboardMetaData().getName(),
					leaderboard.getCurrentScore().getValue(),
					leaderboard.getLeaderboardMetaData().getUnits()
			);
		}
	}

	private void dialogOnUiThread(final String title, final String message)
	{
		runOnUiThread(
				new Runnable()
				{
					@Override
					public void run()
					{
						new AlertDialog.Builder(MainActivity.this)
								.setTitle(title)
								.setMessage(message)
								.setPositiveButton(android.R.string.ok, null)
								.show();
					}
				}
		);
	}
	//endregion
}