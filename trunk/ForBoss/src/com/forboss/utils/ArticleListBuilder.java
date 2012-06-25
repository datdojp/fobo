package com.forboss.utils;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.ContextWrapper;
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
import com.forboss.data.utils.DatabaseHelper;
import com.j256.ormlite.dao.Dao;

public class ArticleListBuilder {
	private Context context;
	private LayoutInflater inflater;
	private String category;
	
	private View root;
	private PullToRefreshListView ptrNewsList;
	private List<Article> ptrNewsListData = new ArrayList<Article>();
	private ArticleAdapter ptrNewsListAdapter;

	public View build(Context context, LayoutInflater inflater, ViewGroup container, String category) {
		this.context = context;
		this.inflater = inflater;
		this.category = category;
		
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
		
		refresh();
		
		return this.root;
	}
	
	public void refresh() {
		try {
			if (ptrNewsListAdapter == null) {
				Dao<Article, String> articleDao = DatabaseHelper.getHelper(context).getArticleDao();
				Article sampleArticle = new Article();
				sampleArticle.setCategory(category);
				ptrNewsListData = articleDao.queryForMatching(sampleArticle);
				
				ptrNewsListAdapter = new ArticleAdapter(context, ptrNewsListData);
				ptrNewsList.setAdapter(ptrNewsListAdapter);
			} else {
				ptrNewsListAdapter.notifyDataSetChanged();
			}
		} catch (SQLException ex) {
			Log.e(this.getClass().getName(), ex.getMessage());
			ForBossUtils.alert(context, "Cơ sở dữ liệu có vấn đề. Hãy khởi động lại ứng dụng.");
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