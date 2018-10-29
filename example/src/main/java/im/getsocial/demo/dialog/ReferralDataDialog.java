package im.getsocial.demo.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import im.getsocial.demo.R;

public class ReferralDataDialog extends DialogFragment {

	public ReferralDataDialog() {
		// empty constructor
	}

	public static void showReferralData(FragmentManager fm, String referralData) {
		ReferralDataDialog referralDataDialog = new ReferralDataDialog();
		Bundle referralDataBundle = new Bundle();
		referralDataBundle.putCharSequence("REFERRAL_DATA", referralData);
		referralDataDialog.setArguments(referralDataBundle);
		referralDataDialog.show(fm, "referral_data_dialog");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		View view = inflater.inflate(R.layout.dialog_referral_data_info, container);

		TextView detailsTextView = (TextView) view.findViewById(R.id.referralDataInfo);
		detailsTextView.setText(getArguments().getCharSequence("REFERRAL_DATA"));

		Button closeButton = (Button) view.findViewById(R.id.referralDataInfo_closeButton);
		closeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
			}
		});

		return view;
	}


}
