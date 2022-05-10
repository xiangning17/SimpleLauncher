package com.xiangning.simplelauncher.ui

import android.os.Bundle
import android.view.View
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.CalendarView
import com.xiangning.simplelauncher.R
import com.xiangning.simplelauncher.calendar.LunarCalendar
import kotlinx.android.synthetic.main.activity_calendar.*

class CalendarActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        initView()
    }

    private fun initView() {
        calendar_view.setMonthView(CalendarMonthView::class.java)
        calendar_view.setOnCalendarSelectListener(
            object : CalendarView.OnCalendarSelectListener {
                override fun onCalendarOutOfRange(calendar: Calendar?) {

                }

                override fun onCalendarSelect(calendar: Calendar?, isClick: Boolean) {
                    if (calendar == null) {
                        return
                    }

                    updateDay(calendar)
                }

            }
        )

        title_view.text = "${calendar_view.curYear}年${calendar_view.curMonth}月"
        calendar_view.setOnMonthChangeListener { year, month ->
            title_view.text = "${year}年${month}月"
        }

        updateDay(calendar_view.selectedCalendar)

    }

    fun updateDay(calendar: Calendar) {
        val festival = calendar.gregorianFestival
        if (festival.isNullOrEmpty()) {
            tv_festival.setVisibility(View.GONE)
        } else {
            tv_festival.setVisibility(View.VISIBLE)
            tv_festival.text = festival
        }

        tv_lunar.text =
            LunarCalendar.getLunarString(calendar.year, calendar.month, calendar.day, true)
        tv_yiji.text = LunarCalendar.getyiji(calendar.year, calendar.month, calendar.day)
    }
}