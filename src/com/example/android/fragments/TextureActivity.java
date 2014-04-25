package com.example.android.fragments;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL11;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.support.v4.app.Fragment;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES10;
import android.opengl.GLES11;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;


// code : https://github.com/dalinaum/TextureViewDemo/blob/master/src/kr/gdg/android/textureview/GLTriangleActivity.java#L99
class RenderThread extends Thread {
    private static final int EGL_OPENGL_ES2_BIT = 4;
    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    private static final String TAG = "RenderThread";
    private SurfaceTexture mSurface;
    private EGLDisplay mEglDisplay;
    private EGLSurface mEglSurface;
    private EGLContext mEglContext;
    private int mProgram;
    private EGL10 mEgl;
    private GL11 mGl;
    private volatile boolean mFinished;
    private TextureView mTextureView;
    private Face[] dataPoints = null;
    private volatile boolean updatedData = false;
   
    private final float[] mVerticesData = {
            -1.0f, 1.0f, 0.0f, 
           +1.0f, +1.0f, 0.0f,
           1.0f, -1.0f, 0.0f,
            
           1.0f, -1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f
    };
    
    private float[][] facePoints;
    private float[][] mouthPoints;
    private float[][] rightEye;
    private float[][] leftEye;

    public RenderThread(SurfaceTexture surface) {
        mSurface = surface;
        
     
        
       
    }
   /* 
    tl.x,tl.y	 ------- br.x,tl.y
    			|		|
    			|		|	
    			|		|
    tl.x, br.y	------- br.x, br.y
    */
    public float[] buildRect(Face dataPoints, int part) //1 = face 2 = mouth 3 = left eye 4 = right eye 
    {
    	float [] holder = new float[18];
    	
    	if(part == 1)
    	{
    	holder[0] = (float)dataPoints.facePoints[0].x; // tl.x, tl.y 
    	holder[1] = (float)dataPoints.facePoints[0].y;     	  
    	holder[2] = (float) 0.0; 
   		
    	holder[3] = (float)dataPoints.facePoints[1].x; // br.x, tl.y 
    	holder[4] = (float)dataPoints.facePoints[0].y;     	  
    	holder[5] = (float)0.0; 
   	  
    	holder[6] = (float)dataPoints.facePoints[1].x; // br.x, br.y 
    	holder[7] = (float)dataPoints.facePoints[1].y;     	  
    	holder[8] = (float)0.0; 
   	  
    	holder[9] = (float)dataPoints.facePoints[1].x; //  br.x, br.y 
    	holder[10] = (float)dataPoints.facePoints[1].y;     	  
    	holder[11] = (float)0.0; 
   	  
    	holder[12] = (float)dataPoints.facePoints[0].x; // tl.x, br.y 
    	holder[13] = (float)dataPoints.facePoints[1].y;     	  
    	holder[14] = (float)0.0; 
   	  
    	holder[15] = (float)dataPoints.facePoints[0].x; // tl.x, tl.y 
    	holder[16] = (float)dataPoints.facePoints[0].y;     	  
    	holder[17] = (float)0.0; 
    	}
    	if(part == 2)
    	{
    	holder[0] = (float)dataPoints.mouthPoints[0].x; // tl.x, tl.y 
    	holder[1] = (float)dataPoints.mouthPoints[0].y;     	  
    	holder[2] = (float) 0.0; 
   		
    	holder[3] = (float)dataPoints.mouthPoints[1].x; // br.x, tl.y 
    	holder[4] = (float)dataPoints.mouthPoints[0].y;     	  
    	holder[5] = (float)0.0; 
   	  
    	holder[6] = (float)dataPoints.mouthPoints[1].x; // br.x, br.y 
    	holder[7] = (float)dataPoints.mouthPoints[1].y;     	  
    	holder[8] = (float)0.0; 
   	  
    	holder[9] = (float)dataPoints.mouthPoints[1].x; //  br.x, br.y 
    	holder[10] = (float)dataPoints.mouthPoints[1].y;     	  
    	holder[11] = (float)0.0; 
   	  
    	holder[12] = (float)dataPoints.mouthPoints[0].x; // tl.x, br.y 
    	holder[13] = (float)dataPoints.mouthPoints[1].y;     	  
    	holder[14] = (float)0.0; 
   	  
    	holder[15] = (float)dataPoints.mouthPoints[0].x; // tl.x, tl.y 
    	holder[16] = (float)dataPoints.mouthPoints[0].y;     	  
    	holder[17] = (float)0.0; 
    	}  
    	if(part == 3)
    	{
    	holder[0] = (float)dataPoints.leftEye[0].x; // tl.x, tl.y 
    	holder[1] = (float)dataPoints.leftEye[0].y;     	  
    	holder[2] = (float) 0.0; 
   		
    	holder[3] = (float)dataPoints.leftEye[1].x; // br.x, tl.y 
    	holder[4] = (float)dataPoints.leftEye[0].y;     	  
    	holder[5] = (float)0.0; 
   	  
    	holder[6] = (float)dataPoints.leftEye[1].x; // br.x, br.y 
    	holder[7] = (float)dataPoints.leftEye[1].y;     	  
    	holder[8] = (float)0.0; 
   	  
    	holder[9] = (float)dataPoints.leftEye[1].x; //  br.x, br.y 
    	holder[10] = (float)dataPoints.leftEye[1].y;     	  
    	holder[11] = (float)0.0; 
   	  
    	holder[12] = (float)dataPoints.leftEye[0].x; // tl.x, br.y 
    	holder[13] = (float)dataPoints.leftEye[1].y;     	  
    	holder[14] = (float)0.0; 
   	  
    	holder[15] = (float)dataPoints.leftEye[0].x; // tl.x, tl.y 
    	holder[16] = (float)dataPoints.leftEye[0].y;     	  
    	holder[17] = (float)0.0; 
    	}  	
    	if(part == 4)
    	{
    	holder[0] = (float)dataPoints.rightEye[0].x; // tl.x, tl.y 
    	holder[1] = (float)dataPoints.rightEye[0].y;     	  
    	holder[2] = (float) 0.0; 
   		
    	holder[3] = (float)dataPoints.rightEye[1].x; // br.x, tl.y 
    	holder[4] = (float)dataPoints.rightEye[0].y;     	  
    	holder[5] = (float)0.0; 
   	  
    	holder[6] = (float)dataPoints.rightEye[1].x; // br.x, br.y 
    	holder[7] = (float)dataPoints.rightEye[1].y;     	  
    	holder[8] = (float)0.0; 
   	  
    	holder[9] = (float)dataPoints.rightEye[1].x; //  br.x, br.y 
    	holder[10] = (float)dataPoints.rightEye[1].y;     	  
    	holder[11] = (float)0.0; 
   	  
    	holder[12] = (float)dataPoints.rightEye[0].x; // tl.x, br.y 
    	holder[13] = (float)dataPoints.rightEye[1].y;     	  
    	holder[14] = (float)0.0; 
   	  
    	holder[15] = (float)dataPoints.rightEye[0].x; // tl.x, tl.y 
    	holder[16] = (float)dataPoints.rightEye[0].y;     	  
    	holder[17] = (float)0.0; 
    	}  	
   	  	return holder;	  
    }
    
    
    
    
    public void updatePoints(Face[] dataVals)
    {
    	dataPoints = new Face[dataVals.length]; 
    	
    	dataPoints = dataVals;
    	
    	facePoints = new float[dataVals.length][18];
     	mouthPoints = new float[dataVals.length][18];
     	rightEye = new float[dataVals.length][18];
     	leftEye = new float[dataVals.length][18];
    	
    	for(int i = 0; i < dataVals.length; i++ )
    	{
    		
    		facePoints[i] = buildRect(dataPoints[i],1);
    	
    		if(dataPoints[i].gotMouth == true)
    		{
    		mouthPoints[i] = buildRect(dataPoints[i],2);
    		}
    	
    		if(dataPoints[i].gotLeftEye == true)
    		{
    		rightEye[i] = buildRect(dataPoints[i],3);
    		}
    	
    		if(dataPoints[i].gotRightEye == true)
    		{
    		leftEye[i] = buildRect(dataPoints[i],4);
    		}
    	}	
  	
    	Log.i(TAG, "Received data in final thread");   
    }


    
    private float[] DrawEllipse (int segments, float width, float height,float x, float y)
    {
    
     float[] vertices = new float[segments*2];
     int count=0;
     
     for (float i = 0; i < 360.0f; i+=(360.0f/segments))
     {
      vertices[count++] = (float) (Math.cos(Math.toRadians(i))*width) + x;
      vertices[count++] = (float) (Math.sin(Math.toRadians(i))*height)+ y;
     }

     return vertices;
    }
    
