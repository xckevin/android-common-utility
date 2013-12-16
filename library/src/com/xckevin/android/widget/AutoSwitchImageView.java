package com.xckevin.android.widget;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class AutoSwitchImageView extends ImageSwitcher implements GestureDetector.OnGestureListener, Animation.AnimationListener {

	private static final String TAG = AutoSwitchImageView.class.getSimpleName();

	private final long AUTO_SWITCH_TIME = 4000;
	
	private final long ANIMTION_DURATION = 300;

	private int DOIT_RADIUS = 4;

	private int DOIT_X_SPACE = 4;
	private int DOIT_Y_SPACE = 2;

	private Context context;

	private Bitmap[] images;

	private Animation leftInAnim;

	private Animation leftOutAnim;

	private Animation rightInAnim;

	private Animation rightOutAnim;

	private GestureDetector gesturedetector;
	
	private OnClickListener listener;

	private Timer timer;

	private TimerTask task;

	private int currentIndex = -1;

	private Paint paint;

	private boolean isSwitch;

	private final Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			if(isSwitch) {
				return false;
			}
			showNextImage();
			return true;
		}
	});

	public AutoSwitchImageView(Context context) {
		super(context);
		init(context);
	}

	public AutoSwitchImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		this.context = context;
		setFactory(new ViewFactory() {

			@Override
			public View makeView() {
				ImageView view = new ImageView(AutoSwitchImageView.this.context);
				view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
				view.setScaleType(ScaleType.FIT_XY);
				view.setLayoutParams(new ImageSwitcher.LayoutParams(ImageSwitcher.LayoutParams.MATCH_PARENT, ImageSwitcher.LayoutParams.MATCH_PARENT));
				return view;
			}

		});
		//		DOIT_RADIUS = context.getResources().getInteger(R.integer.auto_switch_doit_size);
		DOIT_X_SPACE = DOIT_RADIUS;
		DOIT_Y_SPACE = DOIT_RADIUS;
		gesturedetector = new GestureDetector(context, this);
		paint = new Paint();
		paint.setAntiAlias(true);
		if(images != null) {
			currentIndex = 0;
			setImageDrawable(new BitmapDrawable(getResources(), images[currentIndex]));
		}

		isSwitch = false;
		initAnimation();

		leftInAnim.setAnimationListener(this);
		rightInAnim.setAnimationListener(this);
		setInAnimation(leftInAnim);
		setOutAnimation(leftOutAnim);

		startAutoSwitch();
	}

	private void initAnimation() {
		Animation alphaIn = new AlphaAnimation(0.1f, 1.0f);
		alphaIn.setDuration(ANIMTION_DURATION);
		Animation alphtOut = new AlphaAnimation(1.0f, 0.1f);
		alphtOut.setDuration(ANIMTION_DURATION);
		Animation leftInTrans = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f);
		leftInTrans.setDuration(ANIMTION_DURATION);
		Animation leftOutTrans = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f);
		leftOutTrans.setDuration(ANIMTION_DURATION);
		Animation rightInTrans = new TranslateAnimation(Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f);
		rightInTrans.setDuration(ANIMTION_DURATION);
		Animation rightOutTrans = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 1.0f);
		rightOutTrans.setDuration(ANIMTION_DURATION);
		
		AnimationSet leftInAnim = new AnimationSet(true);
		leftInAnim.addAnimation(alphaIn);
		leftInAnim.addAnimation(leftInTrans);
		this.leftInAnim = leftInAnim;
		
		AnimationSet leftOutAnim = new AnimationSet(true);
		leftOutAnim.addAnimation(alphtOut);
		leftOutAnim.addAnimation(leftOutTrans);
		this.leftOutAnim = leftOutAnim;
		
		AnimationSet rightInAnim = new AnimationSet(true);
		rightInAnim.addAnimation(alphaIn);
		rightInAnim.addAnimation(rightInTrans);
		this.rightInAnim = rightInAnim;
		
		AnimationSet rightOutAnim = new AnimationSet(true);
		rightOutAnim.addAnimation(alphtOut);
		rightOutAnim.addAnimation(rightOutTrans);
		this.rightOutAnim = rightOutAnim;
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		if (!isClickable()) {
            setClickable(true);
        }
		this.listener = l;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		drawDoit(canvas);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	/**
	 * release image bitmap memory
	 */
	public void release() {
		if(images != null) {
			for(Bitmap bm : images) {
				if(bm != null && !bm.isRecycled()) {
					bm.recycle();
					bm = null;
				}
			}
		}
	}

	private void startAutoSwitch() {
		if(task == null) {
			if(timer == null) {
				timer = new Timer();
			}
			task = new TimerTask() {

				@Override
				public void run() {
					handler.obtainMessage().sendToTarget();
				}
			};
			timer.schedule(task, 0, AUTO_SWITCH_TIME);
		}
	}

	private void showNextImage() {
		if(images == null) {
			return ;
		}

		setInAnimation(leftInAnim);
		setOutAnimation(leftOutAnim);
		currentIndex = currentIndex + 1 >= images.length ? 0 : currentIndex + 1;
		setImageDrawable(new BitmapDrawable(getResources(), images[currentIndex]));
	}

	private void showPrevImage() {
		if(images == null) {
			return ;
		}

		setInAnimation(rightInAnim);
		setOutAnimation(rightOutAnim);
		currentIndex = currentIndex - 1 < 0 ? images.length - 1 : currentIndex - 1;
		setImageDrawable(new BitmapDrawable(getResources(), images[currentIndex]));
	}

	public void setImages(Bitmap[] images) {
		Bitmap[] prev = this.images;
		this.images = images;
		if(prev != null) {
			for(Bitmap b : prev) {
				b.recycle();
				b = null;
			}
		}
	}

	private void drawDoit(Canvas canvas) {
		if(images != null) {
			final int width = getMeasuredWidth();
			final int height = getMeasuredHeight();
			float cy = height - DOIT_Y_SPACE - DOIT_RADIUS;
			for(int i = 0; i < images.length; i ++) {
				if(i == currentIndex) {
					paint.setColor(Color.WHITE);
				} else {
					paint.setColor(Color.DKGRAY);
				}
				float cx = 0;
				// for align right
				cx = width - (images.length - i) * (2 * DOIT_RADIUS + DOIT_X_SPACE);
				// for center in horizonal
				//				if(images.length % 2 == 0) {
				//					if(i < images.length / 2) {
				//						cx = (float) (width / 2.0 - (images.length / 2 - i - 0.5) * (2 * DOIT_RADIUS + DOIT_X_SPACE));
				//					} else {
				//						cx = (float) (width / 2.0 + (i - images.length / 2 + 0.5) * (2 * DOIT_RADIUS + DOIT_X_SPACE));
				//					}
				//				} else {
				//					float temp = (images.length - 1) / 2;
				//					if(i < temp / 2) {
				//						cx = (float) (width / 2.0 - (temp - i) * 2 * DOIT_RADIUS - (temp - i) * DOIT_X_SPACE);
				//					} else if(i == temp) {
				//						cx = (float) (width / 2.0);
				//					} else {
				//						cx = (float) (width / 2.0 + (i - temp) * (2 * DOIT_RADIUS)  + (i - temp) * DOIT_X_SPACE);
				//					}
				//				}
				canvas.drawCircle(cx, cy, DOIT_RADIUS, paint);
			}
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gesturedetector.onTouchEvent(event);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		if(this.listener != null) {
			this.listener.onClick(this);
		}
		return true;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		float trans = e1.getX() - e2.getX();
		System.out.println("trans: " + trans);
		if (trans > 120 && !isSwitch) {
			showNextImage();
			return true;
		}
		else if (trans < -120 && isSwitch) {
			showPrevImage();
			return true;
		}
		return false;
	}

	@Override
	public void onAnimationStart(Animation animation) {
		isSwitch = true;
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		isSwitch = false;
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		isSwitch = true;
	}

}
