package com.forboss.custom;

import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.forboss.R;
import com.forboss.utils.ForBossUtils;

/**
 * A generic, customizable Android ListView implementation that has 'Pull to Refresh' functionality.
 * 
 * This ListView can be used in place of the normal Android android.widget.ListView class.
 * 
 * Users of this class should implement OnRefreshListener and call setOnRefreshListener(..)
 * to get notified on refresh events. The using class should call onRefreshComplete() when
 * refreshing is finished.
 * 
 * The using class can call setRefreshing() to set the state explicitly to refreshing. This 
 * is useful when you want to show the spinner and 'Refreshing' text when the
 * refresh was not triggered by 'Pull to Refresh', for example on start.
 * 
 * For more information, visit the project page:
 * https://github.com/erikwt/PullToRefresh-ListView
 * 
 * @author Erik Wallentinsen <dev+ptr@erikw.eu>
 * @version 1.0.0
 */
public class PullToRefreshListView extends ListView{

	private static final float	PULL_RESISTANCE					= 1.7f;
	private static final int	BOUNCE_ANIMATION_DURATION		= 700;
	private static final int	BOUNCE_ANIMATION_DELAY			= 100;
	private static final float	BOUNCE_OVERSHOOT_TENSION		= 1.4f;
	private static final int	ROTATE_ARROW_ANIMATION_DURATION	= 250;
	private static final String PREF_NEWS_LIST					= "NewsList";
	private static final String PREF_NEWS_LIST_LAST_UPDATE		= "NewsListLastUpdate";

	private static enum State{
		PULL_TO_REFRESH,
		RELEASE_TO_REFRESH,
		REFRESHING
	}
	
	public static enum FetchMode {
		FETCH_LATER,
		FETCH_OLDER,
		DO_NOTHING
	}

	/**
	 * Interface to implement when you want to get notified of 'pull to refresh'
	 * events.
	 * Call setOnRefreshListener(..) to activate an OnRefreshListener.
	 */
	public interface OnRefreshListener{
		
		/**
		 * Method to be called when a refresh is requested
		 */
		public void onRefresh();
	}

	private static int 			measuredHeaderHeight;

	private float				previousY;
	private int					headerPadding;
	private boolean				scrollbarEnabled;
	private boolean				bounceBackHeader;
	private boolean				lockScrollWhileRefreshing;
	private boolean 			hasResetHeader;
    private String              pullToRefreshText;
    private String              releaseToRefreshText;
    private String              refreshingText;

	private State				state;
	private LinearLayout		headerContainer;
	private RelativeLayout		header;
	private RotateAnimation		flipAnimation;
	private RotateAnimation		reverseFlipAnimation;
    private ImageView			image;
	private ProgressBar			spinner;
	private TextView			text;
	private TextView			txtLastUpdate;
	private OnItemClickListener onItemClickListener;
	private OnRefreshListener	onRefreshListener;
	private FetchMode 			fetchMode = FetchMode.DO_NOTHING;
	private LinearLayout 		footerContainer;
	private Date				lastUpdate;
	
	public PullToRefreshListView(Context context){
		super(context);
		init();
	}

	public PullToRefreshListView(Context context, AttributeSet attrs){
		super(context, attrs);
		init();
	}

