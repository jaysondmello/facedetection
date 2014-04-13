package org.opencv.samples.facedetect;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;



public class MyGLRenderer implements GLSurfaceView.Renderer {

	 public int mProgram = 0;
	 private Triangle mTriangle;
	 private float[] mProjectionMatrix;
	 private float[] mRotationMatrix = new float[16];
	
			
			private final String fragmentShaderCode =
			"precision mediump float;" +
			"uniform vec4 vColor;" +
			"void main() {" +
			"  gl_FragColor = vColor;" +
			"}";
			
			private final String vertexShaderCode =

				    // This matrix member variable provides a hook to manipulate
				    // the coordinates of objects that use this vertex shader.
				    "uniform mat4 uMVPMatrix;   \n" +

				    "attribute vec4 vPosition;  \n" +
				    "void main(){               \n" +
				    // The matrix must be included as part of gl_Position
				    // Note that the uMVPMatrix factor *must be first* in order
				    // for the matrix multiplication product to be correct.
				    " gl_Position = uMVPMatrix * vPosition; \n" +

				    "}  \n";
	
		
	
    public void onSurfaceCreated(GL10 unused, javax.microedition.khronos.egl.EGLConfig config) {
    	
    	Log.i("MyGLRenderer", "MyGLRenderer : onSurfaceCreated() - Entered onSurfaceCreated");
        // Set the background frame color
        GLES20.glClearColor(1.0f, 0.6f, 0.0f, 1.0f);
        
                     
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // creates OpenGL ES program executables
        
        
        // initialize a triangle
        Log.i("MyGLRenderer", "MyGLRenderer : onSurfaceCreated() - Creating new triangle object");
        mTriangle = new Triangle(mProgram);
        Log.i("MyGLRenderer", "MyGLRenderer : onSurfaceCreated() - New Triangle object created");

    }

    public void onDrawFrame(GL10 gl) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        
        Log.i("MyGLRenderer", "MyGLRenderer : onDrawFrame() - Starting to set camera position");
        
        float[] mViewMatrix = new float[16];
		// Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        float[] mMVPMatrix = new float[16];
		// Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        
        Log.i("MyGLRenderer", "MyGLRenderer : onDrawFrame() - Drawing ClientSide");
              
        
        
        float[] scratch = new float[16];

        // Create a rotation transformation for the triangle
        long time = SystemClock.uptimeMillis() % 4000L;
        float angle = 0.090f * ((int) time);
        Matrix.setRotateM(mRotationMatrix, 0, angle, 0, 0, -1.0f);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);

        // Draw triangle
         mTriangle.draw_ClientSide(scratch);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
    	
    	GLES20.glViewport(0, 0, width, height);

    	    float ratio = (float) width / height;

    	    mProjectionMatrix = new float[16];
    	    Log.i("MyGLRenderer", "MyGLRenderer : onSurfaceChanged() - Generating Projection Matrix");
    	    // this projection matrix is applied to object coordinates
    	    // in the onDrawFrame() method
    	    Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    	    Log.i("MyGLRenderer", "MyGLRenderer : onSurfaceChanged() - Done generating projection matrix");
    }
    
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
    

    
    
 



}



