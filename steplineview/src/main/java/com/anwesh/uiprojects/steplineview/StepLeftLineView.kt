package com.anwesh.uiprojects.steplineview

/**
 * Created by anweshmishra on 19/06/19.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.app.Activity
import android.content.Context

val nodes : Int = 5
val lines : Int = 5
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#0D47A1")
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.mirrorValue(a : Int, b : Int) : Float {
    val k : Float = scaleFactor()
    return (1 - k) * a.inverse() + k * b.inverse()
}
fun Float.updateValue(dir : Float, a : Int, b : Int) : Float = dir * mirrorValue(a, b) * scGap

fun Canvas.drawStepLine(i : Int, scale : Float, size : Float, w : Float, paint : Paint) {
    val sci : Float = scale.divideScale(i, lines)
    val gap = size / (lines)
    val currSize : Float = gap * (lines - i)
    val xGap : Float = (2 * size) / lines
    val currGap : Float = xGap * (5 - lines)
    save()
    translate(-w / 2 + paint.strokeWidth / 2 + (w/ 2 + currGap) * sci,  0f)
    drawLine(0f, 0f, 0f, -currGap, paint)
    restore()
}

fun Canvas.drawStepLines(scale : Float, size : Float, w : Float, paint : Paint) {
    for (j in 0..(lines - 1)) {
        drawStepLine(j, scale, size, w, paint)
    }
}

fun Canvas.drawSLLNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    paint.color = foreColor
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    save()
    translate(w / 2, gap * (i + 1))
    rotate(90f * sc2)
    drawStepLines(sc1, size, w, paint)
    restore()
}

class StepLeftLineView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateValue(dir, lines, 1)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (animated) {
                animated = false
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class SLLNode(var i : Int, val state : State = State()) {

        private var next : SLLNode? = null
        private var prev : SLLNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = SLLNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawSLLNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : SLLNode {
            var curr : SLLNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class StepLeftLine(var i : Int) {

        private val root : SLLNode = SLLNode(0)
        private var curr : SLLNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }
}