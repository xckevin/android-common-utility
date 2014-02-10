package com.xckevin.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xckevin.android.utility.R;

public class PullToLoadListView extends ListView implements OnScrollListener {

	private static final String TAG = PullToLoadListView.class.getSimpleName();

	private static final int STATE_NON = 0;
	private static final int STATE_PULL_TO_REFRESH = 1;
	private static final int STATE_RELEASE_TO_REFRESH = 2;
	private static final int STATE_REFRESHING = 3;

	private int state;

	private int firstVisibleItem;
	private int lastVisisibleItem;

	private float prevY = 0;

	private View headerView;
	private View footerView;

	// header widgets
	private ProgressBar headerProgressBar;
	private ImageView headerImageArrow;
	private TextView headerText;
	private RotateAnimation headerArrowAnim;
	private RotateAnimation headerArrowReverseAnim;
	// footer widgets
	private ProgressBar footerProgressBar;
	private TextView footerText;

	private boolean headerIsHanding = false;
	private boolean footerIsHanding = false;

	private int headerHeight;
	private int footerHeight;

	private ResetAnimation resetAnim;

	private OnLoadingListener onLoadingListener;
	
	private OnScrollListener onScrollListener;

	public PullToLoadListView(Context context) {
		super(context);
		init(context);
	}

	public PullToLoadListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		state = STATE_NON;
		firstVisibleItem = 0;
		lastVisisibleItem = 0;

		LayoutInflater inflater = LayoutInflater.from(context);
		headerView = inflater.inflate(R.layout.view_pull_header, null);
		footerView = inflater.inflate(R.layout.view_pull_footer, null);

		headerProgressBar = (ProgressBar) headerView.findViewById(R.id.progressbar);
		headerImageArrow = (ImageView) headerView.findViewById(R.id.arrow);
		headerText = (TextView) headerView.findViewById(R.id.text);
		headerArrowAnim = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		headerArrowAnim.setDuration(300);
		headerArrowAnim.setFillAfter(true);
		headerArrowReverseAnim = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		headerArrowReverseAnim.setDuration(300);
		headerArrowReverseAnim.setFillAfter(true);

		footerProgressBar = (ProgressBar) footerView.findViewById(R.id.progressbar);
		footerText = (TextView) footerView.findViewById(R.id.text);

		measureView(headerView);
		measureView(footerView);
		headerHeight = headerView.getMeasuredHeight();
		footerHeight = footerView.getMeasuredHeight();
		headerView.setPadding(0, -1 * headerView.getMeasuredHeight(), 0, 0);
		footerView.setPadding(0, -1 * footerView.getMeasuredHeight(), 0, 0);
		headerView.invalidate();
		footerView.invalidate();
		addHeaderView(headerView, null, false);
		addFooterView(footerView, null, false);

