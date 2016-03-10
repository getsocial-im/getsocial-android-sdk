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

package im.getsocial.testapp.auth.google;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.GamesActivityResultCodes;

import im.getsocial.sdk.core.AddUserIdentityObserver;
import im.getsocial.sdk.core.CurrentUser;
import im.getsocial.sdk.core.GetSocial;
import im.getsocial.sdk.core.User;
import im.getsocial.sdk.core.UserIdentity;
import im.getsocial.sdk.core.util.Log;

public abstract class GoogleLoginProviderHelperBase implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
	private final String CONCURRENT_OPERATION_ERROR_MESSAGE = "Operation ignored. Waiting on completion of previous operation.";

	private enum Mode
	{
		None, AddUserItentity, RemoveUserIdentity;
	}

	private Activity activity;
	private String provider;

	protected GoogleApiClient googleApiClient;
	private ConnectionResult connectionResult;

	private boolean signInRequested;
	private boolean intentInProgress;

	private AddUserIdentityObserver addIdentityInfoObserver;

	private Mode mode = Mode.None;

	public GoogleLoginProviderHelperBase(Activity activity, String provider)
	{
		this.activity = activity;
		this.provider = provider;

		GoogleApiClient.Builder builder = new GoogleApiClient.Builder(activity)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this);

		addApiAndScope(builder);

		googleApiClient = builder.build();

		googleApiClient.connect();
	}

	abstract GoogleApiClient.Builder addApiAndScope(GoogleApiClient.Builder builder);

	public abstract String getTitle();

	public abstract String getDescription();

	public boolean isAvailableForDevice(Context context)
	{
		return true;
	}

	@Override
	public void onConnectionFailed(ConnectionResult result)
	{
		if(onConnectionStatusChangedListener != null)
		{
			onConnectionStatusChangedListener.onDisconnected();
		}

		// keep a reference to the failed connection result, we will try to resolve it when the user requests a sign in
		connectionResult = result;

		// if the user has already requested a sign in, immediately try to resolve the failed connection result, if we are not already
		if(signInRequested && !intentInProgress)
		{
			resolveSignInError();
		}
	}

	@Override
	public void onConnected(Bundle connectionHint)
	{
		if(onConnectionStatusChangedListener != null)
		{
			onConnectionStatusChangedListener.onConnected();
		}

		// sign in request has been handled
		signInRequested = false;

		// continue with the GetSocial authentication
		performGetSocialConnectionRequest();
	}

	@Override
	public void onConnectionSuspended(int cause)
	{
		// as advised by Google, immediately connect again
		googleApiClient.connect();
	}

	public boolean isConnected()
	{
		return googleApiClient.isConnected();
	}

	private void clearMode()
	{
		mode = Mode.None;
		addIdentityInfoObserver = null;
	}

	public void addUserIdentity(Context context, AddUserIdentityObserver addIdentityInfoObserver)
	{
		if(mode != Mode.None)
		{
			Toast.makeText(context, CONCURRENT_OPERATION_ERROR_MESSAGE, Toast.LENGTH_SHORT).show();
			return;
		}

		mode = Mode.AddUserItentity;

		this.addIdentityInfoObserver = addIdentityInfoObserver;

		if(googleApiClient.isConnected())
		{
			performGetSocialConnectionRequest();
		}
		else
		{
			signInRequested = true;
			googleApiClient.connect();
		}
	}

	public void removeUserIdentity(Context context, CurrentUser.UpdateUserInfoObserver updateUserInfoObserver)
	{
		if(mode != Mode.None)
		{
			Toast.makeText(context, CONCURRENT_OPERATION_ERROR_MESSAGE, Toast.LENGTH_SHORT).show();
			return;
		}

		mode = Mode.RemoveUserIdentity;

		if(updateUserInfoObserver != null)
		{
			updateUserInfoObserver.onComplete();
		}

		clearAccountIfConnected();

		clearMode();
	}

	public abstract void clearAccountIfConnected();

	public abstract int getResolutionRequestCode();

	public abstract int getAskPermissionsRequestCode();

	private void resolveSignInError()
	{
		if(connectionResult.hasResolution())
		{
			try
			{
				intentInProgress = true;

				activity.startIntentSenderForResult(connectionResult.getResolution().getIntentSender(), getResolutionRequestCode(), null, 0, 0, 0);
			}
			catch(IntentSender.SendIntentException e)
			{
				intentInProgress = false;

				googleApiClient.connect();
			}
		}
		else
		{
			clearMode();

			GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), activity, getResolutionRequestCode(), new DialogInterface.OnCancelListener()
					{
						@Override
						public void onCancel(DialogInterface dialog)
						{
							signInRequested = false;
						}
					}
			).show();
		}
	}

	public boolean onActivityResult(int requestCode, int resultCode, Intent data)
	{

		if(requestCode == getResolutionRequestCode())
		{
			intentInProgress = false;

			if(resultCode != Activity.RESULT_OK)
			{

				switch(resultCode){
					case GamesActivityResultCodes.RESULT_SIGN_IN_FAILED:
						Toast.makeText(activity, "Google SDK sent \"GamesActivityResultCodes.RESULT_SIGN_IN_FAILED\". Do you need to add the login account to the app?", Toast.LENGTH_LONG).show();
						break;
					case GamesActivityResultCodes.RESULT_APP_MISCONFIGURED:
						Toast.makeText(activity, "Google SDK sent \"GamesActivityResultCodes.RESULT_APP_MISCONFIGURED\".", Toast.LENGTH_LONG).show();
						break;
					case GamesActivityResultCodes.RESULT_INVALID_ROOM:
						Toast.makeText(activity, "Google SDK sent \"GamesActivityResultCodes.RESULT_INVALID_ROOM\".", Toast.LENGTH_LONG).show();
						break;
					case GamesActivityResultCodes.RESULT_LICENSE_FAILED:
						Toast.makeText(activity, "Google SDK sent \"GamesActivityResultCodes.RESULT_LICENSE_FAILED\".", Toast.LENGTH_LONG).show();
						break;
					case GamesActivityResultCodes.RESULT_NETWORK_FAILURE:
						Toast.makeText(activity, "Google SDK sent \"GamesActivityResultCodes.RESULT_NETWORK_FAILURE\".", Toast.LENGTH_LONG).show();
						break;
					case GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED:
						// @see http://stackoverflow.com/questions/26902935/android-api-isconnected-returning-true-after-signing-out
						// check for "inconsistent state"
						// force a disconnect to sync up state, ensuring that mClient reports "not connected"
						googleApiClient.disconnect();
						Toast.makeText(activity, "Google SDK sent \"GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED\".", Toast.LENGTH_LONG).show();
						break;
					case GamesActivityResultCodes.RESULT_SEND_REQUEST_FAILED:
						Toast.makeText(activity, "Google SDK sent \"GamesActivityResultCodes.RESULT_SEND_REQUEST_FAILED\".", Toast.LENGTH_LONG).show();
						break;
				}

				if(resultCode == Activity.RESULT_CANCELED)
				{
					Toast.makeText(activity, "Google SDK sent \"Activity.RESULT_CANCELED\". If you did not cancel the sign in please check you are using the correct certificate for this build.", Toast.LENGTH_LONG).show();
				}

				clearMode();

				signInRequested = false;
				return true;
			}

			if(!googleApiClient.isConnecting() && !googleApiClient.isConnected())
			{
				googleApiClient.connect();
			}

			if(googleApiClient.isConnected()){
				performGetSocialConnectionRequest();
			}

			return true;
		}
		return false;
	}

	public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		if(getAskPermissionsRequestCode() == requestCode){
			if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{
				performGetSocialConnectionRequest();
			}
			else
			{
				clearMode();
				// Permission Denied
				Toast.makeText(activity, "Contacts permission denied", Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		return false;
	}

	abstract String getAccountName();

	private void performGetSocialConnectionRequest()
	{
		(new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
					{
						int hasWriteContactsPermission = activity.checkSelfPermission(android.Manifest.permission.GET_ACCOUNTS);
						if(hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED)
						{
							activity.requestPermissions(new String[] {android.Manifest.permission.GET_ACCOUNTS}, getAskPermissionsRequestCode());
							return;
						}
					}

					String accountName = getAccountName();

					String token = GoogleAuthUtil.getToken(activity, accountName, "oauth2:profile email");

					switch(mode)
					{
						case None:
							break;
						case AddUserItentity:

							UserIdentity googleIdentity = UserIdentity
									.create(provider, token);

							GetSocial.getInstance().getCurrentUser().addUserIdentity(googleIdentity, new AddUserIdentityObserver()
									{
										@Override
										public void onComplete(AddIdentityResult addIdentityResult)
										{
											if(addIdentityInfoObserver != null)
											{
												addIdentityInfoObserver.onComplete(addIdentityResult);
											}
											clearMode();
										}

										@Override
										public void onError(Exception error)
										{
											if(addIdentityInfoObserver != null)
											{
												addIdentityInfoObserver.onError(error);
											}
											clearMode();
										}

										@Override
										public void onConflict(User currentUser, User remoteUser, UserIdentityResolver resolver)
										{
											if(addIdentityInfoObserver != null)
											{
												addIdentityInfoObserver.onConflict(currentUser,remoteUser,resolver);
											}
										}
									}
							);
							break;
						case RemoveUserIdentity:
							break;
					}
				}
				catch(UserRecoverableAuthException e)
				{
					activity.startActivityForResult(e.getIntent(), getResolutionRequestCode());

					return;
				}
				catch(Exception e)
				{
					clearMode();
					e.printStackTrace();
					Log.e("GetSocial Exception", e.getMessage(), e);
					Toast.makeText(activity, "GetSocial("+e+"):"+e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		}).start();
	}

	public interface OnConnectionStatusChangedListener
	{
		void onConnecting();

		void onConnected();

		void onDisconnecting();

		void onDisconnected();
	}

	private OnConnectionStatusChangedListener onConnectionStatusChangedListener;

	public void setOnConnectionStatusChangedListener(OnConnectionStatusChangedListener onConnectionStatusChangedListener)
	{
		this.onConnectionStatusChangedListener = onConnectionStatusChangedListener;
	}
}
