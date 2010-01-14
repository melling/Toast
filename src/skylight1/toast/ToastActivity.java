package skylight1.toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.drawable.AnimationDrawable;
import android.widget.ImageView;
import skylight1.toast.view.MediaPlayerHelper;
import skylight1.toast.view.MediaPlayerHelper.VideoStartListener;
import android.app.Activity;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

public class ToastActivity extends Activity implements TiltDetector.TiltListener {

    public static final boolean LOG = false;

    private static final String LOG_TAG = ToastActivity.class.getSimpleName();

    private TiltDetector mTiltDetector;

    private SoundPlayer mSoundPlayer;

    private String message;
    private ArrayList<String> messageList;
    private String[] splitList;


    private void fadeOutText() {
        final TextView captionTextView = (TextView) findViewById(R.id.videoText);
        Log.i(ToastActivity.class.getName(), "toast message = " + message);
        captionTextView.setText(message);
        captionTextView.setVisibility(View.VISIBLE);
        Animation fadeOutAnimation = new AlphaAnimation(0.0f, 1.0f);
        fadeOutAnimation.setStartOffset(1000);
        fadeOutAnimation.setDuration(5000);
        fadeOutAnimation.setFillAfter(true);
        captionTextView.setAnimation(fadeOutAnimation);
    }


	private SurfaceView preview;
	private SurfaceHolder holder;
    private MediaPlayer mp;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        message = "Toasts go here";
        // Load up toasts in array
        loadToasts();
        int pick = (int) (Math.random() * (double) splitList.length);
        message = splitList[pick];

        setContentView(R.layout.main);

        // Load the ImageView that will host the animation and
        // set its background to our AnimationDrawable XML resource.
        ImageView img = (ImageView) findViewById(R.id.videoview);
        if (img != null) {
            img.setBackgroundResource(R.drawable.toast_anim);

            // Get the background, which has been compiled to an AnimationDrawable object.
            AnimationDrawable frameAnimation = (AnimationDrawable) img.getBackground();

//             Start the animation (looped playback by default).
//            frameAnimation.start();
            AnimationRoutine animationRoutine = new AnimationRoutine();

            Timer t = new Timer(false);
            t.schedule(animationRoutine, 1000);
        } else {
            Log.e(LOG_TAG, "Error load Toast images for animation.");
        }


//		preview = (SurfaceView) findViewById(R.id.videoview);
        //Fake tilt and create haptic feedback whenever screen touched.
        final OnTouchListener onTouchListener = new OnTouchListener() {
            @Override
            public boolean onTouch(View aView, MotionEvent anEvent) {
                if (anEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    aView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
                            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);

                    onTiltStart();
                    onTiltEnd();

                    return true;
                }
                return false;
            }
        };

        img.setOnTouchListener(onTouchListener);
//		preview.setOnTouchListener(onTouchListener);

//		holder = preview.getHolder();
//		holder.addCallback(new HolderCallback(preview.getRootView()));

        mSoundPlayer = new SoundPlayer(this);
        mTiltDetector = new TiltDetector(this);
        fadeOutText();
        
        //Use volume controls for stream we output on.
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        System.gc();
    }

    /**
     * Called to start frame animation.
     */
    private class AnimationRoutine extends TimerTask {
        AnimationRoutine() {
        }

        public void run() {
            ImageView img = (ImageView) findViewById(R.id.videoview);
            AnimationDrawable frameAnimation = (AnimationDrawable) img.getBackground();
            frameAnimation.start();
        }
    }

    /**
     * Plays clink sound when tilted.
     */
    @Override
    public void onTiltStart() {
        if (LOG) Log.d(LOG_TAG, "onTiltStart()");
        mSoundPlayer.clink();
    }

    /**
     * Fades out the previous message and shows a new one when a tilt ends.
     */
    @Override
    public void onTiltEnd() {
        if (LOG) Log.d(LOG_TAG, "onTiltEnd()");

        int pick = (int) (Math.random() * (double) splitList.length);
        message = splitList[pick];

        if (mp != null) {
            mp.start();
        }

        fadeOutText();
    }

    public void loadToasts() {
        try {
            InputStream is = getAssets().open("toasts.txt");

            // We guarantee that the available method returns the total
            // size of the asset...  of course, this does mean that a single
            // asset can't be more than 2 gigs.
            int size = is.available();

            // Read the entire asset into a local byte buffer.
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            // Convert the buffer into a string.
            String text = new String(buffer);

            if (messageList == null) {
                messageList = new ArrayList<String>();
            }
            text = text.replaceAll("\r", "");
            splitList = text.split("%\n");
            messageList = null;

        } catch (IOException e) {
            // Should never happen!
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        final TextView captionTextView = (TextView) findViewById(R.id.videoText);
        captionTextView.setBackgroundColor(Color.TRANSPARENT);
        captionTextView.setDrawingCacheBackgroundColor(Color.TRANSPARENT);
        captionTextView.setDrawingCacheEnabled(false);
        captionTextView.setVisibility(View.GONE);

        // if the resume is coming back from some pause, then resume the player
        if (mp != null) {
            mp.start();
        }

        mTiltDetector.setTiltListener(this);

        if (messageList == null) {
            loadToasts();
        }
    }

    @Override
    protected void onPause() {
        Log.i(ToastActivity.class.getName(), "paused");
        super.onPause();

        mTiltDetector.setTiltListener(null);

        // if the media player has not already been disposed of (leaving this screen, as
        // compared to pausing to go to another application), then pause the video
        if (mp != null) {
            mp.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mp != null) {
            mp.stop();
            mp = null;
        }

        mSoundPlayer.release();
        mSoundPlayer = null;
	}

}