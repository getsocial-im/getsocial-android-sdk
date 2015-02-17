package im.getsocial.testapp;

import im.getsocial.sdk.GetSocial;
import im.getsocial.sdk.UI.DefaultConfiguration;
import im.getsocial.testapp.animation.Block;
import im.getsocial.testapp.animation.Renderer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.SessionState;

import com.facebook.widget.LoginButton;

public class MainActivity extends Activity
{
	private static final String TAG_ONE = "tag1";
	private static final String GROUP_ONE = "group1";
	
	private static final List<String> FACEBOOK_PERMISSIONS = Arrays.asList("email", "user_friends");
	
	private GetSocial getSocial;
	private LinearLayout linearLayout;
	private TextView footer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		getSocial = GetSocial.getInstance(this);
		
		getSocial.registerPlugin("facebook", new FacebookInvitePlugin(this)
		{
			@Override
			public void authenticateUser()
			{
				logInWithFacebook();
			}
		});
		
		getSocial.authenticateGame("4We9Uqq8SR04tNXqV10M0000000s8i7N997ga98n", new GetSocial.OperationObserver()
		{
			@Override
			public void onSuccess(String data)
			{
				FacebookUtils.getInstance().updateSessionState();
				
				showToast("Game authenticated");
			}
			
			@Override
			public void onFailure()
			{
				showToast("Game not authenticated");
			}
		});
		
		getSocial.setOnLayerStateChangedListener(new GetSocial.OnLayerStateChangedListener()
		{
			@Override
			public void onOpen()
			{
				showToast("GetSocial open");
			}
			
			@Override
			public void onClose()
			{
				showToast("GetSocial close");
			}
		});
		
		getSocial.setOnLoginRequestListener(new GetSocial.OnLoginRequestListener()
		{
			@Override
			public void onLoginRequest()
			{
				String[] providers = {"Log in with Facebook"};
				
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setTitle(R.string.app_name);
				builder.setNegativeButton(android.R.string.cancel, null);
				builder.setItems(providers, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						switch(which)
						{
							case 0:
								logInWithFacebook();
								break;
						}
					}
				});
				builder.create().show();
			}
		});
		
		setContentView();
		
		Button activities = createButton("Standard Activities", true, new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				getSocial.open(GetSocial.VIEW_ACTIVITIES);
			}
		});
		
		Button isolatedActivities = createButton("Isolated Activities", true, new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Map<String, String> properties = new HashMap<String, String>();
				properties.put(GetSocial.PROPERTY_GROUP, GROUP_ONE);
				properties.put(GetSocial.PROPERTY_TAGS, TAG_ONE);
				
				getSocial.open(GetSocial.VIEW_ACTIVITIES, properties);
			}
		});
		
		addRow(activities, isolatedActivities);
		
		Button notification = createButton("Notification Center", true, new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				getSocial.open(GetSocial.VIEW_NOTIFICATIONS);
			}
		});
		
		Button smartInvites = createButton("Smart Invites", true, new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				getSocial.open(GetSocial.VIEW_INVITE);
			}
		});
		
		addRow(notification, smartInvites);
		
		addRow(initializeFacebookLoginButton(), null);
		
		try
		{
			footer.setText(getString(R.string.app_name) + " v." + getPackageManager().getPackageInfo(getPackageName(), 0).versionName + " / SDK v." + GetSocial.VERSION);
		}
		catch(PackageManager.NameNotFoundException e)
		{
		
		}
	}
	
	private void setContentView()
	{
		FrameLayout frameLayout = new FrameLayout(this);
		frameLayout.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		setContentView(frameLayout);
		
			final Renderer renderer = new Renderer(this, new Block());
			GLSurfaceView surface = new GLSurfaceView(this)
			{
				private float lastX;
				private float lastY;
				
				@Override
				public boolean onTouchEvent(MotionEvent event)
				{
					switch(event.getActionMasked())
					{
						case MotionEvent.ACTION_DOWN :
							
							renderer.animate = false;
							
							break;
							
						case MotionEvent.ACTION_MOVE :
							
							renderer.angleOnX += (event.getY() - lastY) / 10;
							renderer.deltaOnX = (event.getY() - lastY) / 10;
							
							renderer.angleOnY += (event.getX() - lastX) / 10;
							renderer.deltaOnY = (event.getX() - lastX) / 10;
							
							break;
							
						case MotionEvent.ACTION_UP :
						case MotionEvent.ACTION_CANCEL :
							
							renderer.animate = true;
							
							break;
					}
					
					lastX = event.getX();
					lastY = event.getY();
					
					return true;
				}
			};
			surface.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			surface.setRenderer(renderer);
			surface.onResume();
			frameLayout.addView(surface);
			
			linearLayout = new LinearLayout(this);
			linearLayout.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			linearLayout.setOrientation(LinearLayout.VERTICAL);
			frameLayout.addView(linearLayout);
			
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			layoutParams.gravity = Gravity.BOTTOM;
			footer = new TextView(this);
			footer.setLayoutParams(layoutParams);
			footer.setGravity(Gravity.CENTER_HORIZONTAL);
			footer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
			frameLayout.addView(footer);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		getSocial.onResume(this);
	}
	
	@Override
	protected void onPause()
	{
		getSocial.onPause();
		
		super.onPause();
	}
	
	@Override
	public void onBackPressed()
	{
		if(!getSocial.handleBackButtonPressed())
		{
			super.onBackPressed();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		
		getSocial.close();

		GetSocial.getInstance().getConfiguration().clear();
		DefaultConfiguration.load(this);
	}
	
	private void logInWithFacebook()
	{
		Session session = Session.getActiveSession();
		
		if(session != null && (session.isOpened() || session.getState().equals(SessionState.CREATED_TOKEN_LOADED)))
		{
			Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, FACEBOOK_PERMISSIONS);
			session.requestNewReadPermissions(newPermissionsRequest);
		}
		else
		{
			Session.setActiveSession((session = new Session(this)));
			
			Session.OpenRequest openRequest = new Session.OpenRequest(this);
			openRequest.setPermissions(FACEBOOK_PERMISSIONS);
			session.openForRead(openRequest);
		}
	}
	
	private View initializeFacebookLoginButton()
	{
		final LoginButton loginButton = new LoginButton(this);
		loginButton.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f));
		loginButton.setReadPermissions(FACEBOOK_PERMISSIONS);
		loginButton.setSessionStatusCallback(new Session.StatusCallback()
		{
			@Override
			public void call(Session session, SessionState state, Exception exception)
			{
				FacebookUtils.getInstance().updateSessionState();
			}
		});
		
		return loginButton;
	}
	
	private Button createButton(final String text, final boolean needsGameAuthenticated, final View.OnClickListener onClickListener)
	{
		Button button = new Button(this);
		button.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f));
		button.setText(text);
		button.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(needsGameAuthenticated && !getSocial.isGameAuthenticated())
				{
					Toast.makeText(view.getContext(), "Game not yet authenticated", Toast.LENGTH_SHORT).show();
				}
				else
				{
					onClickListener.onClick(view);
				}
			}
		});
		
		return button;
	}
	
	private void addRow(View button1, View button2)
	{
		LinearLayout layoutRow = new LinearLayout(this);
		LinearLayout.LayoutParams layoutRowParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutRow.setOrientation(LinearLayout.HORIZONTAL);
		layoutRow.setLayoutParams(layoutRowParams);
		
		layoutRow.addView(button1);
		
		if(button2 != null)
		{
			layoutRow.addView(button2);
		}
		
		linearLayout.addView(layoutRow);
	}
	
	private void showToast(final String message)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
			}
		});
	}
}