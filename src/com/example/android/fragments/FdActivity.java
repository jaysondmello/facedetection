package com.example.android.fragments;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

class Coordinates
{
	int x;
	int y;
}

public class FdActivity extends Fragment implements CvCameraViewListener2 {

    private static final String    TAG                 = "FdActivity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(230, 230, 0, 255);
    private static final Scalar    MOUTH_RECT_COLOR     = new Scalar(245, 10, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;
    
    // Screen Co-ordinates Export
    public Coordinates[] eyeScreenCoordinates;// Eye Screen Co-ordinates, even numbers should be left eyes, odd = right

    private MenuItem               mItemFace50;
    private MenuItem               mItemFace40;
    private MenuItem               mItemFace30;
    private MenuItem               mItemFace20;
    private MenuItem               mItemType;

    private Mat                    mRgba;
    private Mat                    mGray;
    private File                   mCascadeFile;
    private File                   mCascadeFileEye;
    private File                   mCascadeFileMouth;
    private CascadeClassifier      mJavaDetector;
    private DetectionBasedTracker  mNativeDetectorFace;
    private DetectionBasedTracker  mNativeDetectorEye;
    private DetectionBasedTracker  mNativeDetectorMouth;

    private int                    mDetectorType       = NATIVE_DETECTOR; // make NATIVE detector as defualt
    private String[]               mDetectorName;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;

    private CameraBridgeViewBase   mOpenCvCameraView;
    private PointInterface 		   cordinateData;
    
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(getActivity()) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("detection_based_tracker");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getActivity().getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        mNativeDetectorFace = new DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0); // native detector for face called here
                        cascadeDir.delete();
                        /************************************/
                        
