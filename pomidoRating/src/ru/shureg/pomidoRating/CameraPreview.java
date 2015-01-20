package ru.shureg.pomidoRating;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.ViewGroup.LayoutParams;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class CameraPreview extends SurfaceView implements Callback {
    
  SurfaceHolder mHolder;
  byte[] mBuffer;
  Camera mCamera;
  Context mContext;
  int previewSurfaceWidth, previewSurfaceHeight, rotate, target_x, target_y;
  float aspect;
  Size mPreviewSize;
  public ImageView mTarget;
  List<Size> mSupportedPreviewSizes;
  public Boolean settingAuto;
    
  public CameraPreview(Context context, Camera camera) { //1
    super(context);
    mContext = context;
    mCamera = camera;
    mHolder = getHolder(); //2
    mHolder.addCallback(this); //3
  }
  
  @Override
  public void surfaceCreated(SurfaceHolder holder) { //5
    try {
	  //  setCameraDisplayOrientation();
          mCamera.setFaceDetectionListener(faceDetectionListener);
	      mCamera.startFaceDetection();
	      mCamera.setPreviewDisplay(holder);
	      //mCamera.addCallbackBuffer(mBuffer);
	      //mCamera.setPreviewCallbackWithBuffer(mPreviewCallback);
          //   mCamera.setPreviewCallback(mPreviewCallback);

      mCamera.startPreview();

 
     
      //Toast.makeText(mContext, "aa" + maxFaceNumber, Toast.LENGTH_LONG).show();
    }
    catch (IOException e) {
      Toast.makeText(mContext, "Camera preview failed", Toast.LENGTH_LONG).show();
    }
  }
  
  private FaceDetectionListener faceDetectionListener = new FaceDetectionListener() {
      
      @Override
      public void onFaceDetection(Face[] faces, Camera camera) {
    	  if(settingAuto)
    	  {
          if(faces.length>0)
          {
        	  switch (rotate) {
				case Surface.ROTATION_0:
		        	  target_x= (-faces[0].rect.centerY()+1000)*previewSurfaceWidth/2000;
		        	  target_y= (faces[0].rect.centerX()+1000)*previewSurfaceHeight/2000;
		        	  break;
				case Surface.ROTATION_90:
					target_x= (faces[0].rect.centerX()+1000)*previewSurfaceWidth/2000;
					target_y= (faces[0].rect.centerY()+1000)*previewSurfaceHeight/2000;
					break;
				case Surface.ROTATION_270:
					target_x = (-faces[0].rect.centerX()+1000)*previewSurfaceWidth/2000;
					target_y= (-faces[0].rect.centerY()+1000)*previewSurfaceHeight/2000;
					break;
				default:
					target_x= (faces[0].rect.centerX()+1000)*previewSurfaceWidth/2000;
					target_y= (faces[0].rect.centerY()+1000)*previewSurfaceHeight/2000;
				}
        	  mTarget.setX(target_x-mTarget.getWidth()/2);
        	  mTarget.setY(target_y-mTarget.getHeight()/2);
          	}
    	  }
      }
  };

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	  mCamera.stopPreview();
	  Camera.Parameters parameters = mCamera.getParameters();
	  mPreviewSize = parameters.getPreviewSize();
	  WindowManager winManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE); 
     	  
      aspect = (float) mPreviewSize.width / mPreviewSize.height;
 		
      previewSurfaceWidth = this.getWidth();
      previewSurfaceHeight = this.getHeight();
      LayoutParams lp = this.getLayoutParams();
      rotate = winManager.getDefaultDisplay().getRotation(); //2
  	switch (rotate) {
		case Surface.ROTATION_0:
			mCamera.setDisplayOrientation(90);
			//parameters.setRotation(90);
		      lp.height = previewSurfaceHeight;
		      lp.width = (int) (previewSurfaceHeight / aspect);
			Log.v("SHUREG", "0");
			break;
		case Surface.ROTATION_90:
			mCamera.setDisplayOrientation(0);
			//parameters.setRotation(0);
		      lp.width = previewSurfaceWidth;
		      lp.height = (int) (previewSurfaceWidth / aspect);
			Log.v("SHUREG", "90");
			break;
		case Surface.ROTATION_180:
			mCamera.setDisplayOrientation(90);
			//parameters.setRotation(90);
		      lp.height = previewSurfaceHeight;
		      lp.width = (int) (previewSurfaceHeight / aspect);
			break;
		case Surface.ROTATION_270:
			mCamera.setDisplayOrientation(180);
			//parameters.setRotation(0);
			lp.width = previewSurfaceWidth;
		    lp.height = (int) (previewSurfaceWidth / aspect);
			break;
		default:
			break;
		}

      mCamera.setParameters(parameters);
      this.setLayoutParams(lp);
      mCamera.startPreview();
	  mCamera.startFaceDetection();
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
  }
  
  

}