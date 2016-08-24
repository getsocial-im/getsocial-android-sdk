package im.getsocial.testapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by tsvetomirstanchev on 30/03/16.
 */
public class Reachability
{
	private static Context context;
	private static ConnectivityManager connectivityManager;
	private static ConnectivityBroadcastReceiver connectivityBroadcastReceiver;

	private static volatile boolean isConnected;

	public static synchronized void onResume(Context context)
	{
		if(Reachability.context == null)
		{
			Reachability.context = context;

			connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			connectivityBroadcastReceiver = new ConnectivityBroadcastReceiver();

			updateState();

			context.registerReceiver(connectivityBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		}
	}

	public static synchronized void onPause()
	{
		if(Reachability.context != null)
		{
			context.unregisterReceiver(connectivityBroadcastReceiver);

			connectivityManager = null;
			connectivityBroadcastReceiver = null;

			Reachability.context = null;
		}
	}

	public static boolean isConnected()
	{
		return isConnected;
	}

	public static NetworkInfo getActiveNetworkInfo()
	{
		return connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
	}

	/**
	 * Temporarily force a value for isConnected.
	 */
	public static void forceState(boolean isConnected)
	{
		if(Reachability.isConnected != isConnected)
		{
			Reachability.isConnected = isConnected;

			callListeners();
		}
	}

	private static void updateState()
	{
		boolean isConnected = false;

		if(connectivityManager != null)
		{
			NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

			if(networkInfo != null)
			{
				isConnected = networkInfo.isConnected();
			}
		}

		if(Reachability.isConnected != isConnected)
		{
			Reachability.isConnected = isConnected;

			callListeners();
		}
	}

	// //////////////////////////////// //
	// OnInternetIsConnectedChangedListener
	// //////////////////////////////// //
	public interface OnInternetIsConnectedChangedListener
	{
		void onInternetIsConnectedChanged(boolean isConnected);
	}

	private static ArrayList<WeakReference<OnInternetIsConnectedChangedListener>> listeners = new ArrayList<>();

	public static void addOnIsConnectedChangedListener(OnInternetIsConnectedChangedListener listener)
	{
		listeners.add(new WeakReference<OnInternetIsConnectedChangedListener>(listener));
	}

	public static void removeOnIsConnectedChangedListener(OnInternetIsConnectedChangedListener listener)
	{
		for(Iterator<WeakReference<OnInternetIsConnectedChangedListener>> iterator = listeners.iterator(); iterator.hasNext(); )
		{
			WeakReference<OnInternetIsConnectedChangedListener> weakListener = iterator.next();

			if(weakListener.get() != null && weakListener.get() == listener)
			{
				iterator.remove();
			}
		}
	}

	public static void callListeners()
	{
		ArrayList<WeakReference<OnInternetIsConnectedChangedListener>> clonedReferences = new ArrayList<>(listeners);
		for(WeakReference<OnInternetIsConnectedChangedListener> weakListenerReference : clonedReferences)
		{
			final OnInternetIsConnectedChangedListener listener = weakListenerReference.get();
			if(listener != null)
			{
				listener.onInternetIsConnectedChanged(isConnected);
			}
			else
			{
				listeners.remove(weakListenerReference);
			}
		}
	}

	// //////////////////////////////// //
	// ConnectivityBroadcastReceiver // //
	// //////////////////////////////// //
	private static class ConnectivityBroadcastReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			updateState();
		}
	}
}