		super.setOnScrollListener(this);
	}

	private void measureView(View view) {
		ViewGroup.LayoutParams lp = view.getLayoutParams();
		if(lp == null) {
			lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, lp.width);
		int childHeightSpec;
		if(lp.height > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		view.measure(childWidthSpec, childHeightSpec);
	}

	private void resetHeader() {
		//		headerView.setPadding(0, -1 * headerHeight, 0, 0);
		resetAnim = new ResetAnimation(headerView, headerHeight, headerView.getPaddingTop());
		resetAnim.start();
	}

	private void resetFooter() {
		resetAnim = new ResetAnimation(footerView, footerHeight, footerView.getPaddingTop());
		resetAnim.start();
	}

	private void changeHeaderViewByState(int state) {
		if(this.state == state) {
			return ;
		}
		int prevState = this.state;
		this.state = state;

		switch(state) {
		case STATE_NON:
			headerProgressBar.setVisibility(View.INVISIBLE);
			headerImageArrow.setVisibility(View.VISIBLE);
			headerImageArrow.clearAnimation();
			headerText.setText("Pull Down To Refresh");
			break;
		case STATE_PULL_TO_REFRESH:
			headerProgressBar.setVisibility(View.INVISIBLE);
			headerImageArrow.setVisibility(View.VISIBLE);
			headerText.setText("Pull Down To Refresh");
			if(prevState == STATE_RELEASE_TO_REFRESH) {
				headerImageArrow.startAnimation(headerArrowReverseAnim);
			} else {
				headerImageArrow.clearAnimation();
			}
			break;
		case STATE_RELEASE_TO_REFRESH:
			headerProgressBar.setVisibility(View.INVISIBLE);
			headerImageArrow.setVisibility(View.VISIBLE);
			headerImageArrow.startAnimation(headerArrowAnim);
			headerText.setText("Release To Refresh");
			break;
		case STATE_REFRESHING:
			headerProgressBar.setVisibility(View.VISIBLE);
			headerImageArrow.setVisibility(View.INVISIBLE);
			headerImageArrow.clearAnimation();
			headerText.setText("Refreshing");
			break;
		default:
			break;
		}
	}

	private void changeFooterViewByState(int state) {
		if(this.state == state) {
			return ;
		}
		this.state = state;

		switch(state) {
		case STATE_NON:
			footerProgressBar.setVisibility(View.INVISIBLE);
			footerText.setText("Pull Up To Refresh");
			break;
		case STATE_PULL_TO_REFRESH:
			footerProgressBar.setVisibility(View.INVISIBLE);
			footerText.setText("Pull Up To Refresh");
			break;
		case STATE_RELEASE_TO_REFRESH:
			footerProgressBar.setVisibility(View.INVISIBLE);
			footerText.setText("Release To Refresh");
			break;
		case STATE_REFRESHING:
			footerProgressBar.setVisibility(View.VISIBLE);
			footerText.setText("Refreshing");
			break;
		default:
			break;
		}
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		this.onScrollListener = l;
	}

	public void setOnLoadingListener(OnLoadingListener onLoadingListener) {
		this.onLoadingListener = onLoadingListener;
	}

	public void loadCompleted() {
		if(headerIsHanding) {
			changeHeaderViewByState(STATE_NON);
			resetHeader();
			headerIsHanding = false;
		}
		if(footerIsHanding) {
			changeFooterViewByState(STATE_NON);
			resetFooter();
			footerIsHanding = false;
		}
	}

	private void handleMoveHeaderEvent(MotionEvent ev) {
		headerIsHanding = true;
		float tempY = ev.getRawY();
		float vector = tempY - prevY;
		prevY = tempY;
		if(vector > 0) {
			int newPadding = (int) (headerView.getPaddingTop() + vector);
			newPadding = Math.min(newPadding, headerHeight / 2);
			headerView.setPadding(0, newPadding, 0, 0);
			if(state != STATE_REFRESHING) {
				if(headerView.getMeasuredHeight() > headerHeight) {
					changeHeaderViewByState(STATE_RELEASE_TO_REFRESH);
				} else {
					changeHeaderViewByState(STATE_PULL_TO_REFRESH);
				}
			}
		} else {
			if(state == STATE_RELEASE_TO_REFRESH || state == STATE_PULL_TO_REFRESH) {
				int newPadding = (int) (headerView.getPaddingTop() + vector);
				newPadding = Math.max(newPadding, -1 * headerHeight);
				headerView.setPadding(0, newPadding, 0, 0);
				if(headerView.getMeasuredHeight() <= headerHeight) {
					changeHeaderViewByState(STATE_PULL_TO_REFRESH);
				}
				if(newPadding <= -1 * headerHeight) {
					changeHeaderViewByState(STATE_NON);
					headerIsHanding = false;
				}
			}
		}
	}

	private void handleMoveFooterEvent(MotionEvent ev) {
		footerIsHanding = true;
		float tempY = ev.getRawY();
		float vector = tempY - prevY;
		prevY = tempY;
		if(vector < 0) {
			int newPadding = (int) (footerView.getPaddingTop() - vector);
			if(newPadding > 0) {
				newPadding = 0;
			}
			footerView.setPadding(0, newPadding, 0, 0);
			if(state != STATE_REFRESHING) {
				if(newPadding < 0) {
					changeFooterViewByState(STATE_PULL_TO_REFRESH);
				} else {
					changeFooterViewByState(STATE_RELEASE_TO_REFRESH);
				}
			}
		} else {
			int newPadding = (int) (footerView.getPaddingTop() - vector);
			newPadding = Math.min(newPadding, footerHeight);
			footerView.setPadding(0, newPadding, 0, 0);
			if(newPadding < 0) {
				changeFooterViewByState(STATE_PULL_TO_REFRESH);
			}
			if(newPadding <= -1 * footerHeight) {
				changeFooterViewByState(STATE_NON);
				footerIsHanding = false;
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch(ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			prevY = ev.getRawY();
			break;
		case MotionEvent.ACTION_UP:
			if(state == STATE_RELEASE_TO_REFRESH) {
				if(headerIsHanding) {
					changeHeaderViewByState(STATE_REFRESHING);
					if(onLoadingListener != null) {
						onLoadingListener.onLoadNew();
					}
				}
				if(footerIsHanding) {
					changeFooterViewByState(STATE_REFRESHING);
					if(onLoadingListener != null) {
						onLoadingListener.onLoadMore();
					}
				}
			} else if(state == STATE_PULL_TO_REFRESH) {
				if(headerIsHanding) {
					changeHeaderViewByState(STATE_NON);
					resetHeader();
					headerIsHanding = false;
				}
				if(footerIsHanding) {
					changeFooterViewByState(STATE_NON);
					resetFooter();
					footerIsHanding = false;
				}
			} else if(state == STATE_NON) {
				headerIsHanding = false;
				footerIsHanding = false;
			} else {
				// state == STATE_REFRESHING
				// ignore
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if(resetAnim == null || !resetAnim.run) {
				
				if(state != STATE_REFRESHING) {
					Adapter adapter = getAdapter();
					if(adapter == null) {
						handleMoveHeaderEvent(ev);
					} else {
						final int count = adapter.getCount();
						if(count <= 0) {
							handleMoveHeaderEvent(ev);
						} else {
							Log.v(TAG, "first: " + firstVisibleItem);
							Log.v(TAG, "last: " + lastVisisibleItem);
							if(firstVisibleItem == 0 && lastVisisibleItem == count - 1) {
								if(headerIsHanding) {
									handleMoveHeaderEvent(ev);
								} else if(footerIsHanding) {
									handleMoveFooterEvent(ev);
								} else {
									float tempY = ev.getRawY();
									float vector = tempY - prevY;
									if(vector > 0) {
										handleMoveHeaderEvent(ev);
									} else if(vector < 0) {
										handleMoveFooterEvent(ev);
									} else {
										// ignore vector == 0
									}
								}
							} else if(firstVisibleItem == 0) {
								handleMoveHeaderEvent(ev);
							} else {
								// lastVisisibleItem == count - 1
								handleMoveFooterEvent(ev);
							}
						}
					}
				}
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(ev);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if(onScrollListener != null) {
			onScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		this.firstVisibleItem = firstVisibleItem;
		this.lastVisisibleItem = firstVisibleItem + visibleItemCount - 1;
		if(onScrollListener != null) {
			onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
	}

	static class ResetAnimation extends Thread {

		static final int DURATION = 600;

		static final int INTERVAL = 5;

		View view;
		int orignalHeight;
		int paddingTop;

		boolean run = false;

		ResetAnimation(View view, int orignalHeight, int paddingTop) {
			this.view = view;
			this.orignalHeight = orignalHeight;
			this.paddingTop = paddingTop;
		}

		public void run() {
			run = true;
			int total = orignalHeight * 2 + paddingTop;
			int timeTotal = DURATION / INTERVAL;
			int piece = total / timeTotal;
			int time = 0;
			final View view = this.view;
			final int paddingTop = this.paddingTop;
			do {
				final int nextPaddingTop = paddingTop - time * piece;
				view.post(new Runnable() {
					public void run() {
						view.setPadding(0, nextPaddingTop, 0, 0);
						view.postInvalidate();
					}
				});
				try {
					sleep(INTERVAL);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				time ++;
			} while(time < timeTotal);
			run = false;
		}
	}

	public interface OnLoadingListener {

		public void onLoadNew();

		public void onLoadMore();
	}

}
