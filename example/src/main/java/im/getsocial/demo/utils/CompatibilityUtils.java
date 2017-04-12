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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;

public final class CompatibilityUtils {
	/**
	 * Provide backward compatible interface to get a drawable resource.
	 *
	 * @param context current context
	 * @param id      id of resource
	 * @return {@link Drawable} object
	 */
	public static Drawable getDrawable(Context context, int id) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			return context.getDrawable(id);
		} else {
			return context.getResources().getDrawable(id);
		}
	}

	/**
	 * Provide backward compatible interface to convert html string to Spanned text.
	 *
	 * @param html html string
	 * @return converted {@link Spanned}
	 */
	public static Spanned fromHtml(String html) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
		} else {
			return Html.fromHtml(html);
		}
	}
}
