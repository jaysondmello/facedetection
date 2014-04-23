package com.example.android.fragments;

class Point
{
	public double x; 
	public double y;
	
	public Point()
	{
		x  = 0;
		y =  0;
	}
}

class Face
{	
	public Point []facePoints;
	public Boolean gotMouth;
	public Point []mouthPoints;
	public Boolean gotRightEye;
	public Point []rightEye;
	public Boolean gotLeftEye;
	public Point []leftEye;
	
	public Face()
	{
		facePoints = new Point[2];
			
		gotMouth = false;
		mouthPoints = new Point[2];

		gotRightEye = false;
		rightEye = new Point[2];
		
		gotLeftEye = false;
		leftEye = new Point[2];	
		
		for(int n = 0; n < 2; n++)
		{
			facePoints[n] =  new Point();
			mouthPoints[n] = new Point();
			rightEye[n] = new Point();
			leftEye[n] = new Point();
		}
		
	}
	
	
}


public interface PointInterface {
	
	public void sendfromSource(Face[] val);
	
	
	
}
