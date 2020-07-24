/*
 *	Copyright 2015-2017 GetSocial B.V.
 *
 *	Licensed under the Apache License, Version 2.0 (the "License");
 *	you may not use this file except in compliance with the License.
 *	You may obtain a copy of the License at
 *
 *	http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *	See the License for the specific language governing permissions and
 *	limitations under the License.
 */

package im.getsocial.demo.plugin;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.invites.ImageContentProvider;
import im.getsocial.sdk.invites.Invite;
import im.getsocial.sdk.invites.InviteCallback;
import im.getsocial.sdk.invites.InviteChannel;
import im.getsocial.sdk.invites.InviteChannelPlugin;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

/**
 * Invite Channel Plugin for GetSocial SDK.
 * Register plugin via {@link GetSocial#registerInviteChannelPlugin(String pluginId, InviteChannelPlugin plugin)}.
 */
public class InstagramStoriesPlugin extends InviteChannelPlugin {

	private static final String PACKAGE_NAME = "com.instagram.android";

	public InstagramStoriesPlugin() {
	}

	/**
	 * Save remote resource to local FS and returns URI for saved file.
	 *
	 * @param context android context to use FS.
	 * @param url     url to file to be saved.
	 */
	private static void saveResourceToDisk(final Context context, final String url, final InstagramStoriesDownloadResourceListener listener) {
		final String fileName;
		final String fileUri;
		if (url.endsWith("gif")) {
			fileName = "tempgif.gif";
			fileUri = "invite-gif.gif";
		} else if (url.endsWith("mp4")) {
			fileName = "tempvideo.mp4";
			fileUri = "invite-video.mp4";
		} else {
			System.out.println("Invalid invite resource format.");
			return;
		}

		try {
			new InstagramStoriesDownloadResourceTask(new InstagramStoriesDownloadFileListener() {
				@Override
				public void onFileLoaded(@Nullable final String filePath) {
					final String uriBase = getUriBase(context);
					listener.onResourceLoaded(new Uri.Builder()
									.scheme("content")
									.authority(uriBase)
									.path(getUriForFileName(fileName))
									.build());
				}
			}).execute(context, url, fileName, fileUri);
		} catch (final Exception exception) {
			System.out.println("Could not writeRemoteResourceFileToDisk contents of url, returning null. error: " + exception.getMessage());
		}
	}

	private static boolean writeRemoteResourceFileToDisk(final File target, final String source) {
		FileOutputStream fileOutputStream = null;
		InputStream inputStream = null;

		try {
			final URL url = new URL(source);
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setUseCaches(true);
			connection.connect();
			inputStream = connection.getInputStream();

			if (!target.createNewFile()) {
				System.out.println("Couldn't create new file " + target.getPath());
				return false;
			}
			fileOutputStream = new FileOutputStream(target);
			final byte[] buffer = new byte[256];
			int readBytes;
			while (-1 != (readBytes = inputStream.read(buffer))) {
				fileOutputStream.write(buffer, 0, readBytes);
			}
			return true;
		} catch (final IOException ioException) {
			System.out.println("Could not save url content to the cache directory, returning null. error: " + ioException.getMessage());
			return false;
		} finally {
			close(fileOutputStream);
			close(inputStream);
		}
	}

	/**
	 * Safe close the closeable.
	 *
	 * @param closeable stream to close.
	 */
	private static void close(@Nullable final Closeable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (final IOException exception) {
			System.out.println("Failed to close the stream: " + exception.getMessage());
		}
	}

	/**
	 * Get base image URI.
	 *
	 * @param context Android Context.
	 * @return Base image URI if ImageContentProvider is present, null otherwise.
	 */
	private static String getUriBase(final Context context) {
		final ProviderInfo imageContentProvider = getImageContentProvider(context);
		if (imageContentProvider != null) {
			return imageContentProvider.authority;
		}

		System.out.println("Can not create media content URI, %s is not found in the AndroidManifest.xml: " + ImageContentProvider.class.getSimpleName());
		return null;
	}