    private float[] DrawCircle(int circleSegments, float circleSize,float x, float y) 
    {
    	return DrawEllipse(circleSegments, circleSize, circleSize,x, y);
    }
    
    private float findRadius(float x1, float y1, float x2, float y2)
    {
    	float a = (float) Math.pow((x1-x2),2);
    	float b = (float) Math.pow((y1-y2), 2);
    	return (float) (Math.sqrt(a+b))/2;
    }
    
    private float[] findCenter(float x1,float y1,float x2,float y2)
    {
    	float[] answer = new float[2];
    	
    	answer[0]  = (x1+x2)/2;
    	answer[1]  = (y1+y2)/2;
    	return answer;
    }
    
    
    @Override
    public void run() {
    	
    	
        initGL();
        
        while(dataPoints == null)
        {
        	try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	Log.e(TAG, "Datapoints is NULL");   
        }
        
        int attribPosition = GLES20.glGetAttribLocation(mProgram,
                "position");
        
        int uniformColor = GLES20.glGetUniformLocation(mProgram,"uniformColor");
        
        GLES20.glClearColor(0.0f, 0.0f, 1.0f, 0);// this is for background color
        
        GLES20.glUseProgram(mProgram);

        
        while (!mFinished) {

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        int numberofSegment = 60;
          
        for(int i = 0; i < dataPoints.length; i++)
        {
        
        // find face circle params here
        	float radius = findRadius(facePoints[i][0],facePoints[i][1],facePoints[i][9],facePoints[i][10]);
        	float []centerCircle = findCenter(facePoints[i][0],facePoints[i][1],facePoints[i][9],facePoints[i][10]);
        	float[] faceCircle = DrawCircle(numberofSegment,radius,centerCircle[0],centerCircle[1]);
      
        FloatBuffer mVertices = ByteBuffer.allocateDirect(faceCircle.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();

     mVertices.put(faceCircle).position(0);

       GLES20.glEnableVertexAttribArray(attribPosition);

        GLES20.glUseProgram(mProgram);
         checkCurrent();

            mVertices.position(0);
            GLES20.glVertexAttribPointer(attribPosition, 2,
                    GLES20.GL_FLOAT, false, 0, mVertices);
            
            GLES20.glUniform4f(uniformColor,1.0f, 0.0f, 0.0f, 0.0f);   // set the color of the following object here
            
            GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, numberofSegment);
            Log.d(TAG, "Face drawn" + i);

            
            
            // draw mouth
            if(dataPoints[i].gotMouth == true)
            {
                // find mouth circle params here
            	float radiusM = findRadius(mouthPoints[i][0],mouthPoints[i][1],mouthPoints[i][9],mouthPoints[i][10]);
            	float []centerCircleM = findCenter(mouthPoints[i][0],mouthPoints[i][1],mouthPoints[i][9],mouthPoints[i][10]);
            	float[] faceCircleM = DrawEllipse(numberofSegment,radiusM,radiusM*0.2f,centerCircleM[0],centerCircleM[1]);
          
            	
                FloatBuffer mVertices_Mouth = ByteBuffer.allocateDirect(faceCircleM.length * 4)
                        .order(ByteOrder.nativeOrder()).asFloatBuffer();
                
            	mVertices_Mouth.put(faceCircleM).position(0);
            	mVertices_Mouth.position(0);
            	
            	 GLES20.glVertexAttribPointer(attribPosition, 2,
                         GLES20.GL_FLOAT, false, 0, mVertices_Mouth);

                          
                 GLES20.glUniform4f(uniformColor,0.0f, 1.0f, 0.0f, 0.0f);   // set the color of the following object here
                 GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, numberofSegment);
                 Log.i(TAG, "Mouth Drawn");            	
            }
            
            // draw right Eye
            if(dataPoints[i].gotRightEye == true)
            {
                FloatBuffer mVertices_rtEye = ByteBuffer.allocateDirect(rightEye[i].length * 4)
                        .order(ByteOrder.nativeOrder()).asFloatBuffer();
                
                mVertices_rtEye.put(rightEye[i]).position(0);
                mVertices_rtEye.position(0);
            	
            	 GLES20.glVertexAttribPointer(attribPosition, 3,
                         GLES20.GL_FLOAT, false, 0, mVertices_rtEye);

                                       
                 GLES20.glUniform4f(uniformColor,1.0f, 0.6f, 0.0f, 0.0f);   // set the color of the following object here
                 GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
                 Log.i(TAG, "Right Eye Drawn");            	
            }
            
            // draw Left Eye
            if(dataPoints[i].gotLeftEye == true)
            {
                FloatBuffer mVertices_lftEye = ByteBuffer.allocateDirect(leftEye[i].length * 4)
                        .order(ByteOrder.nativeOrder()).asFloatBuffer();
                
                mVertices_lftEye.put(leftEye[i]).position(0);
                mVertices_lftEye.position(0);
            	
            	 GLES20.glVertexAttribPointer(attribPosition, 3,
                         GLES20.GL_FLOAT, false, 0, mVertices_lftEye);

                               
                 GLES20.glUniform4f(uniformColor,1.0f, 0.0f, 0.6f, 0.0f);   // set the color of the following object here
                 GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
                 Log.i(TAG, "Left Eye Drawn");            	
            }
            
        }   
           
            try {
            if (!mEgl.eglSwapBuffers(mEglDisplay, mEglSurface));
            } 
            catch(Exception e)
            {
            	 Log.e(TAG, "cannot swap buffers!");
            }
            checkEglError();

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }
    