                        is = getResources().openRawResource(R.raw.haarcascade_eye);
                        File cascadeDir2 = getActivity().getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFileEye = new File(cascadeDir2, "haarcascade_eye.xml");
                        os = new FileOutputStream(mCascadeFileEye);
                        buffer = new byte[4096];
                       while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFileEye.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier for eye detection");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier for eye from " + mCascadeFileEye.getAbsolutePath());
                                                
                        
                        mNativeDetectorEye =  new DetectionBasedTracker(mCascadeFileEye.getAbsolutePath(), 0); // native detector for face called here
                        cascadeDir2.delete();
                        /************************************/
                        
                        is = getResources().openRawResource(R.raw.haarcascade_mcs_mouth);
                        File cascadeDir3 = getActivity().getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFileMouth = new File(cascadeDir2, "haarcascade_mcs_mouth.xml");
                        os = new FileOutputStream(mCascadeFileMouth);
                        buffer = new byte[4096];
                       while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFileMouth.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier for mouth detection");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier for mouth from " + mCascadeFileMouth.getAbsolutePath());
                                                
                        
                        mNativeDetectorMouth =  new DetectionBasedTracker(mCascadeFileMouth.getAbsolutePath(), 0); // native detector for face called here
                        cascadeDir3.delete();
                       

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public FdActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
    	return inflater.inflate(R.layout.face_detect_surface_view, container, false);
    }

    /** Called when the activity is first created. */
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        cordinateData = (PointInterface)activity; 
       
    }
    
    @Override
    public void onStart() {
        Log.i(TAG, "called onStart");
        super.onStart(); // send the bundle to the super class Activity
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

       // setContentView(R.layout.face_detect_surface_view);


        mOpenCvCameraView = (CameraBridgeViewBase) getView().findViewById(R.id.fd_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, getActivity(), mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }
    
    public double calcScreen2Cart(double val, double screenSize, int type)
    {
    	double answer;
    	
    	if(type == 1) // x coordinate
    	{
    		answer = (val - screenSize/2);
    		answer /= screenSize;
    		return answer*1.8;
    	}
    	else
    	{
    		answer = ((screenSize/2) - val)/screenSize; // 0 for y corindates
    		return answer;
    	}
    }

    @SuppressWarnings("null")
    // Frame Width = 1280 Frame Height = 720 [ Screen Co-ordinates ]
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba(); // copy the input rgba frame
        mGray = inputFrame.gray(); //copy the input gray frame

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            mNativeDetectorFace.setMinFaceSize(mAbsoluteFaceSize);
        }
        
       double frameWidth =  mGray.width();
       double frameHeight = mGray.rows();
      Log.i(TAG,"Frame Width = "+mGray.width() + "Frame Height = " + mGray.rows()); // get Frame Width and Height 544/544

        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
        else if (mDetectorType == NATIVE_DETECTOR) {
            if (mNativeDetectorFace != null)
            {
                mNativeDetectorFace.detect(mGray, faces); // send to native detector with faces as in the input [image of Mat, MatOfRect output array]
            }
        }
        else {
            Log.e(TAG, "Detection method is not selected!");
        }

       Rect[] facesArray = faces.toArray();

        for (int i = 0; i < facesArray.length; i++)
        {
        	Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3); // draw a rectangle
        }
        
       
        Face[] sendObj = 	 new Face[facesArray.length]; 
        // init all objects
        	for(int z = 0; z< sendObj.length;z++)
        	{
        		sendObj[z] = new Face();
        	} 
        
        
        // send face data to OpenGL
       for (int i = 0; i <  facesArray.length; i++ )
       {
        	sendObj[i].facePoints[0].x = calcScreen2Cart(facesArray[i].tl().x, frameWidth,1);
        	sendObj[i].facePoints[0].y = calcScreen2Cart(facesArray[i].tl().y, frameHeight,0);
        	sendObj[i].facePoints[1].x = calcScreen2Cart(facesArray[i].br().x, frameWidth,1); 
        	sendObj[i].facePoints[1].y = calcScreen2Cart(facesArray[i].br().y,frameHeight, 0);     	
        }
    
        
        
       // eye detection in face
        if (facesArray.length > 0)
        {
                 
        	
        	for(int i = 0; i < facesArray.length; i++)
        	{
        		// will divide face detected into two halves, mouth detection in lower half
        		int upperHalfx = (int)facesArray[i].tl().x;
        		int upperHalfy = (int)facesArray[i].tl().y;
        		int upperHalfwidth = (int)facesArray[i].width;
        		int upperHalfheight = (int)facesArray[i].height -(int)facesArray[i].height/3  ;
        		
        		
        		Rect roi = new Rect(upperHalfx,upperHalfy,upperHalfwidth,upperHalfheight);
        		MatOfRect eyes = new MatOfRect();
        		Mat cropped = new Mat();
        	
        		try{ // error occurred in eye detection, we will return now and try next time
        		cropped = mGray.submat(roi);
        		}
        		catch(Exception e)
        		{
        			Log.e(TAG, "Exception Occured in Eye Detection");
        			 return mRgba;
        		}
        		
        		Mat val = new Mat();
        		cropped.copyTo(val);
        	
        		 int eyeSize = 0;
        		 
        		 if (Math.round(val.rows() * mRelativeFaceSize) > 0) {
        			 eyeSize = Math.round(val.rows() * mRelativeFaceSize);
                 }
        		 mNativeDetectorEye.setMinFaceSize(eyeSize);
        		       		
        		
        		if (mNativeDetectorEye != null)
        			mNativeDetectorEye.detect(val, eyes);
        		else
        			Log.i("Fdvuew","mEyeDetector is NULL");
        	
        		Rect[] eyesArray;
        		eyesArray = eyes.toArray();
        		//Log.i("Fdvuew","Eyes Count"+eyesArray.length);
        		
        		eyeScreenCoordinates = new Coordinates[5]; 
        		
        		
                for (int j = 0; j < eyesArray.length; j++)
                {
                	eyesArray[j].x += facesArray[i].x;
                	eyesArray[j].y += facesArray[i].y;
                	
                	Core.rectangle(mRgba, eyesArray[j].tl(), eyesArray[j].br(), FACE_RECT_COLOR, 3); // draw a rectangle  

                	//Log.i(TAG, "Eye Found at ("+eyesArray[j].x+","+eyesArray[j].y+")");
                }
              
   
               for (int j = 0; j < eyesArray.length; j++)           // left and right eye
                {
                	if(j==0)
                	{
                	sendObj[i].gotRightEye = true;
                	sendObj[i].rightEye[0].x = calcScreen2Cart(eyesArray[j].tl().x,frameWidth,1);
                	sendObj[i].rightEye[0].y = calcScreen2Cart(eyesArray[j].tl().y,frameHeight,0);
                	sendObj[i].rightEye[1].x = calcScreen2Cart(eyesArray[j].br().x,frameWidth,1);
                	sendObj[i].rightEye[1].y = calcScreen2Cart(eyesArray[j].br().y,frameHeight,0);
                	}
                	if(j==1)
                	{
                	sendObj[i].gotLeftEye = true;
                    sendObj[i].leftEye[0].x = calcScreen2Cart(eyesArray[j].tl().x,frameWidth,1);
                    sendObj[i].leftEye[0].y = calcScreen2Cart(eyesArray[j].tl().y,frameHeight,0);
                    sendObj[i].leftEye[1].x = calcScreen2Cart(eyesArray[j].br().x,frameWidth,1);
                    sendObj[i].leftEye[1].y = calcScreen2Cart(eyesArray[j].br().y,frameHeight,0);	
                	}              
                }            
        	}
        	
        	
        	 // mouth detection in face
            if (facesArray.length > 0)
            {
            	for(int i = 0; i < facesArray.length; i++)
            	{
            		// will divide face detected into two halves, mouth detection in lower half
            		int lowerHalfx = (int)facesArray[i].tl().x;
            		int lowerHalfy = (int)facesArray[i].tl().y + (int)facesArray[i].height / 2 ;
            		int lowerHalfwidth = (int)facesArray[i].width;
            		int lowerHalfheight = (int)facesArray[i].height/2;
            		
            		
                    Rect roi = new Rect(lowerHalfx,lowerHalfy,lowerHalfwidth,lowerHalfheight);
            		MatOfRect mouth = new MatOfRect();
            		Mat cropped = new Mat();
            	
            		try{
                		cropped = mGray.submat(roi);
                		}
                		catch(Exception e)
                		{
                			Log.e(TAG, "Exception Occured in Mouth Detection");
                			 return mRgba;
                		}
            		
            	
            		Mat val = new Mat();
            		cropped.copyTo(val);
            	
            		 int mouthSize = 0;
            		 
            		 if (Math.round(val.rows() * mRelativeFaceSize) > 0) {
            			 mouthSize = Math.round(val.rows() * mRelativeFaceSize);
                     }
            		 mNativeDetectorMouth.setMinFaceSize(mouthSize);
            		       		
            		
            		if (mNativeDetectorMouth != null)
            			mNativeDetectorMouth.detect(val, mouth);
            		else
            			Log.i("Fdvuew","mMouthDetector is NULL");
            	
            		Rect[] mouthArray;
            		mouthArray = mouth.toArray();
            		//Log.i("Fdvuew","Mouth Count"+eyesArray.length);
            		

                    for (int j = 0; j < mouthArray.length; j++)
                    {
                    	mouthArray[j].x += lowerHalfx; // add offset of faces to the detected mouth / eye co-ordinates
                    	mouthArray[j].y += lowerHalfy;
	
                    	Core.rectangle(mRgba, mouthArray[j].tl(), mouthArray[j].br(), MOUTH_RECT_COLOR, 3); // draw a rectangle        
                    	
                    	// send mouth co-oridinates
                    	sendObj[i].gotMouth = true;
                    	sendObj[i].mouthPoints[0].x = calcScreen2Cart(mouthArray[j].tl().x,frameWidth,1);
                    	sendObj[i].mouthPoints[0].y = calcScreen2Cart(mouthArray[j].tl().y,frameHeight,0);
                    	sendObj[i].mouthPoints[1].x = calcScreen2Cart(mouthArray[j].br().x,frameWidth,1);
                    	sendObj[i].mouthPoints[1].y = calcScreen2Cart(mouthArray[j].br().y,frameHeight,0);   	
                    }              
            	}
            }
      	
        }
          
        
        
        // debug display 
        for (int i = 0; i <  facesArray.length; i++ )
        {
        	Log.i(TAG, "Face Co-ordinates TL"+ sendObj[i].facePoints[0].x + "," +  sendObj[i].facePoints[0].y);
        	Log.i(TAG, "Face Co-ordinates BR"+ sendObj[i].facePoints[1].x + "," +  sendObj[i].facePoints[1].y);
        	
        	if(sendObj[i].gotMouth == true)
        	{
        		Log.i(TAG, "Mouth Co-ordinates"+ sendObj[i].mouthPoints[0].x + "," +  sendObj[i].mouthPoints[0].y);
        		Log.i(TAG, "Mouth Co-ordinates"+ sendObj[i].mouthPoints[1].x + "," +  sendObj[i].mouthPoints[1].y);
        	}
        }
        
        
        // send data if available
        if(sendObj.length > 0)
        {
        	cordinateData.sendfromSource(sendObj);
        	Log.i(TAG, "Data Sent from FdActivity to Texture View");
        }
        
        
        return mRgba; // mRgba is the final frame, maybe send this to openGL ?
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemFace50 = menu.add("Face size 50%");
        mItemFace40 = menu.add("Face size 40%");
        mItemFace30 = menu.add("Face size 30%");
        mItemFace20 = menu.add("Face size 20%");
        mItemType   = menu.add(mDetectorName[mDetectorType]);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemFace50)
            setMinFaceSize(0.5f);
        else if (item == mItemFace40)
            setMinFaceSize(0.4f);
        else if (item == mItemFace30)
            setMinFaceSize(0.3f);
        else if (item == mItemFace20)
            setMinFaceSize(0.2f);
        else if (item == mItemType) {
            int tmpDetectorType = (mDetectorType + 1) % mDetectorName.length;
            item.setTitle(mDetectorName[tmpDetectorType]);
            setDetectorType(tmpDetectorType);
        }
        return true;
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    private void setDetectorType(int type) {
        if (mDetectorType != type) {
            mDetectorType = type;

            if (type == NATIVE_DETECTOR) {
                Log.i(TAG, "Detection Based Tracker enabled");
                mNativeDetectorFace.start();
            } else {
                Log.i(TAG, "Cascade detector enabled");
                mNativeDetectorFace.stop();
            }
        }
    }

}
