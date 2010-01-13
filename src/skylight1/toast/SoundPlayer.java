package skylight1.toast;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

/**
 * Plays requested sounds.
 * 
 */
public class SoundPlayer {
		
	protected SoundPool mSoundPool;
	
	protected int mClinkSoundId;
	
	public SoundPlayer(final Context context) {
		mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
		mClinkSoundId = mSoundPool.load(context, R.raw.clink, 1);
	}
	
	protected void release() {
		if ( null != mSoundPool ) {
			mSoundPool.release();
			mSoundPool = null;
		}
	}	
	
	protected void play(final Integer soundID) {

		if ( null != mSoundPool && null != soundID ) {
		    //Priority 0 so don't interrupt anything higher.
			int streamID = mSoundPool.play(soundID, 1f, 1f, 0, 0, 1);
	
			//Once playing, don't let low priority sounds interrupt us.
			if ( streamID > 0 ) {
				mSoundPool.setPriority(streamID, 1);
			}	    
		}
	}
	
	public void clink() {
		play(mClinkSoundId);
	}
		
}
