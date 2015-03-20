package com.codoon.xiaomiupdata.xmlUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.communication.data.CLog;

import android.util.Xml;

public class ParseStream {
	private static StateInfo info;

	public static ArrayList<StateInfo> parseStream(InputStream is) {
		ArrayList<StateInfo> list = new ArrayList<StateInfo>();
		if (is != null) {
			try {
				XmlPullParser parser = Xml.newPullParser();
				parser.setInput(is, "UTF-8");
				int type = parser.getEventType();
				while (type != XmlPullParser.END_DOCUMENT) {
					switch (type) {
					case XmlPullParser.START_TAG:
						if ("client".equals(parser.getName())) {
							info = new StateInfo();
						} else if ("version".equals(parser.getName())) {
							info.setVersion(Integer.parseInt(parser.nextText()));
						} else if ("date".equals(parser.getName())) {
							info.setData_time(parser.nextText());
						} else if ("version_name".equals(parser.getName())) {
							info.setVersion_name(parser.nextText());
						} else if ("description".equals(parser.getName())) {
							info.setDescription(parser.nextText());
						} else if ("app_name".equals(parser.getName())) {
							info.setApp_name(parser.nextText());
						} else if ("size".equals(parser.getName())) {
							info.setSize(parser.nextText());
						} else if ("app_url".equals(parser.getName())) {
							info.setApp_url(parser.nextText());
						}

						break;

					case XmlPullParser.END_TAG:
						if ("client".equals(parser.getName())) {
							list.add(info);
						}
						break;

					}
					type = parser.next();

				}
				return list;
			} catch (XmlPullParserException e) {
				CLog.i("info", "“Ï≥£–≈œ¢£∫" + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
