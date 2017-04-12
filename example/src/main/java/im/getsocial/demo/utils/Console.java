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

package im.getsocial.demo.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public final class Console {

	private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());

	private static final List<LogMessage> LOG_MESSAGES = new LinkedList<>();

	public static void logInfo(String message) {
		log(message, Level.INFO);
	}

	public static void logWarning(String message) {
		log(message, Level.WARNING);
	}

	public static void logError(String message) {
		log(message, Level.ERROR);
	}

	public static void clear() {
		LOG_MESSAGES.clear();
	}

	/**
	 * @return HTML formatted console output.
	 */
	public static String getFormattedOutput() {
		List<LogMessage> messages = getMessages();

		StringBuilder sb = new StringBuilder();

		for (LogMessage message : messages) {
			sb.append(String.format("<font color=\"%s\"> %s %s %s</font><br>",
					message._level._color,
					TIMESTAMP_FORMAT.format(message._timestamp),
					message._level.toString().substring(0, 1),
					message._message
					)
			);
		}

		return sb.toString();
	}

	private static List<LogMessage> getMessages() {
		return Collections.unmodifiableList(LOG_MESSAGES);
	}

	private static void log(String message, Level level) {
		LOG_MESSAGES.add(new LogMessage(message, level, new Date()));
	}

	public enum Level {
		INFO("#8BC34A"), WARNING("#FBC02D"), ERROR("#F44336");

		public final String _color;

		Level(String color) {
			this._color = color;
		}
	}

	private static class LogMessage {
		final String _message;
		final Level _level;
		final Date _timestamp;

		LogMessage(String message, Level level, Date timestamp) {
			this._message = message;
			this._level = level;
			this._timestamp = timestamp;
		}
	}
}
