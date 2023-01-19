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

import static com.squareup.picasso.Picasso.with;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import im.getsocial.demo.R;
import im.getsocial.demo.ui.PickActionView;
import im.getsocial.demo.utils.DynamicUi;
import im.getsocial.demo.utils.VideoUtils;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.Communities;
import im.getsocial.sdk.FailureCallback;
import im.getsocial.sdk.GetSocialError;
import im.getsocial.sdk.actions.Action;
import im.getsocial.sdk.communities.ActivitiesQuery;
import im.getsocial.sdk.communities.ActivityButton;
import im.getsocial.sdk.communities.ActivityContent;
import im.getsocial.sdk.communities.GetSocialActivity;
import im.getsocial.sdk.communities.PostActivityTarget;
import im.getsocial.sdk.communities.UserId;
import im.getsocial.sdk.media.MediaAttachment;
import im.getsocial.sdk.ui.communities.ActivityDetailsViewBuilder;
import im.getsocial.sdk.ui.communities.ActivityFeedViewBuilder;

public class PostActivityFragment extends BaseFragment implements Callback<GetSocialActivity>, FailureCallback {

    private static final int MAX_WIDTH = 1024;
    private static final int REQUEST_PICK_CUSTOM_IMAGE = 0x1;
    private static final int REQUEST_PICK_CUSTOM_VIDEO = 0x2;

    private ViewContainer _viewContainer;
    private VideoUtils.VideoDescriptor _videoDescriptor;
    private Bitmap _originalImage;
    @Nullable
    private String _activityId;

    @Nullable
    private String _postTopic;

    @Nullable
    private String _postGroup;

    @Nullable
    private String _postComment;

    @Nullable
    private MediaAttachment _videoAttachment;
    @Nullable
    private MediaAttachment _imageAttachment;

    final List<DynamicUi.DynamicInputHolder> _propertiesHolder = new ArrayList<>();

