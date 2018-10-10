package im.getsocial.demo.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import butterknife.BindView;
import butterknife.ButterKnife;
import im.getsocial.demo.R;
import im.getsocial.sdk.CompletionCallback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialException;
import im.getsocial.sdk.iap.entity.PurchaseData;

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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manual_iap, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _viewContainer = new ManualIAPFragment.ViewContainer(view);
    }

    private void trackPurchase() {
        PurchaseData.Builder purchaseBuilder = new PurchaseData.Builder();
        purchaseBuilder.withProductId(_viewContainer._productIdInput.getText().toString());
        purchaseBuilder.withProductTitle(_viewContainer._productTitleInput.getText().toString());
        String productType = _viewContainer._productTypeInput.getSelectedItem().toString();
        purchaseBuilder.withProductType(productType.equalsIgnoreCase("item") ? PurchaseData.ProductType.ITEM : PurchaseData.ProductType.SUBSCRIPTION);
        purchaseBuilder.withPrice(Float.parseFloat(_viewContainer._priceInput.getText().toString()));
        purchaseBuilder.withPriceCurrency(_viewContainer._priceCurrencyInput.getText().toString());
        purchaseBuilder.withPurchaseId(UUID.randomUUID().toString());
        purchaseBuilder.withPurchaseDate(System.currentTimeMillis() / 1000);

        GetSocial.trackPurchaseData(purchaseBuilder.build(), new CompletionCallback() {
            @Override
            public void onSuccess() {
                System.out.println("Purchase was tracked");
            }

            @Override
            public void onFailure(GetSocialException exception) {
                System.out.println("Purchase was not tracked");
            }
        });
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

        ViewContainer(View view) {
            ButterKnife.bind(this, view);
            _sendPurchase.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    trackPurchase();
                }
            });
            String[] productTypes = new String[] { "item", "subscription" };
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, productTypes);
            _productTypeInput.setAdapter(adapter);
        }

    }

}
