package im.getsocial.demo;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;
import com.appsflyer.AppsFlyerLib;

/**
 * Created by orestsavchak on 1/4/18.
 */

public class DemoApplication extends Application {

	private static final String ADJUST_TOKEN = "im.getsocial.demo.adjust.AppToken";

	@Override
	public void onCreate() {
		super.onCreate();
		configureAdjust();
		configureAppsFlyer();
	}

	private void configureAdjust() {
		String appToken = getTokenFromMetaData(ADJUST_TOKEN);
		if (appToken == null) {
			return;
		}
		String environment = AdjustConfig.ENVIRONMENT_PRODUCTION;
		AdjustConfig config = new AdjustConfig(this, appToken, environment);
		Adjust.onCreate(config);

		registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());
	}

	private void configureAppsFlyer() {
		AppsFlyerLib.getInstance().setDebugLog(true);
		AppsFlyerLib.getInstance().init("AP8P3GvwHgw9NBdBTWAqrb", null, getApplicationContext());
		AppsFlyerLib.getInstance().startTracking(this);
	}

	private String getTokenFromMetaData(String key) {
		try {
			return getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData.getString(key);
		} catch (PackageManager.NameNotFoundException e) {
			return null;
		}
	}

	private static class AdjustLifecycleCallbacks implements ActivityLifecycleCallbacks {
		@Override
		public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

		}

		@Override
		public void onActivityStarted(Activity activity) {

		}

		@Override
		public void onActivityResumed(Activity activity) {
			Adjust.onResume();
		}

		@Override
		public void onActivityPaused(Activity activity) {
			Adjust.onPause();
		}

		@Override
		public void onActivityStopped(Activity activity) {

		}

		@Override
		public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

		}

		@Override
		public void onActivityDestroyed(Activity activity) {

		}
	}
}
