package org.n52.android.ar.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.android.ar.view.gl.ARSurfaceView;
import org.n52.android.ar.view.overlay.ARCanvasSurfaceView;
import org.n52.android.newdata.DataSourceInstanceHolder;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class ARView extends FrameLayout {

	private ARCanvasSurfaceView mCanvasOverlayView;
	private ARSurfaceView mARSurfaceView;
	private Map<Object, List<ARObject2>> mARObjectMap = new HashMap<Object, List<ARObject2>>();
	private ArrayList<ARObject2> mARObjectsReusableList = new ArrayList<ARObject2>();

	public ARView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ARView(Context context) {
		super(context);
		init();
	}

	private void init() {
		if (isInEditMode()) {
			return;
		}

		mCanvasOverlayView = new ARCanvasSurfaceView(this);
		addView(mCanvasOverlayView, LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

		final ActivityManager activityManager = (ActivityManager) getContext()
				.getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo config = activityManager
				.getDeviceConfigurationInfo();

		if (config.reqGlEsVersion >= 0x20000 || Build.PRODUCT.startsWith("sdk")) {
			// Add ARSurfaceView only if OpenGL ES Version 2 supported
			mARSurfaceView = new ARSurfaceView(this);
			mARSurfaceView.setZOrderMediaOverlay(true);
			addView(mARSurfaceView, 0, new FrameLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

			// final DisplayMetrics displayMetrics = new DisplayMetrics();
			// getActivity().getWindowManager().getDefaultDisplay()
			// .getMetrics(displayMetrics);
		}
	}

	/**
	 * Sets the ARObjects to render with a specified key. Objects previously set
	 * using this key will get removed by this call.
	 * 
	 * @param arObjects
	 * @param key
	 */
	public void setARObjects(final List<ARObject2> arObjects, final Object key) {
		synchronized (this.mARObjectMap) {
			List<ARObject2> previousMapping = this.mARObjectMap.put(key,
					arObjects);
			if (previousMapping != null) {
				previousMapping.clear();
			}
		}

		mARSurfaceView.notifyARObjectsChanged();
		mCanvasOverlayView.notifyARObjectsChanged();
	}

	/**
	 * Removes all ARObjects which were previously set using the specified key
	 * 
	 * @param key
	 */
	public void clearARObjects(Object key) {
		synchronized (this.mARObjectMap) {
			mARObjectMap.remove(key);
		}

		mARSurfaceView.notifyARObjectsChanged();
		mCanvasOverlayView.notifyARObjectsChanged();
	}

	/**
	 * Removes all ARObjects
	 */
	public void clearARObjects() {
		synchronized (this.mARObjectMap) {
			for (List<ARObject2> itemList : mARObjectMap.values()) {
				itemList.clear();
			}
			mARObjectMap.clear();
		}

		mARSurfaceView.notifyARObjectsChanged();
		mCanvasOverlayView.notifyARObjectsChanged();
	}

	public List<ARObject2> getARObjects() {
		synchronized (mARObjectsReusableList) {
			synchronized (this.mARObjectMap) {
				mARObjectsReusableList.clear();
				for (List<ARObject2> itemList : mARObjectMap.values()) {
					mARObjectsReusableList.addAll(itemList);
				}
				return mARObjectsReusableList;
			}
		}
	}

	public void onPause() {
		mARSurfaceView.onPause();
	}

	public void onResume() {
		mARSurfaceView.onResume();
	}

}
