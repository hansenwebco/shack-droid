package com.stonedonkey.shackdroid;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class HandlerExtendedSites extends Activity {

	public static String VersionCheck(Context ctx) {

		String result = null;
		URL url;
		try {
			url = new URL("http://www.stonedonkey.com/ShackDroid/version.txt");
			URLConnection conn = url.openConnection();
			HttpURLConnection httpConnection = (HttpURLConnection) conn;
			httpConnection.setRequestProperty("User-Agent", Helper.getUserAgentString(ctx));

			int responseCode = httpConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {

				InputStream is = httpConnection.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));

				String line = null;
				line = reader.readLine();

				// the result is null if the versions match
				if (line.equals(ctx.getString(R.string.version_id)))
					result = null;
				else
					result = reader.readLine();
			}

		}
		catch (Exception e) {

			result = "*fail*";
		}

		// update stats
		ShackDroidStats.AddCheckedForNewVersion(ctx);

		return result;
	}

	public static String WhatsNew(String version, Context ctx) {

		String result = "";
		URL url;
		try {
			url = new URL("http://www.stonedonkey.com/ShackDroid/whatsnew.txt");
			URLConnection conn = url.openConnection();
			HttpURLConnection httpConnection = (HttpURLConnection) conn;

			httpConnection.setRequestProperty("User-Agent", Helper.getUserAgentString(ctx));

			int responseCode = httpConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {

				InputStream is = httpConnection.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));

				String line = "";

				while ((line = reader.readLine()) != null) {
					result = result + line + "\n";
				}

			}

		}
		catch (Exception e) {

			result = "";
		}
		return result;

	}

	public static void AddRemoveShackMark(Context ctx, String id, Boolean delete) {

		try {

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
			String shackLogin = prefs.getString("shackLogin", "");

			// make sure they have their username set
			if (shackLogin.length() == 0) {
				new AlertDialog.Builder(ctx).setTitle("Username Required").setPositiveButton("OK", null).setMessage("Please set your Login in settings.").show();

				return;
			}

			String data = URLEncoder.encode("user", "UTF-8") + "=" + URLEncoder.encode(shackLogin, "UTF-8") + "&" + URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8");

			URL url;
			if (delete == false)
				url = new URL("http://socksandthecity.net/shackmarks/shackmark.php?" + data);
			else
				url = new URL("http://socksandthecity.net/shackmarks/unshackmark.php?" + data);

			URLConnection conn = url.openConnection();
			HttpURLConnection httpConnection = (HttpURLConnection) conn;

			int responseCode = httpConnection.getResponseCode();
			String result = "";

			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStream is = httpConnection.getInputStream();

				BufferedReader reader = new BufferedReader(new InputStreamReader(is));

				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}

				result = sb.toString();

				// show result
				if (result.length() > 0 && result.startsWith("ok")) {

					if (delete == false)
						new AlertDialog.Builder(ctx).setTitle("ShackMark Added").setPositiveButton("OK", null).setMessage("The post was successfully added to your ShackMarks").show();
					// else
					// new AlertDialog.Builder(ctx)
					// .setTitle("ShackMark Removed")
					// .setPositiveButton("OK", null)
					// .setMessage(
					// "The post was successfully removed to your ShackMarks")
					// .show();

				}
				else {

					new AlertDialog.Builder(ctx).setTitle("ShackMark Failed").setPositiveButton("OK", null).setMessage("ShackMark failed").show();
				}

			}
		}
		catch (Exception e) {
			new AlertDialog.Builder(ctx).setTitle("Error").setPositiveButton("OK", null).setMessage("Unable to contact ShackMarks server, please try again later.").show();
		}

	}

	public static void INFLOLPost(Context ctx, String shackName, String postID, String tag) {
		SendThomWLOLAsyncTask lol = new SendThomWLOLAsyncTask(ctx,tag,shackName,postID);
		lol.execute();
	}


}
class SendThomWLOLAsyncTask extends AsyncTask<Void, Void, Void> {

	private String _tag;
	private String _shackName;
	private String _postID;
	private Context _context;
	private String _errorMessage = "";
	
	public SendThomWLOLAsyncTask(Context context, String tag, String shackName, String postID) {
		this._tag = tag;
		this._shackName = shackName;
		this._postID = postID;
		this._context = context;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		
		if (_errorMessage.length() == 0)
			new AlertDialog.Builder(_context).setTitle(_tag + "'d").setPositiveButton("OK", null).setMessage("The post was successfully [ " + _tag + "'d ]").show();
		else
			new AlertDialog.Builder(_context).setTitle(_tag + "'d").setPositiveButton("OK", null).setMessage(_errorMessage).show();
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		_tag = _tag.toUpperCase();

		try {

			String data = URLEncoder.encode("who", "UTF-8") + "=" + URLEncoder.encode(_shackName, "UTF-8") + "&" + URLEncoder.encode("what", "UTF-8") + "=" + URLEncoder.encode(_postID, "UTF-8") + "&" + URLEncoder.encode("tag", "UTF-8") + "="
					+ URLEncoder.encode(_tag, "UTF-8") + "&" + "version=-1";

			// post to ShackNews
			URL url = new URL("http://lmnopc.com/greasemonkey/shacklol/report.php?" + data);

			URLConnection conn = url.openConnection();
			HttpURLConnection httpConnection = (HttpURLConnection) conn;

			httpConnection.setRequestProperty("User-Agent", Helper.getUserAgentString(_context));

			int responseCode = httpConnection.getResponseCode();
			String result = "";

			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStream is = httpConnection.getInputStream();

				BufferedReader reader = new BufferedReader(new InputStreamReader(is));

				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}

				result = sb.toString();

				// show result
				if (result.length() > 0 && result.startsWith("ok")) {
					// successful LOL or INF
					//new AlertDialog.Builder(_context).setTitle(_tag + "'d").setPositiveButton("OK", null).setMessage("The post was successfully\n[ " + _tag + "'d ]").show();

					// update stats
					if (_tag.equalsIgnoreCase("LOL"))
						ShackDroidStats.AddLOLsMade(_context);
					else if (_tag.equalsIgnoreCase("INF"))
						ShackDroidStats.AddINFsMade(_context);
					else if (_tag.equalsIgnoreCase("UNF"))
						ShackDroidStats.AddUNFsMade(_context);
					else if (_tag.equalsIgnoreCase("TAG"))
						ShackDroidStats.AddTAGsMade(_context);

				}
				else {
					// error
					_errorMessage = result;
					//new AlertDialog.Builder(_context).setTitle("Error").setPositiveButton("OK", null).setMessage(result).show();
				}
			}
			else {
				// error
				_errorMessage = "Unable to contact ThomW's server, please try again later.";
				//new AlertDialog.Builder(_context).setTitle("Error").setPositiveButton("OK", null).setMessage("Unable to contact ThomW's server, please try again later.").show();
			}
		}
		catch (Exception e) {
			_errorMessage = "Unable to contact ThomW's server, please try again later.";
			//new AlertDialog.Builder(_context).setTitle("Error").setPositiveButton("OK", null).setMessage("Unable to contact ThomW's server, please try again later.").show();
			//e.printStackTrace();
		}

		return null;
	}

}