    void finish() {
        mFinished = true;
    }
    private void checkCurrent() {
        if (!mEglContext.equals(mEgl.eglGetCurrentContext())
                || !mEglSurface.equals(mEgl
                        .eglGetCurrentSurface(EGL10.EGL_DRAW))) {
            checkEglError();
            if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface,
                    mEglSurface, mEglContext)) {
                throw new RuntimeException(
                        "eglMakeCurrent failed "
                                + GLUtils.getEGLErrorString(mEgl
                                        .eglGetError()));
            }
            checkEglError();
        }
    }

    private void checkEglError() {
        final int error = mEgl.eglGetError();
        if (error != EGL10.EGL_SUCCESS) {
            Log.e(TAG, "EGL error = 0x" + Integer.toHexString(error));
        }
    }

    private void checkGlError() {
        final int error = mGl.glGetError();
        if (error != GL11.GL_NO_ERROR) {
            Log.e(TAG, "GL error = 0x" + Integer.toHexString(error));
        }
    }

    private int buildProgram(String vertexSource, String fragmentSource) {
        final int vertexShader = buildShader(GLES20.GL_VERTEX_SHADER,
                vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        final int fragmentShader = buildShader(
                GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (fragmentShader == 0) {
            return 0;
        }

        final int program = GLES20.glCreateProgram();
        if (program == 0) {
            return 0;
        }

        GLES20.glAttachShader(program, vertexShader);
        checkGlError();

        GLES20.glAttachShader(program, fragmentShader);
        checkGlError();

        GLES20.glLinkProgram(program);
        checkGlError();

        int[] status = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status,
                0);
        checkGlError();
        if (status[0] == 0) {
            Log.e(TAG, GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            checkGlError();
        }

        return program;
    }

    private int buildShader(int type, String shaderSource) {
        final int shader = GLES20.glCreateShader(type);
        if (shader == 0) {
            return 0;
        }

        GLES20.glShaderSource(shader, shaderSource);
        checkGlError();
        GLES20.glCompileShader(shader);
        checkGlError();

        int[] status = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status,
                0);
        if (status[0] == 0) {
            Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            return 0;
        }

        return shader;
    }

    
    
    private void initGL() {
        final String vertexShaderSource = "attribute vec4 position;\n"
                + "uniform vec4 uniformColor;\n"
        		+ "varying vec4 fragmentColor;\n"+
                "void main () {\n" +
                "   gl_Position = position;\n" +
                " fragmentColor = uniformColor;" +
                "}";

        final String fragmentShaderSource = "precision mediump float;\n"
                 + "varying highp vec4 fragmentColor;" +
                "void main () {\n" +
                "   gl_FragColor = fragmentColor;\n" +
                "}";

        mEgl = (EGL10) EGLContext.getEGL();

        mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        if (mEglDisplay == EGL10.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed "
                    + GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }

        int[] version = new int[2];
        if (!mEgl.eglInitialize(mEglDisplay, version)) {
            throw new RuntimeException("eglInitialize failed "
                    + GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }

        int[] configsCount = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        int[] configSpec = {
                EGL10.EGL_RENDERABLE_TYPE,
                EGL_OPENGL_ES2_BIT,
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_DEPTH_SIZE, 0,
                EGL10.EGL_STENCIL_SIZE, 0,
                EGL10.EGL_NONE
        };

        EGLConfig eglConfig = null;
        if (!mEgl.eglChooseConfig(mEglDisplay, configSpec, configs, 1,
                configsCount)) {
            throw new IllegalArgumentException(
                    "eglChooseConfig failed "
                            + GLUtils.getEGLErrorString(mEgl
                                    .eglGetError()));
        } else if (configsCount[0] > 0) {
            eglConfig = configs[0];
        }
        if (eglConfig == null) {
            throw new RuntimeException("eglConfig not initialized");
        }

        int[] attrib_list = {
                EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE
        };
        mEglContext = mEgl.eglCreateContext(mEglDisplay,
                eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
        checkEglError();
        mEglSurface = mEgl.eglCreateWindowSurface(
                mEglDisplay, eglConfig, mSurface, null);
        checkEglError();
        if (mEglSurface == null || mEglSurface == EGL10.EGL_NO_SURFACE) {
            int error = mEgl.eglGetError();
            if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                Log.e(TAG,
                        "eglCreateWindowSurface returned EGL10.EGL_BAD_NATIVE_WINDOW");
                return;
            }
            throw new RuntimeException(
                    "eglCreateWindowSurface failed "
                            + GLUtils.getEGLErrorString(error));
        }

        if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface,
                mEglSurface, mEglContext)) {
            throw new RuntimeException("eglMakeCurrent failed "
                    + GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }
        checkEglError();

        mGl = (GL11) mEglContext.getGL();
        checkEglError();

        mProgram = buildProgram(vertexShaderSource,
                fragmentShaderSource);
        
        mFinished = false;
    }
}








