package com.stonedonkey.shackdroid;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.util.Linkify;
import android.text.util.Linkify.TransformFilter;
import android.widget.TextView;

public class ActivityProfile extends Activity {

	private ProgressDialog dialog;
	private String shackname;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Helper.SetWindowState(getWindow(), this);

		setContentView(R.layout.profile);

		Bundle extras = this.getIntent().getExtras();
		shackname = extras.getString("shackname");

		setTitle("ChattyProfil.es");

		new GetProfileAsyncTask(this, shackname).execute();
	}

	protected Dialog onCreateDialog(int id) {
		dialog = new ProgressDialog(this);
		dialog.setMessage("loading " + shackname + " profile, please wait...");
		dialog.setTitle(null);
		dialog.setIndeterminate(true);
		dialog.setCancelable(true);
		return dialog;
	}

	public void FillView(ShackProfile profile) {
		Typeface face = Typeface.createFromAsset(this.getAssets(),"fonts/arial.ttf");
		
		if (profile.getError() != null) {
			// Show error...
			//No idea what to do during error.
			return;
		}
		
		SimpleDateFormat jsonFormater = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		SimpleDateFormat postFormater = new SimpleDateFormat("MMMM dd, yyyy");

		//Chatty Profile
		SetTextViewString(R.id.TextViewShackname, profile.getShackname(), face, null, false);
		String joinDate = null;
		try{
			Date dateObj = jsonFormater.parse(profile.getJoin_date());
			joinDate = postFormater.format(dateObj);
		}catch(Exception e){
			joinDate = null;
		}
		SetTextViewString(R.id.TextViewRegistered, joinDate, face, null, false);
		String firstPostDate = null;
		try{
			Date dateObj = jsonFormater.parse(profile.getFirstpost_date());
			firstPostDate = postFormater.format(dateObj);
		}catch(Exception e){
			firstPostDate = null;
		}
		SetTextViewString(R.id.TextViewFirstPost, firstPostDate, face, null, false);
		SetTextViewString(R.id.TextViewTotalPosts, profile.getPostcount(), face, null, false);
		String postsPerDay = null;
		if(profile.getPostsperday() != null)
		{
			try{
				NumberFormat formatter = new DecimalFormat("#0.00");
				postsPerDay = formatter.format(Double.parseDouble(profile.getPostsperday()));
				if(profile.getPosterclass() != null) postsPerDay += " (" + profile.getPosterclass() + ")";
			}
			catch(Exception e){}
		}
		SetTextViewString(R.id.TextViewPostsPerDay, postsPerDay, face, null, false);
		
		//Gaming Handles
		SetTextViewString(R.id.TextViewSteam, profile.getSteam(), face, "http://steamcommunity.com/id/", false);
		SetTextViewString(R.id.TextViewXboxLive, profile.getXboxlive(), face, "http://live.xbox.com/en-US/Profile?gamertag=", false);
		SetTextViewString(R.id.TextViewPSN, profile.getPlaystation_network(), face, null, false);
		SetTextViewString(R.id.TextViewWii, profile.getWii(), face, null, false);
		SetTextViewString(R.id.TextViewXFire, profile.getXfire(), face, null, false);
		
		//Bio Data
		SetTextViewString(R.id.TextViewAge, profile.getAge(), face, null, false);
		SetTextViewString(R.id.TextViewGender, profile.getSex(), face, null, false);
		SetTextViewString(R.id.TextViewLocation, profile.getLocation(), face, null, false);
		SetTextViewString(R.id.TextViewHomepage, profile.getHomepage(), face, null, false);
		
		//Contact Info
		SetTextViewString(R.id.TextViewAIM, profile.getAim(), face, null, false);
		SetTextViewString(R.id.TextViewYahoo, profile.getYahoo(), face, null, false);
		SetTextViewString(R.id.TextViewMSN, profile.getMsn(), face, null, false);
		SetTextViewString(R.id.TextViewGTalk, profile.getGtalk(), face, null, false);
		
		//User Bio
		SetTextViewString(R.id.TextViewUserBio, profile.getUser_bio(), face, null, true);
	}
	
	//Simple helper to set the text we got from the json query to the view, and add urls when necessary
	private void SetTextViewString(int id, String text, Typeface face, String urlLink, boolean linkifyAllURLs) {
		if(text != null) {
			TextView tv = (TextView) findViewById(id);
			tv.setTypeface(face);
			tv.setText(text);
			if(urlLink != null) {
				tv.setLinkTextColor(getResources().getColor(R.color.shackRedOrange));
				
				TransformFilter transformFilter = new TransformFilter() {
			        public final String transformUrl(final Matcher match, String url) {
			            return URLEncoder.encode(match.group(0));
			        }
			    };
				Linkify.addLinks(tv, Pattern.compile(".*"), urlLink, null, transformFilter); //is this the right way to do the pattern?
			}
			else if(linkifyAllURLs) {
				Linkify.addLinks(tv, Linkify.ALL);
			}
		}
	}
}

