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

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.games.Games;

import im.getsocial.sdk.core.UserIdentity;

public class GooglePlayLoginProviderHelper extends GoogleLoginProviderHelperBase
{
	public final static int REQUEST_CODE_ASK_PERMISSIONS_PLAY = 11235;
	public static final int REQUEST_CODE_RESOLUTION_PLAY = 1112;

	public GooglePlayLoginProviderHelper(Activity activity)
	{
		super(activity, UserIdentity.PROVIDER_GOOGLEPLAY);
	}

	@Override
	public String getTitle()
	{
		return "Google Play";
	}

	@Override
	public String getDescription()
	{
		return "Login with Google Play!";
	}

	@Override
	public void clearAccountIfConnected()
	{
		if(googleApiClient.isConnected())
		{
			// TODO: do something with this result?
			PendingResult<Status> result = Games.signOut(googleApiClient);

			googleApiClient.disconnect();
		}
	}

	public int getResolutionRequestCode(){
		return REQUEST_CODE_RESOLUTION_PLAY;
	}

	public int getAskPermissionsRequestCode(){
		return REQUEST_CODE_ASK_PERMISSIONS_PLAY;
	}

	@Override
	String getAccountName()
	{
		return Games.getCurrentAccountName(googleApiClient);
	}

	@Override
	GoogleApiClient.Builder addApiAndScope(GoogleApiClient.Builder builder)
	{
		return builder.addApi(Games.API, Games.GamesOptions.builder().build())
			   .addScope(Games.SCOPE_GAMES);
	}
}