	/**
	 * Get sharable file name for uri construction.
	 *
	 * @param fileName Cached resource file name according to ImageContract.
	 * @return Uri path part for content sharing.
	 */
	@Nullable
	private static String getUriForFileName(final String fileName) {
		switch (fileName) {
			case "tempimage.jpg":
				return "invite-image.jpg";
			case "tempgif.gif":
				return "invite-gif.gif";
			case "tempsticker.png":
				return "invite-sticker.png";
			case "tempvideo.mp4":
				return "invite-video.mp4";
			default:
				return null;
		}
	}

	/**
	 * Save bitmap to local FS and returns URI for saved file.
	 *
	 * @param context android context to use FS.
	 * @param bitmap  image to be saved.
	 * @return URI for created file.
	 */
	@Nullable
	private static Uri saveBitmapToDisk(final Context context, final Bitmap bitmap, final boolean isPng, final String fileName) {
		final String uriBase = getUriBase(context);
		if (uriBase == null) {
			return null;
		}
		if (writeImageFileToDisk(context, bitmap, isPng, fileName)) {
			return new Uri.Builder()
							.scheme("content")
							.authority(uriBase)
							.path(getUriForFileName(fileName))
							.build();
		}
		return null;
	}

	private static boolean writeImageFileToDisk(final Context context, final Bitmap bitmap, final boolean isPng, final String fileName) {
		final File tempImageFile = new File(context.getCacheDir(), fileName);
		if (tempImageFile.exists() && !tempImageFile.delete()) {
			System.out.println("Couldn't delete the old file " + tempImageFile.getPath());
			return false;
		}
		FileOutputStream fileOutputStream = null;
		try {
			if (!tempImageFile.createNewFile()) {
				System.out.println("Couldn't create the new file " + tempImageFile.getPath());
				return false;
			}
			fileOutputStream = new FileOutputStream(tempImageFile);
			final Bitmap.CompressFormat saveAs = isPng ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;
			bitmap.compress(saveAs, 100, fileOutputStream);
			return true;
		} catch (final IOException ioException) {
			System.out.println("Could not save image to the cache directory, returning null. error: " + ioException.getMessage());
			return false;
		} finally {
			close(fileOutputStream);
		}
	}

	/**
	 * Creates a rounded rectangular bitmap with text.
	 *
	 * @param stickerText text to be drawn.
	 * @return bitmap with text.
	 */
	private static Bitmap generateBitmapFromText(final String stickerText) {
		final int backgroundColor = Color.WHITE;
		final int textColor = Color.RED;

		final int paddingLeft = 15;
		final int paddingTop = 15;
		final int cornerRadius = 15;
		final int charsInLine = 40;
		//make sticker smaller if text is small
		final int width = stickerText.length() > charsInLine ? 400 : 200;
		final int bgWidth = width + (paddingLeft * 2);

		final Paint backgroundPaint = new Paint();
		backgroundPaint.setStyle(Paint.Style.FILL);
		backgroundPaint.setColor(backgroundColor);

		final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextSize(25);
		textPaint.setColor(textColor);
		textPaint.setAntiAlias(true);

		final Layout.Alignment alignment = stickerText.length() > charsInLine ? Layout.Alignment.ALIGN_NORMAL : Layout.Alignment.ALIGN_CENTER;
		final StaticLayout staticLayout = new StaticLayout(stickerText, textPaint, width, alignment, 1, 0, false);

		final RectF background = new RectF();
		background.bottom = staticLayout.getHeight() + (paddingTop * 2);
		background.right = bgWidth;
		background.left = 0;
		background.top = 0;

		final Bitmap image = Bitmap.createBitmap(bgWidth, staticLayout.getHeight() + paddingTop * 2, Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(image);
		canvas.drawRoundRect(background, cornerRadius, cornerRadius, backgroundPaint);

		canvas.save();
		canvas.translate(paddingLeft, paddingTop);
		staticLayout.draw(canvas);

		return image;
	}

	/**
	 * Get info about GetSocial Image Content Provider.
	 *
	 * @param context Android context.
	 * @return info or null if not available.
	 */
	@Nullable
	private static ProviderInfo getImageContentProvider(final Context context) {
		final String providerName = ImageContentProvider.class.getName();
		try {
			final PackageManager pm = context.getPackageManager();
			final PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_PROVIDERS);
			final ProviderInfo[] providers = packageInfo.providers;
			if (providers != null) {
				for (final ProviderInfo provider : providers) {
					if (providerName.equals(provider.name)) {
						return provider;
					}
				}
			}
		} catch (final PackageManager.NameNotFoundException exception) {
			System.out.println("Failed to check if " + providerName + " is declared in the AndroidManifest, error: " + exception.getMessage());
		}
		return null;
	}

