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

package im.getsocial.demo.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import com.squareup.picasso.MemoryPolicy;
import im.getsocial.demo.R;
import im.getsocial.sdk.invites.CustomReferralData;
import im.getsocial.sdk.invites.InviteContent;
import im.getsocial.sdk.invites.InviteTextPlaceholders;
import im.getsocial.sdk.ui.GetSocialUi;
import im.getsocial.sdk.ui.invites.InviteUiCallback;

import java.util.List;

import static com.squareup.picasso.Picasso.with;

public class CustomInviteFragment extends BaseFragment {

	private static final String[] INSERT_OPTIONS = {
			InviteTextPlaceholders.PLACEHOLDER_APP_INVITE_URL
	};

	private static final int REQUEST_PICK_IMAGE_ACTIVITY = 1984;
	private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2012;
	private static final int MAX_WIDTH = 500;

	private ViewContainer _viewContainer;
	private Uri _imageUri;

	public CustomInviteFragment() {
	}

	private void pickImageFromDevice() {
		Intent imagePickerIntent = new Intent(Intent.ACTION_PICK);
		imagePickerIntent.setType("image/*");
		startActivityForResult(imagePickerIntent, REQUEST_PICK_IMAGE_ACTIVITY);
	}

	private void validateInput() {
		String text = _viewContainer._inviteTextInput.getText().toString();

		if (text.isEmpty() || text.contains(InviteTextPlaceholders.PLACEHOLDER_APP_INVITE_URL)) {
			openInviteProviderList();
		} else {
			new AlertDialog.Builder(getContext())
					.setTitle(android.R.string.dialog_alert_title)
					.setMessage("No placeholder for URL found in text, would you like to continue anyway?\nWithout placeholder the invite URL will not be visible.")
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int which) {
							openInviteProviderList();
						}
					})
					.setNegativeButton(android.R.string.no, null)
					.show();
		}
	}

	private void openInviteProviderList() {
		Bitmap bitmap = ((BitmapDrawable)_viewContainer._inviteImageView.getDrawable()).getBitmap();
		InviteContent inviteContent = InviteContent.createBuilder()
				.withSubject(_viewContainer._inviteSubjectInput.getText().toString())
				.withText(_viewContainer._inviteTextInput.getText().toString())
				.withImage(bitmap)
				.build();

		GetSocialUi.createInvitesView()
				.setCustomInviteContent(inviteContent)
				.setCustomReferralData(createCustomReferralData())
				.setInviteCallback(new InviteUiCallback() {
					@Override
					public void onComplete(final String channelId) {
						// Do nothing
					}

					@Override
					public void onCancel(final String channelId) {
						// Do nothing
					}

					@Override
					public void onError(final String channelId, Throwable throwable) {
						// Do nothing
					}
				})
				.show();
	}

	private CustomReferralData createCustomReferralData() {
		CustomReferralData customReferralData = new CustomReferralData();
		for (int i = 0; i < _viewContainer._customReferralDataKeys.size(); i++) {
			String key = _viewContainer._customReferralDataKeys.get(i).getText().toString();
			String value = _viewContainer._customReferralDataValues.get(i).getText().toString();
			if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
				customReferralData.put(key, value);
			}
		}
		return customReferralData;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		switch (requestCode) {
			case REQUEST_PICK_IMAGE_ACTIVITY:
				if (data != null) {
					handlePickedImage(data.getData());
				}
				break;

			default:
				super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
				for (int permission : grantResults) {
					if (permission == PackageManager.PERMISSION_GRANTED) {
						handlePickedImage(_imageUri);
					}
				}
				break;

			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_custom_invite, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		_viewContainer = new ViewContainer(view);
	}


	private void handlePickedImage(final Uri imageUri) {
		_imageUri = imageUri;
		if (!checkPermissionsAndRequestIfNeeded()) {
			return;
		}
		with(getContext())
				.load(_imageUri)
				.resize(MAX_WIDTH, 0)
				.memoryPolicy(MemoryPolicy.NO_CACHE)
				.into(_viewContainer._inviteImageView);
	}

	private boolean checkPermissionsAndRequestIfNeeded() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
				&& ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {
			requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
					PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
			return false;
		}
		return true;
	}

	@Override
	public String getTitle() {
		return "Send Custom Invite";
	}

	@Override
	public String getFragmentTag() {
		return "custominvite";
	}

	class ViewContainer {

		@BindView(R.id.image)
		ImageView _inviteImageView;
		@BindView(R.id.subject)
		EditText _inviteSubjectInput;
		@BindView(R.id.text)
		EditText _inviteTextInput;
		@BindView(R.id.buttonOpenInviteView)
		Button _buttonOpenInviteView;

		@BindViews({R.id.key1, R.id.key2, R.id.key3})
		List<EditText> _customReferralDataKeys;
		@BindViews({R.id.value1, R.id.value2, R.id.value3})
		List<EditText> _customReferralDataValues;

		ViewContainer(View view) {

			ButterKnife.bind(this, view);

			_buttonOpenInviteView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					validateInput();
				}
			});

			View.OnLongClickListener onInsertTextLongClickListener = new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(final View view) {
					AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
							.setCancelable(true)
							.setTitle(R.string.invite_text_placeholders)
							.setItems(INSERT_OPTIONS, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									EditText editText = (EditText) view;
									editText.getText().insert(editText.getSelectionStart(), INSERT_OPTIONS[which]);
								}
							});

					AlertDialog dialog = builder.create();
					dialog.setCanceledOnTouchOutside(true);
					dialog.show();

					return true;
				}
			};

			_inviteSubjectInput.setOnLongClickListener(onInsertTextLongClickListener);
			_inviteTextInput.setOnLongClickListener(onInsertTextLongClickListener);
			_inviteImageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					pickImageFromDevice();
				}
			});
		}
	}
}