@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
@SuppressLint("NewApi")
public class TextureActivity extends Fragment implements TextureView.SurfaceTextureListener {

	
	   private static final String TAG = "TextureActivity";
	private TextureView myTexture;
	   private Camera mCamera;
	private RenderThread mRenderThread;
	public Face FaceVals;
	   
	

	   @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	    {
		   Log.i(TAG, "TextureActivity : onCreateView");
		   
	    	return inflater.inflate(R.layout.activity_texture, container, false);
	    }
	   
	   
	@SuppressLint("NewApi")
	@Override
	public void onStart(){
		
		super.onStart();
	     // setContentView(R.layout.activity_texture);
	     // myTexture = new TextureView();

		//this.getActivity().setContentView(myTexture);
		myTexture = (TextureView) getView().findViewById(R.id.textureView1);
	    myTexture.setSurfaceTextureListener(this);
		Log.i(TAG, "TextureActivity : onStart");
		
		FaceVals = new Face();
		//this.getActivity().setContentView(myTexture);
	}

	
	   @SuppressLint("NewApi")
	@Override
	   public void onSurfaceTextureAvailable(SurfaceTexture surface, int arg1,
	   int arg2) {
		  Log.i(TAG, "Entered onSurfaceTextureAvailable");
		  
		  Face dataVals = new Face();  
		  mRenderThread =  new RenderThread(surface); // send data read to the thread
	      mRenderThread.start();

	   }
	   
