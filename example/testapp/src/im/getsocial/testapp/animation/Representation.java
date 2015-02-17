package im.getsocial.testapp.animation;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;

public interface Representation
{
	public void loadGLTextures(GL10 gl, Context context);
	public void draw(GL10 gl);
}