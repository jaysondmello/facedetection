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
import android.app.Fragment;
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
    private FloatBuffer mVertices;
    private final float[] mVerticesData = {
            -0.5f, 0.5f, 0.0f, 
           +0.5f, +0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
            
            0.5f, -0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
            -0.5f, 0.5f, 0.0f
    };

    public RenderThread(SurfaceTexture surface) {
        mSurface = surface;
    }

    @Override
    public void run() {
        initGL();
        
        mVertices = ByteBuffer.allocateDirect(mVerticesData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertices.put(mVerticesData).position(0);


        int attribPosition = GLES20.glGetAttribLocation(mProgram,
                "position");
        checkGlError();

        GLES20.glEnableVertexAttribArray(attribPosition);
        checkGlError();

        GLES20.glUseProgram(mProgram);
        checkGlError();

        while (!mFinished) {
            checkCurrent();

            mVertices.position(0);
            GLES20.glVertexAttribPointer(attribPosition, 3,
                    GLES20.GL_FLOAT, false, 0, mVertices);
            checkGlError();

            GLES20.glClearColor(1.0f, 0.6f, 0, 0);
            checkGlError();

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            checkGlError();

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
            Log.d(TAG, "draw!!");
            checkGlError();
            
            try {
            if (!mEgl.eglSwapBuffers(mEglDisplay, mEglSurface));
            } 
            catch(Exception e)
            {
            	 Log.e(TAG, "cannot swap buffers!");
            }
            checkEglError();

            try {
                Thread.sleep(3000);
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
                +
                "void main () {\n" +
                "   gl_Position = position;\n" +
                "}";

        final String fragmentShaderSource = "precision mediump float;\n"
                +
                "void main () {\n" +
                "   gl_FragColor = vec4(1.0, 0.0, 0.0, 0.0);\n" +
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

	
	   private static final String TAG = null;
	private TextureView myTexture;
	   private Camera mCamera;
	private RenderThread mRenderThread;
	   
	

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
		//this.getActivity().setContentView(myTexture);
	}

	
	   @SuppressLint("NewApi")
	@Override
	   public void onSurfaceTextureAvailable(SurfaceTexture surface, int arg1,
	   int arg2) {
		  Log.i(TAG, "TextureActivity : Entered onSurfaceTextureAvailable");
		  new RenderThread(surface).start();
	     

	   }
	   
	   @Override
	   public boolean onSurfaceTextureDestroyed(SurfaceTexture arg0) {
	      
	      Log.i(TAG, "TextureActivity : onSurfaceTextureDestroyed");
	      return true;
	   }

	
	

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		// TODO Auto-generated method stub
		
	}

}
