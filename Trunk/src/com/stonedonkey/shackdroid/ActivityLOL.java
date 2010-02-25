package com.stonedonkey.shackdroid;

import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;

public class ActivityLOL extends ListActivity {

	private ProgressDialog dialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Helper.SetWindowState(getWindow(),this);
		
		setContentView(R.layout.topics);
		
		new GetLOLsAsyncTask(this).execute();
	
		
		
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

	
	public GetLOLsAsyncTask(ActivityLOL context) {
		super();
		this.context = context;
		context.showDialog(1);
		
	}


	
	@Override
	protected Integer doInBackground(Void... arg0) {

		try {
		String feedURL = "http://lmnopc.com/greasemonkey/shacklol/api.php?format=xml&tag=lol&limit=50&since=2/23/2010%203:49PM";
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
