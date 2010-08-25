package com.stonedonkey.shackdroid;
import android.graphics.Color;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

public class SpoilerSpan extends ClickableSpan {
    public SpoilerSpan(TextView parent) {
        mParent = parent;
        mHidden = true;
    }

    public void onClick(View widget) {
        if (mHidden) {
            mHidden = false;
            mParent.invalidate();
        }
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        int bgColor = mBGColor;
        int textColor = mTextColor;

        if (mHidden)
            bgColor = textColor = mSpoilerColor;
        
        ds.bgColor = bgColor;
        ds.setColor(textColor);
    }

    TextView mParent;
    Boolean mHidden;
    int mSpoilerColor = Color.parseColor("#383838");
    int mTextColor = Color.parseColor( "#FFFFFF");
    int mBGColor = Color.parseColor("#222222");
}