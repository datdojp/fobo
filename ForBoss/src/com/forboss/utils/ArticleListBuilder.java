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
import com.forboss.MainActivity;
import com.forboss.R;
import com.forboss.custom.PullToRefreshListView;
import com.forboss.custom.PullToRefreshListView.OnRefreshListener;
import com.forboss.data.model.Article;

public class ArticleListBuilder {
	private Context context;
	private LayoutInflater inflater;
	private String category;

	private View root;
	private PullToRefreshListView ptrArticleList;
	private List<Article> ptrArticleListData;
	private ArticleAdapter ptrArticleListAdapter;

	private Handler refreshHandler;

	public View build(Context context, LayoutInflater inflater, ViewGroup container, List<Article> data, String category) {
		this.context = context;
		this.inflater = inflater;
		this.ptrArticleListData = data;
		this.category = category;

		this.root = inflater.inflate(R.layout.ptr_article_list, container, false);
		ptrArticleList = (PullToRefreshListView) root.findViewById(R.id.article_ptr);
		// Disable according to request
		//ptrArticleList.setLockScrollWhileRefreshing(true);
		ptrArticleList.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				MainActivity.getInstance().syncArticleContent();
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
		//		if (ForBossUtils.isSpecialCategory(this.category)) {
		if (ForBossUtils.belongsToGroup(this.category, ForBossUtils.GROUP_EVENT) 
				|| ForBossUtils.belongsToGroup(this.category, ForBossUtils.GROUP_C360)) {
			//			((MarginLayoutParams) ptrArticleList.getLayoutParams()).setMargins(0, 0, 0, 0);
			//TODO: deal with this
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
						ForBossUtils.recycleBitmapOfImage(thumbnailImage, "destroy");
					}
				}
			}
		}
	}

	private boolean isFavArticleList() {
		return this.category == null;
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
				if (isFavArticleList()) {
					view = inflater.inflate(R.layout.event_item, null);
				} else {
					//					if ( ForBossUtils.isSpecialCategory(article.getCategory()) ) {
					if (ForBossUtils.belongsToGroup(article.getCategory(), ForBossUtils.GROUP_EVENT) 
							|| ForBossUtils.belongsToGroup(article.getCategory(), ForBossUtils.GROUP_C360)) {
						view = inflater.inflate(R.layout.event_item, null);
					} else {
						view = inflater.inflate(R.layout.article_item, null);
					}
				}
			}

			view.setTag(article);

			// EVENT || 360
			//			if ( ForBossUtils.isSpecialCategory(article.getCategory()) ) {
			if (ForBossUtils.belongsToGroup(article.getCategory(), ForBossUtils.GROUP_EVENT) 
					|| ForBossUtils.belongsToGroup(article.getCategory(), ForBossUtils.GROUP_C360)) {
				buildSpecialArticleView(view, article, 
						//						ForBossUtils.getEventCategory().equals(article.getCategory()),//showTimeAndPlace 
						//						ForBossUtils.getC360Category().equals(article.getCategory())//showBody
						ForBossUtils.belongsToGroup(article.getCategory(), ForBossUtils.GROUP_EVENT),//showTimeAndPlace 
						ForBossUtils.belongsToGroup(article.getCategory(), ForBossUtils.GROUP_C360)//showBody
						//!isFavArticleList(),//showDetailButton
						//isFavArticleList()//showRemoveFavButton
						);

				// POST
			} else {
				if (isFavArticleList()) {
					buildSpecialArticleView(view, article, 
							false,//showTimeAndPlace 
							true//showBody
							//false,//showDetailButton 
							//true//showRemoveFavButton
							);
				} else {
					buildePostArticleView(view, article);
				}
			}

			return view;
		}

		private void buildSpecialArticleView(View view, Article article, boolean showTimeAndPlace, boolean showBody 
				/*boolean showDetailButton, boolean showRemoveFavButton*/) {
			// set thumbnail
			if (article.getPictureLocation() != null) {
				ImageView thumbnailImage = (ImageView) view.findViewById(R.id.thumbnailImage);
				ForBossUtils.recycleBitmapOfImage(thumbnailImage, "event");
				Bitmap bm = ForBossUtils.loadBitmapFromInternalStorage(article.getPictureLocation(), new ContextWrapper(context));
				thumbnailImage.setImageBitmap(bm);
				thumbnailImage.setTag(bm);
			}

			// set title
			TextView titleText = (TextView) view.findViewById(R.id.titleText);
			titleText.setText(article.getTitle());

			if (showTimeAndPlace) {
				// set place
				TextView placeText = (TextView) view.findViewById(R.id.placeText);
				placeText.setVisibility(View.VISIBLE);
				placeText.setText("@ " + article.getEventPlace());

				// set time
				TextView timeText = (TextView) view.findViewById(R.id.timeText);
				timeText.setVisibility(View.VISIBLE);
				timeText.setText(article.getEventTime());
			}

			if (showBody) {
				// set body
				TextView bodyText = (TextView) view.findViewById(R.id.bodyText);
				bodyText.setVisibility(View.VISIBLE);
				bodyText.setText(article.getBody());
				bodyText.setHeight(105);
			}

			// set functional button
			//			ImageButton funtionalButton = (ImageButton) view.findViewById(R.id.funtionalButton);
			//			funtionalButton.setTag(article);
			//			if (showDetailButton) {
			//				funtionalButton.setImageResource(R.drawable.right_arrow);
			//			} else if (showRemoveFavButton) {
			//				funtionalButton.setImageResource(R.drawable.but_delete);
			//			}

			setOnClickListener(view);
		}

		private void buildePostArticleView(View view, Article article) {
			// set thumbnail
			if (article.getPictureLocation() != null) {
				ImageView thumbnailImage = (ImageView) view.findViewById(R.id.thumbnailImage);
				ForBossUtils.recycleBitmapOfImage(thumbnailImage, "post");

				Bitmap bm = ForBossUtils.loadBitmapFromInternalStorage(article.getPictureLocation(), new ContextWrapper(context));
				if (bm != null) {
					int thumbnailImageWidth = ForBossApplication.getWindowDisplay().getWidth();
					int thumbnailImageHeight =  thumbnailImageWidth * bm.getHeight() / bm.getWidth();
					int twoDpInPx = ForBossUtils.convertDpToPixel(2, context);
					thumbnailImage.setLayoutParams(new RelativeLayout.LayoutParams(
							thumbnailImageWidth + 2 * twoDpInPx, 
							thumbnailImageHeight + 2 * twoDpInPx));
					thumbnailImage.setImageBitmap(bm);
					thumbnailImage.setTag(bm);
				}
			}

			// set title
			TextView title = (TextView) view.findViewById(R.id.title);
			title.setText(article.getTitle());

			// set time
			TextView time = (TextView) view.findViewById(R.id.time);
			time.setText(ForBossUtils.getLastUpdateInfo(article.getCreatedTimeInDate()));

			setOnClickListener(view);
		}

		private void setOnClickListener(View view) {
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