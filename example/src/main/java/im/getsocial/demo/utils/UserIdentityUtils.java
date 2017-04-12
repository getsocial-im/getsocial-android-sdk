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

package im.getsocial.demo.utils;

import java.util.Locale;
import java.util.Random;
import java.util.UUID;

/**
 * Created by orestsavchak on 1/23/17.
 */

public final class UserIdentityUtils {
	private UserIdentityUtils() {
		//
	}


	public static String getDisplayName() {
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

		return displayNames[new Random().nextInt(displayNames.length)] + " Android";
	}

	public static String getAvatar(String userId) {
		return String.format(Locale.getDefault(), "http://api.adorable.io/avatars/200/%s.png", userId);
	}

	public static String getRandomAvatar() {
		return getAvatar(UUID.randomUUID().toString());
	}
}
