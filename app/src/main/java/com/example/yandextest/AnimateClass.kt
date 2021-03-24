package com.example.yandextest

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.widget.TextView

class AnimateClass {

    public fun colorAnimateText(textView: TextView, colorStart : Int, colorEnd : Int){
        val animateColor = ValueAnimator.ofObject(ArgbEvaluator(), colorStart, colorEnd)
        animateColor.duration = 250
        animateColor.addUpdateListener {
            val step = it.animatedValue as Int
            textView.setTextColor(step)
        }
        animateColor.start()
    }
    public fun colorAnimateBackground(textView: TextView, colorStart : Int, colorEnd : Int){
        val animateColor = ValueAnimator.ofObject(ArgbEvaluator(), colorStart, colorEnd)
        animateColor.duration = 250
        animateColor.addUpdateListener {
            val step = it.animatedValue as Int
            textView.setBackgroundColor(step)
        }
        animateColor.start()
    }



    public fun animateSizeZoom(textView: TextView, startSizeTextView : Float, accentSizeTextView : Float){
        val animate = ValueAnimator.ofFloat(startSizeTextView, accentSizeTextView)
        animate.duration = 250
        animate.addUpdateListener {
            val size = it.animatedValue as Float
            textView.textSize = size

        }
        animate.start()
    }


}