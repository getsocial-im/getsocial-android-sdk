package im.getsocial.demo.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.getsocial.demo.R;
import im.getsocial.demo.utils.DynamicUi;
import im.getsocial.sdk.PromoCodes;
import im.getsocial.sdk.promocodes.PromoCodeContent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CreatePromoCodeFragment extends BaseFragment {

	private final List<DynamicUi.DynamicInputHolder> _customData = new ArrayList<>();
	private ViewContainer _viewContainer;
	@Nullable
	private Date _startTime;
	@Nullable
	private Date _endTime;
	private boolean _creating = false;

	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_create_promo_code, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		_viewContainer = new ViewContainer(view);
	}

	@Override
	public String getFragmentTag() {
		return "create_promo_code";
	}

	@Override
	public String getTitle() {
		return "Create Promo Code";
	}

	private void doCreatePromoCode() {
		if (_creating) {
			return;
		}
		_creating = true;
		final PromoCodeContent promoCodeContent = _viewContainer._promoCode.getText().length() == 0
						? PromoCodeContent.createRandomCode()
						: PromoCodeContent.createWithCode(_viewContainer._promoCode.getText().toString());
		promoCodeContent.withMaxClaimCount(_viewContainer._maxClaim.getText().length() == 0 ? 0 : Integer.parseInt(_viewContainer._maxClaim.getText().toString()))
						.withTimeLimit(_startTime, _endTime);
		for (final DynamicUi.DynamicInputHolder inputHolder : _customData) {
			promoCodeContent.addProperty(inputHolder.getText(0), inputHolder.getText(1));
		}

		showLoading("Promo Code", "Creating...");
		PromoCodes.create(promoCodeContent, result -> {
			_creating = false;
			hideLoading();
			((PromoCodesFragment) getFragmentManager().findFragmentByTag("promocodes")).showFullPromoCodeDialog(result);
		}, error -> {
			_creating = false;
			hideLoading();
			Toast.makeText(getContext(), "Failed to create promo code: " + error.getMessage(), Toast.LENGTH_SHORT).show();
		});
	}

	private void showDateTimePicker(final String title, final DateTimePickerCallback callback) {
		final LinearLayout linearLayout = new LinearLayout(getContext());
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		final DatePicker datePicker = new DatePicker(getContext());
		final TimePicker timePicker = new TimePicker(getContext(), null, R.style.CustomDatePickerDialogTheme);

		linearLayout.addView(datePicker, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		linearLayout.addView(timePicker);

		new AlertDialog.Builder(getContext())
						.setTitle(title)
						.setView(linearLayout)
						.setPositiveButton("Set", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface dialog, final int which) {
								dialog.dismiss();
								final Calendar calendar = Calendar.getInstance();
								calendar.set(
												datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
												timePicker.getCurrentHour(), timePicker.getCurrentMinute()
								);
								callback.onDatePicked(calendar.getTime());
							}
						})
						.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface dialog, final int which) {
								dialog.dismiss();
							}
						})
						.create()
						.show();
	}

	private interface DateTimePickerCallback {
		void onDatePicked(Date date);
	}

	public class ViewContainer {

		@BindView(R.id.promo_code)
		EditText _promoCode;

		@BindView(R.id.container_custom_data)
		ViewGroup _customDataContainer;

		@BindView(R.id.max_claim)
		EditText _maxClaim;

		@BindView(R.id.start_time)
		TextView _startTimeLabel;

		@BindView(R.id.end_time)
		TextView _endTimeLabel;

		ViewContainer(final View view) {
			ButterKnife.bind(this, view);
		}

		@OnClick(R.id.button_add_custom_data)
		public void addCustomData() {
			DynamicUi.createDynamicTextRow(getContext(), _customDataContainer, _customData, "Key", "Value");
		}

		@OnClick(R.id.button_change_start_time)
		public void changeStartTime() {
			showDateTimePicker("Select Start Time", new DateTimePickerCallback() {
				@Override
				public void onDatePicked(final Date date) {
					_startTime = date;
					_startTimeLabel.setText("Start time: " + date.toString());
				}
			});
		}

		@OnClick(R.id.button_change_end_time)
		public void changeEndTime() {
			showDateTimePicker("Select End Time", new DateTimePickerCallback() {
				@Override
				public void onDatePicked(final Date date) {
					_endTime = date;
					_endTimeLabel.setText("End time: " + date.toString());
				}
			});
		}

		@OnClick(R.id.button_reset_start_time)
		public void resetStartTime() {
			_startTime = null;
			_startTimeLabel.setText("Start time: (Start Now)");
		}

		@OnClick(R.id.button_reset_end_time)
		public void resetEndTime() {
			_endTime = null;
			_endTimeLabel.setText("End time: (Without Limit)");
		}

		@OnClick(R.id.create_promo_code)
		public void createPromoCode() {
			doCreatePromoCode();
		}

	}
}
