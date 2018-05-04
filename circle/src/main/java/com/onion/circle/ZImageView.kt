package com.onion.circle

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView




/**
 * Created by OnionMac on 2018/5/3.
 */
class ZImageView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : ImageView(context, attrs, defStyleAttr) {

    constructor(context: Context?,attrs: AttributeSet?): this(context,attrs,0)

    constructor(context: Context?): this(context,null)

    private val mPaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }

    private val mXfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

    private val TAG = "zhangqi"
    private var mWidth = 0f
    private var mHeight = 0f

    private lateinit var mBitmap: Bitmap
    private var mRadius: Int? = 0
    private var mStyle: String? = null
    private var mFitXy = false
    companion object {
        const val OVAL = "1"
        const val RECT = "2"
    }

    init {
        mPaint.isDither = true
        mPaint.color = Color.WHITE

        var a: TypedArray? = context?.obtainStyledAttributes(attrs,R.styleable.ZImageView)

        mRadius = a?.getInteger(R.styleable.ZImageView_z_radius,0)
        mStyle = a?.getString(R.styleable.ZImageView_z_style)
        mFitXy = a?.getBoolean(R.styleable.ZImageView_z_fitxy,false)!!

        a.recycle()
    }



    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)

        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        var width = 0
        var height = 0

        val drawable = drawable

        if (widthMode == MeasureSpec.EXACTLY){
            width = widthSize
        }else if(widthMode == MeasureSpec.AT_MOST){
           if(drawable is BitmapDrawable){
               width = drawable.intrinsicWidth
           }else if(drawable is ColorDrawable){
               width = widthSize
           }
        }

        if (heightMode == MeasureSpec.EXACTLY){
            height = heightSize
        }else if(heightMode == MeasureSpec.AT_MOST){
            if(drawable is BitmapDrawable){
                height = drawable.intrinsicHeight
            }else if(drawable is ColorDrawable){
                height = heightSize
            }
        }

        setMeasuredDimension(width,height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mWidth = w.toFloat()
        mHeight = h.toFloat()
    }


    override fun onDraw(canvas: Canvas?) {
        val sc = canvas?.saveLayer(0f, 0f, mWidth, mHeight, mPaint, Canvas.ALL_SAVE_FLAG)
        var drawable = drawable
        var radius = Math.min(mWidth,mHeight)
        if(OVAL.equals(mStyle)){
            canvas?.drawCircle(mWidth/2,mHeight/2,radius/2,mPaint)
            Log.d(TAG,"$drawable")
            mPaint.xfermode = mXfermode
            if(drawable is BitmapDrawable){
                mBitmap = drawable.bitmap
                var min = Math.min(mBitmap.width, mBitmap.height)
                mBitmap = scaleBitmap(mBitmap,radius/min)

                var l = 0f
                var t = 0f
                if(mBitmap.width > mWidth){
                    l = -(mBitmap.width - mWidth) / 2
                }

                if(mBitmap.height > mHeight){
                    t = -(mBitmap.height - mHeight) / 2
                }

                canvas?.drawBitmap(mBitmap,l,t,mPaint)

            }else if(drawable is ColorDrawable){
                var color:Int = drawable.color
                mPaint.color = color
                canvas?.drawCircle(mWidth/2,mHeight/2,radius/2,mPaint)
            }
        }else if(RECT.equals(mStyle)){
            /**
             * 如果宽高不一致 则取高
             */
            if(mWidth != mHeight){
                radius = Math.max(mWidth,mHeight)
            }
            val rect = RectF(0f,0f,mWidth,mHeight)
            mRadius?.let {
                canvas?.drawRoundRect(rect, it.toFloat(), it.toFloat(),mPaint)
            }
            mPaint.xfermode = mXfermode

            if(drawable is BitmapDrawable){
                mBitmap = drawable.bitmap

                var min = Math.min(mBitmap.width, mBitmap.height)
                mBitmap = if (mFitXy){
                    scaleBitmap(mBitmap,mWidth.toInt(),mHeight.toInt())
                }else {
                    scaleBitmap(mBitmap,radius/min)
                }

                canvas?.drawBitmap(mBitmap,0f,0f,mPaint)
            }else if(drawable is ColorDrawable){
                var color:Int = drawable.color
                mPaint.color = color
                mRadius?.let {
                    canvas?.drawRoundRect(rect, it.toFloat(), it.toFloat(),mPaint)
                }
            }
        }


        mPaint.xfermode = null
        /**
         * 还原画布，与canvas.saveLayer配套使用
         */
        canvas?.restoreToCount(sc!!)

        Log.d(TAG,"cancel")
    }

    private fun scaleBitmap(origin: Bitmap?, ratio: Float): Bitmap {

        val width = origin?.width
        val height = origin?.height
        val matrix = Matrix()
        matrix.preScale(ratio, ratio)
        val newBM = Bitmap.createBitmap(origin, 0, 0, width!!, height!!, matrix, false)
        if (newBM == origin) {
            return newBM
        }
//        origin.recycle()
        return newBM
    }

    private fun scaleBitmap(origin: Bitmap?, newWidth: Int, newHeight: Int): Bitmap {

        val height = origin?.height
        val width = origin?.width
        val scaleWidth = newWidth.toFloat() / width!!
        val scaleHeight = newHeight.toFloat() / height!!
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)// 使用后乘
        val newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false)
        if (!origin.isRecycled) {
            origin.recycle()
        }
        return newBM
    }


}