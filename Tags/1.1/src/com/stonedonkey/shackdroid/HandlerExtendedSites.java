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
import android.preference.PreferenceManager;

public class HandlerExtendedSites extends Activity {

	public static void AddRemoveShackMark(Context ctx, String id,Boolean delete) {

		try {

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
			String shackLogin = prefs.getString("shackLogin", "");

			// make sure they have their username set
			if (shackLogin.length() == 0) {
				new AlertDialog.Builder(ctx).setTitle("Username Required")
						.setPositiveButton("OK", null).setMessage(
								"Please set your Login in settings.").show();
			
				return;
			}

			String data = URLEncoder.encode("user", "UTF-8") + "="
					+ URLEncoder.encode(shackLogin, "UTF-8") + "&"
					+ URLEncoder.encode("id", "UTF-8") + "="
					+ URLEncoder.encode(id, "UTF-8");

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

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is));

				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}

				result = sb.toString();

				// show result
				if (result.length() > 0 && result.startsWith("ok")) {

					if (delete == false)
						new AlertDialog.Builder(ctx)
									.setTitle("ShackMark Added")
									.setPositiveButton("OK", null)
									.setMessage("The post was successfully added to your ShackMarks")
									.show();
					//else
					//	new AlertDialog.Builder(ctx)
					//		.setTitle("ShackMark Removed")
					//		.setPositiveButton("OK", null)
					//		.setMessage("The post was successfully removed to your ShackMarks")
					//		.show();					
					
				} else {
					
					new AlertDialog.Builder(ctx).setTitle("ShackMark Failed")
							.setPositiveButton("OK", null).setMessage(
									"ShackMark failed").show();
				}

			}
		} catch (Exception e) {
			new AlertDialog.Builder(ctx)
					.setTitle("Error")
					.setPositiveButton("OK", null)
					.setMessage(
							"Unable to contact ShackMarks server, please try again later.")
					.show();
		}

	}

	public static void INFLOLPost(Context ctx, String shackName, String postID,
			String tag) {

		// create a URL to post to
		try {

			tag = tag.toUpperCase();

			String data = URLEncoder.encode("who", "UTF-8") + "="
					+ URLEncoder.encode(shackName, "UTF-8") + "&"
					+ URLEncoder.encode("what", "UTF-8") + "="
					+ URLEncoder.encode(postID, "UTF-8") + "&"
					+ URLEncoder.encode("tag", "UTF-8") + "="
					+ URLEncoder.encode(tag, "UTF-8") + "&" + "version=-1";

			// post to ShackNews
			URL url = new URL(
					"http://lmnopc.com/greasemonkey/shacklol/report.php?"
							+ data);

			URLConnection conn = url.openConnection();
			HttpURLConnection httpConnection = (HttpURLConnection) conn;

			int responseCode = httpConnection.getResponseCode();
			String result = "";

			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStream is = httpConnection.getInputStream();

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is));

				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}

				result = sb.toString();

				// show result
				if (result.length() > 0 && result.startsWith("ok")) {
					// successful LOL or INF
					new AlertDialog.Builder(ctx).setTitle(tag + "'d")
							.setPositiveButton("OK", null).setMessage(
									"The post was successfully\n[ " + tag
											+ "'d ]").show();
				} else {
					// error
					new AlertDialog.Builder(ctx).setTitle("Error")
							.setPositiveButton("OK", null).setMessage(result)
							.show();
				}
			} else {
				// error
				new AlertDialog.Builder(ctx)
						.setTitle("Error")
						.setPositiveButton("OK", null)
						.setMessage(
								"Unable to contact ThomW's server, please try again later.")
						.show();
			}

		}

		catch (Exception e) {

			new AlertDialog.Builder(ctx)
					.setTitle("Error")
					.setPositiveButton("OK", null)
					.setMessage(
							"Unable to contact ThomW's server, please try again later.")
					.show();
			e.printStackTrace();
		}
	}
}
