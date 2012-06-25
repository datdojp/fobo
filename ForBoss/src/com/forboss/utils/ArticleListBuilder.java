package com.forboss.utils;

import java.io.FileNotFoundException;
import java.util.List;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.forboss.R;
import com.forboss.custom.PullToRefreshListView;
import com.forboss.custom.PullToRefreshListView.OnRefreshListener;
import com.forboss.data.model.Article;

public class ArticleListBuilder {
	private Context context;
	private LayoutInflater inflater;

	private View root;
	private PullToRefreshListView ptrNewsList;
	private List<Article> ptrNewsListData;
	private ArticleAdapter ptrNewsListAdapter;

	private Handler refreshHandler;

	public View build(Context context, LayoutInflater inflater, ViewGroup container, List<Article> data) {
		this.context = context;
		this.inflater = inflater;
		this.ptrNewsListData = data;

		this.root = inflater.inflate(R.layout.article_list, container, false);
		ptrNewsList = (PullToRefreshListView) root.findViewById(R.id.newsList);
		ptrNewsList.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {

			}
		});
		ptrNewsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2,
					long arg3) {
				view.performClick();
			}
		});
		ptrNewsListAdapter = new ArticleAdapter(context, ptrNewsListData);
		ptrNewsList.setAdapter(ptrNewsListAdapter);

		refreshHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				ptrNewsListAdapter.notifyDataSetChanged();
			}
		};

		return this.root;
	}

	public void refresh() {
		if (refreshHandler != null) {
			Message message = refreshHandler.obtainMessage();
			refreshHandler.sendMessage(message);
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

			// set thumbnail
			if (article.getPictureLocation() != null) {
				ImageView thumbnailImage = (ImageView) view.findViewById(R.id.thumbnailImage);
				try {
					thumbnailImage.setImageBitmap(ForBossUtils.loadBitmapFromInternalStorage(article.getPictureLocation(), new ContextWrapper(context)));
				} catch (FileNotFoundException e) {
					Log.e(this.getClass().getName(), e.getMessage());
					e.printStackTrace();
				}
			}

			// set title
			TextView title = (TextView) view.findViewById(R.id.title);
			title.setText(article.getTitle());

			return view;
		}
	}

	public List<Article> getPtrNewsListData() {
		return ptrNewsListData;
	}

	public void setPtrNewsListData(List<Article> ptrNewsListData) {
		this.ptrNewsListData = ptrNewsListData;
	}


}