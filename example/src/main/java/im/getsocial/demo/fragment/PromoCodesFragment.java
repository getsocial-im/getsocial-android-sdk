package im.getsocial.demo.fragment;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import im.getsocial.demo.adapter.MenuItem;
import im.getsocial.sdk.Callback;
import im.getsocial.sdk.CompletionCallback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialException;
import im.getsocial.sdk.invites.InviteContent;
import im.getsocial.sdk.invites.InviteTextPlaceholders;
import im.getsocial.sdk.invites.LinkParams;
import im.getsocial.sdk.promocodes.PromoCode;
import im.getsocial.sdk.promocodes.PromoCodeBuilder;
import im.getsocial.sdk.ui.GetSocialUi;

import java.util.Arrays;
import java.util.List;

public class PromoCodesFragment extends BaseListFragment {

	private static final String PROMO_CODE_PROPERTY = "my_promo_code";

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

	private class MyPromoCodeAction implements MenuItem.Action {

		@Override
		public void execute() {
			if (GetSocial.User.hasPrivateProperty(PROMO_CODE_PROPERTY)) {
				showPromoCodeDialog(GetSocial.User.getPrivateProperty(PROMO_CODE_PROPERTY));
			} else {
				showLoading("Promo Code", "Creating...");
				GetSocial.createPromoCode(PromoCodeBuilder.createRandomCode().addData("my_promo_code", "true"), new Callback<PromoCode>() {
					@Override
					public void onSuccess(final PromoCode result) {
						GetSocial.User.setPrivateProperty(PROMO_CODE_PROPERTY, result.getCode(), new CompletionCallback() {
							@Override
							public void onSuccess() {
								hideLoading();
								showPromoCodeDialog(result.getCode());
							}

							@Override
							public void onFailure(GetSocialException exception) {
								hideLoading();
								Toast.makeText(getContext(), "Failed to save a Promo Code: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
							}
						});
					}

					@Override
					public void onFailure(GetSocialException exception) {
						hideLoading();
						Toast.makeText(getContext(), "Failed to create a Promo Code: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
					}
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

	private static void shareCode(final String promoCode) {
		GetSocialUi.createInvitesView()
				.setCustomInviteContent(InviteContent.createBuilder().withText("Use my Promo Code to get a personal discount: " + promoCode + " . " + InviteTextPlaceholders.PLACEHOLDER_APP_INVITE_URL).build())
				.setLinkParams(new LinkParams() {
					{ put(LinkParams.KEY_PROMO_CODE, promoCode); }
				})
				.show();
	}

	private static void copyCode(Context context, String promoCode) {
		ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("GetSocial Promo Code", promoCode);
		clipboard.setPrimaryClip(clip);
	}

	private void showInfo(PromoCode promoCode) {
		new AlertDialog.Builder(getContext())
				.setTitle("Promo Code")
				.setMessage(formatInfo(promoCode))
				.setCancelable(true)
				.create()
				.show();
	}

	private static String formatInfo(PromoCode promoCode) {
		return String.format("code=%s\ndata=%s\nmaxClaim=%d\nclaimCount=%d\nstartDate=%s" +
						"\nendDate=%s\nenabled=%b\nclaimable=%b\ncreator=%s",
				promoCode.getCode(), promoCode.getData(), promoCode.getMaxClaimCount(),
				promoCode.getClaimCount(), promoCode.getStartDate(), promoCode.getEndDate(),
				promoCode.isEnabled(), promoCode.isClaimable(), promoCode.getCreator().getDisplayName());
	}

	private void showDialogWithEditText(String title, String hint, EditDialogAction... actions) {
		LinearLayout linearLayout = new LinearLayout(getContext());
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
			Button button = new Button(getContext());
			button.setText(dialogAction._name);
			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					dialogAction.onClick(alertDialog, inputView.getText().toString());
				}
			});
			linearLayout.addView(button);
		}

		alertDialog.show();
	}

	private void showDialog(String title, String contentText, DialogAction... actions) {
		LinearLayout linearLayout = new LinearLayout(getContext());
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
			Button button = new Button(getContext());
			button.setText(dialogAction._name);
			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					dialogAction.onClick(alertDialog);
				}
			});
			linearLayout.addView(button);
		}

		alertDialog.show();
	}

	private abstract static class DialogAction {
		final String _name;

		DialogAction(String name) {
			_name = name;
		}

		public abstract void onClick(AlertDialog dialog);
	}

	private class DismissAction extends DialogAction {

		DismissAction() {
			super("Dismiss");
		}

		@Override
		public void onClick(AlertDialog dialog) {
			dialog.dismiss();
		}
	}

	private class CopyAction extends DialogAction {
		private final String _code;

		CopyAction(String code) {
			super("Copy");
			_code = code;
		}

		@Override
		public void onClick(AlertDialog dialog) {
			copyCode(dialog.getContext(), _code);
			dialog.dismiss();
		}
	}

	private class ShareAction extends DialogAction {
		private final String _code;

		ShareAction(String code) {
			super("Share");
			_code = code;
		}

		@Override
		public void onClick(AlertDialog dialog) {
			dialog.dismiss();
			shareCode(_code);
		}
	}

	private class InfoAction extends DialogAction {
		private final String _code;

		InfoAction(String promoCode) {
			super("Info");
			_code = promoCode;
		}

		@Override
		public void onClick(final AlertDialog dialog) {
			showLoading("Promo Code", "Loading...");
			GetSocial.getPromoCode(_code, new Callback<PromoCode>() {
				@Override
				public void onSuccess(PromoCode result) {
					hideLoading();
					dialog.dismiss();
					showFullPromoCodeDialog(result);
				}

				@Override
				public void onFailure(GetSocialException exception) {
					hideLoading();
					Toast.makeText(getContext(), "Failed to load Promo Code info: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	private abstract static class EditDialogAction {
		final String _name;

		EditDialogAction(String name) {
			_name = name;
		}

		public abstract void onClick(AlertDialog dialog, String text);
	}

	private class DismissEditAction extends EditDialogAction {

		DismissEditAction() {
			super("Dismiss");
		}

		@Override
		public void onClick(AlertDialog dialog, String text) {
			dialog.dismiss();
		}
	}

	private class InfoEditAction extends EditDialogAction {

		InfoEditAction() {
			super("Info");
		}

		@Override
		public void onClick(final AlertDialog dialog, String text) {
			GetSocial.getPromoCode(text, new Callback<PromoCode>() {
				@Override
				public void onSuccess(PromoCode result) {
					dialog.dismiss();
					showInfo(result);
				}

				@Override
				public void onFailure(GetSocialException exception) {
					Toast.makeText(getContext(), "Failed to get Promo Code: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
				}
			});
		}
	}

	private class ClaimEditAction extends EditDialogAction {

		ClaimEditAction() {
			super("Claim");
		}

		@Override
		public void onClick(final AlertDialog dialog, String text) {
			GetSocial.claimPromoCode(text, new Callback<PromoCode>() {
				@Override
				public void onSuccess(PromoCode result) {
					dialog.dismiss();
					Toast.makeText(getContext(), "Promo Code claimed:" + result.getCode(), Toast.LENGTH_SHORT).show();
					showInfo(result);
				}

				@Override
				public void onFailure(GetSocialException exception) {
					Toast.makeText(getContext(), "Failed to claim Promo Code: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
}
