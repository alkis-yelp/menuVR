package eat24.menuvr;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;
import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;
import com.google.vr.sdk.widgets.pano.VrPanoramaView.Options;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.egl.EGLConfig;

public class MenuActivity extends GvrActivity implements GvrView.StereoRenderer {

    private float[] headView;
    private float[] headRotation;

    private float[] otherheadRotation;

    private float[] camera;

    private boolean goToCenter = false;

    private int currentImageIndex = 3;
    private int topIndex = 5;

    private static final float CAMERA_Z = 0.01f;

    /** Actual panorama widget. **/
    private VrPanoramaView panoWidgetView;

    /**
     * Arbitrary variable to track load status. In this example, this variable should only be accessed
     * on the UI thread. In a real app, this variable would be code that performs some UI actions when
     * the panorama is fully loaded.
     */
    public boolean loadImageSuccessful;
    /** Tracks the file to be loaded across the lifetime of this app. **/
    private Uri fileUri;

    private String image = "food/item3.jpg";
    /** Configuration information for the panorama. **/
    private Options panoOptions = new Options();
    private ImageLoaderTask backgroundImageLoaderTask;

    private static final String TAG = "MenuActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        headView = new float[16];
        camera = new float[16];
        headRotation = new float[4];

        otherheadRotation = new float[2];

        setContentView(R.layout.panorama_layout);


