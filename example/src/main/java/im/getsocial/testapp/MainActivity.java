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
import android.text.TextUtils;
import android.view.LayoutInflater;
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
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import im.getsocial.sdk.chat.GetSocialChat;
import im.getsocial.sdk.core.AddUserIdentityObserver;
import im.getsocial.sdk.core.CurrentUser;
import im.getsocial.sdk.core.GetSocial;
import im.getsocial.sdk.core.UI.builder.SmartInviteViewBuilder;
import im.getsocial.sdk.core.UI.builder.UserListViewBuilder;
import im.getsocial.sdk.core.User;
import im.getsocial.sdk.core.UserIdentity;
import im.getsocial.sdk.core.resources.Leaderboard;
import im.getsocial.sdk.core.resources.LeaderboardScore;
import im.getsocial.sdk.core.util.InstanceCounter;
import im.getsocial.sdk.core.util.Log;
import im.getsocial.testapp.auth.google.GooglePlayLoginProviderHelper;
import im.getsocial.testapp.auth.google.GooglePlusLoginProviderHelper;
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
	 * <p/>
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
	private GooglePlusLoginProviderHelper googlePlusLoginProviderHelper;
	private GooglePlayLoginProviderHelper googlePlayLoginProviderHelper;

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
	private boolean isOnActionPerformListener = false;
	private boolean isPreventAnonymousInteract = true;

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
		initGoogleServices();
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

		if(googlePlusLoginProviderHelper.onActivityResult(requestCode, resultCode, data))
		{
			return;
		}

		if(googlePlayLoginProviderHelper.onActivityResult(requestCode, resultCode, data))
		{
			return;
		}

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
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		if(googlePlusLoginProviderHelper.onRequestPermissionsResult(requestCode, permissions, grantResults))
		{
			return;
		}
		if(googlePlayLoginProviderHelper.onRequestPermissionsResult(requestCode, permissions, grantResults))
		{
			return;
		}

		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

			InstanceCounter.printReferenceCounts();
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
						updateUserInfoView();
						logInfoAndToast("GetSocial initialization successful");
					}

					@Override
					public void onFailure(String errorMessage)
					{
						logErrorAndToast(errorMessage);
					}
				}
		);

		getSocial.registerPlugin(UserIdentity.PROVIDER_FACEBOOK, new FacebookInvitePlugin(this, callbackManager));

		getSocial.setOnUserAvatarClickHandler(
				new GetSocial.OnUserAvatarClickHandler()
				{
					@Override
					public boolean onUserAvatarClick(User user, int source)
					{
						if(isUserAvatarClickHandlerCustom)
						{
							logInfoAndToast(String.format("User %s avatar clicked", user.getDisplayName()));
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

		getSocial.setOnUnreadNotificationsCountChangedListener(
				new GetSocial.OnUnreadNotificationsCountChangedListener()
				{
					@Override
					public void onUnreadNotificationsCountChanged(int unreadNotificationsCount)
					{
						MainActivity.this.runOnUiThread(
								new Runnable()
								{
									@Override
									public void run()
									{
										updatedNotificationsMenuSubtitle();
									}
								}
						);
					}
				}
		);

		getSocial.setOnUserGeneratedContentListener(
				new GetSocial.OnUserGeneratedContentListener()
				{
					@Override
					public String onUserGeneratedContent(GetSocial.ContentSource source, String content)
					{
						if(isContentModerated)
						{
							return moderateUserGeneratedContent(source, content);
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
		
		getSocial.setInviteFriendsListener(
				new GetSocial.InviteFriendsListener()
				{
					@Override
					public void onInviteFriendsIntent()
					{
						Log.e("onInviteFriendsIntent friends intent invoked");
					}
					
					@Override
					public void onInvitedFriends(int friendsInvited)
					{
						Log.e("Invited %d friends", friendsInvited);
					}
				}
		);

		getSocial.setOnActionPerformListener(new GetSocial.OnActionPerformListener()
											 {
												 @Override
												 public void onActionPerform(GetSocial.Action action,
																			 final ActionFinalizer actionFinalizer)
												 {
													 if(isNonAnonymousUserAction(action) && isPreventAnonymousInteract && getSocial.getCurrentUser().isAnonymous())
													 {

														 runOnUiThread(new Runnable()
																	   {
																		   @Override
																		   public void run()
																		   {
																			   AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
																			   builder.setTitle("Add identity in order to post");
																			   builder.setItems(new CharSequence[]
																							   {"Add FB identity", "Add Google+ identity", "Add GooglePlay identity", "Add Custom Identity", "Cancel"},
																					   new DialogInterface.OnClickListener()
																					   {
																						   public void onClick(DialogInterface dialog, int which)
																						   {

																							   OnResultListener onResultListener = new OnResultListener()
																							   {
																								   @Override
																								   public void onComplete()
																								   {
																									   MainActivity.this.runOnUiThread(new Runnable() {
																										   @Override
																										   public void run()
																										   {
																											   if(!getSocial.getCurrentUser().isAnonymous())
																											   {
																												   actionFinalizer.finalize(true);
																											   }
																											   else
																											   {
																												   actionFinalizer.finalize(false);
																											   }
																										   }
																									   });
																								   }

																								   @Override
																								   public void onError(Exception error)
																								   {
																									   actionFinalizer.finalize(false);
																								   }
																							   };

																							   switch(which)
																							   {
																								   case 0:
																									   addFbUserIdentity(onResultListener);
																									   break;
																								   case 1:
																									   addGooglePlusUserIdentity(onResultListener);
																									   break;
																								   case 2:
																									   addGooglePlayUserIdentity(onResultListener);
																									   break;
																								   case 3:
																									   addCustomUserIdentity(onResultListener);
																									   break;
																								   case 4:
																									   actionFinalizer.finalize(false);
																									   break;
																							   }
																						   }
																					   }
																			   );
																			   builder.create().show();
																		   }
																	   }
														 );


													 }
													 else
													 {
														 if(isOnActionPerformListener)
														 {
															 onActionPerformDialog(action, actionFinalizer);
														 }
														 else
														 {
															 actionFinalizer.finalize(true);
														 }
													 }
												 }
											 }
		);
	}

	private void onActionPerformDialog(final GetSocial.Action action, final GetSocial.OnActionPerformListener.ActionFinalizer actionFinalizer)
	{

		runOnUiThread(new Runnable()
					  {
						  @Override
						  public void run()
						  {
							  new AlertDialog.Builder(MainActivity.this)
									  .setTitle("Permission")
									  .setMessage("Do you allow user to " + action.toString())
									  .setPositiveButton("OK", new DialogInterface.OnClickListener()
											  {
												  public void onClick(DialogInterface dialog, int whichButton)
												  {
													  actionFinalizer.finalize(true);
												  }
											  }
									  )
									  .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
											  {
												  public void onClick(DialogInterface dialog, int whichButton)
												  {
													  actionFinalizer.finalize(false);
												  }
											  }
									  )
									  .show();
						  }
					  }
		);
	}

	//endregion

	//region Update user information
	private void changeDisplayName()
	{
		final View changeDisplayNameView = LayoutInflater.from(this).inflate(R.layout.dialog_change_display_name, null, false);
		final EditText displayNameEditText = (EditText) changeDisplayNameView.findViewById(R.id.display_name);
		displayNameEditText.setText(UserIdentityUtils.getRandomDisplayName());
		displayNameEditText.setSelection(displayNameEditText.getText().length());
		runOnUiThread(new Runnable()
					  {
						  @Override
						  public void run()
						  {
							  new AlertDialog.Builder(MainActivity.this)
									  .setView(changeDisplayNameView)
									  .setPositiveButton("OK", new DialogInterface.OnClickListener()
											  {
												  public void onClick(DialogInterface dialog, int whichButton)
												  {
													  final String name = displayNameEditText.getText().toString();

													  getSocial.getCurrentUser().setDisplayName(name, new CurrentUser.UpdateUserInfoObserver()
															  {
																  @Override
																  public void onComplete()
																  {
																	  logInfoAndToast("User display name was changed to" + name);
																	  updateUserInfoView();
																  }

																  @Override
																  public void onError(Exception error)
																  {
																	  logInfoAndToast("Cannot change display name. Reason %@" + error.getMessage());
																  }
															  }
													  );

												  }
											  }
									  )
									  .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
											  {
												  public void onClick(DialogInterface dialog, int whichButton)
												  {
												  }
											  }
									  )
									  .show();
						  }
					  }
		);
	}

	private void changeUserAvatar()
	{
		final String avatarUrl = UserIdentityUtils.getRandomAvatar();

		getSocial.getCurrentUser().setAvatarUrl(avatarUrl, new CurrentUser.UpdateUserInfoObserver()
				{
					@Override
					public void onComplete()
					{
						logInfoAndToast("User avatar was changed to " + avatarUrl);
						updateUserInfoView();
					}

					@Override
					public void onError(Exception error)
					{
						logInfoAndToast("Cannot change avatar. Reason %@" + error.getMessage());
					}

				}
		);
	}
	//endregion

	//region Working with User Authentication
	private void addFbUserIdentity(final OnResultListener resultListener)
	{
		final AccessTokenTracker accessTokenTracker = new AccessTokenTracker()
		{
			@Override
			protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken)
			{
				if(newAccessToken == null)
				{
					return;
				}
				stopTracking();

				UserIdentity fbIdentity = UserIdentity.createFacebookIdentity(newAccessToken.getToken());

				getSocial.getCurrentUser().addUserIdentity(
						fbIdentity,
						new AddUserIdentityObserver()
						{
							@Override
							public void onComplete(AddIdentityResult addIdentityResult)
							{
								updateUserInfoView();

								//check if we continue with a user that has facebook identity
								if(addIdentityResult==AddIdentityResult.CONFLICT_WAS_RESOLVED_WITH_CURRENT)
								{
									logInfoAndToast("Facebook identity is not added");
									disconnectFromFacebook();
								}
								else
								{
									logInfoAndToast("Adding Facebook identity finished successfully");
								}
								updateUserInfoView();

								if(resultListener != null)
								{
									resultListener.onComplete();
								}
							}

							@Override
							public void onError(Exception error)
							{
								logErrorAndToast("Failed to add Facebook user identity, error: " + error.getMessage());

								if(resultListener != null)
								{
									resultListener.onError(error);
								}
							}

							@Override
							public void onConflict(User currentUser, User remoteUser, UserIdentityResolver resolver)
							{
								showDialogToSolveIdentityConflict(currentUser, remoteUser, resolver);
							}
						}
				);
			}
		};
		accessTokenTracker.startTracking();

		LoginManager.getInstance().logInWithReadPermissions(this, FACEBOOK_PERMISSIONS);
	}

	private void addGooglePlusUserIdentity(final OnResultListener resultListener)
	{
		googlePlusLoginProviderHelper.addUserIdentity(MainActivity.this, new AddUserIdentityObserver()
				{
					@Override
					public void onComplete(AddIdentityResult addIdentityResult)
					{
						if(addIdentityResult==AddIdentityResult.CONFLICT_WAS_RESOLVED_WITH_CURRENT)
						{
							logInfoAndToast("Google+ identity is not added");
						}
						else
						{
							logInfoAndToast("Adding Google+ finished successfully");
						}
						updateUserInfoView();


						if(resultListener != null)
						{
							resultListener.onComplete();
						}
					}

					@Override
					public void onError(Exception error)
					{
						logErrorAndToast("Failed to add Google+ user identity, error: " + error.getMessage());

						if(resultListener != null)
						{
							resultListener.onError(error);
						}
					}

					@Override
					public void onConflict(User currentUser, User remoteUser, UserIdentityResolver resolver)
					{
						showDialogToSolveIdentityConflict(currentUser, remoteUser, resolver);
					}
				}
		);
	}

	private void addGooglePlayUserIdentity(final OnResultListener resultListener)
	{
		googlePlayLoginProviderHelper.addUserIdentity(MainActivity.this, new AddUserIdentityObserver()
				{
					@Override
					public void onComplete(AddIdentityResult addIdentityResult)
					{
						if(addIdentityResult==AddIdentityResult.CONFLICT_WAS_RESOLVED_WITH_CURRENT)
						{
							logInfoAndToast("GooglePlay identity is not added");
						}
						else
						{
							logInfoAndToast("Adding GooglePlay finished successfully");
						}
						updateUserInfoView();

						if(resultListener != null)
						{
							resultListener.onComplete();
						}
					}

					@Override
					public void onError(Exception error)
					{
						logErrorAndToast("Failed to add Google Play user identity, error: " + error.getMessage());

						if(resultListener != null)
						{
							resultListener.onError(error);
						}
					}

					@Override
					public void onConflict(User currentUser, User remoteUser, UserIdentityResolver resolver)
					{
						showDialogToSolveIdentityConflict(currentUser, remoteUser, resolver);
					}
				}
		);
	}

	private void addCustomUserIdentity(final OnResultListener resultListener)
	{

		final View view = getLayoutInflater().inflate(R.layout.dialog_custom_identity, null, false);

		final EditText userdIdEditText = (EditText) view.findViewById(R.id.user_id);

		final EditText tokenEditText = (EditText) view.findViewById(R.id.user_token);

		runOnUiThread(new Runnable()
					  {
						  @Override
						  public void run()
						  {
							  AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
									  .setView(view)
									  .setPositiveButton("Add", new DialogInterface.OnClickListener()
											  {
												  @Override
												  public void onClick(DialogInterface dialog, int which)
												  {
													  String usedId = userdIdEditText.getText().toString().trim();
													  String token = tokenEditText.getText().toString().trim();
													  addUserIdentity(usedId, token, resultListener);
												  }
											  }
									  )
									  .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
											  {
												  @Override
												  public void onClick(DialogInterface dialog, int which)
												  {
													  toast("Action cancelled");
												  }
											  }
									  );
							  builder.show();
						  }
					  }
		);
	}
	
	private void addUserIdentity(String userId, String token, final OnResultListener resultListener)
	{
		if(TextUtils.isEmpty(userId) || TextUtils.isEmpty(token))
		{
			logErrorAndToast("Failed to add User Identity, User Id and Token fields can't be empty");
		}
		else
		{
			UserIdentity identity = UserIdentity.create("Custom", userId, token);
			getSocial.getCurrentUser().addUserIdentity(
					identity,
					new AddUserIdentityObserver()
					{
						@Override
						public void onComplete(AddIdentityResult addIdentityResult)
						{
							if(addIdentityResult==AddIdentityResult.CONFLICT_WAS_RESOLVED_WITH_CURRENT)
							{
								logInfoAndToast("'custom identity' is not added");
							}
							else
							{
								logInfoAndToast("Adding 'custom identity' finished successfully");
							}
							updateUserInfoView();

							
							if(resultListener != null)
							{
								resultListener.onComplete();
							}
						}
						
						@Override
						public void onError(Exception error)
						{
							logErrorAndToast("Failed to add user identity 'custom', error: " + error.getMessage());
							
							if(resultListener != null)
							{
								resultListener.onError(error);
							}
						}
						
						@Override
						public void onConflict(User currentUser, User remoteUser, UserIdentityResolver resolver)
						{
							showDialogToSolveIdentityConflict(currentUser, remoteUser, resolver);
						}
					}
			);
		}
	}

	private void removeFbUserIdentity()
	{
		getSocial.getCurrentUser().removeUserIdentity(UserIdentity.PROVIDER_FACEBOOK,
				new CurrentUser.UpdateUserInfoObserver()
				{
					@Override
					public void onComplete()
					{
						updateUserInfoView();
						logInfoAndToast("Successfully removed Facebook user identity");
					}

					@Override
					public void onError(Exception error)
					{
						logErrorAndToast("Failed to remove Facebook user identity, error: " + error.getMessage());
					}

				}
		);

		disconnectFromFacebook();
	}

	private void removeGooglePlusUserIdentity()
	{
		getSocial.getCurrentUser().removeUserIdentity(UserIdentity.PROVIDER_GOOGLEPLUS,
				new CurrentUser.UpdateUserInfoObserver()
				{
					@Override
					public void onComplete()
					{
						googlePlusLoginProviderHelper.removeUserIdentity(MainActivity.this, new CurrentUser.UpdateUserInfoObserver()
								{
									@Override
									public void onComplete()
									{
										updateUserInfoView();
										logInfoAndToast("Successfully removed Google+ user identity");
									}

									@Override
									public void onError(Exception error)
									{
										logErrorAndToast("Failed to remove Google+ user identity, error: " + error.getMessage());
									}

								}
						);
					}

					@Override
					public void onError(Exception error)
					{
						logErrorAndToast("Failed to remove Google+ user identity, error: " + error.getMessage());
					}

				}
		);

	}

	private void removeGooglePlayUserIdentity()
	{

		getSocial.getCurrentUser().removeUserIdentity(UserIdentity.PROVIDER_GOOGLEPLAY,
				new CurrentUser.UpdateUserInfoObserver()
				{
					@Override
					public void onComplete()
					{
						googlePlayLoginProviderHelper.removeUserIdentity(MainActivity.this, new CurrentUser.UpdateUserInfoObserver()
								{
									@Override
									public void onComplete()
									{
										updateUserInfoView();
										logInfoAndToast("Successfully removed Google Play user identity");
									}

									@Override
									public void onError(Exception error)
									{
										logErrorAndToast("Failed to remove Google Play user identity, error: " + error.getMessage());
									}

								}
						);
					}

					@Override
					public void onError(Exception error)
					{
						logErrorAndToast("Failed to remove Google Play user identity, error: " + error.getMessage());
					}
				}
		);
	}

	private void removeUserIdentity(final String providerId)
	{
		getSocial.getCurrentUser().removeUserIdentity(
				providerId,
				new CurrentUser.UpdateUserInfoObserver()
				{
					@Override
					public void onComplete()
					{
						updateUserInfoView();
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

	private void showDialogToSolveIdentityConflict(final User currentUser, final User remoteUser, final AddUserIdentityObserver.UserIdentityResolver resolver)
	{

		runOnUiThread(new Runnable()
					  {
						  @Override
						  public void run()
						  {
							  new AlertDialog.Builder(MainActivity.this)
									  .setTitle("Conflict")
									  .setMessage("The new identity is already linked to another user. Which one do you want to continue using?")
									  .setPositiveButton("Remote", new DialogInterface.OnClickListener()
											  {
												  public void onClick(DialogInterface dialog, int whichButton)
												  {
													  resolver.resolve(AddUserIdentityObserver.AddIdentityConflictResolutionStrategy.REMOTE);
												  }
											  }
									  )
									  .setNegativeButton("Current", new DialogInterface.OnClickListener()
											  {
												  public void onClick(DialogInterface dialog, int whichButton)
												  {
													  resolver.resolve(AddUserIdentityObserver.AddIdentityConflictResolutionStrategy.CURRENT);
												  }
											  }
									  )
									  .show();
						  }
					  }
		);
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
					public void onUserSelected(User user)
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
		View saveStateView = getLayoutInflater().inflate(R.layout.dialog_save_state, null, false);
		final EditText dataTextView = (EditText) saveStateView.findViewById(R.id.save_state);

		new AlertDialog.Builder(MainActivity.this)
				.setView(saveStateView)
				.setPositiveButton(R.string.save, new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialogInterface, int i)
							{
								String data = dataTextView.getText().toString();
								if(!data.isEmpty())
								{
									getSocial.save(data);
								}
							}
						}
				)
				.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								//save cancelled
							}
						}
				)
				.show();
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
					public void onFailure(String errorMessage)
					{
						logErrorAndToast(errorMessage);
					}
				}
		);
	}
	//endregion

	//region GetSocial Settings
	private void showLanguageSelectionDialog()
	{
		final String[] providers = {"da", "de", "en", "es", "fr", "it", "nb", "nl", "pt", "ru", "sv", "tr", "is", "ja", "ko", "zh-Hans", "zh-Hant"};

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
	//endregion


	// /////////////////////////////////
	// region Helper methods
	// /////////////////////////////////

	private void initFacebook()
	{
		FacebookSdk.sdkInitialize(getApplicationContext());
		callbackManager = CallbackManager.Factory.create();
	}

	private void initGoogleServices()
	{
		if(googlePlusLoginProviderHelper == null)
		{
			googlePlusLoginProviderHelper = new GooglePlusLoginProviderHelper(this);
		}
		if(googlePlayLoginProviderHelper == null)
		{
			googlePlayLoginProviderHelper = new GooglePlayLoginProviderHelper(this);
		}
	}

	private void initUi()
	{
		userInfoView = (UserInfoView) findViewById(R.id.toolbar_userInfo);
		userInfoView.setUser(getSocial.getCurrentUser());
		userInfoView.setOnClickListener(
				new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						if(getSocial.isInitialized())
						{
							FragmentManager fm = getSupportFragmentManager();
							UserInfoDialog userInfoDialog = new UserInfoDialog();
							userInfoDialog.show(fm, "user_info_dialog");
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
		ListViewMenu userAuthenticationMenu = rootMenu.addItem(new ListViewMenu("User Management"));

		userAuthenticationMenu.addItem(
				new ActionableListViewMenu(
						"Change user display name",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								changeDisplayName();
							}
						}
				)
		);

		userAuthenticationMenu.addItem(
				new ActionableListViewMenu(
						"Change user avatar",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								changeUserAvatar();
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
								addFbUserIdentity(null);
							}
						}
				)
		);

		userAuthenticationMenu.addItem(
				new ActionableListViewMenu(
						"Add Google+ user identity",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								addGooglePlusUserIdentity(null);
							}
						}
				)
		);
		userAuthenticationMenu.addItem(
				new ActionableListViewMenu(
						"Add Google Play user identity",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								addGooglePlayUserIdentity(null);
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
								addCustomUserIdentity(null);
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

		ActionableListViewMenu removeGooglePlusUserIdentity = new ActionableListViewMenu(
				"Remove Google+ user identity",
				new ListViewMenuItemAction()
				{
					@Override
					public void execute(ListViewMenu menuItem)
					{
						removeGooglePlusUserIdentity();
					}
				}
		);
		userAuthenticationMenu.addItem(removeGooglePlusUserIdentity);

		ActionableListViewMenu removeGooglePlayUserIdentity = new ActionableListViewMenu(
				"Remove Google Play user identity",
				new ListViewMenuItemAction()
				{
					@Override
					public void execute(ListViewMenu menuItem)
					{
						removeGooglePlayUserIdentity();
					}
				}
		);
		userAuthenticationMenu.addItem(removeGooglePlayUserIdentity);

		userAuthenticationMenu.addItem(
				new ActionableListViewMenu(
						"Remove Custom user identity",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								removeUserIdentity("Custom");
							}
						}
				)
		);

		userAuthenticationMenu.addItem(
				new ActionableListViewMenu(
						"Reset current user",
						new ListViewMenuItemAction()
						{
							@Override
							public void execute(ListViewMenu menuItem)
							{
								getSocial.getCurrentUser().reset(new CurrentUser.UpdateUserInfoObserver()

																 {
																	 @Override
																	 public void onComplete()
																	 {
																		 updateUserInfoView();
																		 disconnectFromFacebook();
																		 logInfoAndToast("Successfully reseted");
																	 }

																	 @Override
																	 public void onError(Exception error)
																	 {
																		 logErrorAndToast(error);
																	 }

																 }

								);
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
						"Prevent anonymous user from posting",
						isPreventAnonymousInteract,
						new OnCheckboxListViewMenuChecked()
						{
							@Override
							public void onCheckChanged(boolean isChecked)
							{
								isPreventAnonymousInteract = isChecked;
								if(isPreventAnonymousInteract)
								{
									toast("Try to perform some action as an anonymous user. (eg. like a post)");
								}
							}
						}
				)
		);


		settingsMenu.addItem(
				new CheckboxListViewMenu(
						"Perform action handler",
						isOnActionPerformListener,
						new OnCheckboxListViewMenuChecked()
						{
							@Override
							public void onCheckChanged(boolean isChecked)
							{
								isOnActionPerformListener = isChecked;
								if(isOnActionPerformListener)
								{
									toast("Try to open any window to see action confirmation dialog");
								}
							}
						}
				)
		);

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
								if(isContentModerated)
								{
									toast("Try to post an activity or chat message");
								}
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
								if(isUserAvatarClickHandlerCustom)
								{
									toast("Tap on any user avatar");
								}
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
								if(isAppAvatarClickHandlerCustom)
								{
									toast("Tap on any app avatar");
								}
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
								if(isActivityActionHandlerCustom)
								{
									toast("Tap on an activity action button to see the effect");
								}
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
								toast("Tap on invite button inside Chat window to see the effect");
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
		if(listViewMenuAdapter != null)
		{
			listViewMenuAdapter.notifyDataSetChanged();
		}
	}

	private String moderateUserGeneratedContent(GetSocial.ContentSource source, String content)
	{
		UiConsole.logInfo(String.format("User Content (%s) was generated \"%s\"", source, content));
		return content + "(verified \uD83D\uDC6E)";
	}

	private void updateChatMenuSubtitle()
	{
		chatMenu.setSubtitle(String.format("Unread conversations: %d", getSocialChat.getUnreadConversationsCount()));
		if(listViewMenuAdapter != null)
		{
			runOnUiThread(
					new Runnable()
					{
						@Override
						public void run()
						{
							listViewMenuAdapter.notifyDataSetChanged();
						}
					}
			);
		}
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

			if(data.hasExtra(InviteActivity.EXTRA_IMAGE))
			{
				smartInviteViewBuilder.setImageUrl(data.getStringExtra(InviteActivity.EXTRA_IMAGE));
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
			public void onFailure(String errorMessage)
			{
				toast("Activity NOT posted. Check your internet connection");
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

	private void updateUserInfoView()
	{
		runOnUiThread(
				new Runnable()
				{
					@Override
					public void run()
					{
						userInfoView.setUser(getSocial.getCurrentUser());
					}
				}
		);

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

	private void disconnectFromFacebook()
	{

		if(AccessToken.getCurrentAccessToken() == null)
		{
			return; // already logged out
		}

		new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
				.Callback()
		{
			@Override
			public void onCompleted(GraphResponse graphResponse)
			{

				LoginManager.getInstance().logOut();

			}
		}
		).executeAsync();
	}

	private boolean isNonAnonymousUserAction(GetSocial.Action action)
	{
		boolean nonAnonymousUserAction = false;

		switch(action)
		{
			case LIKE_ACTIVITY:
			case LIKE_COMMENT:
			case POST_ACTIVITY:
			case POST_COMMENT:
			case SEND_PRIVATE_CHAT_MESSAGE:
			case SEND_PUBLIC_CHAT_MESSAGE:
				nonAnonymousUserAction = true;
				break;
		}

		return nonAnonymousUserAction;
	}

	public interface OnResultListener
	{
		void onComplete();

		void onError(Exception error);
	}
	//endregion
}