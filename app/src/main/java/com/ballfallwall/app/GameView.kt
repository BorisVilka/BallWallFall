package com.ballfallwall.app

import android.content.Context
import android.graphics.*
import android.media.MediaPlayer
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.lang.Math.PI
import java.util.*


class GameView(val ctx: Context, val attributeSet: AttributeSet): SurfaceView(ctx,attributeSet) {

    var ball = BitmapFactory.decodeResource(ctx.resources,R.drawable.ball1)
    var ball1 = BitmapFactory.decodeResource(ctx.resources,R.drawable.ball1)
    var shadow = BitmapFactory.decodeResource(ctx.resources,R.drawable.shadow)
    var stars = BitmapFactory.decodeResource(ctx.resources,R.drawable.stars)
    var bg = BitmapFactory.decodeResource(ctx.resources,R.drawable.bg)
    var bg3 = BitmapFactory.decodeResource(ctx.resources,R.drawable.bg3)
    var bg4 = BitmapFactory.decodeResource(ctx.resources,R.drawable.bush)
    var bottom = BitmapFactory.decodeResource(ctx.resources,R.drawable.bottom)
    var arrow = BitmapFactory.decodeResource(ctx.resources,R.drawable.arrow)
    var right = BitmapFactory.decodeResource(ctx.resources,R.drawable.right)
    var stone = BitmapFactory.decodeResource(ctx.resources,R.drawable.stone)
    var bg5 = getBitmap(R.drawable.bg4)

    var paint = Paint().apply {
        shader = LinearGradient(0f,0f,0f,200f,ctx.getColor(R.color.yel),ctx.getColor(R.color.orange),Shader.TileMode.MIRROR)
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 40f
    }
    var paintT = Paint().apply {
        textSize = 60f
        color = Color.WHITE
    }

    var enemy = BitmapFactory.decodeResource(ctx.resources,R.drawable.br)
    var exc = BitmapFactory.decodeResource(ctx.resources,R.drawable.exc)
    var miss = BitmapFactory.decodeResource(ctx.resources,R.drawable.miss)

    var music = ctx.getSharedPreferences("prefs",Context.MODE_PRIVATE).getBoolean("music",false)
    var sounds = ctx.getSharedPreferences("prefs",Context.MODE_PRIVATE).getBoolean("sound",false)

    var player = MediaPlayer.create(ctx,R.raw.bg)
    var mis = MediaPlayer.create(ctx,R.raw.miss)
    var suc = MediaPlayer.create(ctx,R.raw.suc)
    var lose = MediaPlayer.create(ctx,R.raw.lose)

    var millis = 0
    var paused = false
    private var paintB: Paint = Paint(Paint.DITHER_FLAG)
    private var listener: EndListener? = null

