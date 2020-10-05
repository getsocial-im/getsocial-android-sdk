package im.getsocial.demo.fragment;

import android.content.pm.ActivityInfo;
import android.widget.Toast;
import im.getsocial.demo.adapter.MenuItem;
import im.getsocial.sdk.CompletionCallback;
import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.GetSocialException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomAnalyticsEventsFragment extends BaseListFragment {
	@Override
	public String getFragmentTag() {
		return "customanalyticsevents";
	}

	@Override
	public String getTitle() {
		return "Custom Analytics Events";
	}

	@Override
	protected List<MenuItem> createListData() {
		return Arrays.asList(
				new MenuItem.Builder("Level Completed")
						.withAction(new MenuItem.Action() {
							@Override
							public void execute() {
								Map<String, String> properties = new HashMap<>();
								properties.put("level", "1");
								trackCustomEvent("level_completed", properties);
							}
						}).build(),
				new MenuItem.Builder("Tutorial Completed")
						.withAction(new MenuItem.Action() {
							@Override
							public void execute() {
								trackCustomEvent("tutorial_completed", null);
							}
						}).build(),
				new MenuItem.Builder("Achievement Unlocked")
						.withAction(new MenuItem.Action() {
							@Override
							public void execute() {
								Map<String, String> properties = new HashMap<>();
								properties.put("achievement", "early_backer");
								properties.put("item", "car001");
								trackCustomEvent("achievement_unlocked", properties);
							}
						}).build()
		);
	}

	private void trackCustomEvent(String eventName, Map<String, String> eventProperties) {
		if (GetSocial.trackCustomEvent(eventName, eventProperties)) {
			Toast.makeText(getContext(), "Custom event was tracked.", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getContext(), "Failed to track custom event.", Toast.LENGTH_SHORT).show();
		}
	}

}

