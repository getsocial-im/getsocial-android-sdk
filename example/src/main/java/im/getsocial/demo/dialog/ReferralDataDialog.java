package im.getsocial.demo.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import im.getsocial.demo.R;
import im.getsocial.sdk.PromoCodes;
import im.getsocial.sdk.invites.LinkParams;
import im.getsocial.sdk.invites.ReferralData;

public class ReferralDataDialog extends DialogFragment {

	@BindView(R.id.referralDataInfo)
	TextView _referralDataInfo;

	@BindView(R.id.referralDataInfo_actionButton)
	Button _actionButton;

	@Nullable
	private String _promoCode;

	public ReferralDataDialog() {

	}

	public static void showReferralData(final FragmentManager fm, final ReferralData referralData) {
		final ReferralDataDialog referralDataDialog = new ReferralDataDialog();
		final Bundle referralDataBundle = new Bundle();

		referralDataBundle.putString("REFERRAL_DATA", "Referral data received: [ " + referralData + " ]");
		referralDataBundle.putString("PROMO_CODE", referralData.getLinkParams().get(LinkParams.KEY_PROMO_CODE));

		referralDataDialog.setArguments(referralDataBundle);
		referralDataDialog.show(fm, "referral_data_dialog");
	}

	@Override
	public View onCreateView(@Nullable final LayoutInflater inflater, final ViewGroup container,
													 @Nullable final Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		final View view = inflater.inflate(R.layout.dialog_referral_data_info, container);

		ButterKnife.bind(this, view);

		String text = getArguments().getString("REFERRAL_DATA");
		_promoCode = getArguments().getString("PROMO_CODE");
		if (_promoCode != null) {
			text += "\n\nPROMO CODE:\n" + _promoCode;
		}
		_referralDataInfo.setText(text);
		_actionButton.setVisibility(_promoCode == null ? View.INVISIBLE : View.VISIBLE);

		return view;
	}

	@OnClick(R.id.referralDataInfo_actionButton)
	public void doAction() {
		PromoCodes.claim(_promoCode, result -> {
			Toast.makeText(getContext(), "PromoCode claimed: " + result, Toast.LENGTH_SHORT).show();
		}, error -> {
			Toast.makeText(getContext(), "Failed to claim PromoCode: " + error.getMessage(), Toast.LENGTH_SHORT).show();
		});
	}

	@OnClick(R.id.referralDataInfo_closeButton)
	public void doClose() {
		dismiss();
	}
}
