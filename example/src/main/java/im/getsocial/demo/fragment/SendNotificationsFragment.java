package im.getsocial.demo.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import im.getsocial.demo.R;
import im.getsocial.demo.utils.PixelUtils;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialException;
import im.getsocial.sdk.pushnotifications.Notification;
import im.getsocial.sdk.pushnotifications.NotificationContent;
import im.getsocial.sdk.pushnotifications.NotificationsSummary;
import im.getsocial.sdk.pushnotifications.SendNotificationPlaceholders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendNotificationsFragment extends BaseFragment implements Callback<NotificationsSummary> {

	public static final int DEFAULT_ACTION = -1;

	private ViewContainer _viewContainer;

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
		if (templateName().isEmpty() && customText().isEmpty()) {
			_log.logErrorAndToast("Custom text or template name required!");
			return;
		}

		final NotificationContent notificationContent = customText().isEmpty()
				? NotificationContent.notificationFromTemplate(templateName())
				: NotificationContent.notificationWithText(customText());

		if (!customTitle().isEmpty()) {
			notificationContent.withTitle(customTitle());
		}

		if (!templateName().isEmpty()) {
			notificationContent.withTemplateName(templateName());
			notificationContent.addTemplatePlaceholders(templateData());
		}

		if (!customText().isEmpty()) {
			notificationContent.withText(customText());
		}

		if (!useDefaultAction()) {
			notificationContent.withAction(notificationAction());
		}

		notificationContent.addActionData(actionData());

		GetSocial.User.sendNotification(userIds(), notificationContent, this);
	}

	private boolean useDefaultAction() {
		return notificationAction() == DEFAULT_ACTION;
	}

	@Override
	public void onSuccess(NotificationsSummary result) {
		_log.logInfoAndToast("Successfully sent " + result.getSuccessfullySentCount() + " notifications");
	}

	@Override
	public void onFailure(GetSocialException exception) {
		_log.logErrorAndToast("Failed to send notification: " + exception.getMessage());
	}

	private List<String> userIds() {
		final List<String> userIds = new ArrayList<>();
		for (DynamicInputHolder inputHolder : _viewContainer._userIds) {
			userIds.add(inputHolder.getText(0));
		}
		for (int i = 0; i < placeholders().length; i++) {
			if (_viewContainer._recipients.get(i).isChecked()) {
				userIds.add(placeholders()[i]);
			}
		}
		return userIds;
	}

	private int notificationAction() {
		return NotificationAction.ALL[_viewContainer._selectNotificationType.getSelectedItemPosition()]._action;
	}

	private Map<String, String> actionData() {
		final Map<String, String> actionData = new HashMap<>();
		for (DynamicInputHolder inputHolder : _viewContainer._notificationData) {
			actionData.put(inputHolder.getText(0), inputHolder.getText(1));
		}
		return actionData;
	}

	private String templateName() {
		return _viewContainer._templateName.getText().toString();
	}

	private Map<String, String> templateData() {
		final Map<String, String> templateData = new HashMap<>();
		for (DynamicInputHolder inputHolder : _viewContainer._templateData) {
			templateData.put(inputHolder.getText(0), inputHolder.getText(1));
		}
		return templateData;
	}

	private String customText() {
		return _viewContainer._notificationText.getText().toString();
	}

	private String customTitle() {
		return _viewContainer._notificationTitle.getText().toString();
	}

	public class ViewContainer {

		@BindViews({R.id.checkbox_friends, R.id.checkbox_referred_users, R.id.checkbox_referrer})
		List<CheckBox> _recipients;

		@BindView(R.id.container_user_ids)
		LinearLayout _userIdsContainer;

		@BindView(R.id.spinner_select_notification_type)
		Spinner _selectNotificationType;

		@BindView(R.id.container_notification_data)
		LinearLayout _notificationDataContainer;

		@BindView(R.id.notification_title)
		EditText _notificationTitle;

		@BindView(R.id.notification_text)
		EditText _notificationText;

		@BindView(R.id.template_name)
		EditText _templateName;

		@BindView(R.id.container_template_data)
		LinearLayout _templateDataContainer;

		final List<DynamicInputHolder> _templateData = new ArrayList<>();
		final List<DynamicInputHolder> _notificationData = new ArrayList<>();
		final List<DynamicInputHolder> _userIds = new ArrayList<>();

		public ViewContainer(View view) {
			ButterKnife.bind(this, view);

			_selectNotificationType.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, NotificationAction.names()));
		}

		@OnClick(R.id.button_add_template_data)
		public void addTemplateData() {
			createDynamicTextRow(_templateDataContainer, _templateData, "Key", "Value");
		}

		@OnClick(R.id.button_add_notification_data)
		public void addNotificationData() {
			createDynamicTextRow(_notificationDataContainer, _notificationData, "Key", "Value");
		}

		@OnLongClick(R.id.notification_text)
		public boolean proposeTextPlaceholders() {
			final String[] placeholders = new String[] {
					SendNotificationPlaceholders.CustomText.RECEIVER_DISPLAY_NAME,
					SendNotificationPlaceholders.CustomText.SENDER_DISPLAY_NAME
			};
			final AlertDialog dialog = new AlertDialog.Builder(getContext())
					.setCancelable(true)
					.setTitle(R.string.invite_text_placeholders)
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
			createDynamicTextRow(_userIdsContainer, _userIds, "User ID");
		}

		@OnClick(R.id.button_send_notification)
		public void sendNotificationClicked() {
			sendNotification();
		}
	}

	private void createDynamicTextRow(final ViewGroup parentView, final List<DynamicInputHolder> holderList, String... inputs) {
		final LinearLayout linearLayout = new LinearLayout(getContext());
		linearLayout.setGravity(Gravity.CENTER_VERTICAL);
		linearLayout.setOrientation(LinearLayout.HORIZONTAL);
		linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		final List<EditText> editTexts = new ArrayList<>();
		for (String input : inputs) {
			final EditText inputText = new EditText(getContext());
			inputText.setLayoutParams(newLayoutParams(100, 40));
			inputText.setHint(input);

			linearLayout.addView(inputText);
			editTexts.add(inputText);
		}

		final DynamicInputHolder inputHolder = new DynamicInputHolder() {
			@Override
			public String getText(int position) {
				return editTexts.get(position).getText().toString();
			}
		};

		final Button remove = new Button(getContext());
		remove.setText("Remove");
		remove.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				parentView.removeView(linearLayout);
				holderList.remove(inputHolder);
			}
		});
		linearLayout.addView(remove);

		parentView.addView(linearLayout);
		holderList.add(inputHolder);
	}

	private LinearLayout.LayoutParams newLayoutParams(int width, int height) {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(PixelUtils.dp2px(getContext(), width), PixelUtils.dp2px(getContext(), height));
		params.leftMargin = PixelUtils.dp2px(getContext(), 10);
		return params;
	}

	private interface DynamicInputHolder {
		String getText(int position);
	}

	private static class NotificationAction {
		final String _name;
		final int _action;

		static final NotificationAction[] ALL = new NotificationAction[] {
				new NotificationAction("Default", DEFAULT_ACTION),
				new NotificationAction("Custom", Notification.ActionType.CUSTOM),
				new NotificationAction("Open Activity", Notification.ActionType.OPEN_ACTIVITY),
				new NotificationAction("Open Invites", Notification.ActionType.OPEN_INVITES),
				new NotificationAction("Open Profile", Notification.ActionType.OPEN_PROFILE),
				new NotificationAction("Open URL", Notification.ActionType.OPEN_URL)
		};

		private NotificationAction(String name, int action) {
			_name = name;
			_action = action;
		}

		public static List<String> names() {
			final List<String> names = new ArrayList<>();
			for (NotificationAction action : ALL) {
				names.add(action._name);
			}
			return names;
		}
	}

	private static String[] placeholders() {
		return new String[] {
				SendNotificationPlaceholders.Receivers.FRIENDS,
				SendNotificationPlaceholders.Receivers.REFERRED_USERS,
				SendNotificationPlaceholders.Receivers.REFERRER
		};
	}
}
