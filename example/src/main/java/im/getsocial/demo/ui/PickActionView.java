package im.getsocial.demo.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
			put(ActionTypes.OPEN_ACTIVITY, Arrays.asList(ActionDataKeys.OpenActivity.ACTIVITY_ID, ActionDataKeys.OpenActivity.COMMENT_ID, ActionDataKeys.OpenActivity.FEED_NAME));
			put(ActionTypes.OPEN_INVITES, Collections.<String>emptyList());
			put(ActionTypes.OPEN_URL, Collections.singletonList(ActionDataKeys.OpenUrl.URL));
			put(ActionTypes.CLAIM_PROMO_CODE, Collections.singletonList(ActionDataKeys.ClaimPromoCode.PROMO_CODE));
		}
	};
	private static final String DEFAULT_ACTION = "DEFAULT";

	@BindView(R.id.spinner_select_notification_type)
	Spinner _selectNotificationType;

	@BindView(R.id.container_notification_data)
	LinearLayout _notificationDataContainer;

	final List<DynamicUi.DynamicInputHolder> _notificationData = new ArrayList<>();

	public PickActionView(Context context) {
		super(context);
		init(context);
	}

	public PickActionView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PickActionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public PickActionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	private void init(Context context) {
		LayoutInflater.from(context).inflate(R.layout.view_pick_action, this, true);
		setOrientation(VERTICAL);

		ButterKnife.bind(this);
		_selectNotificationType.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, NotificationAction.names()));
	}

	private String notificationAction() {
		return NotificationAction.ALL[_selectNotificationType.getSelectedItemPosition()]._action;
	}

	@Nullable
	public Action getAction() {
		return Action.builder(notificationAction()).addActionData(actionData()).build();
	}

	private Map<String, String> actionData() {
		final Map<String, String> actionData = new HashMap<>();
		for (DynamicUi.DynamicInputHolder inputHolder : _notificationData) {
			actionData.put(inputHolder.getText(0), inputHolder.getText(1));
		}
		if (isAddFriendRequest()) {
			actionData.put(ActionDataKeys.AddFriend.USER_ID, GetSocial.User.getId());
			actionData.put(KEY_USER_NAME, GetSocial.User.getDisplayName());
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
		key.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				return showPlaceholders(key, val);
			}
		});
	}

	private boolean showPlaceholders(final EditText key, final EditText val) {
		final List<String> placeholders = PLACEHOLDERS.get(notificationAction());
		if (placeholders == null || placeholders.isEmpty()) {
			return false;
		}
		final AlertDialog dialog = new AlertDialog.Builder(getContext())
				.setCancelable(true)
				.setTitle("Select Placeholder")
				.setItems(placeholders.toArray(new String[placeholders.size()]), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						key.setText(placeholders.get(which));
						val.requestFocus();
					}
				})
				.create();

		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
		return true;
	}

	private static class NotificationAction {
		final String _name;
		final String _action;

		static final NotificationAction[] ALL = new NotificationAction[] {
				new NotificationAction("Default", DEFAULT_ACTION),
				new NotificationAction("Custom", ActionTypes.CUSTOM),
				new NotificationAction("Open Activity", ActionTypes.OPEN_ACTIVITY),
				new NotificationAction("Open Invites", ActionTypes.OPEN_INVITES),
				new NotificationAction("Open Profile", ActionTypes.OPEN_PROFILE),
				new NotificationAction("Open URL", ActionTypes.OPEN_URL),
				new NotificationAction("Add Friend", ActionTypes.ADD_FRIEND),
				new NotificationAction("Claim Promo Code", ActionTypes.CLAIM_PROMO_CODE),
		};

		private NotificationAction(String name, String action) {
			_name = name;
			_action = action;
		}

		static List<String> names() {
			final List<String> names = new ArrayList<>();
			for (NotificationAction action : ALL) {
				names.add(action._name);
			}
			return names;
		}
	}
}