	public PullToRefreshListView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		init();
	}
	
	@Override
	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}

	/**
	 * Activate an OnRefreshListener to get notified on 'pull to refresh'
	 * events.
	 * 
	 * @param onRefreshListener The OnRefreshListener to get notified
	 */
	public void setOnRefreshListener(OnRefreshListener onRefreshListener){
		this.onRefreshListener = onRefreshListener;
	}

	/**
	 * @return If the list is in 'Refreshing' state
	 */
	public boolean isRefreshing(){
		return state == State.REFRESHING;
	}

	/**
	 * Default is false. When lockScrollWhileRefreshing is set to true, the list
	 * cannot scroll when in 'refreshing' mode. It's 'locked' on refreshing.
	 * 
	 * @param lockScrollWhileRefreshing
	 */
	public void setLockScrollWhileRefreshing(boolean lockScrollWhileRefreshing){
		this.lockScrollWhileRefreshing = lockScrollWhileRefreshing;
	}
	
	/**
	 * Explicitly set the state to refreshing. This
	 * is useful when you want to show the spinner and 'Refreshing' text when
	 * the refresh was not triggered by 'pull to refresh', for example on start.
	 */
	public void setRefreshing(){
		state = State.REFRESHING;
		scrollTo(0, 0);
		setUiRefreshing();
		setHeaderPadding(0);
	}

	/**
	 * Set the state back to 'pull to refresh'. Call this method when refreshing
	 * the data is finished.
	 */
	public void onRefreshComplete(){
		state = State.PULL_TO_REFRESH;
		if (fetchMode == FetchMode.FETCH_LATER) {
			resetHeader();
		} else if (fetchMode == FetchMode.FETCH_OLDER) {
			resetFooter();
		}
		
		fetchMode = FetchMode.DO_NOTHING;
		
		//store the last update information
		lastUpdate = new Date();
		SharedPreferences settings = getContext().getSharedPreferences(PREF_NEWS_LIST, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(PREF_NEWS_LIST_LAST_UPDATE, lastUpdate.getTime());
		editor.commit();
	}

    /**
     * Change the label text on state 'Pull to Refresh'
     * @param pullToRefreshText Text
     */
    public void setTextPullToRefresh(String pullToRefreshText){
        this.pullToRefreshText = pullToRefreshText;
        if(state == State.PULL_TO_REFRESH){
            text.setText(pullToRefreshText);
        }
    }

    /**
     * Change the label text on state 'Release to Refresh'
     * @param releaseToRefreshText Text
     */
    public void setTextReleaseToRefresh(String releaseToRefreshText){
        this.releaseToRefreshText = releaseToRefreshText;
        if(state == State.RELEASE_TO_REFRESH){
            text.setText(releaseToRefreshText);
        }
    }

    /**
     * Change the label text on state 'Refreshing'
     * @param refreshingText Text
     */
    public void setTextRefreshing(String refreshingText){
        this.refreshingText = refreshingText;
        if(state == State.REFRESHING){
            text.setText(refreshingText);
        }
    }
	
	private void init(){
		setVerticalFadingEdgeEnabled(false);

		headerContainer = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.pull_to_refresh_header, null);
		header = (RelativeLayout) headerContainer.findViewById(R.id.header);
		text = (TextView) header.findViewById(R.id.text);
		txtLastUpdate = (TextView) header.findViewById(R.id.txtLastUpdate);
		image = (ImageView) header.findViewById(R.id.image);
		spinner = (ProgressBar) header.findViewById(R.id.spinner);

        pullToRefreshText = getContext().getString(R.string.ptr_pull_to_refresh);
        releaseToRefreshText = getContext().getString(R.string.ptr_release_to_refresh);
        refreshingText = getContext().getString(R.string.ptr_refreshing);

		flipAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		flipAnimation.setInterpolator(new LinearInterpolator());
		flipAnimation.setDuration(ROTATE_ARROW_ANIMATION_DURATION);
		flipAnimation.setFillAfter(true);

		reverseFlipAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseFlipAnimation.setInterpolator(new LinearInterpolator());
		reverseFlipAnimation.setDuration(ROTATE_ARROW_ANIMATION_DURATION);
		reverseFlipAnimation.setFillAfter(true);

		addHeaderView(headerContainer);
		setState(State.PULL_TO_REFRESH);
		scrollbarEnabled = isVerticalScrollBarEnabled();
		
		ViewTreeObserver vto = header.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new PTROnGlobalLayoutListener());

        footerContainer = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.pull_to_refresh_footer, null);
        addFooterView(footerContainer);
        super.setOnItemClickListener(new PTROnItemClickListener());
        setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView, int scrollState) {
				// Dat: disable footer
				/*
				if( getAdapter().getCount() > 0 
						&& getLastVisiblePosition() == getAdapter().getCount() - 1 
						&& fetchMode != FetchMode.FETCH_OLDER ){
		        	if (onRefreshListener != null) {
		        		fetchMode = FetchMode.FETCH_OLDER;

		        		//display the footer
		        		footerContainer.setVisibility(View.VISIBLE);

		        		onRefreshListener.onRefresh();
		        	}
		        }
		        */
			}
			
			@Override
			public void onScroll(AbsListView absListView, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
        
        //get the last update information saved in preference
        SharedPreferences settings = getContext().getSharedPreferences(PREF_NEWS_LIST, Context.MODE_PRIVATE);
	    long lastUpdateInMs = settings.getLong(PREF_NEWS_LIST_LAST_UPDATE, -1);
	    if (lastUpdateInMs >= 0) {
	    	lastUpdate = new Date(lastUpdateInMs);
	    } else {
	    	lastUpdate = null;
	    }
	}

	private void setHeaderPadding(int padding){
		headerPadding = padding;

		MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) header.getLayoutParams();
		mlp.setMargins(0, padding, 0, 0);
		header.setLayoutParams(mlp);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
