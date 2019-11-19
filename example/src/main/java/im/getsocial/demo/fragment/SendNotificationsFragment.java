package im.getsocial.demo.fragment;

import android.util.Log;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.Toast;
import butterknife.OnItemSelected;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import im.getsocial.demo.R;
import im.getsocial.demo.ui.PickActionView;
import im.getsocial.demo.utils.DynamicUi;
import im.getsocial.demo.utils.VideoUtils;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialException;
import im.getsocial.sdk.actions.Action;
import im.getsocial.sdk.pushnotifications.ActionButton;
import im.getsocial.sdk.media.MediaAttachment;
import im.getsocial.sdk.pushnotifications.NotificationContent;
import im.getsocial.sdk.pushnotifications.NotificationCustomization;
import im.getsocial.sdk.pushnotifications.NotificationsSummary;
import im.getsocial.sdk.pushnotifications.SendNotificationPlaceholders;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.provider.MediaStore.Video.Thumbnails.MINI_KIND;
import static com.squareup.picasso.Picasso.with;

public class SendNotificationsFragment extends BaseFragment implements Callback<NotificationsSummary> {
	private static final int REQUEST_PICK_NOTIFICATION_IMAGE = 0x1;
	private static final int REQUEST_PICK_NOTIFICATION_VIDEO = 0x2;
	private static final int MAX_WIDTH = 500;

