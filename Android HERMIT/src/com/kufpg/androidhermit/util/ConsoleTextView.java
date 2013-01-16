package com.kufpg.androidhermit.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

public class ConsoleTextView extends TextView {

	public final static float TEXT_SIZE = 15;
	public final static String TYPEFACE = "fonts/DroidSansMonoDotted.ttf";
	
	private int mCmdOrderNum;

	public ConsoleTextView(Context context) {
		super(context);
		setupView(context, null, 0);
	}

	public ConsoleTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupView(context, null, 0);
	}

	public ConsoleTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setupView(context, null, 0);
	}
	
	public ConsoleTextView(Context context, String msg, int cmdOrderNum) {
		super(context);
		setupView(context, msg, cmdOrderNum);
	}

	protected void setupView(Context context, String msg, int cmdOrderNum) {
		this.addTextChangedListener(new PrettyPrinter());
		Typeface mTypeface = Typeface.createFromAsset(context.getAssets(), TYPEFACE);
		
		this.setTypeface(mTypeface);
		this.setTextColor(Color.WHITE);
		this.setTextSize(TEXT_SIZE);
		this.setGravity(Gravity.BOTTOM);
		// TODO: Make a better ID system
		this.setId((int) System.currentTimeMillis());
		this.setText("hermit<" + cmdOrderNum + "> ");
		mCmdOrderNum = cmdOrderNum;
		if (msg != null)
			this.append(msg);
	}

	protected class PrettyPrinter implements TextWatcher {
		String lastText = null;

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if (!s.toString().equals(lastText)) {
				lastText = s.toString();

				String res = "";
				String[] sentence = s.toString().split(" ");
				//Avoid HTML-ification of these triangle brackets
				sentence[0] = sentence[0].replace("<", "&lt;")
						.replace(">", "&gt;");
				for (String word : sentence) {
					String color = null;
					if (word.equals("red")) {
						color = "#CC060B"; //red
					} else if (word.equals("green")) {
						color = "#1DDA1C"; //green
					} else if (word.equals("blue")) {
						color = "#0090D3"; //blue
					}
					if (color != null) {
						res += "<font color=\"" + color + "\">" + word
								+ " </font>";
					} else {
						res += word + " ";
					}
				}

				ConsoleTextView.this.setText(Html.fromHtml(res));
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		@Override
		public void afterTextChanged(Editable s) {}

	}

}
