package com.stonedonkey.shackdroid;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.stonedonkey.shackdroid.FragmentTopicView.GetChattyAsyncTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.AbsListView.OnScrollListener;

public class FragmentSearchResults extends ListFragment  {
	private ProgressDialog pd;
	private ArrayList<ShackSearch> searchResults;
	private String searchTerm = "";
	private String author = "";
	private String parentAuthor = "";
	private String totalPages = "1";
	private String totalResults = "0";
	private int currentPage = 1;
	private AdapterSearchResults tva;
	private Boolean threadLoaded = true;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		//
		Bundle extras = getActivity().getIntent().getExtras();
		searchTerm = extras.getString("searchTerm");
		author = extras.getString("author");
		parentAuthor = extras.getString("parentAuthor");
			

		fillSaxData();
		
		ListView lv= getListView();
		lv.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

				// start loading the next page
				if (threadLoaded && firstVisibleItem + visibleItemCount >= totalItemCount && currentPage + 1 <= Integer.parseInt(totalPages)) {

					// get the list of topics
					currentPage++;
					fillSaxData();

				}
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

		});
		
		RotateAnimation anim = new RotateAnimation(0f, 359f, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		anim.setInterpolator(new LinearInterpolator());
		anim.setRepeatCount(Animation.INFINITE);
		anim.setDuration(700);

		ImageView loader = (ImageView) getView().findViewById(R.id.ImageViewTopicLoader);
		loader.setAnimation(anim);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		return inflater.inflate(R.layout.searchresults, null);
	}
	private void fillSaxData() {
		GetSearchResultsAsyncTask chatty = new GetSearchResultsAsyncTask(getActivity());
		chatty.execute();

	}



	private Handler progressBarHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// we implement a handler because most UI items
			// won't update within a thread
			try {
				pd.dismiss();
			}
			catch (Exception ex) {

			}

			//ShowData();
		}
	};
	

	private void ShowData() {

		getActivity().setTitle("Search Results - " + currentPage + " of " + this.totalPages + " - " + this.totalResults + " results.");

		
		if (tva == null) {
			tva = new AdapterSearchResults(getActivity(), searchResults, R.layout.searchresults_row);
			setListAdapter(tva);
		}
		else
		{
			tva.SetPosts(searchResults);
			tva.notifyDataSetChanged();
		}
		
		threadLoaded = true;
		
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		ShowShackPost(position);
	}

	private void ShowShackPost(long position) {

		ShackSearch search = searchResults.get((int) position);
		String postID = search.getId();
		String storyID = search.getStoryID();

		FragmentThreadedView threadView = (FragmentThreadedView) getFragmentManager().findFragmentById(R.id.MixedThreads);
		if (threadView == null) {
			Intent intent = new Intent();
			intent.setClass(getActivity(), FragmentActivityThread.class);
			intent.putExtra("postID", postID); // the value must be a string
			intent.putExtra("storyID", storyID);
			startActivity(intent);
		}
		else {
			threadView.setPostID(postID);
			threadView.setStoryID(storyID);
			threadView.setCurrentPosition(0);
			threadView.setIsNWS(false);

			threadView.fillSaxData(postID);
		}
	}

	class GetSearchResultsAsyncTask extends AsyncTask<Void, Void, Void> {
		protected ProgressDialog dialog;
		protected Context c;

		public GetSearchResultsAsyncTask(Context context) {
			this.c = context;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			threadLoaded = false;
			
			if (currentPage == 1)
				dialog = ProgressDialog.show(getActivity(), null, "Loading Search Results", true, true);
			else
				SetLoaderVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			try {

				URL url = new URL("http://shackapi.stonedonkey.com/search/?SearchTerm=" + URLEncoder.encode(searchTerm, "UTF-8") + "&Author=" + URLEncoder.encode(author, "UTF-8") + "&ParentAuthor=" + URLEncoder.encode(parentAuthor, "UTF-8")
						+ "&page=" + currentPage);

				// SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
				// URL url = new URL(prefs.getString("shackFeedURL",getString(R.string.default_api)) + "/search/?SearchTerm=" + URLEncoder.encode(searchTerm,"UTF-8") + "&Author="+ author + "&ParentAuthor=" + parentAuthor + "&page=" + currentPage);

				// Get a SAXParser from the SAXPArserFactory.
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();

				// Get the XMLReader of the SAXParser we created.
				XMLReader xr = sp.getXMLReader();

				// Create a new ContentHandler and apply it to the XML-Reader
				SaxHandlerSearchResults saxHandler = new SaxHandlerSearchResults();
				xr.setContentHandler(saxHandler);

				// Parse the xml-data from our URL.
				xr.parse(new InputSource(HttpHelper.HttpRequestWithGzip(url.toString(), getActivity())));

				if (searchResults == null) {
					searchResults = saxHandler.getSearchResults();
				}
				else {
					ArrayList<ShackSearch> newPosts = saxHandler.getSearchResults();
					newPosts.removeAll(searchResults);
					searchResults.addAll(searchResults.size(), newPosts);

				}
	
				totalPages = saxHandler.getTotalPages();
				totalResults = saxHandler.getTotalResults();

			}
			catch (Exception e) {

			}
			progressBarHandler.sendEmptyMessage(0);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			ShowData();

			SetLoaderVisibility(View.GONE);

			try {
				dialog.dismiss();
			}
			catch (Exception e) {
			}
		}

	}

	protected void SetLoaderVisibility(int visibility) {
		RelativeLayout loader = (RelativeLayout) getView().findViewById(R.id.TopicLoader);
		loader.setVisibility(visibility);
	}
}
