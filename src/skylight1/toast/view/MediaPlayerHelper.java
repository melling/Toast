package skylight1.toast.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import skylight1.toast.ToastActivity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.util.Log;
import android.view.SurfaceView;

public class MediaPlayerHelper {
	public interface VideoStartListener {
		void videoStarted(int anIndex);
	}

	private List<String> listOfMovies;

	private Context context;

	private MediaPlayer mediaPlayer;

	private SurfaceView surfaceView;

	private VideoStartListener videoStartListener;

	private int videoIndex;

	public MediaPlayerHelper(Context aContext, SurfaceView aSurfaceView, String... aListOfResources) {
		context = aContext;
		surfaceView = aSurfaceView;
		listOfMovies = new ArrayList<String>(Arrays.asList(aListOfResources));
		Log.i(MediaPlayerHelper.class.getName(), String.format("created new media player helper for movies %s", Arrays
				.toString(aListOfResources)));
	}

	public MediaPlayer createMediaListPlayer() {
		mediaPlayer = new MediaPlayer();

		mediaPlayer.setDisplay(surfaceView.getHolder());

		mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				Log.i(ToastActivity.class.getName(), "mp is prepared");

				surfaceView.setBackgroundResource(0);

				// start the video
				mp.start();

				if (videoStartListener != null) {
					videoStartListener.videoStarted(videoIndex);
				}
				videoIndex++;
			}
		});

		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				Log.i(ToastActivity.class.getName(), "mp is completed");
				loadNextMovie();
			}
		});

//		surfaceView.setBackgroundResource(R.drawable.welcome_background);
		// load the first movie
		loadNextMovie();

		return mediaPlayer;
	}

	public void setVideoStartListener(VideoStartListener aVideoStartListener) {
		videoStartListener = aVideoStartListener;
	}

	private void loadNextMovie() {
		if (listOfMovies.isEmpty()) {
			return;
		}

		try {
			final String fileName = listOfMovies.remove(0);
			Log.i(MediaPlayerHelper.class.getName(), String.format("about to load movie %s", fileName));
			final AssetFileDescriptor afd = context.getAssets().openFd(fileName);
//			mediaPlayer.reset();
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
			createMediaListPlayer();
			mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
			mediaPlayer.prepareAsync();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
