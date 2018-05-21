package im.getsocial.demo.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import im.getsocial.demo.R;
import im.getsocial.demo.dependencies.components.NotificationsManager;
import im.getsocial.demo.fragment.BaseFragment;
import im.getsocial.sdk.pushnotifications.Notification;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class NotificationsFilterDialog extends DialogFragment {

	@BindView(R.id.spinner_status)
	Spinner _status;

	@BindView(R.id.checkbox_types)
	CheckBox _findAllTypes;

	@BindView(R.id.custom_types_frame)
	LinearLayout _customTypesFrame;

	private final Set<Integer> _selectedTypes = new HashSet<>();

	public NotificationsFilterDialog() {
		//
	}

	public static void show(FragmentManager fragmentManager) {
		new NotificationsFilterDialog().show(fragmentManager, "notifications_filter_dialog");
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new  AlertDialog.Builder(getActivity())
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

	private void drawAllTypes() {
		final List<Pair<String, Integer>> allNotificationTypes = getAllNotificationTypes();

		for (final Pair<String, Integer> pair : allNotificationTypes) {
			CheckBox checkBox = new CheckBox(getContext());
			checkBox.setText(pair.first);
			checkBox.setChecked(_selectedTypes.contains(pair.second));
			checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						_selectedTypes.add(pair.second);
					} else {
						_selectedTypes.remove(pair.second);
					}
				}
			});
			_customTypesFrame.addView(checkBox);
		}
	}

	private void setUpView(View view) {
		ButterKnife.bind(this, view);

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, Arrays.asList("Read and Unread", "Read", "Unread"));
		_status.setAdapter(arrayAdapter);
		_status.setSelection(notificationManager().getFilterStatus());

		_selectedTypes.addAll(notificationManager().getChosenTypes());

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
		notificationManager().saveFilter(_status.getSelectedItemPosition(), _selectedTypes);
	}

	private static List<Pair<String, Integer>> getAllNotificationTypes() {
		Field[] allValidIdFields = Notification.NotificationType.class.getFields();
		List<Pair<String, Integer>> allValidIds = new ArrayList<>();
		for (Field field : allValidIdFields) {
			final String name = capitalize(field.getName().replace("_", " ").toLowerCase());
			try {
				allValidIds.add(new Pair<String, Integer>(name, (Integer) field.get(Notification.NotificationType.class)));
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
