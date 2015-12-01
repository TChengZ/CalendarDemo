package com.jackchan.demo.calendardemo.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;


import com.jackchan.demo.calendardemo.R;

import java.util.List;

/**
 * 自定义日历卡
 * 
 * @author chenzj
 * 
 */
public class CalendarCard extends View {

	private static final int TOTAL_COL = 7; // 7列
	private static final int TOTAL_ROW = 6; // 6行

	private Paint mTextPaint; // 绘制文本的画笔
	private int mViewWidth; // 视图的宽度
	private int mViewHeight; // 视图的高度
	private int mCellSpaceWidth; // 单元格宽度
	private int mCellSpaceHeight;// 单元格高度
	private Row rows[] = new Row[TOTAL_ROW]; // 行数组，每个元素代表一行
	private CustomDate mShowDate; // 自定义的日期，包括year,month,day
	private OnCellCallback mCellClickListener; // 单元格点击回调事件
	private int touchSlop; //
	private boolean callBackCellSpace;

	private Cell mClickCell;
	private float mDownX;
	private float mDownY;
	/**
	 * 今日图标
	 */
	private BitmapDrawable mTodayBitmap = null;

	/**
	 *
	 * 单元格的状态
	 */
	public final static int TODAY = 1;
	public final static int NO_THIS_MONTH_WEEK_DAY = 2;
	public final static int NO_THIS_MONTH_WEEKEND = 3;
	public final static int THIS_MONTH_WEEK_DAY = 4;
	public final static int THIS_MONTH_WEEKEND = 5;


	/**
	 *
	 * @author chenzj
	 * 
	 */
	public interface OnCellCallback {
		void clickDate(CustomDate date); // 回调点击的日期
	}

