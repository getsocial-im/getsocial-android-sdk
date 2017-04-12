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
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import com.squareup.picasso.MemoryPolicy;
import im.getsocial.demo.R;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialException;
import im.getsocial.sdk.activities.ActivityPost;
import im.getsocial.sdk.activities.ActivityPostContent;
import im.getsocial.sdk.ui.GetSocialUi;
import im.getsocial.sdk.ui.activities.ActionButtonListener;
import im.getsocial.sdk.ui.activities.ActivityFeedViewBuilder;

import static com.squareup.picasso.Picasso.with;

public class PostActivityFragment extends BaseFragment implements Callback<ActivityPost> {

	private static final String KEY_TMP_IMAGE_PATH = "GetSocial_ImagePath_Key";
	private static final int REQUEST_PICK_IMAGE_ACTIVITY = 1984;
	private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2012;

	private static final int MAX_WIDTH = 1024;

	private ViewContainer _viewContainer;
	private Uri _imageUri;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_post_activity, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		_viewContainer = new ViewContainer(view);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (_imageUri != null) {
			outState.putString(KEY_TMP_IMAGE_PATH, _imageUri.toString());
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		if (savedInstanceState != null) {
			_imageUri = savedInstanceState.containsKey(KEY_TMP_IMAGE_PATH)
					? Uri.parse(savedInstanceState.getString(KEY_TMP_IMAGE_PATH))
					: null;
			if (_imageUri != null) {
				handlePickedImage(_imageUri);
			}
		}
	}

	@Override
	public String getFragmentTag() {
		return "postactivity";
	}

	@Override
	public String getTitle() {
		return "Post Activity";
	}

	private void doPost() {
		final String text = _viewContainer._postText.getText().toString();

		final Bitmap bitmap = _viewContainer._image.getDrawable() == null ? null : ((BitmapDrawable)_viewContainer._image.getDrawable()).getBitmap();

		final String buttonTitle = _viewContainer._buttonTitle.getText().toString();
		final String buttonAction = _viewContainer._buttonAction.getText().toString();

		boolean hasText = !TextUtils.isEmpty(text);
		boolean hasImage = _viewContainer._hasImage.isChecked() && bitmap != null;
		boolean hasButton = _viewContainer._hasButton.isChecked()
				&& !buttonTitle.isEmpty()
				&& !buttonAction.isEmpty();

		if (!hasText && !hasButton && !hasImage) {
			hideLoadingAndShowError("Can not post activity without any data");
			return;
		}

		ActivityPostContent.Builder builder = new ActivityPostContent.Builder();
		if (hasText) {
			builder.withText(text);
		}
		if (hasImage) {
			builder.withImage(bitmap);
		}
		if (hasButton) {
			builder.withButton(buttonTitle, buttonAction);
		}
		showLoading("Posting activity", "Wait...");
		boolean postToGlobalFeed = _viewContainer._feed.getSelectedItemPosition() == 0;
		if (postToGlobalFeed) {
			GetSocial.postActivityToGlobalFeed(builder.build(), this);
		} else {
			GetSocial.postActivityToFeed(ActivitiesFragment.CUSTOM_FEED_NAME, builder.build(), this);
		}
	}

	@Override
	public void onSuccess(ActivityPost activityPost) {
		hideLoading();

		Toast.makeText(getContext(), "Activity was successfully posted", Toast.LENGTH_SHORT).show();

		boolean postToGlobalFeed = _viewContainer._feed.getSelectedItemPosition() == 0;
		ActivityFeedViewBuilder feedView = postToGlobalFeed ? GetSocialUi.createGlobalActivityFeedView() : GetSocialUi.createActivityFeedView(ActivitiesFragment.CUSTOM_FEED_NAME);

		feedView.withButtonActionListener(new ActionButtonListener() {
			@Override
			public void onButtonClicked(String action, ActivityPost post) {
				Toast.makeText(getContext(), "Activity Feed button clicked, action: " + action, Toast.LENGTH_SHORT).show();
			}
		}).show();
	}

	@Override
	public void onFailure(GetSocialException exception) {
		hideLoadingAndShowError(exception.getMessage());
	}

	private void hideLoadingAndShowError(String message) {
		hideLoading();
		Toast.makeText(getContext(), "Failed to post activity: " + message, Toast.LENGTH_SHORT).show();
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

	private void handlePickedImage(final Uri imageUri) {
		_imageUri = imageUri;
		if (!checkPermissionsAndRequestIfNeeded()) {
			return;
		}
		with(getContext())
				.load(_imageUri)
				.resize(MAX_WIDTH, 0)
				.memoryPolicy(MemoryPolicy.NO_CACHE)
				.into(_viewContainer._image);
	}

	private void pickImageFromDevice() {
		Intent imagePickerIntent = new Intent(Intent.ACTION_PICK);
		imagePickerIntent.setType("image/*");
		startActivityForResult(imagePickerIntent, REQUEST_PICK_IMAGE_ACTIVITY);
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


	class ViewContainer {

		@BindView(R.id.activity_context_text)
		EditText _postText;
		@BindView(R.id.checkbox_has_button)
		CheckBox _hasButton;
		@BindView(R.id.button_container)
		LinearLayout _buttonDataContainer;
		@BindView(R.id.input_button_title)
		EditText _buttonTitle;
		@BindView(R.id.input_button_action)
		EditText _buttonAction;
		@BindView(R.id.checkbox_has_image)
		CheckBox _hasImage;
		@BindView(R.id.image_view_post_image)
		ImageView _image;
		@BindView(R.id.selector_feed)
		Spinner _feed;

		ViewContainer(View view) {
			ButterKnife.bind(this, view);

			ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
					R.array.activityFeedsItems, android.R.layout.simple_spinner_item);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			_feed.setAdapter(adapter);
		}

		@OnClick(R.id.button_post)
		void postActivity() {
			doPost();
		}

		@OnClick(R.id.image_view_post_image)
		void changeImage() {
			pickImageFromDevice();
		}

		@OnCheckedChanged(R.id.checkbox_has_button)
		void toggleHasButton(boolean hasButton) {
			_buttonDataContainer.setVisibility(hasButton ? View.VISIBLE : View.GONE);
		}

		@OnCheckedChanged(R.id.checkbox_has_image)
		void toggleHasImage(boolean hasImage) {
			_image.setVisibility(hasImage ? View.VISIBLE : View.GONE);
		}

	}
}
