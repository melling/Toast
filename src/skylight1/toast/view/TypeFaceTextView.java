package skylight1.toast.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.widget.TextView;

public class TypeFaceTextView extends TextView {
	private static final Paint BLACK_BORDER_PAINT = new Paint();

	static {
		BLACK_BORDER_PAINT.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
	}

	private static final int BORDER_WIDTH = 1;

	private Typeface typeface;

	public TypeFaceTextView(Context context) {
		super(context);
	}

	public TypeFaceTextView(Context context, AttributeSet attrs) {
		super(context, attrs);

		setDrawingCacheEnabled(false);

		setTypeface(attrs);
	}

	private void setTypeface(AttributeSet attrs) {
		final String typefaceFileName = attrs.getAttributeValue(null, "typeface");
		if (typefaceFileName != null) {
			typeface = Typeface.createFromAsset(getContext().getAssets(), typefaceFileName);
		}

		setTypeface(typeface);
	}

	public TypeFaceTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		setTypeface(attrs);
	}

	@Override
	public void draw(Canvas aCanvas) {
		aCanvas.saveLayer(null, BLACK_BORDER_PAINT, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
				| Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.MATRIX_SAVE_FLAG);
		drawBackground(aCanvas, -BORDER_WIDTH, -BORDER_WIDTH);
		drawBackground(aCanvas, BORDER_WIDTH + BORDER_WIDTH, 0);
		drawBackground(aCanvas, 0, BORDER_WIDTH + BORDER_WIDTH);
		drawBackground(aCanvas, -BORDER_WIDTH - BORDER_WIDTH, 0);
		aCanvas.restore();
		super.draw(aCanvas);
	}

	private void drawBackground(Canvas aCanvas, int aDX, int aDY) {
		aCanvas.translate(aDX, aDY);
		super.draw(aCanvas);
	}
}
