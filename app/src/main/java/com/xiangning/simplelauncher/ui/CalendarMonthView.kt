package com.xiangning.simplelauncher.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.MonthView

/**
 * Created by xiangning on 2022/5/10.
 */
class CalendarMonthView(context: Context) : MonthView(context) {
    private val mTextPaint = Paint()
    private val mSchemeBasicPaint = Paint()
    private val mRadio: Float
    private val mPadding: Int
    private val mSchemeBaseLine: Float
    private val mLunarMargin: Int

    private val mCurDayPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = Color.parseColor("#A2A5A7")
    }

    /**
     * @param canvas    canvas
     * @param calendar  日历日历calendar
     * @param x         日历Card x起点坐标
     * @param y         日历Card y起点坐标
     * @param hasScheme hasScheme 非标记的日期
     * @return true 则绘制onDrawScheme，因为这里背景色不是是互斥的
     */
    override fun onDrawSelected(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        hasScheme: Boolean
    ): Boolean {
        return false
    }

    override fun onDrawScheme(canvas: Canvas, calendar: Calendar, x: Int, y: Int) {
        mSchemeBasicPaint.color = calendar.schemeColor
        canvas.drawCircle(
            x + mItemWidth - mPadding - mRadio / 2,
            y + mPadding + mRadio,
            mRadio,
            mSchemeBasicPaint
        )
        canvas.drawText(
            calendar.scheme,
            x + mItemWidth - mPadding - mRadio / 2 - getTextWidth(calendar.scheme) / 2,
            y + mPadding + mSchemeBaseLine, mTextPaint
        )
    }

    /**
     * 获取字体的宽
     * @param text text
     * @return return
     */
    private fun getTextWidth(text: String): Float {
        return mTextPaint.measureText(text)
    }

    override fun onDrawText(
        canvas: Canvas,
        calendar: Calendar,
        x: Int,
        y: Int,
        hasScheme: Boolean,
        isSelected: Boolean
    ) {
        val cx = x + mItemWidth / 2
        val top = y - mItemHeight / 6
        val lunarWidth = mSelectedLunarTextPaint.measureText(calendar.lunar)
        val widthInset = (mItemWidth - lunarWidth).coerceAtMost(0f) / 2f

        if (isSelected) {
            // 绘制背景
            mSelectedPaint.style = Paint.Style.FILL
            canvas.drawRect(
                (x + widthInset),
                (y).toFloat(),
                (x + mItemWidth - widthInset),
                (y + mItemHeight).toFloat(),
                mSelectedPaint
            )

            canvas.drawText(
                calendar.day.toString(), cx.toFloat(), mTextBaseLine + top,
                mSelectTextPaint
            )
            canvas.drawText(
                calendar.lunar,
                cx.toFloat(),
                mTextBaseLine + y + mItemHeight / 10 + mLunarMargin,
                mSelectedLunarTextPaint
            )
        } else if (hasScheme) {
            canvas.drawText(
                calendar.day.toString(), cx.toFloat(), mTextBaseLine + top,
                if (calendar.isCurrentDay) mCurDayTextPaint else if (calendar.isCurrentMonth) mSchemeTextPaint else mOtherMonthTextPaint
            )
            canvas.drawText(
                calendar.lunar, cx.toFloat(), mTextBaseLine + y + mItemHeight / 10,
                if (calendar.isCurrentDay) mCurDayLunarTextPaint else mSchemeLunarTextPaint
            )
        } else {
            if (calendar.isCurrentDay) {
                // 绘制今天背景
                canvas.drawRect(
                    (x + widthInset),
                    (y).toFloat(),
                    (x + mItemWidth - widthInset),
                    (y + mItemHeight).toFloat(),
                    mCurDayPaint
                )
            }

            canvas.drawText(
                calendar.day.toString(), cx.toFloat(), mTextBaseLine + top,
                if (calendar.isCurrentDay) mCurDayTextPaint else if (calendar.isCurrentMonth) mCurMonthTextPaint else mOtherMonthTextPaint
            )
            canvas.drawText(
                calendar.lunar, cx.toFloat(), mTextBaseLine + y + mItemHeight / 10 + mLunarMargin,
                if (calendar.isCurrentDay) mCurDayLunarTextPaint else if (calendar.isCurrentMonth) mCurMonthLunarTextPaint else mOtherMonthLunarTextPaint
            )
        }
    }

    init {
        mTextPaint.textSize = dipToPx(context, 8f).toFloat()
        mTextPaint.color = -0x1
        mTextPaint.isAntiAlias = true
        mTextPaint.isFakeBoldText = true
        mSchemeBasicPaint.isAntiAlias = true
        mSchemeBasicPaint.style = Paint.Style.FILL
        mSchemeBasicPaint.textAlign = Paint.Align.CENTER
        mSchemeBasicPaint.color = -0x12acad
        mSchemeBasicPaint.isFakeBoldText = true
        mRadio = dipToPx(getContext(), 7f).toFloat()
        mPadding = 0
        mLunarMargin = dipToPx(context, 3f)
        val metrics = mSchemeBasicPaint.fontMetrics
        mSchemeBaseLine =
            mRadio - metrics.descent + (metrics.bottom - metrics.top) / 2 + dipToPx(
                getContext(),
                1f
            )
    }

    companion object {
        fun dipToPx(context: Context, dpValue: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }
    }
}