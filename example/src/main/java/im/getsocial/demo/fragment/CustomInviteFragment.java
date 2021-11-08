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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
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
import im.getsocial.demo.utils.VideoUtils;
import im.getsocial.sdk.invites.InviteContent;
import im.getsocial.sdk.invites.InviteTextPlaceholders;
import im.getsocial.sdk.invites.LinkParams;
import im.getsocial.sdk.media.MediaAttachment;
import im.getsocial.sdk.ui.GetSocialUi;
import im.getsocial.sdk.ui.invites.InviteUiCallback;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import static android.provider.MediaStore.Video.Thumbnails.MINI_KIND;
import static com.squareup.picasso.Picasso.with;

public class CustomInviteFragment extends BaseFragment {

	private static final String[] INSERT_OPTIONS = {
			InviteTextPlaceholders.PLACEHOLDER_APP_INVITE_URL,
			InviteTextPlaceholders.PLACEHOLDER_PROMO_CODE,
			InviteTextPlaceholders.PLACEHOLDER_USER_NAME
	};

	private static final int REQUEST_PICK_CUSTOM_INVITE_IMAGE = 0x1;
	private static final int REQUEST_PICK_CUSTOM_LP_IMAGE = 0x2;
	private static final int REQUEST_PICK_CUSTOM_VIDEO = 0x3;
	private static final int MAX_WIDTH = 500;
	private ViewContainer _viewContainer;
	private VideoUtils.VideoDescriptor _video;