class GetProfileAsyncTask extends AsyncTask<Void, Void, Integer> {

	ActivityProfile context;
	ShackProfile profile;
	String error;
	String shackname;

	public GetProfileAsyncTask(ActivityProfile context, String shackname) {
		super();
		this.context = context;
		this.shackname = shackname;

		context.showDialog(1);
	}

	@Override
	protected Integer doInBackground(Void... arg0) {

		try {
			profile = getShackProfile(shackname, context);

			return 1; // great success!

		} catch (Exception ex) {
			error = ex.getMessage();
			return null;
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		if (result != null && result == 1) {
			context.FillView(profile);
		}
		// wrap this dismiss in a try/catch this throws errors on occasion
		// according to
		// error reports
		try {
			context.dismissDialog(1);
		} catch (Exception ex) {
		}
	}
	
	private static ShackProfile getShackProfile(String shackname, Context context) throws JSONException
	{
		ShackProfile shackprofile = new ShackProfile();
		// TODO: we really need a generic call for these HTTP connections where
		// we simply retreive data from a URL
		String result = "";
		URL url;
		try {
			url = (new URI("http","chattyprofil.es","/api/profile/" + shackname, null)).toURL(); //This takes care of encodeing, because URLEncoder.encode() turns spaces into +'s but davinci turns spaces into %20.
			URLConnection conn = url.openConnection();
			HttpURLConnection httpConnection = (HttpURLConnection) conn;

			httpConnection.setRequestProperty("User-Agent", Helper.getUserAgentString(context));
			
			int responseCode = httpConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {

				InputStream is = httpConnection.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				
				String line = "";
				
				while ((line = reader.readLine()) != null) {
					result = result + line + "\n";
				}
			}

		} catch (Exception e) {

			result = "";
		}
		
		if (result.length() > 0)
		{
			JSONObject json = new JSONObject(result);
			if(!json.has("error")) {
				if(json.has("profile")){
					JSONObject profile = json.getJSONObject("profile");
					
					shackprofile.setShackname(GetJSONString(profile, "shackname"));
					shackprofile.setJoin_date(GetJSONString(profile, "join_date"));
					shackprofile.setFirstpost_date(GetJSONString(profile, "firstpost_date"));
					shackprofile.setMostrecentpost_date(GetJSONString(profile, "mostrecentpost_date"));
					shackprofile.setPostcount(GetJSONString(profile, "postcount"));
					shackprofile.setPostsperday(GetJSONString(profile, "postsperday"));
					shackprofile.setPosterclass(GetJSONString(profile, "posterclass"));
					shackprofile.setLastupdate(GetJSONString(profile, "lastupdate"));
					shackprofile.setIs_mod(GetJSONString(profile, "is_mod"));
					shackprofile.setIs_subscriber(GetJSONString(profile, "is_subscriber"));
					shackprofile.setSex(GetJSONString(profile, "sex"));
					shackprofile.setBirthdate(GetJSONString(profile, "birthdate"));
					shackprofile.setAge(GetJSONString(profile, "age"));
					shackprofile.setLocation(GetJSONString(profile, "location"));
					shackprofile.setHomepage(GetJSONString(profile, "homepage"));
					shackprofile.setYahoo(GetJSONString(profile, "yahoo"));
					shackprofile.setWii(GetJSONString(profile, "wii"));
					shackprofile.setXfire(GetJSONString(profile, "xfire"));
					shackprofile.setXboxlive(GetJSONString(profile, "xboxlive"));
					shackprofile.setPlaystation_network(GetJSONString(profile, "playstation_network"));
					shackprofile.setAim(GetJSONString(profile, "aim"));
					shackprofile.setGtalk(GetJSONString(profile, "gtalk"));
					shackprofile.setMsn(GetJSONString(profile, "msn"));
					shackprofile.setIcq(GetJSONString(profile, "icq"));
					shackprofile.setSteam(GetJSONString(profile, "steam"));
					shackprofile.setUser_bio(GetJSONString(profile, "user_bio"));
				}
			}
			else {
				final String error = json.getString("error");
				shackprofile.setError(error);
			}
		}
		else
		{
			shackprofile.setError("ChattyProfil.es is not available at this time.");
		}
		
		return shackprofile;
		
	}
	
	//Return string if found otherwise null, also returns "null" strings as null
	private static String GetJSONString(JSONObject jsonObj, String key) {
		try{
			if(jsonObj.has(key)) {
				String value = jsonObj.getString(key);
				if(!value.equalsIgnoreCase("null"))
				{
					return value;
				}
			}
		}catch(JSONException e) { }
		
		return null;
	}
}