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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.squareup.picasso.Picasso;
import im.getsocial.demo.R;
import im.getsocial.demo.utils.VideoUtils;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialException;
import im.getsocial.sdk.activities.ActivityPost;
import im.getsocial.sdk.activities.ActivityPostContent;
import im.getsocial.sdk.ui.GetSocialUi;
import im.getsocial.sdk.ui.UiAction;
import im.getsocial.sdk.ui.activities.ActionButtonListener;
import im.getsocial.sdk.ui.activities.ActivityFeedViewBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static android.provider.MediaStore.Video.Thumbnails.MINI_KIND;
import static com.squareup.picasso.Picasso.with;
import static im.getsocial.demo.utils.VideoUtils.getVideoContent;

public class PostActivityFragment extends BaseFragment implements Callback<ActivityPost> {

	private static final int MAX_WIDTH = 1024;
	private static final int REQUEST_PICK_CUSTOM_IMAGE = 0x1;
	private static final int REQUEST_PICK_CUSTOM_VIDEO = 0x2;

	private ViewContainer _viewContainer;
	private String _videoPath;
	private Bitmap _originalImage;

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
	public String getFragmentTag() {
		return "postactivity";
	}

	@Override
	public String getTitle() {
		return "Post Activity";
	}

	private void doPost() {
		showLoading("Posting activity", "Wait...");
		_viewContainer._image.setEnabled(false);

		final String text = _viewContainer._postText.getText().toString();

		final Bitmap bitmap = _originalImage == null ? null : _originalImage;

		final String buttonTitle = _viewContainer._buttonTitle.getText().toString();
		final String buttonAction = _viewContainer._buttonAction.getText().toString();

		boolean hasText = !TextUtils.isEmpty(text);
		boolean hasImage = _viewContainer._hasImage.isChecked() && bitmap != null;
		boolean hasVideo = _viewContainer._hasVideo.isChecked() && _videoPath != null;
		boolean hasButton = _viewContainer._hasButton.isChecked()
				&& !buttonTitle.isEmpty()
				&& !buttonAction.isEmpty();

		if (!hasText && !hasButton && !hasImage && !hasVideo) {
			hideLoadingAndShowError("Can not post activity without any data");
			_viewContainer._image.setEnabled(true);
			return;
		}

		boolean postToGlobalFeed = _viewContainer._feed.getSelectedItemPosition() == 0;
		if (postToGlobalFeed && GetSocial.User.isAnonymous()) {
			_viewContainer._image.setEnabled(true);
			hideLoading();
			showAuthorizeUserDialogForPendingAction("Post to global feed", new UiAction.Pending() {
				@Override
				public void proceed() {
					doPost();
				}
			});
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
		if (hasVideo) {
			builder.withVideo(VideoUtils.getVideoContent(_videoPath));
		}
		showLoading("Posting activity", "Wait...");

		if (postToGlobalFeed) {
			GetSocial.postActivityToGlobalFeed(builder.build(), this);
		} else {
			GetSocial.postActivityToFeed(ActivitiesFragment.CUSTOM_FEED_NAME, builder.build(), this);
		}
	}

	@Override
	public void onSuccess(ActivityPost activityPost) {
		hideLoading();
		_viewContainer._image.setEnabled(true);

		Toast.makeText(getContext(), "Activity was successfully posted", Toast.LENGTH_SHORT).show();

		boolean postToGlobalFeed = _viewContainer._feed.getSelectedItemPosition() == 0;
		ActivityFeedViewBuilder feedView = postToGlobalFeed ? GetSocialUi.createGlobalActivityFeedView() : GetSocialUi.createActivityFeedView(ActivitiesFragment.CUSTOM_FEED_NAME);

		feedView.setButtonActionListener(new ActionButtonListener() {
			@Override
			public void onButtonClicked(String action, ActivityPost post) {
				Toast.makeText(getContext(), "Activity Feed button clicked, action: " + action, Toast.LENGTH_SHORT).show();
			}
		}).show();
	}

	@Override
	public void onFailure(GetSocialException exception) {
		_viewContainer._image.setEnabled(true);
		hideLoadingAndShowError(exception.getMessage());
	}

	private void hideLoadingAndShowError(String message) {
		hideLoading();
		Toast.makeText(getContext(), "Failed to post activity: " + message, Toast.LENGTH_SHORT).show();
	}

	private void loadOriginalImage(final Uri imageUri) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					_originalImage = Picasso.with(getContext()).load(imageUri).get();
				} catch (IOException e) {
					_viewContainer._image = null;
					Toast.makeText(getContext(), "Could not load original image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		}).start();
	}

	@Override
	protected void onImagePickedFromDevice(Uri imageUri, int requestCode) {
		if (requestCode == REQUEST_PICK_CUSTOM_IMAGE) {
			with(getContext())
					.load(imageUri)
					.resize(MAX_WIDTH, 0)
					.memoryPolicy(MemoryPolicy.NO_CACHE)
					.into(_viewContainer._image);
			loadOriginalImage(imageUri);
		}
	}

	@Override
	protected void onVideoPickedFromDevice(Uri videoUri, int requestCode) {
		if (requestCode == REQUEST_PICK_CUSTOM_VIDEO) {
			String realPath;
			if (videoUri.getScheme().equalsIgnoreCase("content")) {
				realPath = VideoUtils.getRealPathFromUri(getContext(), videoUri);
			} else {
				realPath = videoUri.getPath();
			}
			File f = new File(realPath);
			if (f.exists()) {
				_videoPath = realPath;
				Bitmap thumbnail = null;
				if (realPath.endsWith("gif")) {
					try {
						FileInputStream is = new FileInputStream(realPath);
						thumbnail = BitmapFactory.decodeStream(is);
					} catch(Exception e) {
						e.printStackTrace();
					}
				} else {
					thumbnail = ThumbnailUtils.createVideoThumbnail(_videoPath, MINI_KIND);
				}
				_viewContainer._video.setImageBitmap(thumbnail);
				_viewContainer._video.setVisibility(View.VISIBLE);
				_viewContainer._selectVideo.setVisibility(View.GONE);
			}
		}

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
		@BindView(R.id.checkbox_has_video)
		CheckBox _hasVideo;
		@BindView(R.id.image_view_post_image)
		ImageView _image;
		@BindView(R.id.image_view_post_video)
		ImageView _video;
		@BindView(R.id.selector_feed)
		Spinner _feed;
		@BindView(R.id.select_video)
		Button _selectVideo;

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
			pickImageFromDevice(REQUEST_PICK_CUSTOM_IMAGE);
		}

		@OnClick(R.id.select_video)
		void changeVideo() {
			pickVideoFromDevice(REQUEST_PICK_CUSTOM_VIDEO);
		}

		@OnCheckedChanged(R.id.checkbox_has_button)
		void toggleHasButton(boolean hasButton) {
			_buttonDataContainer.setVisibility(hasButton ? View.VISIBLE : View.GONE);
		}

		@OnCheckedChanged(R.id.checkbox_has_image)
		void toggleHasImage(boolean hasImage) {
			if (hasImage) {
				_videoPath = null;
				_hasVideo.setChecked(false);
			}
			_image.setVisibility(hasImage ? View.VISIBLE : View.GONE);
			_originalImage = hasImage ? ((BitmapDrawable)_image.getDrawable()).getBitmap() : null;
		}

		@OnCheckedChanged(R.id.checkbox_has_video)
		void toggleHasVideo(boolean hasVideo) {
			if (hasVideo) {
				_hasImage.setChecked(false);
			} else {
				_video.setImageDrawable(null);
				_videoPath = null;
				_video.setVisibility(View.GONE);
			}
			_selectVideo.setVisibility(hasVideo ? View.VISIBLE : View.GONE);
		}

	}
}