	public CustomInviteFragment() {
		//
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
		final InviteContent inviteContent = InviteContent.createBuilder()
				.withSubject(_viewContainer._inviteSubjectInput.getText().toString())
				.withText(_viewContainer._inviteTextInput.getText().toString())
				.withMediaAttachment(createMediaAttachment())
				.build();

		final LinkParams params = createLinkParams();
		GetSocialUi.createInvitesView()
				.setCustomInviteContent(inviteContent)
				.setLinkParams(params)
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

	private MediaAttachment createMediaAttachment() {
		final Bitmap bitmap = _viewContainer._inviteImageView.getDrawable() == null ? null : ((BitmapDrawable)_viewContainer._inviteImageView.getDrawable()).getBitmap();
		final String imageUrl = _viewContainer._inviteImageUrlInput.getText().toString();
		if (bitmap != null) {
			return MediaAttachment.image(bitmap);
		} else if (_video != null) {
			return MediaAttachment.video(_video._video);
		} else if (imageUrl.trim().length() > 0) {
			return MediaAttachment.imageUrl(imageUrl);
		}
		return null;
	}

	private LinkParams createLinkParams() {
		LinkParams linkParams = new LinkParams();
		if (_viewContainer._landingPageTitle.getText().toString().length() > 0) {
			linkParams.put(LinkParams.KEY_CUSTOM_TITLE, _viewContainer._landingPageTitle.getText().toString());
		}

		if (_viewContainer._landingPageDescription.getText().toString().length() > 0) {
			linkParams.put(LinkParams.KEY_CUSTOM_DESCRIPTION, _viewContainer._landingPageDescription.getText().toString());
		}

		if (_viewContainer._landingPageImageURL.getText().toString().length() > 0) {
			linkParams.put(LinkParams.KEY_CUSTOM_IMAGE, _viewContainer._landingPageImageURL.getText().toString());
		}

		if (_viewContainer._landingPageVideoURL.getText().toString().length() > 0) {
			linkParams.put(LinkParams.KEY_CUSTOM_YOUTUBE_VIDEO, _viewContainer._landingPageVideoURL.getText().toString());
		}

		final Bitmap bitmap = _viewContainer._landingPageImageView.getDrawable() == null ? null : ((BitmapDrawable)_viewContainer._landingPageImageView.getDrawable()).getBitmap();
		if (bitmap != null) {
			linkParams.put(LinkParams.KEY_CUSTOM_IMAGE, bitmap);
		}

		for (int i = 0; i < _viewContainer._linkParamsKeys.size(); i++) {
			String key = _viewContainer._linkParamsKeys.get(i).getText().toString();
			String value = _viewContainer._linkParamsValues.get(i).getText().toString();
			if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
				linkParams.put(key, value);
			}
		}
		return linkParams;
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

	@Override
	public String getTitle() {
		return "Send Custom Invite";
	}

	@Override
	public String getFragmentTag() {
		return "custominvite";
	}

	@Override
	protected void onImagePickedFromDevice(Uri imageUri, int requestCode) {
		ImageView imageView;
		if (requestCode == REQUEST_PICK_CUSTOM_INVITE_IMAGE) {
				imageView = _viewContainer._inviteImageView;
				_viewContainer._inviteImageView.setVisibility(View.VISIBLE);
				_viewContainer._buttonRemoveInviteImage.setVisibility(View.VISIBLE);
		} else {
			imageView = _viewContainer._landingPageImageView;
			_viewContainer._landingPageImageView.setVisibility(View.VISIBLE);
			_viewContainer._buttonRemoveImage.setVisibility(View.VISIBLE);
		}
		with(getContext())
				.load(imageUri)
				.resize(MAX_WIDTH, 0)
				.memoryPolicy(MemoryPolicy.NO_CACHE)
				.into(imageView);
	}

	@Override
	protected void onVideoPickedFromDevice(Uri videoUri, int requestCode) {
		if (requestCode == REQUEST_PICK_CUSTOM_VIDEO) {
			_video = VideoUtils.open(getContext(), videoUri);
			if (_video == null) {
				return;
			}
			_viewContainer._inviteVideoView.setImageBitmap(_video._thumbnail);
			_viewContainer._inviteVideoView.setVisibility(View.VISIBLE);
			_viewContainer._selectVideoButton.setVisibility(View.GONE);
			_viewContainer._removeVideoButton.setVisibility(View.VISIBLE);
		}
	}


	class ViewContainer {

		@BindView(R.id.image)
		ImageView _inviteImageView;
		@BindView(R.id.subject)
		EditText _inviteSubjectInput;
		@BindView(R.id.text)
		EditText _inviteTextInput;
		@BindView(R.id.input_invite_imageurl)
		EditText _inviteImageUrlInput;
		@BindView(R.id.buttonOpenInviteView)
		Button _buttonOpenInviteView;

		@BindView(R.id.landingPageImage)
		ImageView _landingPageImageView;

		@BindView(R.id.landingPageTitle)
		EditText _landingPageTitle;
		@BindView(R.id.landingPageDescription)
		EditText _landingPageDescription;
		@BindView(R.id.landingPageImageURL)
		EditText _landingPageImageURL;
		@BindView(R.id.landingPageVideoURL)
		EditText _landingPageVideoURL;

		@BindView(R.id.button_select_image)
		Button _buttonSelectLandingPageImage;
		@BindView(R.id.button_use_same_image)
		Button _buttonUseSameImage;
		@BindView(R.id.button_remove_image)
		Button _buttonRemoveImage;


		@BindViews({R.id.key1, R.id.key2, R.id.key3})
		List<EditText> _linkParamsKeys;
		@BindViews({R.id.value1, R.id.value2, R.id.value3})
		List<EditText> _linkParamsValues;

		@BindView(R.id.video)
		ImageView _inviteVideoView;
		@BindView(R.id.select_video)
		Button _selectVideoButton;
		@BindView(R.id.remove_video)
		Button _removeVideoButton;

		@BindView(R.id.button_select_invite_image)
		Button _buttonSelectInviteImage;

		@BindView(R.id.button_remove_invite_image)
		Button _buttonRemoveInviteImage;

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
			_buttonSelectInviteImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					pickImageFromDevice(REQUEST_PICK_CUSTOM_INVITE_IMAGE);
				}
			});
			_buttonRemoveInviteImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					_inviteImageView.setImageDrawable(null);
					_inviteImageView.setVisibility(View.GONE);
					_buttonRemoveInviteImage.setVisibility(View.GONE);
				}
			});
			_selectVideoButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickVideoFromDevice(REQUEST_PICK_CUSTOM_VIDEO);
				}
			});
			_removeVideoButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					_video = null;
					_inviteVideoView.setImageDrawable(null);
					_inviteVideoView.setVisibility(View.GONE);
					_removeVideoButton.setVisibility(View.GONE);
					_selectVideoButton.setVisibility(View.VISIBLE);
				}
			});


			_buttonSelectLandingPageImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pickImageFromDevice(REQUEST_PICK_CUSTOM_LP_IMAGE);
				}
			});
			_buttonUseSameImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (_inviteImageView.getDrawable() != null) {
						_landingPageImageView.setImageDrawable(_inviteImageView.getDrawable());
						_landingPageImageView.setVisibility(View.VISIBLE);
						_buttonRemoveImage.setVisibility(View.VISIBLE);
					}
				}
			});
			_buttonRemoveImage.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					_landingPageImageView.setImageDrawable(null);
					_landingPageImageView.setVisibility(View.GONE);
					_buttonRemoveImage.setVisibility(View.GONE);
				}
			});
		}
	}
}