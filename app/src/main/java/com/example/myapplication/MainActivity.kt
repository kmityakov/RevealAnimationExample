package com.example.myapplication

import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toRectF
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_fragment_circular.setOnClickListener {
            showCircularFragment()
        }
        btn_fragment_elliptical.setOnClickListener {
            showEllipticalFragment()
        }
    }

    fun showCircularFragment() {
        val coords = IntArray(2)
        btn_fragment_circular.getLocationInWindow(coords)
        val x = coords[0] + btn_fragment_circular.width / 2
        val y = coords[1] + btn_fragment_circular.height / 2 - statusBarHeight()
        val fragment = CircularDialogFragment.newInstance(
            Point(x, y),
            Math.max(btn_fragment_circular.width / 2, btn_fragment_circular.height / 2).toFloat()
        )
        fragment.show(supportFragmentManager, "TAG")
    }

    fun showEllipticalFragment() {
        val rect = Rect()
        btn_fragment_elliptical.getGlobalVisibleRect(rect)

        val fragment = EllipticalDialogFragment.newInstance(rect.toRectF())
        fragment.show(supportFragmentManager, "TAG")
    }

    fun statusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId)
        } else {
            val rect = Rect()
            val window = window
            window.decorView.getWindowVisibleDisplayFrame(rect)
            return rect.top
        }
    }
}
