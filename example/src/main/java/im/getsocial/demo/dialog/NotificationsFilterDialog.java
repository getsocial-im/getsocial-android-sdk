package im.getsocial.demo.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import im.getsocial.demo.R;
import im.getsocial.demo.dependencies.components.NotificationsManager;
import im.getsocial.demo.fragment.BaseFragment;
import im.getsocial.demo.ui.PickActionView;
import im.getsocial.demo.utils.DynamicUi;
import im.getsocial.sdk.actions.ActionTypes;
import im.getsocial.sdk.pushnotifications.Notification;
import im.getsocial.sdk.pushnotifications.NotificationStatus;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class NotificationsFilterDialog extends DialogFragment {

	@BindView(R.id.statuses_frame)
	LinearLayout _status;

	@BindView(R.id.checkbox_types)
	CheckBox _findAllTypes;

	@BindView(R.id.custom_types_frame)
	LinearLayout _customTypesFrame;

	@BindView(R.id.container_action_types)
	LinearLayout _actionTypesContainer;

	private final Set<String> _selectedTypes = new HashSet<>();
	private final Set<String> _selectedStatuses = new HashSet<>();
	private final List<DynamicUi.DynamicInputHolder> _selectedActionTypes = new ArrayList<>();

	public NotificationsFilterDialog() {
		//
	}

	public static void show(FragmentManager fragmentManager) {
		new NotificationsFilterDialog().show(fragmentManager, "notifications_filter_dialog");
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
				.setTitle("Setup Filter")
				.setPositiveButton("Save",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								saveFilterConfigurations();
								dialog.dismiss();
							}
						}
				)
				.setNegativeButton("Discard",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								dialog.dismiss();
							}
						}
				);

		View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_notifications_filter, null);
		builder.setView(view);

		setUpView(view);

		return builder.create();
	}

	@OnCheckedChanged(R.id.checkbox_types)
	public void findAllTypes(boolean allTypes) {
		_customTypesFrame.removeAllViews();

		if (!allTypes) {
			drawAllTypes();
		}
	}

	@OnClick(R.id.button_add_action_type)
	public void addActionType() {
		addActionType(null);
	}

	private void addActionType(@Nullable String defaultText) {
		EditText view = DynamicUi.createDynamicTextRow(getContext(), _actionTypesContainer, _selectedActionTypes, "Action")
				.getView(0);
		view.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				showActionTypes((EditText) view);
				return true;
			}
		});
		view.setText(defaultText);
	}

	private void showActionTypes(final EditText id) {
		final List<String> placeholders = getAllActionTypes();
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

	private List<String> getAllActionTypes() {
		List<Pair<String, String>> pairs = getAllFields(ActionTypes.class);
		List<String> actionTypes = new ArrayList<>();
		for (Pair<String, String> pair : pairs) {
			actionTypes.add(pair.second);
		}
		return actionTypes;
	}

	private void drawAllTypes() {
		drawListOfCheckboxes(Notification.NotificationType.class, _customTypesFrame, _selectedTypes);
	}

	private void drawAllStatuses() {
		drawListOfCheckboxes(NotificationStatus.class, _status, _selectedStatuses);
	}

	private void drawListOfCheckboxes(Class clazz, ViewGroup container, final Set<String> set) {
		final List<Pair<String, String>> allNotificationTypes = getAllFields(clazz);

		for (final Pair<String, String> pair : allNotificationTypes) {
			CheckBox checkBox = new CheckBox(getContext());
			checkBox.setText(pair.first);
			checkBox.setChecked(set.contains(pair.second));
			checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						set.add(pair.second);
					} else {
						set.remove(pair.second);
					}
				}
			});
			container.addView(checkBox);
		}
	}

	private void setUpView(View view) {
		ButterKnife.bind(this, view);

		_selectedStatuses.addAll(notificationManager().getFilterStatus());
		_selectedTypes.addAll(notificationManager().getChosenTypes());

		for (String action : notificationManager().getActionTypes()) {
			addActionType(action);
		}

		drawAllStatuses();
		_findAllTypes.setChecked(_selectedTypes.isEmpty());
		if (!_selectedTypes.isEmpty()) {
			drawAllTypes();
		}
	}

	private NotificationsManager notificationManager() {
		return ((BaseFragment.ActivityListener) getActivity()).dependencies().notificationsManager();
	}

	private void saveFilterConfigurations() {
		if (_findAllTypes.isChecked()) {
			_selectedTypes.clear();
		}
		Set<String> actionTypes = new HashSet<>();
		for (DynamicUi.DynamicInputHolder inputHolder : _selectedActionTypes) {
			actionTypes.add(inputHolder.getText(0));
		}
		notificationManager().saveFilter(_selectedStatuses, _selectedTypes, actionTypes);
	}

	private static List<Pair<String, String>> getAllFields(Class clazz) {
		Field[] allValidIdFields = clazz.getFields();
		List<Pair<String, String>> allValidIds = new ArrayList<>();
		for (Field field : allValidIdFields) {
			final String name = capitalize(field.getName().replace("_", " ").toLowerCase());
			try {
				allValidIds.add(new Pair<String, String>(name, (String) field.get(null)));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return allValidIds;
	}

	private static String capitalize(final String line) {
		return Character.toUpperCase(line.charAt(0)) + line.substring(1);
	}
}
