package im.getsocial.demo.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import im.getsocial.demo.R;
import im.getsocial.demo.dependencies.components.NotificationsManager;
import im.getsocial.demo.fragment.BaseFragment;
import im.getsocial.demo.utils.DynamicUi;
import im.getsocial.sdk.actions.ActionTypes;
import im.getsocial.sdk.notifications.Notification;
import im.getsocial.sdk.notifications.NotificationStatus;

public final class SortOrderDialog extends DialogFragment {

	@BindView(R.id.sort_by)
	LinearLayout _status;

	private final List<Pair<String, String>> _sortKeys;
	private final SortOrderDelegate _delegate;

	private Dialog _dialog;

	public SortOrderDialog(List<Pair<String, String>> sortKeys, SortOrderDelegate delegate) {
		this._sortKeys = sortKeys;
		this._delegate = delegate;
	}

	public static void show(FragmentManager manager, List<Pair<String, String>> sortKeys, SortOrderDelegate delegate) {
		new SortOrderDialog(sortKeys, delegate).show(manager, "dialog_sort_order");
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
				.setTitle("Sort By")
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								dialog.dismiss();
							}
						}
				);

		View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_sort_order, null);
		builder.setView(view);

		setUpView(view);

		_dialog = builder.create();
		return _dialog;
	}

	private void drawSortKeys() {
		for (final Pair<String, String> pair : _sortKeys) {
			Button button = new Button(getContext());
			button.setText(pair.second + pair.first);
			button.setOnClickListener((view) -> {
				this._delegate.onSortKeySelected(pair);
				_dialog.dismiss();
			});
			_status.addView(button);
		}
	}

	private void setUpView(View view) {
		ButterKnife.bind(this, view);

		drawSortKeys();
	}
}