    public static Fragment updateActivity(final String activityId) {
        final PostActivityFragment fragment = new PostActivityFragment();
        final Bundle args = new Bundle();
        args.putString("activity", activityId);
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment postToTopic(final String topicId) {
        final PostActivityFragment fragment = new PostActivityFragment();
        final Bundle args = new Bundle();
        args.putString("topic", topicId);
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment postToGroup(final String id) {
        final PostActivityFragment fragment = new PostActivityFragment();
        final Bundle args = new Bundle();
        args.putString("group", id);
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment postComment(final String id) {
        final PostActivityFragment fragment = new PostActivityFragment();
        final Bundle args = new Bundle();
        args.putString("comment", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_activity, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _viewContainer = new ViewContainer(view);
        final Bundle bundle = getArguments();
        if (bundle != null) {
            _activityId = bundle.getString("activity");

            if (_activityId != null) {
                Communities.getActivity(_activityId, post -> {
                    _viewContainer.setPost(post);
                }, this::onFailure);
            }
            _postTopic = bundle.getString("topic");
            _postGroup = bundle.getString("group");
            _postComment = bundle.getString("comment");
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
        showLoading("Posting activity", "Wait...");
        _viewContainer._image.setEnabled(false);

        final String text = _viewContainer._postText.getText().toString();

        final String buttonTitle = _viewContainer._buttonTitle.getText().toString();
        final Action action = _viewContainer._pickActionView.getAction();

        final boolean hasText = !TextUtils.isEmpty(text);
        final boolean hasImage = _viewContainer._hasImage.isChecked() && _originalImage != null || _imageAttachment != null;
        final boolean hasVideo = _viewContainer._hasVideo.isChecked() && _videoDescriptor != null || _videoAttachment != null;
        final boolean hasButton = _viewContainer._hasButton.isChecked()
                && !buttonTitle.isEmpty()
                && (action != null);

        if (!hasText && !hasButton && !hasImage && !hasVideo) {
            hideLoadingAndShowError("Can not post activity without any data");
            _viewContainer._image.setEnabled(true);
            return;
        }

        final ActivityContent builder = new ActivityContent();
        if (hasText) {
            builder.withText(text);
        }
        if (hasButton) {
            builder.withButton(ActivityButton.create(buttonTitle, action));
        }
        if (hasImage) {
            builder.addAttachment(_imageAttachment != null ? _imageAttachment : MediaAttachment.image(_originalImage));
        }
        if (hasVideo) {
            builder.addAttachment(_videoAttachment != null ? _videoAttachment : MediaAttachment.video(_videoDescriptor._video));
        }
        for (final DynamicUi.DynamicInputHolder property : _propertiesHolder) {
            builder.addProperty(property.getText(0), property.getText(1));
        }
        String labelsText = _viewContainer._labels.getText().toString();
        if (!labelsText.isEmpty()) {
            builder.addLabels(Arrays.asList(labelsText.split(",")));
        }

        if (_activityId != null) {
            showLoading("Updating activity", "Wait...");
            Communities.updateActivity(_activityId, builder, this, this);
            return;
        }
        showLoading("Posting activity", "Wait...");
        Communities.postActivity(builder, target(), this, this);
    }

    private PostActivityTarget target() {
        if (_postTopic != null) {
            return PostActivityTarget.topic(_postTopic);
        }
        if (_postGroup != null) {
            return PostActivityTarget.group(_postGroup);
        }
        if (_postComment != null) {
            return PostActivityTarget.comment(_postComment);
        }
        return PostActivityTarget.timeline();
    }

    private ActivitiesQuery query() {
        if (_postTopic != null) {
            return ActivitiesQuery.activitiesInTopic(_postTopic);
        }
        if (_postGroup != null) {
            return ActivitiesQuery.activitiesInGroup(_postGroup);
        }
        return ActivitiesQuery.feedOf(UserId.currentUser());
    }

    @Override
    public void onSuccess(final GetSocialActivity activity) {
        hideLoading();
        _viewContainer._image.setEnabled(true);

        Toast.makeText(getContext(), "Activity was successfully posted", Toast.LENGTH_SHORT).show();

        if (_activityId != null) {
            ActivityDetailsViewBuilder.create(_activityId)
                    .setActionListener(_activityListener.dependencies().actionListener())
                    .setShowActivityFeedView(true).show();
            return;
        }

        if (_postComment == null) {
            ActivityFeedViewBuilder.create(query())
                    .setActionListener(_activityListener.dependencies().actionListener())
                    .show();
        }
    }

    @Override
    public void onFailure(final GetSocialError exception) {
        _viewContainer._image.setEnabled(true);
        hideLoadingAndShowError(exception.getMessage());
    }

    private void hideLoadingAndShowError(final String message) {
        hideLoading();
        Toast.makeText(getContext(), "Failed to post activity: " + message, Toast.LENGTH_SHORT).show();
    }

    private void loadOriginalImage(final Uri imageUri) {
        new Thread(() -> {
            try {
                _originalImage = Picasso.with(getContext()).load(imageUri).get();
            } catch (final IOException e) {
                _viewContainer._image = null;
                Toast.makeText(getContext(), "Could not load original image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).start();
    }

    @Override
    protected void onImagePickedFromDevice(final Uri imageUri, final int requestCode) {
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
    protected void onVideoPickedFromDevice(final Uri videoUri, final int requestCode) {
        if (requestCode == REQUEST_PICK_CUSTOM_VIDEO) {
            _videoDescriptor = VideoUtils.open(getContext(), videoUri);
            if (_videoDescriptor == null) {
                return;
            }
            _viewContainer._video.setImageBitmap(_videoDescriptor._thumbnail);
            _viewContainer._video.setVisibility(View.VISIBLE);
            _viewContainer._selectVideo.setVisibility(View.GONE);
        }
    }

    class ViewContainer {

        @BindView(R.id.pick_action_view)
        PickActionView _pickActionView;
        @BindView(R.id.activity_context_text)
        EditText _postText;
        @BindView(R.id.checkbox_has_button)
        CheckBox _hasButton;
        @BindView(R.id.button_container)
        LinearLayout _buttonDataContainer;
        @BindView(R.id.input_button_title)
        EditText _buttonTitle;
        @BindView(R.id.checkbox_has_image)
        CheckBox _hasImage;
        @BindView(R.id.checkbox_has_video)
        CheckBox _hasVideo;
        @BindView(R.id.image_view_post_image)
        ImageView _image;
        @BindView(R.id.image_view_post_video)
        ImageView _video;
        @BindView(R.id.properties)
        LinearLayout _properties;
        @BindView(R.id.select_video)
        Button _selectVideo;
        @BindView(R.id.post_activity_labels)
        EditText _labels;

        ViewContainer(final View view) {
            ButterKnife.bind(this, view);
        }

        private void createRow(final String k, final String v) {
            final DynamicUi.DynamicInputHolder inputHolder = DynamicUi.createDynamicTextRow(getContext(), _properties, _propertiesHolder, "Key", "Value");
            final EditText key = inputHolder.getView(0);
            final EditText val = inputHolder.getView(1);
            key.setText(k);
            val.setText(v);
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
        void toggleHasButton(final boolean hasButton) {
            _buttonDataContainer.setVisibility(hasButton ? View.VISIBLE : View.GONE);
        }

        @OnCheckedChanged(R.id.checkbox_has_image)
        void toggleHasImage(final boolean hasImage) {
            if (!hasImage) {
                _imageAttachment = null;
            }
            _image.setVisibility(hasImage ? View.VISIBLE : View.GONE);
            _originalImage = hasImage ? ((BitmapDrawable) _image.getDrawable()).getBitmap() : null;
        }

        @OnCheckedChanged(R.id.checkbox_has_video)
        void toggleHasVideo(final boolean hasVideo) {
            if (!hasVideo) {
                _video.setImageDrawable(null);
                _videoDescriptor = null;
                _video.setVisibility(View.GONE);
                _videoAttachment = null;
            }
            _selectVideo.setVisibility(hasVideo ? View.VISIBLE : View.GONE);
        }

        @OnClick(R.id.add_property)
        void addProperty() {
            createRow("", "");
        }

        public void setPost(final GetSocialActivity post) {
            _postText.setText(post.getText());
            final ActivityButton button = post.getButton();
            if (button != null) {
                _hasButton.setChecked(true);
                _buttonTitle.setText(button.getTitle());
                _pickActionView.setAction(button.getAction());
            } else {
                _hasButton.setChecked(false);
            }
            for (final MediaAttachment attachment : post.getAttachments()) {
                if (attachment.getVideoUrl() != null) {
                    _hasVideo.setChecked(true);
                    Picasso.with(getContext()).load(attachment.getImageUrl()).into(_video);
                    _video.setVisibility(View.VISIBLE);
                    _videoAttachment = attachment;
                } else if (attachment.getImageUrl() != null) {
                    _hasImage.setChecked(true);
                    Picasso.with(getContext()).load(attachment.getImageUrl()).into(_image);
                    _imageAttachment = attachment;
                }
            }
            for (final Map.Entry<String, String> property : post.getProperties().entrySet()) {
                createRow(property.getKey(), property.getValue());
            }
            _labels.setText(TextUtils.join(",", post.getLabels()));
        }
    }
}
