package im.getsocial.demo.fragment;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import im.getsocial.demo.adapter.MenuItem;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.PromoCodes;
import im.getsocial.sdk.communities.CurrentUser;
import im.getsocial.sdk.communities.UserUpdate;
import im.getsocial.sdk.invites.InviteContent;
import im.getsocial.sdk.invites.InviteTextPlaceholders;
import im.getsocial.sdk.invites.LinkParams;
import im.getsocial.sdk.promocodes.PromoCode;
import im.getsocial.sdk.promocodes.PromoCodeContent;
import im.getsocial.sdk.ui.invites.InvitesViewBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PromoCodesFragment extends BaseListFragment {

	private static final String PROMO_CODE_PROPERTY = "my_promo_code";

	private static void shareCode(final String promoCode) {
		final InviteContent content = new InviteContent();
		content.setText("Use my Promo Code to get a personal discount: " + InviteTextPlaceholders.PLACEHOLDER_PROMO_CODE + " . " + InviteTextPlaceholders.PLACEHOLDER_APP_INVITE_URL);
		content.setLinkParams(new HashMap<String, Object>() {
			{
				put(LinkParams.KEY_PROMO_CODE, promoCode);
			}
		});
		InvitesViewBuilder.create().setCustomInviteContent(content).show();
	}

	private static void copyCode(final Context context, final String promoCode) {
		final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		final ClipData clip = ClipData.newPlainText("GetSocial Promo Code", promoCode);
		clipboard.setPrimaryClip(clip);
	}

	private static String formatInfo(final PromoCode promoCode) {
		return String.format("code=%s\ndata=%s\nmaxClaim=%d\nclaimCount=%d\nstartDate=%s" +
										"\nendDate=%s\nenabled=%b\nclaimable=%b\ncreator=%s",
						promoCode.getCode(), promoCode.getProperties(), promoCode.getMaxClaimCount(),
						promoCode.getClaimCount(), promoCode.getStartDate(), promoCode.getEndDate(),
						promoCode.isEnabled(), promoCode.isClaimable(), promoCode.getCreator().getDisplayName());
	}

	@Override
	protected List<MenuItem> createListData() {
		return Arrays.asList(
						MenuItem.builder("My Promo Code").withAction(new MyPromoCodeAction()).build(),
						navigationListItem("Create Promo Code", CreatePromoCodeFragment.class),
						MenuItem.builder("Claim Promo Code").withAction(new ClaimPromoCodeAction()).build(),
						MenuItem.builder("Promo Code Info").withAction(new InfoPromoCodeAction()).build()
		);
	}

	@Override
	public String getFragmentTag() {
		return "promocodes";
	}

	@Override
	public String getTitle() {
		return "Promo Codes";
	}

	private void showPromoCodeDialog(final String promoCode) {
		showDialog(
						"Promo Code",
						promoCode,
						new ShareAction(promoCode),
						new CopyAction(promoCode),
						new InfoAction(promoCode),
						new DismissAction()
		);
	}

	public void showFullPromoCodeDialog(final PromoCode promoCode) {
		showDialog(
						"Promo Code",
						formatInfo(promoCode),
						new ShareAction(promoCode.getCode()),
						new CopyAction(promoCode.getCode()),
						new DismissAction()
		);
	}

	private void showInfo(final PromoCode promoCode) {
		new AlertDialog.Builder(getContext())
						.setTitle("Promo Code")
						.setMessage(formatInfo(promoCode))
						.setCancelable(true)
						.create()
						.show();
	}

	private void showDialogWithEditText(final String title, final String hint, final EditDialogAction... actions) {
		final LinearLayout linearLayout = new LinearLayout(getContext());
		linearLayout.setPadding(20, 20, 20, 20);
		linearLayout.setOrientation(LinearLayout.VERTICAL);

		final EditText inputView = new EditText(getContext());
		inputView.setPadding(20, 20, 20, 20);
		inputView.setHint(hint);

		linearLayout.addView(inputView);

		final AlertDialog alertDialog = new AlertDialog.Builder(getContext())
						.setView(linearLayout)
						.setTitle(title)
						.setCancelable(true)
						.create();

		for (final EditDialogAction dialogAction : actions) {
			final Button button = new Button(getContext());
			button.setText(dialogAction._name);
			button.setOnClickListener(view -> dialogAction.onClick(alertDialog, inputView.getText().toString()));
			linearLayout.addView(button);
		}

		alertDialog.show();
	}

	private void showDialog(final String title, final String contentText, final DialogAction... actions) {
		final LinearLayout linearLayout = new LinearLayout(getContext());
		linearLayout.setPadding(20, 20, 20, 20);
		linearLayout.setOrientation(LinearLayout.VERTICAL);

		final TextView textView = new TextView(getContext());
		textView.setPadding(20, 20, 20, 20);
		textView.setText(contentText);

		linearLayout.addView(textView);

		final AlertDialog alertDialog = new AlertDialog.Builder(getContext())
						.setView(linearLayout)
						.setTitle(title)
						.setCancelable(true)
						.create();

		for (final DialogAction dialogAction : actions) {
			final Button button = new Button(getContext());
			button.setText(dialogAction._name);
			button.setOnClickListener(view -> dialogAction.onClick(alertDialog));
			linearLayout.addView(button);
		}

		alertDialog.show();
	}

	private abstract static class DialogAction {
		final String _name;

		DialogAction(final String name) {
			_name = name;
		}

		public abstract void onClick(AlertDialog dialog);
	}

	private abstract static class EditDialogAction {
		final String _name;

		EditDialogAction(final String name) {
			_name = name;
		}

		public abstract void onClick(AlertDialog dialog, String text);
	}

	private class MyPromoCodeAction implements MenuItem.Action {

		@Override
		public void execute() {
			final CurrentUser user = GetSocial.getCurrentUser();
			if (user == null) {
				Toast.makeText(getContext(), "SDK is not initialized", Toast.LENGTH_SHORT).show();
				return;
			}
			if (user.getPrivateProperties().containsKey(PROMO_CODE_PROPERTY)) {
				showPromoCodeDialog(user.getPrivateProperties().get(PROMO_CODE_PROPERTY));
			} else {
				showLoading("Promo Code", "Creating...");
				PromoCodes.create(PromoCodeContent.createRandomCode().addProperty("my_promo_code", "true"), result -> {
					GetSocial.getCurrentUser().updateDetails(new UserUpdate().setPrivateProperty(PROMO_CODE_PROPERTY, result.getCode()), () -> {
						hideLoading();
						showPromoCodeDialog(result.getCode());
					}, error -> {
						hideLoading();
						Toast.makeText(getContext(), "Failed to save a Promo Code: " + error.getMessage(), Toast.LENGTH_SHORT).show();
					});
				}, exception -> {
					hideLoading();
					Toast.makeText(getContext(), "Failed to create a Promo Code: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
				});
			}
		}
	}

	private class ClaimPromoCodeAction implements MenuItem.Action {

		@Override
		public void execute() {
			showDialogWithEditText(
							"Claim Promo Code",
							"Promo Code...",
							new ClaimEditAction(),
							new DismissEditAction()
			);
		}
	}

	private class InfoPromoCodeAction implements MenuItem.Action {

		@Override
		public void execute() {
			showDialogWithEditText(
							"Promo Code Info",
							"Promo Code...",
							new InfoEditAction(),
							new DismissEditAction()
			);
		}
	}

	private class DismissAction extends DialogAction {

		DismissAction() {
			super("Dismiss");
		}

		@Override
		public void onClick(final AlertDialog dialog) {
			dialog.dismiss();
		}
	}

	private class CopyAction extends DialogAction {
		private final String _code;

		CopyAction(final String code) {
			super("Copy");
			_code = code;
		}

		@Override
		public void onClick(final AlertDialog dialog) {
			copyCode(dialog.getContext(), _code);
			dialog.dismiss();
		}
	}

	private class ShareAction extends DialogAction {
		private final String _code;

		ShareAction(final String code) {
			super("Share");
			_code = code;
		}

		@Override
		public void onClick(final AlertDialog dialog) {
			dialog.dismiss();
			shareCode(_code);
		}
	}

	private class InfoAction extends DialogAction {
		private final String _code;

		InfoAction(final String promoCode) {
			super("Info");
			_code = promoCode;
		}

		@Override
		public void onClick(final AlertDialog dialog) {
			showLoading("Promo Code", "Loading...");
			PromoCodes.get(_code, result -> {
				hideLoading();
				dialog.dismiss();
				showFullPromoCodeDialog(result);
			}, exception -> {
				hideLoading();
				Toast.makeText(getContext(), "Failed to load Promo Code info: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
			});
		}
	}

	private class DismissEditAction extends EditDialogAction {

		DismissEditAction() {
			super("Dismiss");
		}

		@Override
		public void onClick(final AlertDialog dialog, final String text) {
			dialog.dismiss();
		}
	}

	private class InfoEditAction extends EditDialogAction {

		InfoEditAction() {
			super("Info");
		}

		@Override
		public void onClick(final AlertDialog dialog, final String text) {
			PromoCodes.get(text, result -> {
				dialog.dismiss();
				showInfo(result);
			}, exception -> {
				Toast.makeText(getContext(), "Failed to get Promo Code: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
			});
		}
	}

	private class ClaimEditAction extends EditDialogAction {

		ClaimEditAction() {
			super("Claim");
		}

		@Override
		public void onClick(final AlertDialog dialog, final String text) {
			PromoCodes.claim(text, result -> {
				dialog.dismiss();
				Toast.makeText(getContext(), "Promo Code claimed:" + result.getCode(), Toast.LENGTH_SHORT).show();
				showInfo(result);
			}, exception -> {
				Toast.makeText(getContext(), "Failed to claim Promo Code: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
			});
		}
	}
}
