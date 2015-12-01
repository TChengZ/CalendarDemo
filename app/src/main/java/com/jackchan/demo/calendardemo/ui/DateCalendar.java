package com.jackchan.demo.calendardemo.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


import com.jackchan.demo.calendardemo.R;

import java.util.List;


public class DateCalendar extends FrameLayout implements CalendarCard.OnCellCallback{

    private Context mContext = null;
    private TextView mTvDate;
    private ViewPager mViewPager;
    private int mCurrentIndex = 499;
    private CalendarCard[] mShowViews;
    private CalendarViewAdapter<CalendarCard> adapter;
    private SildeDirection mDirection = SildeDirection.NO_SILDE;

    public interface onDateCalendarCallback{

        void clickDate(CustomDate date); // 回调点击的日期
    }

    private onDateCalendarCallback iDateCalendarCallback = null;

    @Override
    public void clickDate(CustomDate date) {
        if(null != iDateCalendarCallback){
            iDateCalendarCallback.clickDate(date);
        }
    }

    enum SildeDirection {
        RIGHT, LEFT, NO_SILDE
    }

    public DateCalendar(Context context) {
        super(context);
        mContext = context;
        initData();
    }

    public DateCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initData();
    }

    public DateCalendar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initData();
    }


    private void initData(){
        View view = LayoutInflater.from(mContext).inflate(R.layout.datecalendar, null);
        addView(view);
        mViewPager = (ViewPager) this.findViewById(R.id.vp_calendar);
        mTvDate = (TextView) this.findViewById(R.id.tvCurrentDate);
        mShowViews = new CalendarCard[3];
        for (int i = 0; i < 3; i++) {
            mShowViews[i] = new CalendarCard(mContext, this);
        }
        //设置当前数据在View[1],并更新前后一个月数据
        CustomDate date = mShowViews[1].getShowDate();
        mShowViews[2].setShowDate(date.year, date.month + 1, 1);
        mShowViews[0].setShowDate(date.year, date.month - 1, 1);
        mTvDate.setText(date.year + "年" + date.month + "月");
        adapter = new CalendarViewAdapter(mShowViews);
        setViewPager();
    }

    private void setViewPager() {
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(mCurrentIndex);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                Log.d("DEMO", "position:" + position);
                measureDirection(position);
                updateCalendarView(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
        notifyDataChange();
    }

    /**
     * 计算方向
     *
     * @param arg0
     */
    private void measureDirection(int arg0) {

        if (arg0 > mCurrentIndex) {
            mDirection = SildeDirection.RIGHT;

        } else if (arg0 < mCurrentIndex) {
            mDirection = SildeDirection.LEFT;
        }
        mCurrentIndex = arg0;
    }

    // 更新日历视图
    private void updateCalendarView(int arg0) {
        CustomDate date = mShowViews[arg0 % mShowViews.length].getShowDate();
        mTvDate.setText(date.year + "年" + date.month + "月");
        mShowViews[(arg0 + 1) % mShowViews.length].setShowDate(date.year, date.month + 1, 1);
        mShowViews[(arg0 - 1)% mShowViews.length].setShowDate(date.year, date.month - 1, 1);
        mDirection = SildeDirection.NO_SILDE;
        notifyDataChange();
    }

    /**
     * 获得当前正在展示的日期
     * @return
     */
    public CustomDate getCurrentShowDate(){
        return mShowViews[mCurrentIndex % mShowViews.length].getShowDate();
    }

    public void notifyDataChange(){
        CalendarCard[] views = adapter.getAllItems();
        for (int i = 0; i < views.length; i++) {
            views[i].update();
        }
    }
}