        initializePanoramaView();
        initializeGvrView();
    }

    /**
     * Called when the Activity is already running and it's given a new intent.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, this.hashCode() + ".onNewIntent()");
        // Save the intent. This allows the getIntent() call in onCreate() to use this new Intent during
        // future invocations.
        setIntent(intent);
        // Load the new image.
        handleIntent(intent);
    }

    /**
     * Load custom images based on the Intent or load the default image. See the Javadoc for this
     * class for information on generating a custom intent via adb.
     */
    private void handleIntent(Intent intent) {
        // Determine if the Intent contains a file to load.
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Log.i(TAG, "ACTION_VIEW Intent recieved");

            fileUri = intent.getData();
            if (fileUri == null) {
                Log.w(TAG, "No data uri specified. Use \"-d /path/filename\".");
            } else {
                Log.i(TAG, "Using file " + fileUri.toString());
            }

            panoOptions.inputType = intent.getIntExtra("inputType", Options.TYPE_STEREO_OVER_UNDER);
            Log.i(TAG, "Options.inputType = " + panoOptions.inputType);
        } else {
            Log.i(TAG, "Intent is not ACTION_VIEW. Using default pano image.");
            fileUri = null;
            panoOptions.inputType = Options.TYPE_MONO;
        }
        loadImage();


    }

    private void loadImage() {
        // Load the bitmap in a background thread to avoid blocking the UI thread. This operation can
        // take 100s of milliseconds.
        if (backgroundImageLoaderTask != null) {
            // Cancel any task from a previous intent sent to this activity.
            backgroundImageLoaderTask.cancel(true);
        }
        backgroundImageLoaderTask = new ImageLoaderTask();
        backgroundImageLoaderTask.execute(Pair.create(fileUri, panoOptions));
    }

    public void initializeGvrView() {

//        ImageView iw = (ImageView)findViewById(R.id.imageView1);
//        int resID = getResources().getIdentifier("item3", "drawable",  getPackageName());
//        iw.setImageResource(resID);

        GvrView gvrView = (GvrView) findViewById(R.id.gvr_view);

//        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        gvrView.setRenderer(this);
        gvrView.setTransitionViewEnabled(true);

        // Enable Cardboard-trigger feedback with Daydream headsets. This is a simple way of supporting
        // Daydream controller input for basic interactions using the existing Cardboard trigger API.
        gvrView.enableCardboardTriggerEmulation();

        if (gvrView.setAsyncReprojectionEnabled(true)) {
            // Async reprojection decouples the app framerate from the display framerate,
            // allowing immersive interaction even at the throttled clockrates set by
            // sustained performance mode.
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }


        setGvrView(gvrView);
    }

    public void initializePanoramaView() {
//        setContentView(R.layout.panorama_layout);
        panoWidgetView = (VrPanoramaView) findViewById(R.id.pano_view);
        panoWidgetView.setEventListener(new ActivityEventListener());
        panoWidgetView.getHeadRotation(otherheadRotation);
        handleIntent(getIntent());
    }

    /**
     * Prepares OpenGL ES before we draw a frame.
     *
     * @param headTransform The head transformation in the new frame.
     */
    @Override
    public void onNewFrame(HeadTransform headTransform) {


        // Build the camera matrix and apply it to the ModelView.
        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        headTransform.getHeadView(headView, 0);
        // Update the 3d audio engine with the most recent head rotation.
        headTransform.getQuaternion(headRotation, 0);

        // rotation up
        if(headRotation[0] > 0.45 && headRotation[3] > 0) {
            String string = new String();
        }

        // rotation down
        if(headRotation[0] > 0.45 && headRotation[3] < 0) {
            String string = new String();
        }

        // rotation left
        if(headRotation[1] > 0.45 && headRotation[3] > 0 && !goToCenter) {
            goToCenter = true;
            if(currentImageIndex + 1 > 0) {
                currentImageIndex = currentImageIndex - 1;
                image = "food/item" + currentImageIndex + ".jpg";
            } else {
                currentImageIndex = topIndex;
                image = "food/item" + topIndex + ".jpg";
            }
            loadImage();
        }

        // rotation right
        if(headRotation[1] > 0.45 && headRotation[3] < 0 && !goToCenter) {
            goToCenter = true;
            if(currentImageIndex + 1 > topIndex) {
                currentImageIndex = 1;
                image = "food/item1.jpg";
            } else {
                currentImageIndex = currentImageIndex + 1;
                image = "food/item" + currentImageIndex + ".jpg";
            }
            loadImage();
        }

        if(headRotation[1] < 0.45) {
            goToCenter = false;
        }

        checkGLError("onReadyToDraw");
    }

    /**
     * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
     *
     * @param label Label to report in case of error.
     */
    private static void checkGLError(String label) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }

    @Override
    public void onDrawEye(Eye eye) {
        String string = new String();
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int i, int i1) {
        String string = new String();
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        String string = new String();
    }

    @Override
    public void onRendererShutdown() {
        String string = new String();
    }

    @Override
    protected void onPause() {
        if(panoWidgetView != null)
            panoWidgetView.pauseRendering();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(panoWidgetView != null)
            panoWidgetView.resumeRendering();
    }

    @Override
    protected void onDestroy() {
        // Destroy the widget and free memory.
        if(panoWidgetView != null)
            panoWidgetView.shutdown();

        // The background task has a 5 second timeout so it can potentially stay alive for 5 seconds
        // after the activity is destroyed unless it is explicitly cancelled.
        if (backgroundImageLoaderTask != null) {
            backgroundImageLoaderTask.cancel(true);
        }
        super.onDestroy();
    }

    /**
     * Helper class to manage threading.
     */
    class ImageLoaderTask extends AsyncTask<Pair<Uri, Options>, Void, Boolean> {

        /**
         * Reads the bitmap from disk in the background and waits until it's loaded by pano widget.
         */
        @Override
        protected Boolean doInBackground(Pair<Uri, Options>... fileInformation) {
            Options panoOptions = null;  // It's safe to use null VrPanoramaView.Options.
            InputStream istr = null;
            if (fileInformation == null || fileInformation.length < 1
                    || fileInformation[0] == null || fileInformation[0].first == null) {
                AssetManager assetManager = getAssets();
                try {
                    istr = assetManager.open(image);
                    panoOptions = new Options();
                    panoOptions.inputType = Options.TYPE_MONO;
                } catch (IOException e) {
                    Log.e(TAG, "Could not decode default bitmap: " + e);
                    return false;
                }
            } else {
                try {
                    istr = new FileInputStream(new File(fileInformation[0].first.getPath()));
                    panoOptions = fileInformation[0].second;
                } catch (IOException e) {
                    Log.e(TAG, "Could not load file: " + e);
                    return false;
                }
            }

            panoWidgetView.loadImageFromBitmap(BitmapFactory.decodeStream(istr), panoOptions);
            try {
                istr.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close input stream: " + e);
            }

            return true;
        }
    }

    /**
     * Listen to the important events from widget.
     */
    private class ActivityEventListener extends VrPanoramaEventListener {
        /**
         * Called by pano widget on the UI thread when it's done loading the image.
         */
        @Override
        public void onLoadSuccess() {
            loadImageSuccessful = true;
        }

        /**
         * Called by pano widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage) {
            loadImageSuccessful = false;
            Toast.makeText(
                    MenuActivity.this, "Error loading pano: " + errorMessage, Toast.LENGTH_LONG)
                    .show();
            Log.e(TAG, "Error loading pano: " + errorMessage);
        }
    }
}
