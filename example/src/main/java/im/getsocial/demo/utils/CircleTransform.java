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

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.squareup.picasso.Transformation;

public class CircleTransform implements Transformation {
	@Override
	public Bitmap transform(Bitmap source) {
		int size = Math.min(source.getWidth(), source.getHeight());

		// a square max size in a circle
		// calculated to avoid cropped images
		int diagonalSize = (int)(size / 1.41);

		Bitmap scaledBitmap = Bitmap.createScaledBitmap(source, diagonalSize, diagonalSize, false);
		source.recycle();

		int left = (source.getWidth() - scaledBitmap.getWidth()) / 2;
		int top = (source.getHeight() - scaledBitmap.getHeight()) / 2;

		Bitmap squaredBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(squaredBitmap);
		c.drawColor(Color.WHITE);
		c.drawBitmap(scaledBitmap, left, top, null);
		scaledBitmap.recycle();

		Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
		paint.setShader(shader);
		paint.setAntiAlias(true);

		float radius = size / 2f;

		canvas.drawCircle(radius, radius, radius, paint);

		squaredBitmap.recycle();
		return bitmap;
	}

	@Override
	public String key() {
		return "circle";
	}
}