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
import com.google.android.gms.plus.Plus;

import im.getsocial.sdk.core.UserIdentity;

public class GooglePlusLoginProviderHelper extends GoogleLoginProviderHelperBase
{
	public final static int REQUEST_CODE_ASK_PERMISSIONS_PLUS = 11236;
	public static final int REQUEST_CODE_RESOLUTION_PLUS = 1113;

	public GooglePlusLoginProviderHelper(Activity activity)
	{
		super(activity, UserIdentity.PROVIDER_GOOGLEPLUS);
	}

	@Override
	public String getTitle()
	{
		return "Google Plus";
	}

	@Override
	public String getDescription()
	{
		return "Login with Google Plus!";
	}

	@Override
	public void clearAccountIfConnected()
	{
		if(googleApiClient.isConnected())
		{
			Plus.AccountApi.clearDefaultAccount(googleApiClient);
		}
	}

	public int getResolutionRequestCode(){
		return REQUEST_CODE_RESOLUTION_PLUS;
	}

	public int getAskPermissionsRequestCode(){
		return REQUEST_CODE_ASK_PERMISSIONS_PLUS;
	}

	@Override
	String getAccountName()
	{
		return Plus.AccountApi.getAccountName(googleApiClient);
	}

	@Override
	GoogleApiClient.Builder addApiAndScope(GoogleApiClient.Builder builder)
	{
		return builder.addApi(Plus.API, Plus.PlusOptions.builder().build())
			   .addScope(Plus.SCOPE_PLUS_PROFILE)
			   .addScope(Plus.SCOPE_PLUS_LOGIN);
	}

}
