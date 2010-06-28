package com.stonedonkey.shackdroid;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public class AsyncSaxHandlerTopics extends AsyncTask<Void, Void, Integer> {

	AdapterTopicView adapter;
	SaxHandlerTopicView saxHandler;
	InputStream stream;
	Activity ctx;
	int count = 0;
	public AsyncSaxHandlerTopics(AdapterTopicView adapter, Activity ctx, String url){
		long l = System.currentTimeMillis();
		this.ctx = ctx;
		this.adapter = adapter;
		saxHandler = new SaxHandlerTopicView(ctx,"topic");
		saxHandler.addAsync(this);
		try {
			stream = HttpHelper.HttpRequestWithGzip(url,ctx);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i("Asnyc constructor:", String.valueOf(System.currentTimeMillis() - l));
	}
	 
	@Override
	protected void onPreExecute(){
		//ctx.setProgressBarIndeterminateVisibility(true);
	}
	
	@Override
	protected void onProgressUpdate(Void... params){
		adapter.notifyDataSetChanged();
	}
	
	@Override
	protected Integer doInBackground(Void... params) {
		
		try{
			// Get a SAXParser from the SAXPArserFactory.
			final SAXParserFactory spf = SAXParserFactory.newInstance();
			final SAXParser sp = spf.newSAXParser();
	
			// Get the XMLReader of the SAXParser we created.
			final XMLReader xr = sp.getXMLReader();
			// Create a new ContentHandler and apply it to the XML-Reader
			
			xr.setContentHandler(saxHandler);
			// Parse the xml-data from our URL.
			xr.parse(new InputSource(stream));
		}
		catch(Exception e){}
		
		return 1;
	}

	@Override
	protected void onPostExecute(Integer result){
		//ctx.setProgressBarIndeterminateVisibility(false);
		Log.i("AsyncSAX", "POST EXECUTE");
		adapter.notifyDataSetChanged();
	}
	
	public void postUpdate(ShackPost post){
		adapter.add(post);
		if (count < 8){
			publishProgress();
		}
		count++;
	}
}