//	    return super.onInterceptTouchEvent(ev);
		return true;
	}
	
	private boolean isDisablePull = false;
	public void disablePull() {
		isDisablePull = true;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		if(lockScrollWhileRefreshing && state == State.REFRESHING){
			return true;
		}

		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				if (getFirstVisiblePosition() == 0) {
					previousY = event.getY();
				} else {
					previousY = -1;
				}
				break;

			case MotionEvent.ACTION_UP:
				if(previousY != -1 && (state == State.RELEASE_TO_REFRESH || getFirstVisiblePosition() == 0)){
					switch(state){
						case RELEASE_TO_REFRESH:
							setState(State.REFRESHING);
							bounceBackHeader();

							break;

						case PULL_TO_REFRESH:
							resetHeader();
							break;
					}
				}
				break;

			case MotionEvent.ACTION_MOVE:
				if(previousY != -1){
					float y = event.getY();
					float diff = y - previousY;
					if (diff > 0 && isDisablePull && getFirstVisiblePosition() == 0) {
						break;
					}
					if(diff > 0) diff /= PULL_RESISTANCE;
					previousY = y;

					int newHeaderPadding = Math.max(headerPadding + Math.round(diff), -header.getHeight()); 
					if(!lockScrollWhileRefreshing && state == State.REFRESHING && newHeaderPadding > 0){
						newHeaderPadding = 0;
					}
					
					setHeaderPadding(newHeaderPadding);

					//set last update information
					txtLastUpdate.setText("Cập nhật gần nhất: " + ForBossUtils.getLastUpdateInfo(lastUpdate));
					
					if(state == State.PULL_TO_REFRESH && headerPadding > 0){
						setState(State.RELEASE_TO_REFRESH);

						image.clearAnimation();
						image.startAnimation(flipAnimation);
					}else if(state == State.RELEASE_TO_REFRESH && headerPadding < 0){
						setState(State.PULL_TO_REFRESH);

						image.clearAnimation();
						image.startAnimation(reverseFlipAnimation);
					}
				}

				break;
		}

		return super.onTouchEvent(event);
	}

	
	
	private void bounceBackHeader(){
		int yTranslate = state == State.REFRESHING ? -(headerContainer.getHeight() - header.getHeight()) : -headerContainer.getHeight();

        TranslateAnimation bounceAnimation = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0,
                TranslateAnimation.ABSOLUTE, 0,
                TranslateAnimation.ABSOLUTE, 0,
                TranslateAnimation.ABSOLUTE, yTranslate);

		bounceAnimation.setDuration(BOUNCE_ANIMATION_DURATION);
		bounceAnimation.setFillEnabled(true);
		bounceAnimation.setFillAfter(false);
		bounceAnimation.setFillBefore(true);
		bounceAnimation.setInterpolator(new OvershootInterpolator(BOUNCE_OVERSHOOT_TENSION));
		bounceAnimation.setAnimationListener(new HeaderAnimationListener());

		startAnimation(bounceAnimation);
	}

	private void resetHeader(){
		if(headerPadding == -header.getHeight() || getFirstVisiblePosition() > 0){
			setState(State.PULL_TO_REFRESH);
			return;
		}

		if(getAnimation() != null && !getAnimation().hasEnded()){
			bounceBackHeader = true;
		}else{
			bounceBackHeader();
		}
	}
	
	private void resetFooter() {
		footerContainer.setVisibility(View.GONE);
	}
	
	private void setUiRefreshing(){
		spinner.setVisibility(View.VISIBLE);
		image.clearAnimation();
		image.setVisibility(View.INVISIBLE);
		text.setText(refreshingText);
	}

	public void autoRefresh() {
		setState(State.REFRESHING);
	}
	
	private void setState(State state){
		this.state = state;
		switch(state){
			case PULL_TO_REFRESH:
				spinner.setVisibility(View.INVISIBLE);
				image.setVisibility(View.VISIBLE);
				text.setText(pullToRefreshText);
				break;

			case RELEASE_TO_REFRESH:
				spinner.setVisibility(View.INVISIBLE);
				image.setVisibility(View.VISIBLE);
				text.setText(releaseToRefreshText);
				break;

			case REFRESHING:
				setUiRefreshing();

				if(onRefreshListener == null){
					setState(State.PULL_TO_REFRESH);
				}else{
					fetchMode = FetchMode.FETCH_LATER;
					onRefreshListener.onRefresh();
				}

				break;
		}
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		
		if(!hasResetHeader){
            if(measuredHeaderHeight > 0 && state != State.REFRESHING){
            	setHeaderPadding(-measuredHeaderHeight);
            }
            
			hasResetHeader = true;
		}
	}

	private class HeaderAnimationListener implements AnimationListener{

		private int		height;
		private State	stateAtAnimationStart;

		@Override
		public void onAnimationStart(Animation animation){
			stateAtAnimationStart = state;

			android.view.ViewGroup.LayoutParams lp = getLayoutParams();
			height = lp.height;
			lp.height = getHeight() + headerContainer.getHeight();
			setLayoutParams(lp);

			if(scrollbarEnabled){
				setVerticalScrollBarEnabled(false);
			}
		}

		@Override
		public void onAnimationEnd(Animation animation){
			setHeaderPadding(stateAtAnimationStart == State.REFRESHING ? 0 : -header.getHeight());

			android.view.ViewGroup.LayoutParams lp = getLayoutParams();
			lp.height = height;
			setLayoutParams(lp);

			if(scrollbarEnabled){
				setVerticalScrollBarEnabled(true);
			}

			if(bounceBackHeader){
				bounceBackHeader = false;

				postDelayed(new Runnable(){

					@Override
					public void run(){
						bounceBackHeader();
					}
				}, BOUNCE_ANIMATION_DELAY);
			}else if(stateAtAnimationStart != State.REFRESHING){
				setState(State.PULL_TO_REFRESH);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation){}
	}
	
	private class PTROnGlobalLayoutListener implements OnGlobalLayoutListener{

		@Override
        public void onGlobalLayout() {
            int initialHeaderHeight = header.getHeight();
            
            if(initialHeaderHeight > 0){
            	measuredHeaderHeight = initialHeaderHeight;
            	
            	if(measuredHeaderHeight > 0 && state != State.REFRESHING){
                	setHeaderPadding(-measuredHeaderHeight);
                    requestLayout();
                }
            }
            
            getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }
    }
	
	private class PTROnItemClickListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
			hasResetHeader = false;
			
			if(onItemClickListener != null){
				onItemClickListener.onItemClick(adapterView, view, position, id);
			}
		}
	}

	public FetchMode getFetchMode() {
		return fetchMode;
	}
}
