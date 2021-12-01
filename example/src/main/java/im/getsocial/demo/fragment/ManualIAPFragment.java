package im.getsocial.demo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.getsocial.demo.R;
import im.getsocial.sdk.Analytics;
import im.getsocial.sdk.iap.PurchaseData;

import java.util.UUID;

public class ManualIAPFragment extends BaseFragment {

	private ManualIAPFragment.ViewContainer _viewContainer;

	@Override
	public String getFragmentTag() {
		return "manualiaptracking";
	}

	@Override
	public String getTitle() {
		return "Manual IAP Tracking";
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_manual_iap, container, false);
	}

	@Override
	public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		_viewContainer = new ViewContainer(view);
	}

	private void trackPurchase() {
		final String productType = _viewContainer._productTypeInput.getSelectedItem().toString();
		final PurchaseData purchaseData = new PurchaseData()
						.withProductId(_viewContainer._productIdInput.getText().toString())
						.withProductTitle(_viewContainer._productTitleInput.getText().toString())
						.withProductType(productType.equalsIgnoreCase("item") ? PurchaseData.ProductType.ITEM : PurchaseData.ProductType.SUBSCRIPTION)
						.withPrice(Float.parseFloat(_viewContainer._priceInput.getText().toString()))
						.withPriceCurrency(_viewContainer._priceCurrencyInput.getText().toString())
						.withPurchaseId(UUID.randomUUID().toString())
						.withPurchaseDate(System.currentTimeMillis() / 1000);


		if (Analytics.trackPurchaseEvent(purchaseData)) {
			_log.logInfoAndToast("Successfully tracked purchase data");
		} else {
			_log.logErrorAndToast("Failed to track purchase data.");
		}
	}

	class ViewContainer {

		@BindView(R.id.product_id_input)
		EditText _productIdInput;

		@BindView(R.id.product_title_input)
		EditText _productTitleInput;

		@BindView(R.id.product_type_input)
		Spinner _productTypeInput;

		@BindView(R.id.product_price_input)
		EditText _priceInput;

		@BindView(R.id.product_price_currency_input)
		EditText _priceCurrencyInput;

		@BindView(R.id.button_send_purchase)
		Button _sendPurchase;

		ViewContainer(final View view) {
			ButterKnife.bind(this, view);
			_sendPurchase.setOnClickListener(view1 -> trackPurchase());
			final String[] productTypes = new String[] {"item", "subscription"};
			final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, productTypes);
			_productTypeInput.setAdapter(adapter);
		}

	}

}
