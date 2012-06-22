package com.forboss.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.forboss.R;
import com.forboss.custom.PullToRefreshListView;
import com.forboss.custom.PullToRefreshListView.OnRefreshListener;
import com.forboss.data.model.Article;

public class ArticleListBuilder {
	private View root;
	private LayoutInflater inflater;
	private PullToRefreshListView ptrNewsList;
	private List<Article> ptrNewsListData = new ArrayList<Article>();
	private ArticleAdapter ptrNewsListAdapter;

	public View build(LayoutInflater inflater, ViewGroup container) {
		this.inflater = inflater;
		this.root = inflater.inflate(R.layout.article_list, container, false);
		
		ptrNewsList = (PullToRefreshListView) root.findViewById(R.id.newsList);
		ptrNewsList.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				((ViewPagerFragmentActivity) getActivity()).syncAppNews(
						ptrNewsList.getFetchMode() == PullToRefreshListView.FetchMode.FETCH_LATER_NEWS,
						ptrNewsList.getFetchMode() == PullToRefreshListView.FetchMode.FETCH_OLDER_NEWS);
			}
		});
		ptrNewsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2,
					long arg3) {
				view.performClick();
			}
		});
		
		return this.root;
	}
	
	public void loadDataFromDB(boolean isFirstLoad) {
		if (isFistLoad || ptrNewsListAdapter == null) {
			ptrNewsListAdapter = new NewsAdapter(getActivity(), ptrNewsListData);
			ptrNewsList.setAdapter(ptrNewsListAdapter);
		} else {
			ptrNewsListAdapter.notifyDataSetChanged();
		}
	}

	public class ArticleAdapter extends ArrayAdapter<Article> {
		private List<Article> data;
		public ArticleAdapter(Context context, List<Article> data) {
			super(context, R.layout.article_item, data);
			this.data = data;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Article article = data.get(position);
			View view;
			if (convertView != null) {
				view = convertView;
			} else {
				view = inflater.inflate(R.layout.article_item, null);
			}

			//set content
			// TODO

			return view;
		}
	}
}