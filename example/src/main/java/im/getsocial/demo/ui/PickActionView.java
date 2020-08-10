package im.getsocial.demo.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.getsocial.demo.R;
import im.getsocial.demo.utils.DynamicUi;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.actions.Action;
import im.getsocial.sdk.actions.ActionDataKeys;
import im.getsocial.sdk.actions.ActionTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PickActionView extends LinearLayout {

	public static final String KEY_USER_NAME = "user_name";

	private static final Map<String, List<String>> PLACEHOLDERS = new HashMap<String, List<String>>() {
		{
			put(ActionTypes.OPEN_PROFILE, Collections.singletonList(ActionDataKeys.OpenProfile.USER_ID));
			put(ActionTypes.OPEN_ACTIVITY, Arrays.asList(ActionDataKeys.OpenActivity.ACTIVITY_ID, ActionDataKeys.OpenActivity.COMMENT_ID, ActionDataKeys.OpenActivity.TOPIC_ID, ActionDataKeys.OpenActivity.USER_ID));
			put(ActionTypes.OPEN_INVITES, Collections.emptyList());
			put(ActionTypes.OPEN_URL, Collections.singletonList(ActionDataKeys.OpenUrl.URL));
			put(ActionTypes.CLAIM_PROMO_CODE, Collections.singletonList(ActionDataKeys.ClaimPromoCode.PROMO_CODE));
			put(ActionTypes.ADD_FRIEND, Collections.singletonList(ActionDataKeys.AddFriend.USER_ID));
		}
	};
	private static final String DEFAULT_ACTION = "$$DEFAULT";
	private static final String CUSTOM_ACTION = "custom";

	final List<DynamicUi.DynamicInputHolder> _notificationData = new ArrayList<>();
	@BindView(R.id.spinner_select_notification_type)
	Spinner _selectNotificationType;
	@BindView(R.id.container_notification_data)
	LinearLayout _notificationDataContainer;

	public PickActionView(final Context context) {
		super(context);
		init(context);
	}

	public PickActionView(final Context context, @Nullable final AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PickActionView(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public PickActionView(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	private void init(final Context context) {
		LayoutInflater.from(context).inflate(R.layout.view_pick_action, this, true);
		setOrientation(VERTICAL);

		ButterKnife.bind(this);
		_selectNotificationType.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, NotificationAction.names()));
	}

	private String notificationAction() {
		return NotificationAction.ALL[_selectNotificationType.getSelectedItemPosition()]._action;
	}

	@Nullable
	public Action getAction() {
		if (notificationAction().equals(DEFAULT_ACTION)) {
			return null;
		}
		return Action.create(notificationAction(), actionData());
	}

	private Map<String, String> actionData() {
		final Map<String, String> actionData = new HashMap<>();
		for (final DynamicUi.DynamicInputHolder inputHolder : _notificationData) {
			actionData.put(inputHolder.getText(0), inputHolder.getText(1));
		}
		if (isAddFriendRequest()) {
			actionData.put(ActionDataKeys.AddFriend.USER_ID, GetSocial.getCurrentUser().getId());
			actionData.put(KEY_USER_NAME, GetSocial.getCurrentUser().getDisplayName());
		}
		return actionData;
	}

	public boolean isAddFriendRequest() {
		return notificationAction().equals(ActionTypes.ADD_FRIEND);
	}

	@OnClick(R.id.button_add_notification_data)
	public void addNotificationData() {
		final DynamicUi.DynamicInputHolder inputHolder = DynamicUi.createDynamicTextRow(getContext(), _notificationDataContainer, _notificationData, "Key", "Value");
		final EditText key = inputHolder.getView(0);
		final EditText val = inputHolder.getView(1);
		key.setOnLongClickListener(view -> showPlaceholders(key, val));
	}

	private void createRow(final String k, final String v) {
		final DynamicUi.DynamicInputHolder inputHolder = DynamicUi.createDynamicTextRow(getContext(), _notificationDataContainer, _notificationData, "Key", "Value");
		final EditText key = inputHolder.getView(0);
		final EditText val = inputHolder.getView(1);
		key.setOnLongClickListener(view -> showPlaceholders(key, val));
		key.setText(k);
		val.setText(v);
	}

	private boolean showPlaceholders(final EditText key, final EditText val) {
		final List<String> placeholders = PLACEHOLDERS.get(notificationAction());
		if (placeholders == null || placeholders.isEmpty()) {
			return false;
		}
		final AlertDialog dialog = new AlertDialog.Builder(getContext())
						.setCancelable(true)
						.setTitle("Select Placeholder")
						.setItems(placeholders.toArray(new String[placeholders.size()]), (dialog1, which) -> {
							key.setText(placeholders.get(which));
							val.requestFocus();
						})
						.create();

		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
		return true;
	}

	public void setAction(final Action action) {
		_selectNotificationType.setSelection(position(action.getType()));
		for (final Map.Entry<String, String> property : action.getData().entrySet()) {
			createRow(property.getKey(), property.getValue());
		}
	}

	private static int position(final String type) {
		for (int i = 0; i < NotificationAction.ALL.length; i++) {
			final NotificationAction notificationAction = NotificationAction.ALL[i];
			if (notificationAction._action.equals(type)) {
				return i;
			}
		}

		// hardcode - custom
		return 1;
	}

	private static class NotificationAction {
		static final NotificationAction[] ALL = new NotificationAction[] {
						new NotificationAction("Default", DEFAULT_ACTION),
						new NotificationAction("Custom", CUSTOM_ACTION),
						new NotificationAction("Open Activity", ActionTypes.OPEN_ACTIVITY),
						new NotificationAction("Open Invites", ActionTypes.OPEN_INVITES),
						new NotificationAction("Open Profile", ActionTypes.OPEN_PROFILE),
						new NotificationAction("Open URL", ActionTypes.OPEN_URL),
						new NotificationAction("Add Friend", ActionTypes.ADD_FRIEND),
						new NotificationAction("Claim Promo Code", ActionTypes.CLAIM_PROMO_CODE),
		};
		final String _name;
		final String _action;

		private NotificationAction(final String name, final String action) {
			_name = name;
			_action = action;
		}

		static List<String> names() {
			final List<String> names = new ArrayList<>();
			for (final NotificationAction action : ALL) {
				names.add(action._name);
			}
			return names;
		}
	}
}
