package im.getsocial.demo.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public final class DynamicUi {

	private DynamicUi() {
		//
	}

	public static DynamicInputHolder createDynamicTextRow(final Context context, final ViewGroup parentView, final List<DynamicInputHolder> holderList, String... inputs) {
		final LinearLayout linearLayout = new LinearLayout(context);
		linearLayout.setGravity(Gravity.CENTER_VERTICAL);
		linearLayout.setOrientation(LinearLayout.HORIZONTAL);
		linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		final List<EditText> editTexts = new ArrayList<>();
		for (String input : inputs) {
			final EditText inputText = new EditText(context);

			inputText.setContentDescription(input);
			inputText.setLayoutParams(newLayoutParams(context, 100, 40));
			inputText.setHint(input);

			linearLayout.addView(inputText);
			editTexts.add(inputText);
		}

		final DynamicInputHolder inputHolder = new DynamicInputHolder() {
			@Override
			public String getText(int position) {
				return getView(position).getText().toString();
			}

			@Override
			public EditText getView(int position) {
				return editTexts.get(position);
			}
		};

		final Button remove = new Button(context);
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

		return inputHolder;
	}

	private static LinearLayout.LayoutParams newLayoutParams(Context context, int width, int height) {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(PixelUtils.dp2px(context, width), PixelUtils.dp2px(context, height));
		params.leftMargin = PixelUtils.dp2px(context, 10);
		return params;
	}

	public interface DynamicInputHolder {
		String getText(int position);

		EditText getView(int position);
	}
}
