package im.getsocial.demo.dependencies;

import im.getsocial.demo.dependencies.components.Clipboard;
import im.getsocial.demo.dependencies.components.NotificationsManager;
import im.getsocial.demo.utils.NotificationHandler;
import im.getsocial.sdk.actions.ActionListener;

public interface DependenciesContainer {

	NotificationsManager notificationsManager();

	ActionListener actionListener();

	NotificationHandler notificationHandler();

	Clipboard clipboard();
}
