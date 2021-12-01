package im.getsocial.demo.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.getsocial.demo.R;

public class DialogWithScrollableText extends DialogFragment {
	@BindView(R.id.text)
	TextView _textView;

	public static void show(final String text, final FragmentManager fm) {
		final DialogWithScrollableText dialog = new DialogWithScrollableText();
		final Bundle bundle = new Bundle();

		bundle.putString("text", text);

		dialog.setArguments(bundle);
		dialog.show(fm, "referral_data_dialog");
	}

	@Override
	public View onCreateView(@Nullable final LayoutInflater inflater, final ViewGroup container,
													 @Nullable final Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		final View view = inflater.inflate(R.layout.dialog_scrollable_text, container);

		ButterKnife.bind(this, view);

		final String text = getArguments().getString("text");
		_textView.setText(text);

		return view;
	}

	@OnClick(R.id.close_btn)
	public void close() {
		dismiss();
	}
}
