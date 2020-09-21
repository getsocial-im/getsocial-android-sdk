package im.getsocial.demo.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.PurchaseHistoryResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import im.getsocial.demo.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PurchaseFragment extends BaseFragment implements PurchasesUpdatedListener {

	private final List<InAppPurchaseProduct> _availableProducts = new ArrayList<>();
	private BillingClient _billingClient;
	private PurchaseFragment.ViewContainer _viewContainer;

	@Override
	public String getFragmentTag() {
		return "purchase";
	}

	@Override
	public String getTitle() {
		return "Purchase";
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		_billingClient = BillingClient.newBuilder(getContext())
				.enablePendingPurchases()
				.setListener(this)
				.build();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_iap, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		_viewContainer = new ViewContainer(view);
	}

	@Override
	public void onResume() {
		super.onResume();
		_availableProducts.clear();
		setupBillingConnection();
	}

	private void setupBillingConnection() {
		_billingClient.startConnection(new BillingClientStateListener() {

			@Override
			public void onBillingSetupFinished(BillingResult result) {
				if (result.getResponseCode() == BillingClient.BillingResponseCode.OK) {
					// The billing client is ready. You can query purchases here.
					loadConsumableItems();
					loadSubscriptions();
					loadPurchaseHistory();
				} else {
					System.out.println("purchase client failed: " + result);
				}
			}

			@Override
			public void onBillingServiceDisconnected() {
				// Try to restart the connection on the next request to
				// Google Play by calling the startConnection() method.
				System.out.println("purchase client disconnected");
			}
		});

	}

	private void loadConsumableItems() {
		List<String> list = new ArrayList<>();
		list.add("im.getsocial.sdk.demo.internal.iap.managed2");
		list.add("im.getsocial.sdk.demo.internal.iap.managed.new");
		loadProducts(SkuDetailsParams.newBuilder()
				.setSkusList(list)
				.setType(BillingClient.SkuType.INAPP)
				.build());
	}

	private void loadSubscriptions() {
		loadProducts(SkuDetailsParams.newBuilder()
				.setSkusList(Collections.singletonList("im.getsocial.sdk.demo.internal.iap.subscription"))
				.setType(BillingClient.SkuType.SUBS)
				.build());
	}

	private void loadPurchaseHistory() {
		Purchase.PurchasesResult result = _billingClient.queryPurchases(BillingClient.SkuType.INAPP);
		for (Purchase purchase : result.getPurchasesList()) {
			System.out.println("PURCHASE IS " + purchase.getPurchaseToken());
			if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
				consumePurchasedItem(purchase);
			}
		}
	}

	private void loadProducts(SkuDetailsParams detailsParams) {
		_billingClient.querySkuDetailsAsync(detailsParams, new SkuDetailsResponseListener() {
			@Override
			public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> list) {
				System.out.println("details response code: " + billingResult.getResponseCode());
				if (list != null) {
					for (SkuDetails detail : list) {
						_availableProducts.add(new InAppPurchaseProduct(detail));
					}
					_viewContainer._iapList.setAdapter(new InAppPurchaseAdapter(getContext(), _availableProducts));
				}
			}
		});
	}

	private void purchaseItem(SkuDetails skuDetails) {
		_billingClient.launchBillingFlow(getActivity(), BillingFlowParams.newBuilder()
				.setSkuDetails(skuDetails)
				.build()
		);

	}

	private void consumePurchasedItem(Purchase purchase) {
		ConsumeParams params = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
		_billingClient.consumeAsync(params, new ConsumeResponseListener() {
			@Override
			public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
				System.out.println("got consumePurchase response");
			}

		});
	}

	@Override
	public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
		if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
			for (Purchase purchase : purchases) {
				if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
					consumePurchasedItem(purchase);
				} else {
					System.out.println("Status is PENDING");
				}
			}
		}
	}

	static class InAppPurchaseProduct {
		SkuDetails _skuDetails;

		InAppPurchaseProduct(SkuDetails details) {
			_skuDetails = details;
		}

		String getProductTitle() {
			return _skuDetails.getTitle();
		}

	}

	class ViewContainer {

		@BindView(R.id.iap_list)
		ListView _iapList;

		@BindView(R.id.button_manual_iap)
		Button _buttonManualIap;

		ViewContainer(View view) {
			ButterKnife.bind(this, view);
			_availableProducts.clear();
			_buttonManualIap.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					addContentFragment(new ManualIAPFragment());
				}
			});
		}

	}

	class InAppPurchaseAdapter extends ArrayAdapter<InAppPurchaseProduct> {

		InAppPurchaseAdapter(Context context, List<InAppPurchaseProduct> objects) {
			super(context, 0, objects);
		}

		@NonNull
		@Override
		public View getView(int position, @Nullable View convertView, ViewGroup parent) {
			InAppPurchaseAdapter.ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_iap, null);
				convertView.setTag(holder = new InAppPurchaseAdapter.ViewHolder(convertView));
			} else {
				holder = (InAppPurchaseAdapter.ViewHolder) convertView.getTag();
			}
			holder.setInAppPurchaseProduct(getItem(position));

			return convertView;
		}

		class ViewHolder {

			@BindView(R.id.product_title)
			TextView _productTitle;
			private InAppPurchaseProduct _inAppPurchaseProduct;

			ViewHolder(View view) {
				ButterKnife.bind(this, view);
			}

			void setInAppPurchaseProduct(InAppPurchaseProduct product) {
				_inAppPurchaseProduct = product;
				populate();
			}

			private void populate() {
				_productTitle.setText(_inAppPurchaseProduct.getProductTitle());
			}

			@OnClick(R.id.button_buy)
			void buyItem() {
				PurchaseFragment.this.purchaseItem(_inAppPurchaseProduct._skuDetails);
			}

		}

	}
}
