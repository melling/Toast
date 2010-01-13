/**
 *
 */
package skylight1.toast.view;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Preview extends SurfaceView {
	private SurfaceHolder mHolder;

	private Camera mCamera;

	public Preview(Context context) {
		super(context);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(new SurfaceHolder.Callback() {
			public void surfaceCreated(SurfaceHolder holder) {
				// The Surface has been created, acquire the camera and tell it where
				// to draw.
				try {
					mCamera.setPreviewDisplay(holder);
				} catch (Exception exception) {
					mCamera.release();
					mCamera = null;
					// TODO: add more exception handling logic here
				}
			}

			public void surfaceDestroyed(SurfaceHolder holder) {
				// Surface will be destroyed when we return, so stop the preview.
				// Because the CameraDevice object is not a shared resource, it's very
				// important to release it when the activity is paused.
				mCamera.stopPreview();
			}

			public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
				// Now that the size is known, set up the camera parameters and begin
				// the preview.
				Camera.Parameters parameters = mCamera.getParameters();
				Log.i(Preview.class.getName(), String.format("params are %s", parameters.flatten()));
				parameters.setPreviewSize(w, h);
				mCamera.setParameters(parameters);
				mCamera.startPreview();
			}
		});
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
}