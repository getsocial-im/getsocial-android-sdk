package im.getsocial.demo.dialog.action_dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by orestsavchak on 7/20/17.
 */

public class ActionDialog {
	private final List<Action> _actionList;
	private final AlertDialog.Builder _dialog;

	public ActionDialog(Context context) {
		_dialog = new AlertDialog.Builder(context)
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						dialogInterface.dismiss();
					}
				});
		_actionList = new ArrayList<>();
	}

	public ActionDialog addAction(Action action) {
		_actionList.add(action);
		return this;
	}

	public ActionDialog addActions(List<Action> actions) {
		_actionList.addAll(actions);
		return this;
	}

	public ActionDialog setTitle(String title) {
		_dialog.setTitle(title);
		return this;
	}

	public void show() {
		List<String> items = new ArrayList<>(_actionList.size());
		for (Action action : _actionList) {
			items.add(action._title);
		}
		_dialog.setItems(items.toArray(new String[items.size()]),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						_actionList.get(i).execute();
						dialogInterface.dismiss();
					}
				})
				.show();
	}

	public abstract static class Action {
		private final String _title;

		public Action(String title) {
			_title = title;
		}

		public abstract void execute();
	}

}
