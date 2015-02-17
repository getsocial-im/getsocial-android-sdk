package im.getsocial.testapp.animation;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.DisplayMetrics;

import java.util.ArrayList;
import java.util.List;

import im.getsocial.testapp.R;

public class Block extends BlockBase
{
	private List<Integer> faces = new ArrayList<Integer>();
	
	private void loadCubeFaces()
	{
		faces.add(R.drawable.adam);
		faces.add(R.drawable.arangel);
		faces.add(R.drawable.barbara);
		faces.add(R.drawable.chirag);
		faces.add(R.drawable.damjan);
		faces.add(R.drawable.demian);
		faces.add(R.drawable.jon);
		faces.add(R.drawable.marco);
		faces.add(R.drawable.marieke);
		faces.add(R.drawable.milly);
		faces.add(R.drawable.pedro);
		faces.add(R.drawable.peter);
		faces.add(R.drawable.queralt);
		faces.add(R.drawable.reinout);
		faces.add(R.drawable.remco);
		faces.add(R.drawable.sergio);
		faces.add(R.drawable.snupi);
		faces.add(R.drawable.tsvetomir);
		faces.add(R.drawable.viral);
		faces.add(R.drawable.wessel);
	}
	
	private Bitmap getRandomFace(Resources resources)
	{
		return BitmapFactory.decodeResource(resources, faces.remove((int) (Math.random() * faces.size())));
	}
	
	private Bitmap getAppLogo(Resources resources)
	{
		return BitmapFactory.decodeResource(resources, R.drawable.logo);
	}
	
	private Bitmap flipAndRotate(Bitmap src)
	{
		Matrix matrix = new Matrix();
		matrix.preScale(-1, 1);
		matrix.preRotate(180);
		
		Bitmap dst = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, false);
		dst.setDensity(DisplayMetrics.DENSITY_DEFAULT);
		
		return dst;
	}
	
	@Override
	public Bitmap[] loadGLTextures(Resources resources)
	{
		loadCubeFaces();
		
		Bitmap[] bitmaps = new Bitmap[6];
		
		bitmaps[0] = getRandomFace(resources);
		bitmaps[1] = getRandomFace(resources);
		bitmaps[2] = getRandomFace(resources);
		bitmaps[3] = getRandomFace(resources);
		bitmaps[4] = getAppLogo(resources);
		bitmaps[5] = flipAndRotate(getAppLogo(resources));
		
		return bitmaps;
	}
}