	   @Override
	   public boolean onSurfaceTextureDestroyed(SurfaceTexture arg0) {
	      
	      Log.i(TAG, "onSurfaceTextureDestroyed");
	      return true;
	   }

	
	

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	public void onSurfaceTextureUpdatedManual() {
		// TODO Auto-generated method stub
		Log.i(TAG, "Face Avbl");
		//FaceVals = dataReadWrite(1,null);	
		//mRenderThread.updatePoints(FaceVals);
	}
	
	public void setOnFrameAvailableListener (SurfaceTexture.OnFrameAvailableListener l)
	{
		
	}
	
	public Face dataReadWrite(int flag, Face data) // 1 for read, 0 for write
	{
		if(flag == 0)
		{
			FaceVals = data;
			Log.i(TAG, "Coordinates WRITE");
			return null;
		}
		else
		{
			Log.i(TAG, "Coordinates READ");
			return FaceVals;
		}
	}
		
	
	
	
	
	public void changeParamsreceivedfromInterface(Face[] val)
	{
		  Log.i(TAG, "Value received from Interface");
		  //dataReadWrite(0,val[0]); // write only one face for now
		 // onSurfaceTextureUpdatedManual();
		  mRenderThread.updatePoints(val);
	      
	}


	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		// TODO Auto-generated method stub
		
	}

}
