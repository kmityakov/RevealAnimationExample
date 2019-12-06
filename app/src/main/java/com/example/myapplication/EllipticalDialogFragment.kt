package com.example.myapplication

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.graphics.Color
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.animation.doOnEnd
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_circular.btn_close
import kotlinx.android.synthetic.main.fragment_circular.text_fragment
import kotlinx.android.synthetic.main.fragment_eliptical.*


class EllipticalDialogFragment : AppCompatDialogFragment() {

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
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_eliptical, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_close.setOnClickListener {
            dismissWithAnimation()
        }

        val startRect = arguments!!.getParcelable<RectF>(ARG_INITIAL_RECT)!!
        startEnterAnimation(startRect)

    }

    fun dismissWithAnimation() {
        val endRect = arguments!!.getParcelable<RectF>(ARG_INITIAL_RECT)!!
        startExitAnimation(endRect) {
            dismiss()
        }
    }

    fun startEnterAnimation(startRectF: RectF) {
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


                val rectangleBoundsAnim = ObjectAnimator.ofObject(
                    root_container,
                    "bounds",
                    RectangleEvaluator(),
                    startRectF,
                    RectF(
                        0.0f,
                        0.0f,
                        view!!.width.toFloat(),
                        view!!.height.toFloat() + root_container.cornerRadius
                    )
                )
                rectangleBoundsAnim.addUpdateListener {
                    root_container.invalidate()
                }
                val textAnimation =
                    ObjectAnimator.ofFloat(text_fragment, "alpha", 0.0f, 1.0f).apply {
                        duration =
                            TEXT_ANIMATION_DURATION
                        startDelay = RECT_ANIMATION_DURATION - TEXT_ANIMATION_DURATION
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
        })
    }

    fun startExitAnimation(endRectF: RectF, onComplete: () -> Unit) {
        view?.let {
            val rectangleBoundsAnim = ObjectAnimator.ofObject(
                root_container,
                "bounds",
                RectangleEvaluator(),
                RectF(
                    0.0f,
                    0.0f,
                    view!!.width.toFloat(),
                    view!!.height.toFloat() + root_container.cornerRadius
                ),
                endRectF
            )
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
        } ?: run {
            onComplete()
        }

    }

    companion object {
        const val ARG_INITIAL_RECT = "ARG_INITIAL_RECT"
        const val RECT_ANIMATION_DURATION = 600L
        const val TEXT_ANIMATION_DURATION = 300L

        fun newInstance(rect: RectF): EllipticalDialogFragment {
            return EllipticalDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_INITIAL_RECT, rect)
                }
            }
        }
    }
}