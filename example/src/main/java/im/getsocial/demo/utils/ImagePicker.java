package im.getsocial.demo.utils;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import javax.annotation.Nullable;

/**
 * Created by orestsavchak on 7/24/17.
 */

public class ImagePicker {


	public interface Callback {
		void onImageChosen(Uri imageUri, int requestCode);
		void onCancel();
	}

	private static final String KEY_TMP_IMAGE_PATH = "GetSocial_Key_ImagePath";
	private static final int REQUEST_PICK_IMAGE_ACTIVITY = 1984;
	private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2012;

	private final int _requestCode;
	private final Fragment _fragment;

	private Callback _callback;
	private Uri _imageUri;

	public ImagePicker(Fragment fragment, int requestCode) {
		_fragment = fragment;
		_requestCode = requestCode;
	}

	public void onSaveInstanceState(Bundle outState) {
		if (_imageUri != null) {
			outState.putString(KEY_TMP_IMAGE_PATH, _imageUri.toString());
		}
	}

	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			_imageUri = savedInstanceState.containsKey(KEY_TMP_IMAGE_PATH)
					? Uri.parse(savedInstanceState.getString(KEY_TMP_IMAGE_PATH))
					: null;
			if (_imageUri != null) {
				handlePickedImage(_imageUri);
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
		}
		return false;
	}

	public boolean onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode == PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
			for (int permission : grantResults) {
				if (permission == PackageManager.PERMISSION_GRANTED) {
					handlePickedImage(_imageUri);
					return true;
				}
			}
			_callback.onCancel();
			return true;
		}
		return false;
	}

	public void pickImageFromDevice(Callback callback) {
		_callback = callback;
		Intent imagePickerIntent = new Intent(Intent.ACTION_PICK);
		imagePickerIntent.setType("image/*");
		_fragment.startActivityForResult(imagePickerIntent, REQUEST_PICK_IMAGE_ACTIVITY);
	}

	private void handlePickedImage(final Uri imageUri) {
		_imageUri = imageUri;
		if (!checkPermissionsAndRequestIfNeeded()) {
			return;
		}
		_callback.onImageChosen(_imageUri, _requestCode);
	}

	private boolean checkPermissionsAndRequestIfNeeded() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
				&& ActivityCompat.checkSelfPermission(_fragment.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {
			_fragment.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
					PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
			return false;
		}
		return true;
	}
}