	@Override
	public boolean isAvailableForDevice(final InviteChannel inviteChannel) {
		return hasInstagramInstalled(getContext());
	}

	@Override
	@SuppressWarnings({"PMD.ConfusingTernary"})
	public void presentChannelInterface(final InviteChannel inviteChannel, final Invite invite, final InviteCallback callback) {
		if (invite.getVideoUrl() != null) {
			saveResourceToDisk(getContext(), invite.getVideoUrl(), new InstagramStoriesDownloadResourceListener() {
				@Override
				public void onResourceLoaded(@Nullable final Uri uri) {
					if (uri != null) {
						sendContent(invite, uri, "video/*", callback);
					}
				}
			});
		} else if (invite.getImage() != null) {
			final Uri uri = saveBitmapToDisk(getContext(), invite.getImage(), false, "tempimage.jpg");
			if (uri != null) {
				sendContent(invite, uri, "image/*", callback);
			}
		} else {
			sendContent(invite, null, "image/*", callback);
		}
	}

	private void sendContent(final Invite invite, @Nullable final Uri fileUri, final String mimeType, final InviteCallback callback) {
		final Intent intent = new Intent("com.instagram.share.ADD_TO_STORY");
		if (fileUri == null) {
			intent.setType(mimeType);
			intent.putExtra("top_background_color", "#000000");
		} else {
			intent.setDataAndType(fileUri, mimeType);
		}
		if (invite.getText() != null) {
			final Uri stickerUri = saveBitmapToDisk(getContext(), generateBitmapFromText(invite.getText()), true, "tempsticker.png");
			intent.putExtra("interactive_asset_uri", stickerUri);
			getContext().grantUriPermission(PACKAGE_NAME, stickerUri, FLAG_GRANT_READ_URI_PERMISSION);
		}
		intent.setFlags(FLAG_GRANT_READ_URI_PERMISSION);
		intent.putExtra("content_url", invite.getReferralUrl());

		if (getContext().getPackageManager().resolveActivity(intent, 0) != null) {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				getContext().startActivity(intent);
				callback.onComplete();
			} catch (final Exception exception) {
				callback.onError(exception);
			}
		}
	}

	private boolean hasInstagramInstalled(final Context context) {
		try {
			context.getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
			return true;
		} catch (final PackageManager.NameNotFoundException exception) {
			return false;
		}
	}

	public interface InstagramStoriesDownloadResourceListener {
		void onResourceLoaded(@Nullable Uri uri);
	}

	private interface InstagramStoriesDownloadFileListener {
		void onFileLoaded(@Nullable String filePath);
	}

	private static class InstagramStoriesDownloadResourceTask extends AsyncTask<Object, Integer, String> {

		private final InstagramStoriesDownloadFileListener _listener;

		InstagramStoriesDownloadResourceTask(final InstagramStoriesDownloadFileListener listener) {
			_listener = listener;
		}

		@Override
		protected String doInBackground(final Object... params) {
			final Context context = (Context) params[0];
			final String uriBase = getUriBase(context);

			if (uriBase == null) {
				return null;
			}

			final String fileName = (String) params[2];
			final File tempImageFile = new File(context.getCacheDir(), fileName);
			if (tempImageFile.exists() && !tempImageFile.delete()) {
				System.out.println("Failed to delete file " + tempImageFile.getPath());
				return null;
			}

			final String resourceUrl = (String) params[1];
			if (!writeRemoteResourceFileToDisk(tempImageFile, resourceUrl)) {
				return null;
			}

			return String.format("file://%s/%s", context.getCacheDir(), fileName);
		}

		@Override
		protected void onPostExecute(final String filePath) {
			_listener.onFileLoaded(filePath);
		}
	}

}