    val updateThread = Thread {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                if (!paused) {
                    millis ++
                    update.run()
                }
            }
        }, 500, 16)
    }

    var bx = 0f
    var by = 0f

    var mx = 0f
    var my = 0f

    var sx = 0f
    var sy = 0f

    init {
        player.setOnCompletionListener {
            it.start()
        }
        if(music) player.start()
        stone = Bitmap.createScaledBitmap(stone,stone.width/4,stone.height/4,true)
        shadow = Bitmap.createScaledBitmap(shadow,shadow.width/4,shadow.height/4,true)
        stars = Bitmap.createScaledBitmap(stars,stars.width/4,stars.height/4,true)
        enemy = Bitmap.createScaledBitmap(enemy,enemy.width/4,enemy.height/4,true)

        exc = Bitmap.createScaledBitmap(exc,exc.width/4,exc.height/4,true)
        miss = Bitmap.createScaledBitmap(miss,miss.width/4,miss.height/4,true)

       // bg3 = Bitmap.createScaledBitmap(bg3,bg3.width/8*3,bg3.height/5*2,true)
       // bg4 = Bitmap.createScaledBitmap(bg4,bg4.width/8*3,bg4.height/6*2,true)
       // bottom = Bitmap.createScaledBitmap(bottom,bottom.width/4*3,bottom.height/5*2,true)

        ball1 = Bitmap.createScaledBitmap(ball1,bg5!!.height/2,bg5!!.height/2,true)
        holder.addCallback(object : SurfaceHolder.Callback{
            override fun surfaceCreated(holder: SurfaceHolder) {

            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                val canvas = holder.lockCanvas()
                if(canvas!=null) {

                    bg = Bitmap.createScaledBitmap(bg,canvas.width,canvas.height,true)
                    bg3 = Bitmap.createScaledBitmap(bg3,canvas.width,bg3.height/5*2,true)
                    bg4 = Bitmap.createScaledBitmap(bg4,canvas.width,bg4.height/6*2,true)
                    bottom = Bitmap.createScaledBitmap(bottom,canvas.width,bottom.height/5*2,true)

                    sx = 200f
                    sy = canvas.height-bottom.height.toFloat()/2f
                    bx = sx
                    by = sy
                  //  bg5 = Bitmap.createScaledBitmap(bg5,canvas.width/8,ball1.height*2,true)
                    right = Bitmap.createScaledBitmap(right,right.width/4,canvas.height,true)
                    ball = Bitmap.createScaledBitmap(ball,canvas.width/16,canvas.width/16,true)
                    mx = canvas.width-right.width.toFloat()/1.3f
                    my = canvas.height/2f
                    tx = canvas.width/40f
                    draw(canvas)
                    holder.unlockCanvasAndPost(canvas)
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                //  paused = true
                //  player.stop()
                updateThread.interrupt()
                 player.stop()
                player.release()
                lose.release()
                mis.release()
                suc.release()
            }

        })

        updateThread.start()
    }

    private fun getBitmap(drawableRes: Int): Bitmap? {
        val drawable = ctx.resources.getDrawable(drawableRes)
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }

    var dx = -1f
    var dy = -1f
    var path = Path()
    var ty = 0f
    var tx = 5f
    var start = false
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when(event!!.action) {
            MotionEvent.ACTION_DOWN -> {
               if(bx==sx && by==sy && !start && !drawM && !drawS) {
                   dx = event.x
                   dy = event.y
               }
            }
            MotionEvent.ACTION_UP -> {
                if(bx==sx && by==sy && !start && !drawM && !drawS && dx!=-1f && dy!=-1f) {
                    ty = 55f*((dy-by)/(dx-bx))
                    start = true
                    dx = -1f
                    dy = -1f
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if(bx==sx && by==sy && !start && !drawM && !drawS) {
                    dx = event.x
                    dy = event.y
                }
            }
        }
        return true
    }


    var k = -1
    var score = 0
    var health = 4
    var drawS = false
    var drawM = false

    val update = Runnable{
        var isEnd = false
        try {
            val canvas = holder.lockCanvas()
            val st1 = score>=10
            if(my<=enemy.height) k = 1
            else if(my>=canvas.height-enemy.height) k = -1
            my += k*2
            if(start) {
                bx += tx
                by += ty
                ty += 2f
                if(bx>=mx || by>=canvas.height-bottom.height.toFloat()/2f) {
                    start = false
                    millis = 0
                    if(!(((by-my<=ball.height && by-my>0) || (my-by>0 && my-by<=enemy.height)) &&  kotlin.math.abs(bx - mx) <=ball.width)) {
                        drawM = true
                        health--
                        if(sounds) {
                            mis.seekTo(0)
                            mis.start()
                        }
                    } else {
                        score++
                        listener?.score(score)
                        drawS = true
                        if(sounds) {
                            suc.seekTo(0)
                            suc.start()
                        }
                    }
                }
                if(st1) {
                    if(bx>=canvas.width/2f && bx<=canvas.width/2f+stone.width && by>=sy-stone.height/2f) {
                        start = false
                        millis = 0
                        drawM = true
                        health--
                        if(sounds) {
                            mis.seekTo(0)
                            mis.start()
                        }
                    }
                }
            }
            canvas.drawBitmap(bg,0f,0f,paintB)
            canvas.drawBitmap(bottom,0f,canvas.height-bottom.height.toFloat(),paintB)
            canvas.drawBitmap(bg3,0f,canvas.height-bottom.height.toFloat()-bg3.height+80,paintB)
            canvas.drawBitmap(bg4,0f,canvas.height-bottom.height.toFloat()-bg4.height/2,paintB)
            canvas.drawBitmap(shadow,bx-shadow.width/2f,canvas.height-bottom.height.toFloat()/3f,paintB)
            canvas.drawBitmap(right,canvas.width-right.width.toFloat(),0f,paintB)
            if(st1) canvas.drawBitmap(stone,canvas.width/2f,sy-stone.height/2f,paintB)
            if(!start && dx!=-1f && dy!=-1f) drawArrow(paint,canvas,bx,by,dx,dy)
            if(drawS) {
                if(millis>=100) {
                    start = false
                    drawS = false
                    bx = sx
                    by = sy
                } else {
                    canvas.drawBitmap(stars,mx-stars.width/2f,my,paintB)
                    canvas.drawBitmap(exc,canvas.width/2f-exc.width/2f,canvas.height/2f-exc.height/2f,paintB)
                }
            }
            if(drawM) {
                if(millis>=100) {
                    start = false
                    drawM = false
                    bx = sx
                    by = sy
                } else {
                    canvas.drawBitmap(miss,canvas.width/2f-miss.width/2f,canvas.height/2f-miss.height/2f,paintB)
                }
            }
            canvas.drawBitmap(ball,bx-ball.width/2f,by-ball.height/2f,paintB)
            canvas.drawBitmap(enemy,mx,my,paintB)
            canvas.drawBitmap(bg5!!,50f,40f,paintB)
            canvas.drawBitmap(bg5!!,canvas.width/2f-bg5!!.width/2f,40f,paintB)
            var st = ball1.width*0.5f+50
            for(i in 0 until health) {
                canvas.drawBitmap(ball1,st,40f+bg5!!.height/2f-ball1.height/2f,paintB)
                st += bg5!!.height*0.7f
            }
            canvas.drawText("$score",canvas.width/2f-30f,60+bg5!!.height/2f,paintT)
            holder.unlockCanvasAndPost(canvas)
            if(health==0) isEnd = true
            if(isEnd) {
                if(sounds) {
                    lose.seekTo(0)
                    lose.start()
                }
                Log.d("TAG","END")
                if(listener!=null) listener!!.end()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun drawArrow(
        paint: Paint,
        canvas: Canvas,
        from_x: Float,
        from_y: Float,
        to_x: Float,
        to_y: Float
    ) {
        val angle: Float
        val anglerad: Float
        val radius: Float
        val lineangle: Float

        //values to change for other appearance *CHANGE THESE FOR OTHER SIZE ARROWHEADS*
        radius = 40f
        angle = 45f

        //some angle calculations
        anglerad = ((PI * angle / 180f).toFloat())
        canvas.drawCircle(from_x,from_y,ball.height/2f+20f,paint)
        lineangle = kotlin.math.atan2(to_y - from_y, to_x - from_x) as Float

        //tha line
        canvas.drawLine(from_x, from_y, to_x, to_y, paint)

        //tha triangle
        val path = Path()
        path.fillType = Path.FillType.EVEN_ODD
        path.moveTo(to_x, to_y)
        path.lineTo(
            (to_x - radius * kotlin.math.cos(lineangle - anglerad / 2.0)).toFloat(),
            (to_y - radius * kotlin.math.sin(lineangle - anglerad / 2.0)).toFloat()
        )
        path.lineTo(
            (to_x - radius * kotlin.math.cos(lineangle + anglerad / 2.0)).toFloat(),
            (to_y - radius * kotlin.math.sin(lineangle + anglerad / 2.0)).toFloat()
        )
        path.close()
        canvas.drawPath(path, paint)
    }
    fun setEndListener(list: EndListener) {
        this.listener = list
    }
    fun togglePause() {
        paused = !paused
        if(music) {
           try {
               if(paused) player.pause()
               else player.start()
           } catch (e: Exception) {
               e.printStackTrace()
           }
        }
    }
    fun rotateBitmap(source: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.preRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
    companion object {
        interface EndListener {
            fun end();
            fun score(score: Int);
        }

    }
}