package im.getsocial.demo.utils;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import javax.annotation.Nullable;

/**
 * Created by orestsavchak on 7/24/17.
 */

public class ImagePicker {


	private static final String KEY_TMP_IMAGE_PATH = "GetSocial_Key_ImagePath";
	private static final String KEY_TMP_VIDEO_PATH = "GetSocial_Key_VideoPath";
	private static final int REQUEST_PICK_IMAGE_ACTIVITY = 1984;
	private static final int REQUEST_PICK_VIDEO_ACTIVITY = 1994;
	private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2012;
	private final int _requestCode;
	private final Fragment _fragment;
	private Callback _callback;
	private Uri _imageUri;
	private Uri _videoUri;

	public ImagePicker(Fragment fragment, int requestCode) {
		_fragment = fragment;
		_requestCode = requestCode;
	}

	public void onSaveInstanceState(Bundle outState) {
		if (_imageUri != null) {
			outState.putString(KEY_TMP_IMAGE_PATH, _imageUri.toString());
		}
		if (_videoUri != null) {
			outState.putString(KEY_TMP_VIDEO_PATH, _videoUri.toString());
		}
	}

	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			_imageUri = savedInstanceState.containsKey(KEY_TMP_IMAGE_PATH)
					? Uri.parse(savedInstanceState.getString(KEY_TMP_IMAGE_PATH))
					: null;
			_videoUri = savedInstanceState.containsKey(KEY_TMP_VIDEO_PATH)
					? Uri.parse(savedInstanceState.getString(KEY_TMP_VIDEO_PATH))
					: null;
			if (_imageUri != null) {
				handlePickedImage(_imageUri);
			}
			if (_videoUri != null) {
				handlePickedVideo(_videoUri);
			}
		}
	}

	public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (REQUEST_PICK_IMAGE_ACTIVITY == requestCode) {
			if (data != null) {
				handlePickedImage(data.getData());
			} else {
				_callback.onCancel();
			}
			return true;
		} else if (REQUEST_PICK_VIDEO_ACTIVITY == requestCode) {
			if (data != null) {
				handlePickedVideo(data.getData());
			} else {
				_callback.onCancel();
			}
			return true;
		}
		return false;
	}

	public boolean onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode == PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
			for (int permission : grantResults) {
				if (permission == PackageManager.PERMISSION_GRANTED) {
					if (_imageUri != null) {
						handlePickedImage(_imageUri);
						return true;
					} else if (_videoUri != null) {
						handlePickedVideo(_videoUri);
						return true;
					}
				}
			}
			_callback.onCancel();
			return true;
		}
		return false;
	}

	public void pickImageFromDevice(Callback callback) {
		pickContentFromDevice("image/*", REQUEST_PICK_IMAGE_ACTIVITY, callback);
	}

	public void pickVideoFromDevice(Callback callback) {
		pickContentFromDevice("video/*|image/gif", REQUEST_PICK_VIDEO_ACTIVITY, callback);
	}

	private void pickContentFromDevice(String contentType, int requestType, Callback callback) {
		_callback = callback;
		Intent imagePickerIntent = new Intent(Intent.ACTION_PICK);
		imagePickerIntent.setType(contentType);
		_fragment.startActivityForResult(imagePickerIntent, requestType);
	}

	private void handlePickedImage(final Uri imageUri) {
		_imageUri = imageUri;
		if (!checkPermissionsAndRequestIfNeeded()) {
			return;
		}
		_callback.onImageChosen(_imageUri, _requestCode);
	}

	private void handlePickedVideo(final Uri videoUri) {
		_videoUri = videoUri;
		if (!checkPermissionsAndRequestIfNeeded()) {
			return;
		}
		_callback.onVideoChosen(_videoUri, _requestCode);
	}

	private boolean checkPermissionsAndRequestIfNeeded() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
				&& ActivityCompat.checkSelfPermission(_fragment.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {
			_fragment.requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
					PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
			return false;
		}
		return true;
	}

	public interface Callback {
		void onImageChosen(Uri imageUri, int requestCode);

		void onVideoChosen(Uri videoUri, int requestCode);

		void onCancel();
	}
}
