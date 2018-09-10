package im.getsocial.demo.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

public class EditTextWOCopyPaste extends android.support.v7.widget.AppCompatEditText {
	public EditTextWOCopyPaste(Context context) {
		super(context);
		configure();
	}

	public EditTextWOCopyPaste(Context context, AttributeSet attrs) {
		super(context, attrs);
		configure();
	}

	public EditTextWOCopyPaste(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		configure();
	}

	private void configure() {
		setCustomSelectionActionModeCallback(new ActionMode.Callback() {
			@Override
			public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
				return false;
			}

			@Override
			public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
				return false;
			}

			@Override
			public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
				return false;
			}

			@Override
			public void onDestroyActionMode(ActionMode actionMode) {
				// do nothing here
			}
		});
		setLongClickable(false);
		setTextIsSelectable(false);
	}

}
