package com.svi.hiit.util;

import android.os.Handler;
import android.util.Log;

/**
 * 
 * @author user868020
 * This method was derived from the following URL:
 * 	http://stackoverflow.com/questions/1877417/how-to-set-a-timer-in-android
 *
 */
public class IntervalTimer
{
    private Handler handler;
    private Runnable runMethod;
    private int intervalMs;
    private boolean enabled = false;
    private boolean oneTime = false;

    public IntervalTimer(Handler handler, Runnable runMethod, int intervalMs)
    {
        this.handler = handler;
        this.runMethod = runMethod;
        this.intervalMs = intervalMs;
    }

    public IntervalTimer(Handler handler, Runnable runMethod, int intervalMs, boolean oneTime)
    {
        this(handler, runMethod, intervalMs);
        this.oneTime = oneTime;
    }

    public void start()
    {
        if (enabled)
            return;

        if (intervalMs < 1)
        {
            Log.e("timer start", "Invalid interval:" + intervalMs);
            return;
        }

        enabled = true;
        handler.postDelayed(timer_tick, intervalMs);        
    }

    public void stop()
    {
        if (!enabled)
            return;

        enabled = false;
        handler.removeCallbacks(runMethod);
        handler.removeCallbacks(timer_tick);
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    private Runnable timer_tick = new Runnable()
    {
        public void run()
        {
            if (!enabled)
                return;

            handler.post(runMethod);

            if (oneTime)
            {
                enabled = false;
                return;
            }

            handler.postDelayed(timer_tick, intervalMs);
        }
    }; 
}