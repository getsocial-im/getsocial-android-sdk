package im.getsocial.demo;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public final class Utils {

	public static final String PROD = "Production";
	public static final String TESTING_SSL = "Testing SSL";
	public static final String TESTING = "Testing";

	// this should match what we have in HadesConfigurationProvider
	public static final List<String> HADES_CONFIGS = Arrays.asList(PROD, TESTING_SSL, TESTING);

	private static final String KEY_HADES_CONFIGS = "hades_configuration";
	private static final String KEY_CUSTOM_FB = "custom_fb";
	private static final String KEY_OPEN_UI = "open_ui";
	private static final String KEY_CUSTOM_ERROR_MESSAGE = "custom_error_message";
	private static final String KEY_REFERRAL_DATA_CHECK = "referral_data_check";
	private static final String TAG = "GETSOCIAL_RESTART";

	private Utils() {
		//
	}

	public static void restartApplication(@Nullable Context context) {
		try {
			if (context != null) {
				final PackageManager pm = context.getPackageManager();
				if (pm != null) {
					final Intent startActivityIntent = pm.getLaunchIntentForPackage(context.getPackageName());
					if (startActivityIntent != null) {
						startActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						final int mPendingIntentId = 424242;
						final PendingIntent pendingIntent = PendingIntent.getActivity(context, mPendingIntentId, startActivityIntent, PendingIntent.FLAG_CANCEL_CURRENT);
						final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
						alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pendingIntent);

						System.exit(0);
					} else {
						Log.e(TAG, "Was not able to restart application, mStartActivity null");
					}
				} else {
					Log.e(TAG, "Was not able to restart application, PM null");
				}
			} else {
				Log.e(TAG, "Was not able to restart application, Context null");
			}
		} catch (Exception exception) {
			Log.e(TAG, "Was not able to restart application");
			exception.printStackTrace();
		}
	}

	@SuppressLint("ApplySharedPref")
	public static void setHadesConfiguration(@Nullable Context context, String hadesConfiguration) {
		preferences(context).edit()
				.clear()
				.putInt(KEY_HADES_CONFIGS, HADES_CONFIGS.indexOf(hadesConfiguration))
				.commit();
	}

	@Nullable
	public static String getCurrentHadesConfiguration(@Nullable Context context) {
		final int currentType = preferences(context).getInt(KEY_HADES_CONFIGS, -1);
		return currentType == -1 ? null : HADES_CONFIGS.get(currentType);
	}

	public static boolean isCustomFacebookPlugin(Context context) {
		return preferences(context).getBoolean(KEY_CUSTOM_FB, true);
	}

	@SuppressLint("ApplySharedPref")
	public static void setCustomFacebookPlugin(Context context, boolean isCustomPlugin) {
		preferences(context).edit()
				.putBoolean(KEY_CUSTOM_FB, isCustomPlugin)
				.commit();
	}

	private static SharedPreferences preferences(@Nullable Context context) {
		if (context == null) {
			throw new IllegalArgumentException("Context is null");
		}
		return context.getSharedPreferences("getsocial", Context.MODE_PRIVATE);
	}

	@SuppressLint("ApplySharedPref")
	public static void setOpenUiImmediately(@Nullable Context context, boolean value) {
		preferences(context).edit()
				.putBoolean(KEY_OPEN_UI, value)
				.commit();
	}

	@SuppressLint("ApplySharedPref")
	public static void setForceBillingService(@Nullable Context context, int value) {
		preferences(context).edit()
				.putInt("force_billing_service", value)
				.commit();
	}

	@SuppressLint("ApplySharedPref")
	public static int getForceBillingService(@Nullable Context context) {
		return preferences(context).getInt("force_billing_service", 0);
	}

	public static String getPurchaseTrackingMethod(@Nullable Context context) {
		int currentValue = getForceBillingService(context);
		return currentValue == 0 ? "Billing Client" : "Billing Service";
	}

	public static boolean shouldOpenUiImmediately(Context context) {
		return preferences(context).getBoolean(KEY_OPEN_UI, false);
	}

	public static boolean isCustomErrorMessageEnabled(Context context) {
		return preferences(context).getBoolean(KEY_CUSTOM_ERROR_MESSAGE, false);
	}

	public static void setCustomErrorMessageEnabled(Context context, boolean newValue) {
		preferences(context).edit().putBoolean(KEY_CUSTOM_ERROR_MESSAGE, newValue).commit();
	}

	public static boolean isReferralDataCheckEnabled(Context context) {
		return preferences(context).getBoolean(KEY_REFERRAL_DATA_CHECK, false);
	}

	@SuppressLint("ApplySharedPref")
	public static void toggleReferralDataCheck(Context context) {
		final boolean currentState = isReferralDataCheckEnabled(context);
		preferences(context).edit()
				.putBoolean(KEY_REFERRAL_DATA_CHECK, !currentState)
				.commit();
	}
}
