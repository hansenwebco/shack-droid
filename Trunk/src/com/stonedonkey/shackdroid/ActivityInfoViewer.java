package com.stonedonkey.shackdroid;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;

public class ActivityInfoViewer extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.info_viewer);
		
		Bundle extras = this.getIntent().getExtras();
		int id = 0;
		String action = null;
		if (extras != null) {
			 id =  extras.getInt("id");
			 action = extras.getString("action");
		}
		
		final String html = "<html><body bgcolor='#222222'><br/><br/><br/><font color='white'><center>LOADING...</center></font></body></html>";
		WebView wv = (WebView) findViewById(R.id.WebViewInfoView);
		wv.loadData(html, "text/html","utf-8");
		wv.setVerticalFadingEdgeEnabled(false);
		wv.setVerticalScrollbarOverlay(true);
		
		Animation anim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.toggle_infoviewer);
		View v = findViewById(R.id.LinearLayoutInfoViewerWrapper);
		v.startAnimation(anim);
		anim = null;
		
		if (action != null && action.equals("story"))
		{
			 new GetShackStoryAsyncTask(this,id).execute();
		}
	}
}
class GetShackStoryAsyncTask extends AsyncTask<Void,Void,Integer>{

	Context _context;
	String  _html; 
	int _storyID;
	public GetShackStoryAsyncTask(Context context, int storyID)
	{
		super();
		_context = context;
		_storyID = storyID;
	}
	@Override
	protected Integer doInBackground(Void... arg0) {
		try {
			_html = Helper.getShackStory(_storyID, _context);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			_html = "Error fetching story.";
		}
		return null;
	}
	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		
		final String html = "<html><head><style>body { color:white } a { color:white; }</style></head><body bgcolor='#222222'>" 
			+ _html + "</body></html>"; 
		
		WebView wv = (WebView)((Activity) _context).findViewById(R.id.WebViewInfoView);
		wv.loadData(html, "text/html","utf-8");
		wv.setVerticalFadingEdgeEnabled(false);
		wv.setVerticalScrollbarOverlay(true);
	}
}