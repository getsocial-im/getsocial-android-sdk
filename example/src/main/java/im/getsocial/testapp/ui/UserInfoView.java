/*
 * Published under the MIT License (MIT)
 * Copyright: (c) 2015 GetSocial B.V.
 */

package im.getsocial.testapp.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import im.getsocial.sdk.core.IdentityInfo;
import im.getsocial.sdk.core.UserIdentity;
import im.getsocial.sdk.core.util.Log;
import im.getsocial.testapp.R;

public class UserInfoView extends RelativeLayout
{
	private UserIdentity user;
	
	private TextView displayNameTextView;
	private TextView guidTextView;
	private ImageView avatarImageView;
	private View rootView;
	
	public UserInfoView(Context context)
	{
		super(context);
		init(null, 0);
	}
	
	public UserInfoView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(attrs, 0);
	}
	
	public UserInfoView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}
	
	public void setUser(UserIdentity user)
	{
		this.user = user;
		updateContent();
	}
	
	private void init(AttributeSet attrs, int defStyle)
	{
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		rootView = inflater.inflate(R.layout.view_user_info, this, true);
		
		displayNameTextView = (TextView) rootView.findViewById(R.id.userInfo_displayName);
		guidTextView = (TextView) rootView.findViewById(R.id.userInfo_extra);
		avatarImageView = (ImageView) rootView.findViewById(R.id.userInfo_avatar);
		
		updateContent();
	}
	
	private void updateContent()
	{
		if(user != null)
		{
			displayNameTextView.setText(user.getDisplayName());
			guidTextView.setText(printProviders());
			new DownloadImage().execute(user.getAvatarUrl());
		}
		else
		{
			displayNameTextView.setText(R.string.logged_out);
			guidTextView.setText(R.string.touch_to_login);
			avatarImageView.setImageResource(R.drawable.avatar_default);
		}
	}
	
	private String printProviders()
	{
		StringBuilder sb = new StringBuilder();
		
		List<String> providers = new ArrayList(user.getIdentities().keySet());
		for(int i = 0; i < providers.size(); i++)
		{
			sb.append(providers.get(i));
			if(i < providers.size() - 1)
			{
				sb.append(" / ");
			}
		}
		
		return sb.toString();
	}
	
	public class DownloadImage extends AsyncTask<String, Integer, Drawable>
	{
		@Override
		protected Drawable doInBackground(String... arg0)
		{
			return downloadImage(arg0[0]);
		}
		
		@Override
		protected void onPostExecute(Drawable image)
		{
			if(image != null)
			{
				avatarImageView.setImageDrawable(image);
			}
		}
		
		private Drawable downloadImage(String stringUrl)
		{
			URL url;
			BufferedOutputStream out;
			InputStream in;
			BufferedInputStream buf;
			
			try
			{
				url = new URL(stringUrl);
				in = url.openStream();
				buf = new BufferedInputStream(in);
				
				Bitmap bitmap = BitmapFactory.decodeStream(buf);
				
				if(buf != null)
				{
					buf.close();
				}
				
				return new BitmapDrawable(getRoundedBitmap(bitmap));
				
			}
			catch(Exception e)
			{
				Log.e("Error loading image: " + e.toString());
			}
			
			return null;
		}
		
		private Bitmap getRoundedBitmap(Bitmap bitmap)
		{
			int radius = Math.min(bitmap.getWidth(), bitmap.getHeight());
			
			Bitmap roundedBitmap = Bitmap.createBitmap(radius, radius,
					Bitmap.Config.ARGB_8888
			);
			Canvas canvas = new Canvas(roundedBitmap);
			
			final int color = 0xffa19774;
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, radius, radius);
			
			paint.setAntiAlias(true);
			paint.setFilterBitmap(true);
			paint.setDither(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(Color.parseColor("#BAB399"));
			canvas.drawCircle(radius / 2 + 0.7f,
					radius / 2 + 0.7f, radius / 2 + 0.1f, paint
			);
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
			canvas.drawBitmap(bitmap, rect, rect, paint);
			
			return roundedBitmap;
		}
	}
}
