/*
*    	Copyright 2015-2017 GetSocial B.V.
*
*	Licensed under the Apache License, Version 2.0 (the "License");
*	you may not use this file except in compliance with the License.
*	You may obtain a copy of the License at
*
*    	http://www.apache.org/licenses/LICENSE-2.0
*
*	Unless required by applicable law or agreed to in writing, software
*	distributed under the License is distributed on an "AS IS" BASIS,
*	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*	See the License for the specific language governing permissions and
*	limitations under the License.
*/

package im.getsocial.demo.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import im.getsocial.demo.adapter.EnabledCheck;
import im.getsocial.demo.adapter.MenuItem;
import im.getsocial.demo.utils.EditTextWOCopyPaste;
import im.getsocial.demo.utils.PixelUtils;
import im.getsocial.demo.utils.UserIdentityUtils;
import im.getsocial.sdk.CompletionCallback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialException;
import im.getsocial.sdk.usermanagement.AuthIdentityProviderIds;

import static com.squareup.picasso.Picasso.with;

public class UserManagementFragment extends BaseListFragment {

	private static final int MAX_WIDTH = 500;
	private static final int REQUEST_PICK_AVATAR = 0x1;

	public UserManagementFragment() {
	}

	protected void invalidateUi() {
		invalidateList();
		_activityListener.invalidateUi();
	}

