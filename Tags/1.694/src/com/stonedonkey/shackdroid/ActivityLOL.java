package com.stonedonkey.shackdroid;

import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;

public class ActivityLOL extends ListActivity {

	private ProgressDialog dialog;
	private String view;
	
	

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Helper.SetWindowState(getWindow(),this);
		
		
		setContentView(R.layout.topics);
		setTitle("ShackLOLs - Last 24 Hours");
		
		Bundle extras = this.getIntent().getExtras();
		view = extras.getString("view");
		int lolView= extras.getInt("lolView");
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String login = prefs.getString("shackLogin", "");
		
		new GetLOLsAsyncTask(this,view,lolView,login).execute();
	}
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Intent intent = new Intent();
		intent.setClass(this, ActivityThreadedView.class);
		intent.putExtra("postID", String.valueOf(id)); // the value must be a string
		startActivity(intent);
	} 
	
	protected Dialog onCreateDialog(int id) {
		dialog = new ProgressDialog(this);
		dialog.setMessage("loading " + view.toUpperCase() + "s, please wait...");
		dialog.setTitle(null);
		dialog.setIndeterminate(true);
		dialog.setCancelable(true);
		return dialog;
	}
}
class GetLOLsAsyncTask extends AsyncTask<Void,Void,Integer>{

	ActivityLOL context;
	ArrayList<ShackLOL> posts; 
	String error;
	String view;
	String login;
	int lolView = -1;
	
	public GetLOLsAsyncTask(ActivityLOL context,String view, int lolView, String login) {
		super();
		this.context = context;
		this.view = view.toLowerCase();
		this.lolView = lolView;
		this.login = login;
		
		context.showDialog(1);
	}

	@Override
	protected Integer doInBackground(Void... arg0) {

		try {
			
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		date = cal.getTime();
			
		SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy hh:mm Z");
		String queryDate = sdf.format(date);
		
		queryDate = URLEncoder.encode(queryDate);
		String feedURL = null;
		

		
		switch (lolView){
			case 0:
				feedURL = "http://lmnopc.com/greasemonkey/shacklol/api.php?format=xml&tag=" + view + "&limit=50&since=" + queryDate;
				break;
			case 1: // stuff you wrote
				feedURL = "http://lmnopc.com/greasemonkey/shacklol/api.php?format=xml&author=" + URLEncoder.encode(login) + "&tag=" + view + "&limit=50&order=date_desc";
				break;
			case 2:  // stuff you lol'd
				feedURL = "http://lmnopc.com/greasemonkey/shacklol/api.php?format=xml&tagger=" + URLEncoder.encode(login) + "&tag=" + view + "&limit=50&order=date_desc";
				break;
		}
		
		URL url = new URL(feedURL);
 
		// Get a SAXParser from the SAXPArserFactory.
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();

		//  Get the XMLReader of the SAXParser we created.
		XMLReader xr = sp.getXMLReader();

		// Create a new ContentHandler and apply it to the XML-Reader
		SaxHandlerLOL saxHandler = new SaxHandlerLOL();
		xr.setContentHandler(saxHandler);

		// Parse the xml-data from our URL.
		xr.parse(new InputSource(HttpHelper.HttpRequestWithGzip(url.toString(),context)));
		
		posts = saxHandler.getMessages();
		
		return 1; // great success!
		
		}
		catch (Exception ex)
		{
		    error = ex.getMessage();
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		if (result != null && result == 1)
		{
			if (posts.size() > 0) {
				AdapterLOL tva = new AdapterLOL(context,posts,R.layout.lol_row);
				context.setListAdapter(tva);
			}
		}
		// wrap this dismiss in a try/catch this throws errors on occasion according to 
		// error reports
		try {
			context.dismissDialog(1);
		}
		catch (Exception ex) { } 
	}
}