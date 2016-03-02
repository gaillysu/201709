/*
 * COPYRIGHT (C) 2014 Hugo Garcia-Cotte All Rights Reserved.
 */
package net.medcorp.library.ble.util;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * The Class QueuedMainThreadHandler, is a handler that will ensure that only one command will be run at a time.
 * And that they will be run on the UI thread.
 * There are different types of queues that are independants
 * When you do mQueuedMainThreadHandler.run(aHandler); it will wait until :
 * a- mQueuedMainThreadHandler.next() is called
 * or
 * b- that the maximum lock time have passed
 * /!\/!\/!\Backbone Class : Modify with care/!\/!\/!\
 */
public class QueuedMainThreadHandler {

    public enum QueueType {
        MEDBT, SyncController,OtaController,GoogleFit,LocalDatabase
    }

	/*Classic singleton class*/
	private static Map<QueueType,QueuedMainThreadHandler> mInstances = new HashMap<>();
	
	/**
	 * Classic singleton
	 */
	protected QueuedMainThreadHandler() {
		//Here to defeat instantiation
	}
	
	/**
	 * Gets the single instance of QueuedMainThreadHandler.
	 *
	 * @return single instance of QueuedMainThreadHandler
	 */
	public static QueuedMainThreadHandler getInstance(QueueType type) {
	   if(mInstances.get(type) == null) {
           mInstances.put(type , new QueuedMainThreadHandler());
	   }
	   return mInstances.get(type);
	}
	/*End - Classic singleton class*/
	   
	
	/** The queue length. We can't store more than this number of Runnables */
	int QUEUE_LENGTH = 255;
	
	/** The Handler of the ui thread. */
	Handler mUiThread = new Handler(Looper.getMainLooper());
	
	/** This Runnable, is the Timeout task. It's timer is set to 0 every time we put a new lock */
	Runnable mUnlockTask = new Runnable() {
        @Override
        public void run() {
    		Log.v(QueuedMainThreadHandler.class.toString(),"Lock timeout");
        	QueuedMainThreadHandler.this.next();
        }
    };
	
	/** The stored commands, that are waiting to be executed. */
	BlockingQueue<Runnable> mCommands = new ArrayBlockingQueue<Runnable>(QUEUE_LENGTH);
	
	/** The lock. */
	boolean mLock = false;
	
	/** The max lock time before timeout. */
	int MAX_LOCK_TIME = 5000;
	
	/**
	 * Post a runnable or store it (if we are already locked)
	 * Run the next() function when the runnable have finished
	 *
	 * @param r the runnable
	 */
	public synchronized void post(Runnable r){
	
		//Let's check if we are locked
		if(mLock == true){
			try {
				//If we are locked, we'll store this runnable for later
				mCommands.put(r);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
		} else {
			//If we are not locked, we put the lock then run a task (in the UI thread)
			lock();
			mUiThread.post(r);
		
		}
	}
	
	/**
	 * Post a runnable after a certain time.
	 *
	 * @param r the runnable
	 * @param delay the delay before posting
	 */
	public synchronized void postDelayed(final Runnable r, final long delay){
		mUiThread.postDelayed(new Runnable() {
            @Override
            public void run() {
            	QueuedMainThreadHandler.this.post(r);
            }
        }, delay);
	}
	
	/**
	 * This function will release the lock and allow our handler to handle the next task (if any)
	 */
	public synchronized void next(){
		unlock();
		
		if(mCommands.peek() != null){
			post(mCommands.poll());
		}
	}
	
	/**
	 * Deletes all pending commands without executing them
	 */
	public synchronized void clear(){
		unlock();
		
		mCommands.clear();
	}
	
	/**
	 * Locks the handler
	 */
	 synchronized void lock(){
		//Log.v(QueuedMainThreadHandler.class.toString(),"Lock acquired");
		mLock = true;
		
		//Here we reset the Timeout timer
		mUiThread.removeCallbacks(mUnlockTask);
		mUiThread.postDelayed(mUnlockTask, MAX_LOCK_TIME);
		
	}
	
	/**
	 * Unlocks the handler.
	 */
	 synchronized void unlock(){
		//Log.v(QueuedMainThreadHandler.class.toString(),"Lock released");
		mLock = false;
		
		//Here me stop the timeout timer
		mUiThread.removeCallbacks(mUnlockTask);
	}
	   
	 
}
