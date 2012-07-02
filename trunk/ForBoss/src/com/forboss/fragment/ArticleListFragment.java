package com.forboss.fragment;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.forboss.data.model.Article;
import com.forboss.utils.ArticleListBuilder;

public class ArticleListFragment extends Fragment {
	private Context context;
	private ArticleListBuilder articleListBuilder = new ArticleListBuilder();
	private List<Article> data;
	private String category;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View result = articleListBuilder.build(context, inflater, container, data, category);
		return result;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		articleListBuilder.destroy();
	}

	public ArticleListBuilder getArticleListBuilder() {
		return articleListBuilder;
	}

	public void setArticleListBuilder(ArticleListBuilder articleListBuilder) {
		this.articleListBuilder = articleListBuilder;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public List<Article> getData() {
		return data;
	}

	public void setData(List<Article> data) {
		this.data = data;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
	
}