	@NonNull
	protected List<MenuItem> createListData() {
		List<MenuItem> listData = new ArrayList<>();

		listData.add(new MenuItem.Builder("Change Display Name")
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						changeDisplayName();
					}
				})
				.build());

		listData.add(new MenuItem.Builder("Change User Avatar")
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						changeUserAvatar();
					}
				})
				.build());

		listData.add(new MenuItem.Builder("Choose Avatar")
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						pickImageFromDevice(REQUEST_PICK_AVATAR);
					}
				})
				.build());

		listData.add(new MenuItem.Builder("Add Facebook user identity")
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						addFacebookUserIdentity(new CompletionCallback() {

							@Override
							public void onSuccess() {
								invalidateUi();
							}

							@Override
							public void onFailure(GetSocialException exception) {
								_log.logErrorAndToast("Authorization failed with exception: " + exception.getMessage());
							}
						});
					}
				})
				.withEnabledCheck(new EnabledCheck() {
					@Override
					public boolean isOptionEnabled() {
						return !GetSocial.User.getAuthIdentities().containsKey(AuthIdentityProviderIds.FACEBOOK);
					}
				})
				.build());

		listData.add(new MenuItem.Builder("Add Custom user identity")
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						addCustomUserIdentity(new CompletionCallback() {
							@Override
							public void onSuccess() {
								invalidateUi();
							}

							@Override
							public void onFailure(GetSocialException exception) {
								_log.logErrorAndToast("Authorization failed with exception: " + exception.getMessage());
							}
						});
					}
				})
				.withEnabledCheck(new EnabledCheck() {
					@Override
					public boolean isOptionEnabled() {
						return !GetSocial.User.getAuthIdentities().containsKey(CUSTOM_PROVIDER);
					}
				})
				.build());

		listData.add(new MenuItem.Builder("Remove Facebook user identity")
				.withSubtitle("Log out from Facebook")
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						removeFacebookUserIdentity();
					}
				})
				.withEnabledCheck(new EnabledCheck() {
					@Override
					public boolean isOptionEnabled() {
						return GetSocial.User.getAuthIdentities().containsKey(AuthIdentityProviderIds.FACEBOOK);
					}
				})
				.build());

		listData.add(new MenuItem.Builder("Remove Custom user identity")
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						removeCustomUserIdentity();
					}
				})
				.withEnabledCheck(new EnabledCheck() {
					@Override
					public boolean isOptionEnabled() {
						return GetSocial.User.getAuthIdentities().containsKey(CUSTOM_PROVIDER);
					}
				})
				.build());

		listData.add(new MenuItem.Builder("Add property")
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						setPublicProperty();
					}
				}).build()
		);

		listData.add(new MenuItem.Builder("Get property")
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						getPublicProperty();
					}
				}).build()
		);

		listData.add(new MenuItem.Builder("Log out")
				.withAction(new MenuItem.Action() {
					@Override
					public void execute() {
						logOut();
					}
				}).build()
		);
		return listData;

	}

	@Override
	protected void onImagePickedFromDevice(Uri imageUri, int requestCode) {
		if (requestCode == REQUEST_PICK_AVATAR) {
			with(getContext())
					.load(imageUri)
					.resize(MAX_WIDTH, 0)
					.memoryPolicy(MemoryPolicy.NO_CACHE)
					.into(new Target() {
						@Override
						public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
							setAvatarBitmap(bitmap);
						}

						@Override
						public void onBitmapFailed(Drawable errorDrawable) {
							showAlert("Error", "Failed to load image");
						}

						@Override
						public void onPrepareLoad(Drawable placeHolderDrawable) {

						}
					});
		}
	}

	private void setAvatarBitmap(Bitmap bitmap) {
		GetSocial.User.setAvatar(bitmap, new SafeCompletionCallback() {
			@Override
			public void onSafeSuccess() {
				_activityListener.invalidateUi();
				Toast.makeText(getContext(), "Avatar has been changed successfully!", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onSafeFailure(GetSocialException exception) {
				Toast.makeText(getContext(), "Error changing avatar: \n" + exception.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void getPublicProperty() {
		final EditTextWOCopyPaste keyInput = new EditTextWOCopyPaste(getContext());
		keyInput.setContentDescription("public_property_key");
		keyInput.setLongClickable(false);
		final int _8dp = PixelUtils.dp2px(getContext(), 8);
		FrameLayout frameLayout = new FrameLayout(getContext());
		frameLayout.setPadding(_8dp, _8dp, _8dp, _8dp);
		frameLayout.addView(keyInput);

		new AlertDialog.Builder(getContext())
				.setView(frameLayout)
				.setTitle("User Property")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialogInterface, int which) {
						Toast.makeText(getContext(), keyInput.getText().toString() + " = " + GetSocial.User.getPublicProperty(keyInput.getText().toString()), Toast.LENGTH_SHORT).show();
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int which) {
						dialogInterface.cancel();
					}
				})
				.create()
				.show();
	}

	private void setPublicProperty() {
		final EditTextWOCopyPaste keyInput = new EditTextWOCopyPaste(getContext());
		keyInput.setLongClickable(false);
		final EditTextWOCopyPaste valInput = new EditTextWOCopyPaste(getContext());
		valInput.setLongClickable(false);

		keyInput.setHint("Key");
		keyInput.setContentDescription("public_property_key");
		valInput.setHint("Value");
		valInput.setContentDescription("public_property_value");

		final int _8dp = PixelUtils.dp2px(getContext(), 8);
		LinearLayout frameLayout = new LinearLayout(getContext());
		frameLayout.setOrientation(LinearLayout.VERTICAL);
		frameLayout.setPadding(_8dp, _8dp, _8dp, _8dp);

		frameLayout.addView(keyInput);
		frameLayout.addView(valInput);

		new AlertDialog.Builder(getContext())
				.setView(frameLayout)
				.setTitle("User Property")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialogInterface, int which) {
						GetSocial.User.setPublicProperty(
								keyInput.getText().toString(),
								valInput.getText().toString(),
								new SafeCompletionCallback() {
									@Override
									public void onSafeSuccess() {
										dialogInterface.dismiss();
										_activityListener.invalidateUi();
										Toast.makeText(getContext(), "Public property has been changed successfully!", Toast.LENGTH_SHORT).show();
									}

									@Override
									public void onSafeFailure(GetSocialException exception) {
										Toast.makeText(getContext(), "Error changing public property: \n" + exception.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
									}
								}
						);
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int which) {
						dialogInterface.cancel();
					}
				})
				.create()
				.show();
	}

	//region Presenter

	private void changeUserAvatar() {
		GetSocial.User.setAvatarUrl(UserIdentityUtils.getRandomAvatar(),
				new SafeCompletionCallback() {
					@Override
					public void onSafeSuccess() {
						_activityListener.invalidateUi();
						Toast.makeText(getContext(), "Avatar has been changed successfully!", Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onSafeFailure(GetSocialException exception) {
						Toast.makeText(getContext(), "Error changing avatar: \n" + exception.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
					}
				});
	}

	private void changeDisplayName() {
		final EditTextWOCopyPaste displayNameInput = new EditTextWOCopyPaste(getContext());
		displayNameInput.setLongClickable(false);

		final int _8dp = PixelUtils.dp2px(getContext(), 8);
		FrameLayout frameLayout = new FrameLayout(getContext());
		frameLayout.setPadding(_8dp, _8dp, _8dp, _8dp);
		frameLayout.addView(displayNameInput);

		displayNameInput.setText(UserIdentityUtils.getDisplayName());
		displayNameInput.setSelection(displayNameInput.getText().length());
		displayNameInput.setContentDescription("display_name_input");

		new AlertDialog.Builder(getContext())
				.setView(frameLayout)
				.setTitle("User Display Name")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialogInterface, int which) {
						GetSocial.User.setDisplayName(displayNameInput.getText().toString(),
								new SafeCompletionCallback() {
									@Override
									public void onSafeSuccess() {
										dialogInterface.dismiss();
										_activityListener.invalidateUi();
										Toast.makeText(getContext(), "Display name has been changed successfully!", Toast.LENGTH_SHORT).show();
									}

									@Override
									public void onSafeFailure(GetSocialException exception) {
										Toast.makeText(getContext(), "Error changing display name: \n" + exception.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
									}
								}
						);
					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int which) {
						dialogInterface.cancel();
					}
				})
				.create()
				.show();
	}

	private void removeFacebookUserIdentity() {
		removeUserIdentity(AuthIdentityProviderIds.FACEBOOK);

		disconnectFromFacebook();
	}

	private void removeCustomUserIdentity() {
		removeUserIdentity(CUSTOM_PROVIDER);
	}

	private void logOut() {
		showLoading("Log Out", "Wait...");
		GetSocial.User.reset(new CompletionCallback() {
			@Override
			public void onSuccess() {
				invalidateUi();
				hideLoading();
				Toast.makeText(getContext(), "User has been successfully logged out!", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFailure(GetSocialException exception) {
				hideLoading();
				Toast.makeText(getContext(), "Failed to log out user, error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}
	//endregion

	//region helpers
	private void removeUserIdentity(final String providerId) {
		GetSocial.User.removeAuthIdentity(
				providerId,
				new SafeCompletionCallback() {
					@Override
					public void onSafeSuccess() {
						invalidateUi();
						_log.logInfoAndToast(String.format("Successfully removed user identity '%s'", providerId));
					}

					@Override
					public void onSafeFailure(GetSocialException exception) {
						_log.logErrorAndToast(String.format("Failed to remove user identity '%s', error: %s", providerId, exception.getMessage()));
					}

				}
		);
	}

	@Override
	public String getTitle() {
		return "User Management";
	}

	@Override
	public String getFragmentTag() {
		return "usermanagement";
	}


	//endregion
}
