package com.example.myapplication

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.RectF
import android.os.Bundle
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import kotlinx.android.synthetic.main.activity_eliptical.*

class EllipticalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eliptical)

        btn_close.setOnClickListener {
            intent.getParcelableExtra<RectF>(ARG_INITIAL_RECT)?.let {
                startExitAnimation(it) {
                    finish()
                }
            } ?: run {
                finish()
            }
        }

        if (root_container.viewTreeObserver.isAlive) {
            root_container.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    root_container.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val startRectF = intent.getParcelableExtra<RectF>(ARG_INITIAL_RECT)
                    startEnterAnimation(startRectF)
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0);
    }

    fun startEnterAnimation(startRectF: RectF) {
        val rectangleBoundsAnim = ObjectAnimator.ofObject(
            root_container,
            "bounds",
            RectangleEvaluator(),
            startRectF,
            RectF(
                0.0f,
                0.0f,
                root_container.width.toFloat(),
                root_container.height.toFloat() + root_container.cornerRadius
            )
        )
        rectangleBoundsAnim.duration = RECT_ANIMATION_DURATION
        rectangleBoundsAnim.addUpdateListener {
            root_container.invalidate()
        }
        val textAnimation =
            ObjectAnimator.ofFloat(text_fragment, "alpha", 0.0f, 1.0f).apply {
                duration =
                    TEXT_ANIMATION_DURATION
                startDelay =
                    RECT_ANIMATION_DURATION - TEXT_ANIMATION_DURATION
            }
        AnimatorSet().apply {
            interpolator = DecelerateInterpolator()
            playTogether(rectangleBoundsAnim, textAnimation)
            doOnEnd {
                rectangleBoundsAnim.removeAllListeners()
            }
            start()
        }
    }

    override fun onBackPressed() {
        intent.getParcelableExtra<RectF>(ARG_INITIAL_RECT)?.let {
            startExitAnimation(it) {
                super.onBackPressed()
            }
        } ?: run {
            super.onBackPressed()
        }
    }

    fun startExitAnimation(endRectF: RectF, onComplete: () -> Unit) {
        val rectangleBoundsAnim = ObjectAnimator.ofObject(
            root_container,
            "bounds",
            RectangleEvaluator(),
            RectF(
                0.0f,
                0.0f,
                root_container.width.toFloat(),
                root_container.height.toFloat() + root_container.cornerRadius
            ),
            endRectF
        )
        rectangleBoundsAnim.duration = RECT_ANIMATION_DURATION
        rectangleBoundsAnim.addUpdateListener {
            root_container.invalidate()
        }
        val textAnimation =
            ObjectAnimator.ofFloat(text_fragment, "alpha", 1.0f, 0.0f).apply {
                duration =
                    TEXT_ANIMATION_DURATION
            }
        AnimatorSet().apply {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    onComplete()
                    super.onAnimationEnd(animation)
                }
            })
            doOnEnd {
                rectangleBoundsAnim.removeAllListeners()
            }
            interpolator = AccelerateInterpolator()
            playTogether(rectangleBoundsAnim, textAnimation)
            start()
        }
    }

    companion object {
        const val ARG_INITIAL_RECT = "ARG_INITIAL_RECT"
        const val RECT_ANIMATION_DURATION = 500L
        const val TEXT_ANIMATION_DURATION = 300L
    }
}