	private ViewContainer _viewContainer;
	private byte[] _video;
	private Bitmap _image;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_send_notification, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		_viewContainer = new ViewContainer(view);
	}

	@Override
	public String getFragmentTag() {
		return "send_notifications";
	}

	@Override
	public String getTitle() {
		return "Send notifications";
	}

	private void sendNotification() {
		final Action action = _viewContainer._pickActionView.getAction();
		final String templateName = templateName();
		final String text = customText();

		// if no template specified and selected "friend request" - create everything automatically
		if (templateName.isEmpty() && _viewContainer._pickActionView.isAddFriendRequest()) {
			final NotificationContent notificationContent =
					NotificationContent.notificationWithText(SendNotificationPlaceholders.CustomText.SENDER_DISPLAY_NAME + " wants to become friends")
							.withTitle("Friend request")
							.withAction(_viewContainer._pickActionView.getAction())
							.addActionButtons(actionButtons());
			showLoading("Sending notification","Wait please...");
			GetSocial.User.sendNotification(userIds(), notificationContent, this);
			return;
		}

		if (templateName.isEmpty() && text.isEmpty()) {
			_log.logErrorAndToast("Custom text or template name required!");
			return;
		}

		final NotificationContent notificationContent = text.isEmpty()
				? NotificationContent.notificationFromTemplate(templateName)
				: NotificationContent.notificationWithText(text);

		if (!customTitle().isEmpty()) {
			notificationContent.withTitle(customTitle());
		}

		if (!templateName.isEmpty()) {
			notificationContent.withTemplateName(templateName);
			notificationContent.addTemplatePlaceholders(templateData());
		}
		if (!text.isEmpty()) {
			notificationContent.withText(text);
		}

		if (action != null) {
			notificationContent.withAction(action);
		}

		MediaAttachment attachment = getMediaAttachment();
		if (attachment != null) {
			notificationContent.withMediaAttachment(attachment);
		}

		// set customization
		NotificationCustomization customization = NotificationCustomization
				.withBackgroundImageConfiguration(backgroundImageUrl())
				.withTitleColor(titleColor())
				.withTextColor(textColor());
		notificationContent.withCustomization(customization);

		notificationContent.addActionButtons(actionButtons());

		switch (_viewContainer._badgeChangeMode.getSelectedItemPosition()) {
			case 1:
				notificationContent.withBadge(NotificationContent.Badge.increaseBy(badgeCount()));
				break;
			case 2:
				notificationContent.withBadge(NotificationContent.Badge.setTo(badgeCount()));
				break;
			default:
				break;
		}

		showLoading("Sending notification","Wait please...");
		GetSocial.User.sendNotification(userIds(), notificationContent, this);
	}

	private int badgeCount() {
		String badgeCountString = _viewContainer._badgeChange.getText().toString();
		try {
			return Integer.parseInt(badgeCountString);
		} catch (NumberFormatException ignored) {
			return 0;
		}
	}

	@Nullable
	private MediaAttachment getMediaAttachment() {
		if (_image != null) {
			return MediaAttachment.image(_image);
		}
		if (_video != null) {
			return MediaAttachment.video(_video);
		}
		if (!customImageUrl().isEmpty()) {
			return MediaAttachment.imageUrl(customImageUrl());
		}
		if (!customVideoUrl().isEmpty()) {
			return MediaAttachment.videoUrl(customVideoUrl());
		}
		return null;
	}

	@Override
	public void onSuccess(NotificationsSummary result) {
		hideLoading();
		_log.logInfoAndToast("Successfully sent " + result.getSuccessfullySentCount() + " notifications");
	}

	@Override
	public void onFailure(GetSocialException exception) {
		hideLoading();
		_log.logErrorAndToast("Failed to send notification: " + exception.getMessage());
	}

	private List<String> userIds() {
		final List<String> userIds = new ArrayList<>();
		for (DynamicUi.DynamicInputHolder inputHolder : _viewContainer._userIds) {
			userIds.add(inputHolder.getText(0));
		}
		for (int i = 0; i < placeholders().length; i++) {
			if (_viewContainer._recipients.get(i).isChecked()) {
				userIds.add(placeholders()[i]);
			}
		}
		return userIds;
	}
	private String templateName() {
		return _viewContainer._templateName.getText().toString();
	}

	private Map<String, String> templateData() {
		final Map<String, String> templateData = new HashMap<>();
		for (DynamicUi.DynamicInputHolder inputHolder : _viewContainer._templateData) {
			templateData.put(inputHolder.getText(0), inputHolder.getText(1));
		}
		return templateData;
	}

	private List<ActionButton> actionButtons() {
		final List<ActionButton> actionButtons = new ArrayList<>();
		for (DynamicUi.DynamicInputHolder inputHolder : _viewContainer._actionButtons) {
			actionButtons.add(ActionButton.create(inputHolder.getText(0), inputHolder.getText(1)));
		}
		return actionButtons;
	}

	private String customText() {
		return _viewContainer._notificationText.getText().toString();
	}

	private String customTitle() {
		return _viewContainer._notificationTitle.getText().toString();
	}

	private String customImageUrl() {
		return _viewContainer._imageUrl.getText().toString();
	}

	private String customVideoUrl() {
		return _viewContainer._videoUrl.getText().toString();
	}

	private String backgroundImageUrl() {
		return _viewContainer._backgroundImageUrl.getText().toString();
	}

	private String titleColor() {
		return _viewContainer._titleColor.getText().toString();
	}

	private String textColor() {
		return _viewContainer._textColor.getText().toString();
	}

	@Override
	protected void onImagePickedFromDevice(Uri imageUri, int requestCode) {
		if (requestCode == REQUEST_PICK_NOTIFICATION_IMAGE) {
			_viewContainer.setImageViewState(ViewState.SELECTED);
			_viewContainer.setVideoViewState(ViewState.HIDDEN);

			with(getContext())
					.load(imageUri)
					.resize(MAX_WIDTH, 0)
					.memoryPolicy(MemoryPolicy.NO_CACHE)
					.into(_viewContainer._imagePreview);
			loadOriginalImage(imageUri);
		}
	}

	private void loadOriginalImage(final Uri imageUri) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					_image = Picasso.with(getContext()).load(imageUri).get();
				} catch (IOException e) {
					_viewContainer._imagePreview = null;
					Toast.makeText(getContext(), "Could not load original image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		}).start();
	}

	@Override
	protected void onVideoPickedFromDevice(Uri videoUri, int requestCode) {
		if (requestCode == REQUEST_PICK_NOTIFICATION_VIDEO) {
			String realPath = VideoUtils.getRealPathFromUri(getContext(), videoUri);
			File f = new File(realPath);
			if (f.exists()) {
				Bitmap thumbnail = null;
				if (realPath.endsWith("gif")) {
					try {
						FileInputStream is = new FileInputStream(realPath);
						thumbnail = BitmapFactory.decodeStream(is);
					} catch(Exception exception) {
						exception.printStackTrace();
					}
				} else {
					thumbnail = ThumbnailUtils.createVideoThumbnail(realPath, MINI_KIND);
				}
				_viewContainer._videoPreview.setImageBitmap(thumbnail);

				_viewContainer.setImageViewState(ViewState.HIDDEN);
				_viewContainer.setVideoViewState(ViewState.SELECTED);

				_video = VideoUtils.getVideoContent(realPath);
			}
		}
	}

	private enum ViewState {
		VISIBLE, SELECTED, HIDDEN
	}

	public class ViewContainer {

		@BindViews({R.id.checkbox_friends, R.id.checkbox_referred_users, R.id.checkbox_referrer})
		List<CheckBox> _recipients;

		@BindView(R.id.pick_action_view)
		PickActionView _pickActionView;

		@BindView(R.id.container_action_buttons)
		LinearLayout _actionButtonsContainer;

		@BindView(R.id.container_user_ids)
		LinearLayout _userIdsContainer;

		@BindView(R.id.notification_title)
		EditText _notificationTitle;

		@BindView(R.id.notification_text)
		EditText _notificationText;

		@BindView(R.id.template_name)
		EditText _templateName;

		@BindView(R.id.notification_image_url)
		EditText _imageUrl;

		@BindView(R.id.notification_video_url)
		EditText _videoUrl;

		@BindView(R.id.container_template_data)
		LinearLayout _templateDataContainer;

		@BindView(R.id.button_select_image)
		Button _selectImageButton;
		@BindView(R.id.button_select_video)
		Button _selectVideoButton;

		@BindView(R.id.image_preview)
		ImageView _imagePreview;
		@BindView(R.id.video_preview)
		ImageView _videoPreview;

		@BindView(R.id.button_remove_image)
		Button _removeImageButton;
		@BindView(R.id.button_remove_video)
		Button _removeVideoButton;

		@BindView(R.id.notification_background_image_url)
		EditText _backgroundImageUrl;

		@BindView(R.id.notification_title_color)
		EditText _titleColor;

		@BindView(R.id.notification_text_color)
		EditText _textColor;

		@BindView(R.id.spinner_select_badge_change)
		Spinner _badgeChangeMode;

		@BindView(R.id.badge_change)
		EditText _badgeChange;

		final List<DynamicUi.DynamicInputHolder> _templateData = new ArrayList<>();
		final List<DynamicUi.DynamicInputHolder> _userIds = new ArrayList<>();
		final List<DynamicUi.DynamicInputHolder> _actionButtons = new ArrayList<>();

		public ViewContainer(View view) {
			ButterKnife.bind(this, view);
			_badgeChangeMode.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, new String[] {"None", "Increase by", "Set to"}));
		}

		@OnClick(R.id.button_select_image)
		void selectImageFromDevice() {
			pickImageFromDevice(REQUEST_PICK_NOTIFICATION_IMAGE);
		}

		@OnClick(R.id.button_select_video)
		void selectVideoFromDevice() {
			pickVideoFromDevice(REQUEST_PICK_NOTIFICATION_VIDEO);
		}

		@OnClick(R.id.button_remove_image)
		void removeImage() {
			setImageViewState(ViewState.VISIBLE);
			setVideoViewState(ViewState.VISIBLE);
		}

		@OnClick(R.id.button_remove_video)
		void removeVideo() {
			setImageViewState(ViewState.VISIBLE);
			setVideoViewState(ViewState.VISIBLE);
		}

		@OnItemSelected(R.id.spinner_select_badge_change)
		void onBadgeModeChanged(Spinner spinner, int position) {
			_badgeChange.setVisibility(position == 0 ? View.GONE : View.VISIBLE);
			_badgeChange.setText("");
		}

		void setVideoViewState(ViewState state) {
			Log.d("TAAAG", "Set video view state to " + state);
			_removeVideoButton.setVisibility(visibleIf(state, ViewState.SELECTED));
			_selectVideoButton.setVisibility(visibleIf(state, ViewState.VISIBLE));
			_videoPreview.setVisibility(visibleIf(state, ViewState.SELECTED));
			if (state != ViewState.SELECTED) {
				_videoPreview.setImageDrawable(null);
				_video = null;
			}
		}

		void setImageViewState(ViewState state) {
			Log.d("TAAAG", "Set image view state to " + state);
			_removeImageButton.setVisibility(visibleIf(state, ViewState.SELECTED));
			_selectImageButton.setVisibility(visibleIf(state, ViewState.VISIBLE));
			_imagePreview.setVisibility(visibleIf(state, ViewState.SELECTED));
			if (state != ViewState.SELECTED) {
				_imagePreview.setImageDrawable(null);
				_image = null;
			}
		}

		private int visibleIf(ViewState currentState, ViewState visibleState) {
			return currentState == visibleState ? View.VISIBLE : View.GONE;
		}

		@OnClick(R.id.button_add_template_data)
		public void addTemplateData() {
			DynamicUi.createDynamicTextRow(getContext(), _templateDataContainer, _templateData, "Key", "Value");
		}

		@OnLongClick(R.id.notification_text)
		public boolean proposeTextPlaceholders() {
			final String[] placeholders = new String[] {
					SendNotificationPlaceholders.CustomText.RECEIVER_DISPLAY_NAME,
					SendNotificationPlaceholders.CustomText.SENDER_DISPLAY_NAME
			};
			final AlertDialog dialog = new AlertDialog.Builder(getContext())
					.setCancelable(true)
					.setTitle("Select Placeholder")
					.setItems(placeholders, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							_notificationText.getText().insert(_notificationText.getSelectionStart(), placeholders[which]);
						}
					})
					.create();

			dialog.setCanceledOnTouchOutside(true);
			dialog.show();

			return true;
		}

		@OnClick(R.id.button_add_user_id)
		public void addUserId() {
			DynamicUi.createDynamicTextRow(getContext(), _userIdsContainer, _userIds, "User ID");
		}

		@OnClick(R.id.button_add_action_button)
		public void addActionButtons() {
			DynamicUi.createDynamicTextRow(getContext(), _actionButtonsContainer, _actionButtons, "Title", "Action ID")
					.getView(1)
					.setOnLongClickListener(new View.OnLongClickListener() {
						@Override
						public boolean onLongClick(View view) {
							showActionButtonPlaceholders((EditText) view);
							return true;
						}
					});
		}

		@OnClick(R.id.button_send_notification)
		public void sendNotificationClicked() {
			sendNotification();
		}
	}

	private void showActionButtonPlaceholders(final EditText id) {
		final List<String> placeholders = Arrays.asList(ActionButton.CONSUME_ACTION, ActionButton.IGNORE_ACTION);
		final AlertDialog dialog = new AlertDialog.Builder(getContext())
				.setCancelable(true)
				.setTitle("Select Placeholder")
				.setItems(placeholders.toArray(new String[placeholders.size()]), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						id.setText(placeholders.get(which));
					}
				})
				.create();
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}


	private static String[] placeholders() {
		return new String[] {
				SendNotificationPlaceholders.Receivers.FRIENDS,
				SendNotificationPlaceholders.Receivers.REFERRED_USERS,
				SendNotificationPlaceholders.Receivers.REFERRER
		};
	}
}
