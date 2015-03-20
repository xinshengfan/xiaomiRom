package com.communication.provider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.communication.data.FileManager;

import android.content.Context;

public class Java2Xml {
	private StringBuilder sportXML, sleepXML;
	
	private Context mContext;
	
	private SimpleDateFormat mDateTimeFormat;

	public Java2Xml(Context context) {
		mContext=context;
		mDateTimeFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm");
		mDateTimeFormat.setTimeZone(TimeZone.getDefault());
		sportXML = new StringBuilder("<sportsdata>");
		sleepXML = new StringBuilder("<sleepdata>");
	}

	public void addSportNode(int stpes,int kCal, int distance,  long time) {
		sportXML.append("<item steps=\"" + stpes + "\"  distance=\"" + distance
				+ "\" kcal=\"" + kCal + "\" time=\"" + mDateTimeFormat.format(new Date(time)) + "\"/>");
	}

	public void addSleepNode(int level, long time) {
		sleepXML.append("<item level=\"" + level + "\" time=\"" + mDateTimeFormat.format(new Date(time))  + "\"/>");
	}

	public String getXmlContent() {
		String head = "<xml><root>";
		String end = "</root></xml>";
		sportXML.append("</sportsdata>");
		sleepXML.append("</sleepdata>");
		return head + sportXML.toString() + sleepXML.toString() + end;
	}
	
	public void save(){
		   new FileManager().saveXmlToSD(mContext, "sportdata.xml", getXmlContent());
	}
}
