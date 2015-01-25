package com.quazar.sms_firewall.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.quazar.sms_firewall.R;
import com.quazar.sms_firewall.dialogs.listeners.SelectListener;
import com.quazar.sms_firewall.utils.ContentUtils;
import com.quazar.sms_firewall.utils.LogUtil;

public class SelectListItemDialog extends Dialog implements OnScrollListener{

	private static final int PAGE_SIZE = 30;
	private int visibleThreshold = 5;
	private int currentPage = 0;
	private int previousTotal = 0;
	private boolean loading = true;
	private ListView listView;
	private List<HashMap<String, Object>> items;
	private List<HashMap<String, Object>> allItems;

	public SelectListItemDialog(final Context context, String title, final List<HashMap<String, Object>> allItems,
			final SelectListener<HashMap<String, Object>> listener){
		super(context, R.style.Dialog);
		setContentView(R.layout.list_common);
		this.allItems = allItems;
		this.items = new ArrayList<HashMap<String, Object>>();
		addPage(1);
		((TextView) findViewById(R.id.list_title)).setText(title);
		listView = (ListView) findViewById(R.id.list);
		listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				listener.recieveSelection(items.get(position));
				dismiss();
			}
		});
		SimpleAdapter adapter =
				new SimpleAdapter(context, items, R.layout.item_common, new String[] {ContentUtils.NAME,
						ContentUtils.NUMBER, ContentUtils.TEXT, ContentUtils.DATE }, new int[] {R.id.item_name,
						R.id.item_number, R.id.item_text, R.id.item_date });
		listView.setAdapter(adapter);
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		listView.setOnScrollListener(this);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState){}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount){
		((SimpleAdapter) view.getAdapter()).notifyDataSetChanged();
		if (loading) {
			if (totalItemCount > previousTotal) {
				loading = false;
				previousTotal = totalItemCount;
				currentPage++;
			}
		}
		if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
			try {
				loadPage(currentPage + 1);
			}
			catch(Exception ex) {
				LogUtil.error(getContext(), "onScroll", ex);
			}
			loading = true;
		}
	}

	private void loadPage(int page) throws Exception{
		addPage(page);
		((SimpleAdapter) listView.getAdapter()).notifyDataSetChanged();
	}

	private void addPage(int page){
		int end = page * PAGE_SIZE;
		int start = currentPage * PAGE_SIZE;
		if (end > allItems.size()) {
			end = allItems.size();
		}
		if (start >= end) {
			return;
		}
		items.addAll(allItems.subList(start, end));
	}
}
