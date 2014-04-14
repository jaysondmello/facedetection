package com.example.android.fragments;



import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class MainFaceActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_face);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_face, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_face,
					container, false);
			return rootView;
		}
	}
	
	 /** Called when the user clicks the Relative Layout button */
    public void callRelativeLayout(View view) {
        Intent intent = new Intent(this, FdActivity.class);

        Log.i("MainActivity", "MainActivity.call Relative Layout — done with creating intent");
        startActivity(intent);
    }
    
    /** Called when the user clicks the OpenGL */
    public void callOpenGL(View view) {
        Intent intent = new Intent(this, OpenGLActivity.class);

        Log.i("MainActivity", "MainActivity.call OPen GL — done with creating intent");
        startActivity(intent);
    }
    
    public void callFragger(View view) {
        Intent intent = new Intent(this, FraggerActivity.class);

        Log.i("MainActivity", "MainActivity.call Fragger — done with creating intent");
        startActivity(intent);
    }

}
