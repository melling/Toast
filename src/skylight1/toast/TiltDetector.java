package skylight1.toast;

import static skylight1.toast.ToastActivity.*;

import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;

/**
 * Notifies listener of tilt events.
 *
 */
public class TiltDetector implements SensorEventListener {
	//TODO SensorEvent docs state SensorManager#getOrientation more accurate than listening to Sensor.TYPE_ORIENTATION
	
	/**
	 * Receives notifications from a TiltDetector.
	 *
	 */
	public static interface TiltListener {
		
		/**
		 * Notifies the start of a tilt.
		 */
		void onTiltStart();

		/**
		 * Notifies the end of a tilt.
		 */		
		void onTiltEnd();
		
	}
	
	private static final String LOG_TAG = TiltDetector.class.getSimpleName();
	
	/**
	 * Degrees off vertical on either axis that must be reached to be considered tilted.
	 */
	private static final int OFF_VERTICAL_THRESHOLD = 30;
	
	/**
	 * Force tilts to last this long.
	 * Prevents rapid notifications.
	 */
	private static final int MIN_TILT_LENGTH_MS = 500;
	
	/**
	 * Results must be the same this many times before notifying listener.
	 * Reduces erratic notifications.
	 */
	private static final int SETTLE_COUNT = 5;
	

	//Orientation values for when device is pointing up.
	private static final int POINTING_UP_PITCH = -90;
	private static final int POINTING_UP_ROLL = 0;
	
	private TiltListener mListener;
	
	private boolean mPreviousTilted;
	
	private SensorManager mSensors;
	
	private Sensor mSensor;
	
	private long mRecentTiltTime;
	
	private int mRecentTiltStartCount;
	
	private int mRecentTiltEndCount;
	
	public TiltDetector(final Context context) {
		mSensors = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		
   	   	final List<Sensor> orientaionSensors = mSensors.getSensorList(Sensor.TYPE_ORIENTATION);
   	   	if ( orientaionSensors.isEmpty() ) {
   	   		if ( LOG ) Log.w(LOG_TAG, "No orientation sensor found. No tilt notifications will be sent.");
   	   	} else {
   	   		mSensor = orientaionSensors.get(0);
   	   	}
	}	
	
	public void setTiltListener(final TiltListener listener) {
		if ( mListener == listener ) return;
		
		//Skip if no sensor. Will never generate notifications anyway.
		if ( null == mSensor ) return;
		
		mListener = listener;
		
		if ( null == mListener ) {
			mSensors.unregisterListener(this);
			return;
		}

		//Request faster rate than UI because we check multiple samples before calling UI.
		mSensors.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
	}
	
	private boolean isAxisTilted(final int pointingUp, final float currentValue) {
		final int lowThreshold = pointingUp - OFF_VERTICAL_THRESHOLD;
		final int highThreshold = pointingUp + OFF_VERTICAL_THRESHOLD;
	
		if ( currentValue > lowThreshold 
				&& currentValue < highThreshold ) {

			if ( LOG ) Log.d(LOG_TAG, "Axis not tilted. Current value, " + currentValue + ", is close to pointing up value, " + pointingUp);
			return false;
		}
		
		if ( LOG ) Log.d(LOG_TAG, "Axis tilted. Current value, " + currentValue + ", is far enough from pointing up value, " + pointingUp);
		return true;
	}
	
	public void onSensorChanged(final SensorEvent event) {
		
		if ( LOG ) Log.d(LOG_TAG, "onSensorChanged : Azimuth = " + event.values[0]
		+ ", Pitch = " + event.values[1] + ", Roll = " + event.values[2]);
		
		if ( 0 != mRecentTiltTime ) {
			final long mCurrentTime = SystemClock.uptimeMillis();
			if ( mCurrentTime - mRecentTiltTime < MIN_TILT_LENGTH_MS ) {
				if ( LOG ) Log.d(LOG_TAG, "Too soon after last tilt. Ignoring sensor event.");
				return;
			}
			mRecentTiltTime = 0;
		}

		//event.values[0] is not used, it's the direction the phone is pointing along the ground.
		boolean isTilted = isAxisTilted(POINTING_UP_PITCH, event.values[1]);
		if ( !isTilted ) {
			isTilted = isAxisTilted(POINTING_UP_ROLL, event.values[2]);
		}
		
		if ( isTilted ) {
			handleTilt();
			return;
		}
		
		handleNoTilt();
	}
	
	private void handleTilt() {
		//We already sent a notification for this tilt, so skip.
		if ( mPreviousTilted ) return;
				
		mRecentTiltStartCount++;
		mRecentTiltEndCount = 0;
		
		if ( mRecentTiltStartCount < SETTLE_COUNT ) {
			if ( LOG ) Log.d(LOG_TAG, "Tilt detected. Waiting for more readings before notifying.");
			return;
		}

		mPreviousTilted = true;
		mRecentTiltTime = SystemClock.uptimeMillis();
		
		if ( null != mListener ) {
			mListener.onTiltStart();
		}
	}
	
	private void handleNoTilt() {
		//We haven't been tilted or already sent a notification for the tilt ending, so skip.
		if ( !mPreviousTilted ) return;
		
		mRecentTiltStartCount = 0;
		mRecentTiltEndCount++;
		
		if ( mRecentTiltEndCount < SETTLE_COUNT ) {
			if ( LOG ) Log.d(LOG_TAG, "No tilt detected. Waiting for more readings before notifying.");
			return;
		}

		mPreviousTilted = false;
		
		if ( null != mListener ) {
			mListener.onTiltEnd();		
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//Do nothing.
	}

}
