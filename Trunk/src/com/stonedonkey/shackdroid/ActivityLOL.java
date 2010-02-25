package com.stonedonkey.shackdroid;

import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class ActivityLOL extends ListActivity {

	private ProgressDialog dialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Helper.SetWindowState(getWindow(),this);
		
		setContentView(R.layout.topics);
		
		Bundle extras = this.getIntent().getExtras();
		String view = extras.getString("view");
		
		new GetLOLsAsyncTask(this,view).execute();
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
		dialog.setMessage("loading LOLs, please wait...");
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
	
	public GetLOLsAsyncTask(ActivityLOL context,String view) {
		super();
		this.context = context;
		this.view = view.toLowerCase();
		
		context.showDialog(1);
	}

	@Override
	protected Integer doInBackground(Void... arg0) {

		try {
		
		SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy hh:mm Z");
		String queryDate = sdf.format(new Date());
		
		queryDate = URLEncoder.encode(queryDate);
		
		String feedURL = "http://lmnopc.com/greasemonkey/shacklol/api.php?format=xml&tag=" + view + "&limit=50&since=" + queryDate;
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
		xr.parse(new InputSource(url.openStream()));
		
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
		if (result == 1)
		{
			AdapterLOL tva = new AdapterLOL(context,posts,R.layout.lol_row);
			context.setListAdapter(tva);
		}
		context.dismissDialog(1);
	}
}