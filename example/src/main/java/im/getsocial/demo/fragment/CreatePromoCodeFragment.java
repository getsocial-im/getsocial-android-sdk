package im.getsocial.demo.fragment;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.getsocial.demo.R;
import im.getsocial.demo.utils.DynamicUi;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialException;
import im.getsocial.sdk.promocodes.PromoCode;
import im.getsocial.sdk.promocodes.PromoCodeBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CreatePromoCodeFragment extends BaseFragment {

	private ViewContainer _viewContainer;

	@Nullable
	private Date _startTime;
	@Nullable
	private Date _endTime;

	private final List<DynamicUi.DynamicInputHolder> _customData = new ArrayList<>();

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_create_promo_code, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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
		final PromoCodeBuilder promoCodeBuilder = _viewContainer._promoCode.getText().length() == 0
				? PromoCodeBuilder.createRandomCode()
				: PromoCodeBuilder.createWithCode(_viewContainer._promoCode.getText().toString());
		promoCodeBuilder.withMaxClaimCount(_viewContainer._maxClaim.getText().length() == 0 ? 0 : Integer.parseInt(_viewContainer._maxClaim.getText().toString()))
				.withTimeLimit(_startTime, _endTime);
		for (DynamicUi.DynamicInputHolder inputHolder : _customData) {
			promoCodeBuilder.addData(inputHolder.getText(0), inputHolder.getText(1));
		}

		showLoading("Promo Code", "Creating...");
		GetSocial.createPromoCode(promoCodeBuilder, new Callback<PromoCode>() {
			@Override
			public void onSuccess(PromoCode result) {
				hideLoading();
				((PromoCodesFragment) getFragmentManager().findFragmentByTag("promocodes")).showFullPromoCodeDialog(result);
			}

			@Override
			public void onFailure(GetSocialException exception) {
				hideLoading();
				Toast.makeText(getContext(), "Failed to create promo code: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
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

		ViewContainer(View view) {
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
				public void onDatePicked(Date date) {
					_startTime = date;
					_startTimeLabel.setText("Start time: " + date.toString());
				}
			});
		}

		@OnClick(R.id.button_change_end_time)
		public void changeEndTime() {
			showDateTimePicker("Select End Time", new DateTimePickerCallback() {
				@Override
				public void onDatePicked(Date date) {
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

	private void showDateTimePicker(String title, final DateTimePickerCallback callback) {
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
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Calendar calendar = Calendar.getInstance();
						calendar.set(
								datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
								timePicker.getCurrentHour(), timePicker.getCurrentMinute()
						);
						callback.onDatePicked(calendar.getTime());
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.create()
				.show();
	}

	private interface DateTimePickerCallback {
		void onDatePicked(Date date);
	}
}
