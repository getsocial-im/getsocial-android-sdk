package im.getsocial.testapp.animation;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.GLUtils;

public abstract class BlockBase implements Representation
{
	private int[] textures;
	private IntBuffer[] vertexBuffers;
	private IntBuffer[] texCoordBuffers;
	
	private int[][] vertexGroups = {
			{	// front face
				-0x10000,  0x10000,  0x10000,	// top left
				 0x10000,  0x10000,  0x10000,	// top right
				-0x10000, -0x10000,  0x10000,	// bottom left
				 0x10000, -0x10000,  0x10000	// bottom right
			},
			{	// back face
				 0x10000,  0x10000, -0x10000,	// top left
				-0x10000,  0x10000, -0x10000,	// top right
				 0x10000, -0x10000, -0x10000,	// bottom left
				-0x10000, -0x10000, -0x10000	// bottom right
			},
			{	// left face
				-0x10000,  0x10000, -0x10000,	// top left
				-0x10000,  0x10000,  0x10000,	// top right
				-0x10000, -0x10000, -0x10000,	// bottom left
				-0x10000, -0x10000,  0x10000	// bottom right
			},
			{	// right face
				 0x10000,  0x10000,  0x10000,	// top left
				 0x10000,  0x10000, -0x10000,	// top right
				 0x10000, -0x10000,  0x10000,	// bottom left
				 0x10000, -0x10000, -0x10000	// bottom right
			},
			{	// top face
				-0x10000,  0x10000, -0x10000,	// top left
				 0x10000,  0x10000, -0x10000,	// top right
				-0x10000,  0x10000,  0x10000,	// bottom left
				 0x10000,  0x10000,  0x10000	// bottom right
			},
			{	// bottom face
				 0x10000, -0x10000,  0x10000,	// top left
				-0x10000, -0x10000,  0x10000,	// top right
				 0x10000, -0x10000, -0x10000,	// bottom left
				-0x10000, -0x10000, -0x10000	// bottom right
			}
	};
	
	private int[][] texCoordGroups = {
			{	// front face
				 0,        0,		// top left
				 0x10000,  0,		// top right
				 0,		   0x10000,	// bottom left
				 0x10000,  0x10000 	// bottom right
			},
			{	// back face
				 0,        0,		// top left
				 0x10000,  0,		// top right
				 0,		   0x10000,	// bottom left
				 0x10000,  0x10000 	// bottom right
			},
			{	// left face
				 0,        0,		// top left
				 0x10000,  0,		// top right
				 0,		   0x10000,	// bottom left
				 0x10000,  0x10000 	// bottom right
			},
			{	// right face
				 0,        0,		// top left
				 0x10000,  0,		// top right
				 0,		   0x10000,	// bottom left
				 0x10000,  0x10000 	// bottom right
			},
			{	// top face
				 0,        0,		// top left
				 0x10000,  0,		// top right
				 0,		   0x10000,	// bottom left
				 0x10000,  0x10000 	// bottom right
			},
			{	// bottom face
				 0,        0,		// top left
				 0x10000,  0,		// top right
				 0,		   0x10000,	// bottom left
				 0x10000,  0x10000 	// bottom right
			}
	};

	public BlockBase()
	{
		vertexBuffers = new IntBuffer[vertexGroups.length];
		texCoordBuffers = new IntBuffer[texCoordGroups.length];
		
		for(int i = 0; i < vertexGroups.length; i++)
		{
			//vertices
			ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertexGroups[i].length * 4);
			byteBuffer.order(ByteOrder.nativeOrder());
			vertexBuffers[i] = byteBuffer.asIntBuffer();
			vertexBuffers[i].put(vertexGroups[i]);
			vertexBuffers[i].position(0);
			
			//texture coordinates
			byteBuffer = ByteBuffer.allocateDirect(texCoordGroups[i].length * 4);
			byteBuffer.order(ByteOrder.nativeOrder());
			texCoordBuffers[i] = byteBuffer.asIntBuffer();
			texCoordBuffers[i].put(texCoordGroups[i]);
			texCoordBuffers[i].position(0);
		}
	}
	
	/**
	 * Must return an array containing 6 Bitmaps for texture mapping on a Block. The Bitmaps should be in the following order:
	 * 
	 * <ul>
	 * <li>Front face</li>
	 * <li>Back face</li>
	 * <li>Left face</li>
	 * <li>Right face</li>
	 * <li>Top face</li>
	 * <li>Bottom face</li>
	 * </ul>
	 */
	public abstract Bitmap[] loadGLTextures(Resources resources);
	
	public void loadGLTextures(GL10 gl, Context context)
	{
		// get bitmaps from implementor
		Bitmap[] bitmaps = loadGLTextures(context.getResources());
		
		// let OpenGL generate bitmap ID's
		textures = new int[bitmaps.length];
		gl.glGenTextures(bitmaps.length, textures, 0);
		
		for(int i = 0; i < bitmaps.length; i++)
		{
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[i]);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
			
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmaps[i], 0);
			
			bitmaps[i].recycle();
		}
	}
	
	public void draw(GL10 gl)
	{
		// enable the client state for our buffers
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		
		for(int i = 0; i < vertexBuffers.length; i++)
		{
			// bind texture
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[i]);
			
			// announce buffers
			gl.glVertexPointer(3, GL10.GL_FIXED, 0, vertexBuffers[i]);
			gl.glTexCoordPointer(2, GL10.GL_FIXED, 0, texCoordBuffers[i]);
			
			// draw
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertexGroups[i].length / 3);
		}
		
		// disable the client state before leaving
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}
}