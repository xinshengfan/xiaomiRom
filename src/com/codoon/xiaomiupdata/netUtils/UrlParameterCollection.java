package com.codoon.xiaomiupdata.netUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UrlParameterCollection implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<UrlParameter> UrlParameterMap = new ArrayList<UrlParameter>();

	public void Add(UrlParameter urlParameter) {
		UrlParameterMap.add(urlParameter);
	}

	public void AddArray(UrlParameterCollection urlParameterCollection) {
		for (int i = 0; i < urlParameterCollection.Parameters().size(); i++) {
			UrlParameterMap.add(urlParameterCollection.Get(i));
		}
	}

	public List<UrlParameter> Parameters() {
		return UrlParameterMap;
	}

	public int GetCount() {
		return UrlParameterMap.size();
	}

	public UrlParameter Get(int i) {
		if (i >= 0 && i < GetCount()) {
			return UrlParameterMap.get(i);
		}
		return null;
	}

	public UrlParameter GetByName(String paraName) {
		for (int i = 0; i < GetCount(); i++) {
			if (UrlParameterMap.get(i).name.equals(paraName)) {
				return UrlParameterMap.get(i);
			}
		}
		return null;
	}

	public void Remove(UrlParameter urlParameter) {
		UrlParameterMap.remove(urlParameter);
	}

	public void Clear() {
		UrlParameterMap.clear();
	}

	@SuppressWarnings("unchecked")
	public void sort() {
		UrlParameter.ComparatorUrlParameter comparator = new UrlParameter.ComparatorUrlParameter();
		Collections.sort(UrlParameterMap, comparator);
	}
}
