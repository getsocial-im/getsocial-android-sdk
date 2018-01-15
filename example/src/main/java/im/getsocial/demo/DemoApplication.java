package im.getsocial.demo;

import android.app.Activity;
import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;

/**
 * Created by orestsavchak on 1/4/18.
 */

public class DemoApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		String appToken = getTokenFromMetaData();
		if (appToken == null) {
			return;
		}
		String environment = AdjustConfig.ENVIRONMENT_PRODUCTION;
		AdjustConfig config = new AdjustConfig(this, appToken, environment);
		Adjust.onCreate(config);

		registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());
	}

	private String getTokenFromMetaData() {
		ApplicationInfo app;
		try {
			app = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
		} catch (PackageManager.NameNotFoundException e) {
			return null;
		}
		Bundle bundle = app.metaData;
		return bundle.getString("im.getsocial.demo.adjust.AppToken");
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
