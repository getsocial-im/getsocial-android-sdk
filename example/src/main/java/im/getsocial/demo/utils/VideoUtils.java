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
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.DataInputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class VideoUtils {

	public static class VideoDescriptor {
		public final Bitmap _thumbnail;
		public final byte[] _video;

		VideoDescriptor(final Bitmap thumbnail, final byte[] video) {
			_thumbnail = thumbnail;
			_video = video;
		}
	}

	public static VideoDescriptor open(final Context context, final Uri uri) {
		try {
			return new VideoDescriptor(thumbnail(context, uri), content(context, uri));
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static byte[] content(final Context context, final Uri uri) throws IOException {
		try (final ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r")) {
			final FileDescriptor fd = fileDescriptor.getFileDescriptor();

			try (final InputStream stream = new FileInputStream(fd)) {
				final byte[] bytes = new byte[(int) fileDescriptor.getStatSize()];
				try (final DataInputStream dis = new DataInputStream(stream)) {
					dis.readFully(bytes);
					return bytes;
				}
			}
		}
	}

	private static Bitmap thumbnail(final Context context, final Uri uri) throws IOException {
		try (final ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r")) {
			final FileDescriptor fd = fileDescriptor.getFileDescriptor();
			final MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
			try {
				mediaMetadataRetriever.setDataSource(fd);
				return mediaMetadataRetriever.getFrameAtTime();
			} finally {
				mediaMetadataRetriever.release();
			}
		}
	}
}
