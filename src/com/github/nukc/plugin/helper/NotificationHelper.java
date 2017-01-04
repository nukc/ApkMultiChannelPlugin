package com.github.nukc.plugin.helper;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;

/**
 * Created by Nukc.
 */
public class NotificationHelper {

    public static void info(String message) {
        _notify(message, NotificationType.INFORMATION);
    }

    public static void warn(String message) {
        _notify(message, NotificationType.WARNING);
    }

    public static void error(String message) {
        _notify(message, NotificationType.ERROR);
    }

    private static void _notify(String message, NotificationType type) {
        Notification notification = new Notification(
                NotificationHelper.class.getPackage().getName(),
                "ApkMultiChannel", message, type);
        Notifications.Bus.notify(notification);
    }
}
