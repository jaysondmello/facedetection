package com.example.android.fragments;



import android.app.Fragment;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


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


public class OpenGLActivity extends Fragment { // OPENGL ES 2.0

    private GLSurfaceView mGLView;
 
        
   @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
	    mGLView = new MyGLSurfaceView(this.getActivity());
    	return inflater.inflate(R.layout.activity_open_gl, container, false);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        
        Log.i("OpenGLActivity", "OpenGLActivity.onCreate() — Entered OpenGLActivity");

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
      
        //setContentView(mGLView);
        this.getActivity().setContentView(mGLView);
        
       //(GLSurfaceView) getView().findViewById(R.id.fd_activity_surface_view);
        Log.i("OpenGLActivity", "OpenGLActivity.onCreate() — Leaving OpenGLActivity");
    }
}