package com.stonedonkey.shackdroid;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpHelper { 

	public static InputStream HttpRequestWithGzip(String url) throws ClientProtocolException, IOException
	{
		DefaultHttpClient client = new DefaultHttpClient();
		HttpUriRequest request = new HttpGet(url);
		request.addHeader("Accept-Encoding", "gzip");
		 
		HttpResponse response = client.execute(request);
		
		Header contentEncoding = response.getFirstHeader("Content-Encoding");
		
		InputStream content;
		if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip"))
			content = new GZIPInputStream(response.getEntity().getContent());
		else
			content = new DataInputStream(response.getEntity().getContent());
		
		return content;
	}
}
