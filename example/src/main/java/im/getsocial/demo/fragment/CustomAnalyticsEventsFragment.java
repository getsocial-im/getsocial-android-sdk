package im.getsocial.demo.fragment;

import im.getsocial.demo.adapter.MenuItem;
import im.getsocial.sdk.Analytics;

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
												final Map<String, String> properties = new HashMap<>();
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
												final Map<String, String> properties = new HashMap<>();
												properties.put("achievement", "early_backer");
												properties.put("item", "car001");
												trackCustomEvent("achievement_unlocked", properties);
											}
										}).build()
		);
	}

	private void trackCustomEvent(final String eventName, final Map<String, String> eventProperties) {
		Analytics.trackCustomEvent(eventName, eventProperties);
	}

}

