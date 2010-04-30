package com.stonedonkey.shackdroid;

import android.app.Activity;
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
		
		final String testHtml = "<html><head><style>body { color:white } a { color:white; }</style></head><body bgcolor='#000000'>Call of Duty: World at War developer Treyarch has <a href=\"http://www.callofduty.com/blackops/intel/view/blog/38\" rel=\"nofollow\" target=\"_blank\" class=\"external\">announced</a> that the latest entry in the long-running shooter series is named Call of Duty: Black Ops and will be released on November 9, 2010, ahead of its revelation <a href=\"http://www.shacknews.com/onearticle.x/63539\">on tonight's episode</a> of GameTrailers TV.<p>While the announcement gives no details on the game, it has long been <a href=\"http://www.shacknews.com/onearticle.x/58608\">rumoured</a> that Treyarch's next Call of Duty title would be set in the Vietnam War era. PC, Xbox 360 and PlayStation 3 releases should be expected, considering the franchise's history.</p><p><div class=\"focalbox\"><a href=\"http://www.shacknews.com/screenshots.x?gallery=10546&amp;game_id=5286#img124581\"><img src=\"http://www.shacknews.com/images/sshots/Screenshot/10546/10546_48ac1624bdbd7_thumbnail.jpg\"></a><a href=\"/screenshots.x?gallery=14346&amp;game_id=6192&amp;id=153909\"><img src=\"/images/sshots/Screenshot/14346/14346_4bbc86ec6548f_thumbnail.jpg\"></a><br /><em>Treyarch's 2008 World at War and sister studio Infinity Ward's 2009 Modern Warfare 2.</em></div></p><p>While all seems in order at Treyarch, its sister studio--Call of Duty creator Infinity Ward--is in turmoil. After the studio's co-founders Vince Zampella and Jason West were <a href=\"http://www.shacknews.com/onearticle.x/62577\">fired</a> by owner Activision in March, a large proportion of its staff quit--many to join West and Zampella's <a href=\"http://www.shacknews.com/onearticle.x/63252\">new studio</a>, Respawn Entertainment. Activision is now being <a href=\"http://www.shacknews.com/onearticle.x/63521\">sued</a> by thirty-eight current and former employees for unpaid royalties plus damages.</p><p>We'll no doubt learn more about Call of Duty: Black Ops on GameTrailers TV tonight.</body></html>";
		
		//TextView tv = (TextView) findViewById(R.id.TextViewInfoView);
		//tv.setText(Html.fromHtml(testHtml));		
		//Linkify.addLinks(tv,Linkify.ALL);
		
		WebView wv = (WebView)findViewById(R.id.WebViewInfoView);
		wv.loadData(testHtml, "text/html","utf-8");
		wv.setVerticalFadingEdgeEnabled(false);
		wv.setVerticalScrollbarOverlay(true);
		
		Animation anim = AnimationUtils.loadAnimation(getBaseContext(), R.anim.toggle_infoviewer);
		View v = findViewById(R.id.WebViewInfoView);
		v.startAnimation(anim);
		anim = null;
		
		
		
	}

}
