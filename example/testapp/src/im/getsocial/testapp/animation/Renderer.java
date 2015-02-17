package im.getsocial.testapp.animation;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;

public class Renderer implements android.opengl.GLSurfaceView.Renderer
{
	private Context context;
	private Representation representation;
	public volatile boolean animate = true;
	public volatile float angleOnY;
	public volatile float angleOnX;
	public volatile float deltaOnY = 1.5f;
	public volatile float deltaOnX = 0;
	
	public Renderer(Context context, Representation representation)
	{
		this.context = context;
		this.representation = representation;
	}
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
		// enable transparent textures
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		// load textures
		representation.loadGLTextures(gl, this.context);
		
		// configure OpenGL
		gl.glEnable(GL10.GL_TEXTURE_2D);			// enable Texture Mapping
		gl.glShadeModel(GL10.GL_SMOOTH); 			// enable Smooth Shading
		gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);	// white background
		gl.glEnable(GL10.GL_ALPHA_TEST);			// enable alpha testing to prevent transparent pixels writing to the z-buffer
		gl.glAlphaFunc(GL10.GL_GREATER, .1f);		// only pixels with an alpha level greater than .1f may write to the z-buffer
		gl.glClearDepthf(1.0f); 					// depth buffer setup
		gl.glEnable(GL10.GL_DEPTH_TEST); 			// enables Depth Testing
		gl.glDepthFunc(GL10.GL_LESS); 				// the type of depth testing to do
		
		// nicest perspective calculations
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
	}
	
	public void onSurfaceChanged(GL10 gl, int width, int height)
	{
		float widthRatio = (float) width / height;
		
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glFrustumf(-widthRatio, widthRatio, -1, 1, 3, 10);
	}
	
	public void onDrawFrame(GL10 gl)
	{
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glTranslatef(0, 0, -6.0f);
		
		gl.glRotatef(angleOnX + (float) Math.sin(angleOnY * .025) * 25, 1, 0, 0);
		gl.glRotatef(angleOnY, 0, 1, 0);
		
		representation.draw(gl);
		
		if(animate)
		{
			angleOnX += deltaOnX;
			angleOnY += deltaOnY;
			
			if(Math.abs(angleOnX) > 0)
			{
				angleOnX *= .95;
			}
			
			if(Math.abs(deltaOnX) > 0)
			{
				deltaOnX *= .95;
			}
			
			if(Math.abs(deltaOnY) > 1.5)
			{
				deltaOnY *= .95;
			}
		}
	}
}