package org.opencv.samples.facedetect;



import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;


class MyGLSurfaceView extends GLSurfaceView {

    public MyGLSurfaceView(Context context){
        super(context);
        
        Log.i("MyGLSurfaceView", "MyGLSurfaceView : Entered the constructor");
      //Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);
        Log.i("MyGLSurfaceView", "MyGLSurfaceView : setEGLContextClientVersion Set");
        
           // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(new MyGLRenderer());
        
        // Render the view only when there is a change in the drawing data, removed for rotation of object
        // setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY); 
         Log.i("MyGLSurfaceView", "MyGLSurfaceView : setRenderMode Set");
         
         Log.i("MyGLSurfaceView", "MyGLSurfaceView : Left the constructor");
    }
}


public class OpenGLActivity extends Activity { // OPENGL ES 2.0

    private GLSurfaceView mGLView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.i("OpenGLActivity", "OpenGLActivity.onCreate() — Entered OpenGLActivity");

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        mGLView = new MyGLSurfaceView(this);
        setContentView(mGLView);
        Log.i("OpenGLActivity", "OpenGLActivity.onCreate() — Leaving OpenGLActivity");
    }
}