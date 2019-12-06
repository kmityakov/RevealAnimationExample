package com.example.myapplication

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_circular.*


class CircularDialogFragment : AppCompatDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            DialogFragment.STYLE_NO_TITLE,
            R.style.Dialog_NoTitle
        );
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = object : AppCompatDialog(context, theme) {
            override fun onBackPressed() {
                dismissWithAnimation()
            }
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                it.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                it.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            }
            it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_circular, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_close.setOnClickListener {
            dismissWithAnimation()
        }

        val point = arguments!!.getParcelable<Point>(ARG_ANIMATION_START_POINT)!!
        startEnterAnimation(point.x, point.y)

    }

    fun dismissWithAnimation() {
        val point = arguments!!.getParcelable<Point>(ARG_ANIMATION_START_POINT)!!
        startExitAnimation(point.x, point.y) {
            dismiss()
        }
    }

    fun startEnterAnimation(centerX: Int, centerY: Int) {
        view?.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                v.removeOnLayoutChangeListener(this)

                val startRadius = arguments?.getFloat(ARG_INITIAL_RADIUS) ?: 0.0f
                val endRadius = Math.hypot(right.toDouble(), bottom.toDouble()) / 1.5f

                val circularAnimation = ViewAnimationUtils.createCircularReveal(
                    v,
                    centerX,
                    centerY,
                    startRadius,
                    endRadius.toFloat()
                ).apply {
                    duration =
                        CIRCULAR_ANIMATION_DURATION
                }
                val textAnimation =
                    ObjectAnimator.ofFloat(text_fragment, "alpha", 0.0f, 1.0f).apply {
                        duration =
                            TEXT_ANIMATION_DURATION
                        startDelay = CIRCULAR_ANIMATION_DURATION - TEXT_ANIMATION_DURATION
                    }
                AnimatorSet().apply {
                    interpolator = DecelerateInterpolator()
                    playTogether(circularAnimation, textAnimation)
                    start()
                }
            }
        })
    }

    fun startExitAnimation(centerX: Int, centerY: Int, onComplete: () -> Unit) {
        view?.let {
            val startRadius = Math.hypot(it.width.toDouble(), it.height.toDouble()) / 1.5f
            val endRadius = arguments?.getFloat(ARG_INITIAL_RADIUS) ?: 0.0f
            val circularAnimation = ViewAnimationUtils.createCircularReveal(
                it,
                centerX,
                centerY,
                startRadius.toFloat(),
                endRadius
            ).apply {
                duration =
                    CIRCULAR_ANIMATION_DURATION
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
                interpolator = AccelerateInterpolator()
                playTogether(circularAnimation, textAnimation)
                start()
            }
        } ?: run {
            onComplete()
        }

    }

    companion object {
        const val ARG_ANIMATION_START_POINT = "ARG_ANIMATION_START_POINT"
        const val ARG_INITIAL_RADIUS = "ARG_INITIAL_RADIUS"
        const val CIRCULAR_ANIMATION_DURATION = 600L
        const val TEXT_ANIMATION_DURATION = 300L

        fun newInstance(point: Point, initialRadius: Float): CircularDialogFragment {
            return CircularDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ANIMATION_START_POINT, point)
                    putFloat(ARG_INITIAL_RADIUS, initialRadius)
                }
            }
        }
    }
}