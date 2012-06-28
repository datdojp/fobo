package com.forboss.utils;

import java.io.FileNotFoundException;
import java.util.List;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.forboss.ArticleDetailActivity;
import com.forboss.ForBossApplication;
import com.forboss.ForBossViewPagerFragmentActivity;
import com.forboss.R;
import com.forboss.custom.PullToRefreshListView;
import com.forboss.custom.PullToRefreshListView.OnRefreshListener;
import com.forboss.data.model.Article;

public class ArticleListBuilder {
	private Context context;
	private LayoutInflater inflater;

	private View root;
	private PullToRefreshListView ptrArticleList;
	private List<Article> ptrArticleListData;
	private ArticleAdapter ptrArticleListAdapter;

	private Handler refreshHandler;

	public View build(Context context, LayoutInflater inflater, ViewGroup container, List<Article> data) {
		this.context = context;
		this.inflater = inflater;
		this.ptrArticleListData = data;

		this.root = inflater.inflate(R.layout.article_list, container, false);
		ptrArticleList = (PullToRefreshListView) root.findViewById(R.id.article_ptr);
		ptrArticleList.setLockScrollWhileRefreshing(true);
		ptrArticleList.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				ForBossViewPagerFragmentActivity.getInstance().syncArticleContent();
			}
		});
		ptrArticleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int arg2,
					long arg3) {
				view.performClick();
			}
		});
		ptrArticleListAdapter = new ArticleAdapter(context, ptrArticleListData);
		ptrArticleList.setAdapter(ptrArticleListAdapter);
		if (data != null && data.size() > 0 && data.get(0).getCategory().equals(ForBossUtils.getEventCategory())) {
			((MarginLayoutParams) ptrArticleList.getLayoutParams()).setMargins(0, 0, 0, 0);
		}

		refreshHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				ptrArticleListAdapter.notifyDataSetChanged();
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

	public void destroy() {
		if (ptrArticleList != null) {
			for(int i = 0; i < ptrArticleList.getChildCount(); i++) {
				View view = ptrArticleList.getChildAt(i);
				if (view != null) {
					ImageView thumbnailImage = (ImageView) view.findViewById(R.id.thumbnailImage);
					if (thumbnailImage != null) {
						recycleBitmapOfImage(thumbnailImage, "destroy");
					}
				}
			}
		}
	}

	private void recycleBitmapOfImage(ImageView img, String tag) {
		Bitmap oldBm = (Bitmap) img.getTag();
		if (oldBm != null) {
			img.setImageBitmap(null);
			img.setTag(null);
			oldBm.recycle();
			Log.d(this.getClass().getName(), "...........Recycle bitmap for " + tag + "..........");
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
			View view;
			Article article = data.get(position);

			if (convertView != null) {
				view = convertView;
			} else {
				if (article.getCategory().equals(ForBossUtils.getEventCategory())) {
					view = inflater.inflate(R.layout.event_item, null);
				} else {
					view = inflater.inflate(R.layout.article_item, null);
				}

			}

			view.setTag(article);

			// EVENT
			if (article.getCategory().equals(ForBossUtils.getEventCategory())) {
				// set thumbnail
				if (article.getPictureLocation() != null) {
					ImageView thumbnailImage = (ImageView) view.findViewById(R.id.thumbnailImage);
					recycleBitmapOfImage(thumbnailImage, "event");
					try {
						Bitmap bm = ForBossUtils.loadBitmapFromInternalStorage(article.getPictureLocation(), new ContextWrapper(context));
						thumbnailImage.setImageBitmap(bm);
						thumbnailImage.setTag(bm);
					} catch (FileNotFoundException e) {
						Log.e(this.getClass().getName(), e.getMessage());
						e.printStackTrace();
					}
				}

				// set title
				TextView titleText = (TextView) view.findViewById(R.id.titleText);
				titleText.setText(article.getTitle());

				// set place
				TextView placeText = (TextView) view.findViewById(R.id.placeText);
				placeText.setText("@ " + article.getEventPlace());

				// set time
				TextView timeText = (TextView) view.findViewById(R.id.timeText);
				timeText.setText(article.getEventTime());

				// set detail button
				view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Article article = (Article) v.getTag();
						ForBossUtils.putBundleData("article", article);
						Intent intent = new Intent(context, ArticleDetailActivity.class);
						context.startActivity(intent);
					}
				});
				// POST
			} else {
				// set thumbnail
				if (article.getPictureLocation() != null) {
					ImageView thumbnailImage = (ImageView) view.findViewById(R.id.thumbnailImage);
					recycleBitmapOfImage(thumbnailImage, "post");
					try {
						Bitmap bm = ForBossUtils.loadBitmapFromInternalStorage(article.getPictureLocation(), new ContextWrapper(context));
						int thumbnailImageWidth = ForBossApplication.getWindowDisplay().getWidth();
						int thumbnailImageHeight =  thumbnailImageWidth * bm.getHeight() / bm.getWidth();
						int twoDpInPx = ForBossUtils.convertDpToPixel(2, context);
						thumbnailImage.setLayoutParams(new RelativeLayout.LayoutParams(
								thumbnailImageWidth + 2 * twoDpInPx, 
								thumbnailImageHeight + 2 * twoDpInPx));
						thumbnailImage.setImageBitmap(bm);
						thumbnailImage.setTag(bm);
					} catch (FileNotFoundException e) {
						Log.e(this.getClass().getName(), e.getMessage());
						e.printStackTrace();
					}
				}

				// set title
				TextView title = (TextView) view.findViewById(R.id.title);
				title.setText(article.getTitle());

				// set time
				TextView time = (TextView) view.findViewById(R.id.time);
				time.setText(ForBossUtils.getLastUpdateInfo(article.getCreatedTimeInDate()));

				// set "view detail" action
				view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Article article = (Article) v.getTag();
						ForBossUtils.putBundleData("article", article);
						Intent intent = new Intent(context, ArticleDetailActivity.class);
						context.startActivity(intent);
					}
				});
			}

			return view;
		}
	}

	public List<Article> getPtrArticleListData() {
		return ptrArticleListData;
	}

	public void setPtrArticleListData(List<Article> ptrArticleListData) {
		this.ptrArticleListData = ptrArticleListData;
	}

	public PullToRefreshListView getPtrArticleList() {
		return ptrArticleList;
	}

	public void setPtrArticleList(PullToRefreshListView ptrArticleList) {
		this.ptrArticleList = ptrArticleList;
	}


}