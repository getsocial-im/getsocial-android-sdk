/*
 * Published under the MIT License (MIT)
 * Copyright: (c) 2015 GetSocial B.V.
 */

package im.getsocial.testapp;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

/**
 * Created by tsvetomirstanchev on 14/10/15.
 */
public class UserIdentityUtils
{

	private static String TESTAPP_PREFERENCES = "Testapp_Preferences";


	private static String getInstallationId(Context context)
	{
		SharedPreferences sharedPreferences = context.getSharedPreferences(TESTAPP_PREFERENCES, Context.MODE_PRIVATE);
		String uuid = sharedPreferences.getString("installationId", null);

		if(uuid == null)
		{
			uuid = UUID.randomUUID().toString();
			SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putString("installationId", uuid);
			editor.commit();
		}

		return uuid;
	}

	public static String getInstallationIdWithSuffix(String suffix, Context context)
	{
		String uuid = getInstallationId(context);

		return String.format("%s%s", uuid, suffix);
	}

	public static String getDisplayName(String userId)
	{
		String[] displayNames = new String[] {
			"Batman",
			"Spiderman",
			"Captain America",
			"Green Lantern",
			"Wolverine",
			"Catwomen",
			"Iron Man",
			"Superman",
			"Wonder Woman",
			"Aquaman"
		};

		int hashCode = Math.abs(userId.hashCode());

		return displayNames[hashCode % displayNames.length] + " Android";
	}

	public static String getAvatar(String userId)
	{
		return String.format("http://api.adorable.io/avatars/200/%s.png", userId);
	}
}
