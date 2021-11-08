package im.getsocial.demo.utils;

import im.getsocial.sdk.pushnotifications.Notification;

public interface NotificationHandler {

	boolean handleNotification(Notification notification, NotificationContext context);

}