	public CalendarCard(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public CalendarCard(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CalendarCard(Context context) {
		super(context);
		init(context);
	}

	public CalendarCard(Context context, OnCellCallback listener) {
		super(context);
		this.mCellClickListener = listener;
		init(context);
	}

	private void init(Context context) {
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		Drawable drawableToday = getContext().getResources().getDrawable(R.drawable.bg_today);
		mTodayBitmap = (BitmapDrawable) drawableToday;
		initDate();
	}

	private void initDate() {
		mShowDate = new CustomDate();
		fillDate();//
	}


	private void fillDate() {
		int monthDay = DateUtil.getCurrentMonthDay(); // 今天
		int lastMonthDays = DateUtil.getMonthDays(mShowDate.year,
				mShowDate.month - 1); // 上个月的天数
		int currentMonthDays = DateUtil.getMonthDays(mShowDate.year,
				mShowDate.month); // 当前月的天数
		int firstDayWeek = DateUtil.getWeekDayFromDate(mShowDate.year,
				mShowDate.month);
//		//由于我们把星期一放在第一列，因此需要重新计算一个月第一天是哪一天
//		if(firstDayWeek == 0){
//			//第一天为周日放在第七列
//			firstDayWeek = 7;
//		}
		boolean isCurrentMonth = false;
		Log.d("DEMO", "month:" + mShowDate.month);
		if (DateUtil.isCurrentMonth(mShowDate)) {
			isCurrentMonth = true;
		}
		int day = 0;
		for (int j = 0; j < TOTAL_ROW; j++) {
			rows[j] = new Row(j);
			for (int i = 0; i < TOTAL_COL; i++) {
				int position = i + j * TOTAL_COL; // 单元格位置
				// 这个月的
				if (position >= firstDayWeek
						&& position < firstDayWeek + currentMonthDays) {
					day++;
					int dayOfWeek = DateUtil.day_of_week(mShowDate.year, mShowDate.month, day);
					rows[j].cells[i] = new Cell(CustomDate.modifiDayForObject(
							mShowDate, day),
							(dayOfWeek > 1 && dayOfWeek < 7)?THIS_MONTH_WEEK_DAY:THIS_MONTH_WEEKEND,//计算是周末还是工作日
							i, j);
					// 今天
					if (isCurrentMonth && day == monthDay ) {
						CustomDate date = CustomDate.modifiDayForObject(mShowDate, day);
						rows[j].cells[i] = new Cell(date, TODAY, i, j);
					}
					// 过去一个月
				} else if (position < firstDayWeek) {
					CustomDate customDate = new CustomDate(mShowDate.year,
							mShowDate.month - 1, lastMonthDays
							- (firstDayWeek - position - 1));
					int dayOfWeek = DateUtil.day_of_week(customDate.year, customDate.month, customDate.day);
					rows[j].cells[i] = new Cell(customDate,
							(dayOfWeek > 1 && dayOfWeek < 7)?NO_THIS_MONTH_WEEK_DAY:NO_THIS_MONTH_WEEKEND,//计算是周末还是工作日
							i, j);
					// 下个月
				} else if (position >= firstDayWeek + currentMonthDays) {
					CustomDate customDate = new CustomDate(mShowDate.year,
							mShowDate.month + 1, position - firstDayWeek
							- currentMonthDays + 1);
					int dayOfWeek = DateUtil.day_of_week(customDate.year, customDate.month, customDate.day);
					rows[j].cells[i] = new Cell(customDate,
							(dayOfWeek > 1 && dayOfWeek < 7)?NO_THIS_MONTH_WEEK_DAY:NO_THIS_MONTH_WEEKEND,//计算是周末还是工作日
							i, j);
				}
			}
		}
	}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for (int i = 0; i < TOTAL_ROW; i++) {
			if (rows[i] != null) {
				rows[i].drawCells(canvas);
			}
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mViewWidth = w;
		mViewHeight = h;
		mCellSpaceHeight = mViewHeight / TOTAL_ROW;
		mCellSpaceWidth = mViewWidth / TOTAL_COL;
		if (!callBackCellSpace) {
			callBackCellSpace = true;
		}
		mTextPaint.setTextSize(mCellSpaceWidth / 3);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDownX = event.getX();
			mDownY = event.getY();
			break;
		case MotionEvent.ACTION_UP:
			float disX = event.getX() - mDownX;
			float disY = event.getY() - mDownY;
			if (Math.abs(disX) < touchSlop && Math.abs(disY) < touchSlop) {
				int col = (int) (mDownX / mCellSpaceWidth);
				int row = (int) (mDownY / mCellSpaceHeight);
				measureClickCell(col, row);
			}
			break;
		default:
			break;
		}

		return true;
	}

	/**
	 * 计算点击的单元格
	 * @param col
	 * @param row
	 */
	private void measureClickCell(int col, int row) {
		if (col >= TOTAL_COL || row >= TOTAL_ROW)
			return;
		if (mClickCell != null) {
			rows[mClickCell.j].cells[mClickCell.i] = mClickCell;
		}
		if (rows[row] != null) {
			mClickCell = new Cell(rows[row].cells[col].date,
					rows[row].cells[col].state, rows[row].cells[col].i,
					rows[row].cells[col].j);

			CustomDate date = rows[row].cells[col].date;
			date.week = col;
			mCellClickListener.clickDate(date);
		}
	}

	/**
	 * 组元素
	 * 
	 * @author chenzj
	 * 
	 */
	class Row {
		public int j;

		Row(int j) {
			this.j = j;
		}

		public Cell[] cells = new Cell[TOTAL_COL];

		// 绘制单元格
		public void drawCells(Canvas canvas) {
			for (int i = 0; i < cells.length; i++) {
				if (cells[i] != null) {
					cells[i].drawSelf(canvas);
				}
			}
		}

	}

	/**
	 * 单元格元素
	 * 
	 * @author chenzj
	 * 
	 */
	class Cell {
		public CustomDate date;
		public int state;
		public int i;
		public int j;

		public Cell(CustomDate date, int state, int i, int j) {
			super();
			this.date = date;
			this.state = state;
			this.i = i;
			this.j = j;
		}

		public void drawSelf(Canvas canvas) {
			switch (state) {
			case TODAY: // 今天
				mTextPaint.setColor(Color.WHITE);
				canvas.drawBitmap(mTodayBitmap.getBitmap(), (float)((mCellSpaceWidth * (i + 0.5)) - mTodayBitmap.getIntrinsicWidth()/2),
						(float) (((j + 0.5) * mCellSpaceHeight) - mTodayBitmap.getIntrinsicHeight()/2),
						null);
				break;
			case THIS_MONTH_WEEK_DAY: // 当前月日期
				mTextPaint.setColor(getResources().getColor(R.color.this_month_weekday));
				break;
			case THIS_MONTH_WEEKEND:
				mTextPaint.setColor(getResources().getColor(R.color.this_month_weekend));
				break;
			case NO_THIS_MONTH_WEEK_DAY:
				mTextPaint.setColor(getResources().getColor(R.color.no_this_month_weekday));
				break;
			case NO_THIS_MONTH_WEEKEND:
				mTextPaint.setColor(getResources().getColor(R.color.no_this_month_weekend));
				break;
			default:
				break;
			}
			// 绘制文字
			String content = date.day + "";
			canvas.drawText(content,
					(float) ((i + 0.5) * mCellSpaceWidth - mTextPaint
							.measureText(content) / 2), (float) ((j + 0.7)
							* mCellSpaceHeight - mTextPaint
							.measureText(content, 0, 1) / 2), mTextPaint);
		}
	}


	public void update() {
		fillDate();
		invalidate();
	}

	public CustomDate getShowDate() {
		return mShowDate;
	}

	public void setShowDate(int year, int month, int day){
		if(month > 12){
			month = 1;
			year++;
		}else if(month <1){
			month = 12;
			year--;
		}
		mShowDate.setYear(year);
		mShowDate.setMonth(month);
		mShowDate.setDay(day);
	}
}
