/*
 * Published under the MIT License (MIT)
 * Copyright: (c) 2015 GetSocial B.V.
 */

package im.getsocial.testapp.ui;

import android.content.Context;
import android.content.Intent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import im.getsocial.testapp.ConsoleActivity;

public final class UiConsole
{
	private final static DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");
	
	private static UiConsole instance = new UiConsole();
	
	private List<LogMessage> messages;
	
	public UiConsole()
	{
		messages = new LinkedList<LogMessage>();
	}
	
	public static void logInfo(String message)
	{
		Log(message, Level.INFO);
	}
	
	
	public static void logWarning(String message)
	{
		Log(message, Level.WARNING);
	}
	
	public static void logError(String message)
	{
		Log(message, Level.ERROR);
	}
	
	public static void clear()
	{
		instance.messages.clear();
	}
	
	/**
	 * @return HTML formatted console output
	 */
	public static String getFormattedOutput()
	{
		List<UiConsole.LogMessage> messages = getMessages();
		
		StringBuilder sb = new StringBuilder();
		
		for(LogMessage message : messages)
		{
			sb.append(String.format("<font color=\"%s\"> %s %s %s</font><br>",
							message.level.color,
							TIMESTAMP_FORMAT.format(message.timestamp),
							message.level.toString().substring(0, 1),
							message.message
					)
			);
		}
		
		return sb.toString();
	}
	
	public static void showConsoleActivity(Context context)
	{
		Intent k = new Intent(context, ConsoleActivity.class);
		context.startActivity(k);
	}
	
	private static List<LogMessage> getMessages()
	{
		return Collections.unmodifiableList(instance.messages);
	}
	
	private static void Log(String message, Level level)
	{
		instance.messages.add(new LogMessage(message, level, new Date()));
	}
	
	public static class LogMessage
	{
		public final String message;
		public final Level level;
		public final Date timestamp;
		
		public LogMessage(String message, Level level, Date timestamp)
		{
			this.message = message;
			this.level = level;
			this.timestamp = timestamp;
		}
	}
	
	public enum Level
	{
		INFO("#8BC34A"), WARNING("#FBC02D"), ERROR("#F44336");
		
		public final String color;
		
		Level(String color)
		{
			this.color = color;
		}
	}
}
