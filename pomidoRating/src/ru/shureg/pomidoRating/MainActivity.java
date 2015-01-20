package ru.shureg.pomidoRating;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ru.shureg.pomidoRating.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;


public class MainActivity extends Activity implements OnTouchListener {

	
		  public static final String CATCH_PREFERENCES = "catchsettings";
		  public static final String CATCH_PREFERENCES_AUTO = "auto";
		  public static final String CATCH_PREFERENCES_SERVER = "server";
		  public static final String CATCH_PREFERENCES_SAVE= "save";
		  public static final String CATCH_PREFERENCES_DEF= "default";
		  public static final int CATCH_SETTING_REQUEST_CODE= 1;
		  SharedPreferences mPreferences;
		  Resources catchResources;
		  ImageView mBullet;
		  CameraPreview preview;
		  Camera mCamera;
		  FrameLayout mFrame;
		  Context mContext;
		  flyingObject tomat, flower, activeObject;
		  AnimationDrawable animation;
		  public static Boolean settingAuto, settingServer, settingSave, settingDefault;
		  int clickCount = 0;
       	  long startTime;
		  long duration;
		  static final int DBL_CLICK_DURATION = 250;
		  static final int WAIT_CLICK_DURATION = 1200;
		  
		  private class flyingObject
		  {
			  public int animationId, hitId,  waitButtonId , shootButtonId;
			  public int start_x, start_y;
			  public ImageButton button;
			  public int Size;
			  
			  
			  public flyingObject(int _shootId, int _hitId, int _animationId, int _waitButtonId, int _shootButtonId)
			  {
				  hitId=_hitId;
				   waitButtonId = _waitButtonId;
				  shootButtonId = _shootButtonId;
				  animationId=_animationId;
			  }
			  
			  public void setLayout(int _start_x, int _start_y, int size)
			  {
				    start_y = _start_y;
				    start_x = _start_x;
				    Size=size;
			  }
			  
			  public void setButtonWait()
			  {
				  button.setBackground(catchResources.getDrawable(waitButtonId));
			  }
			  
			  public void setButtonShoot()
			  {
				  button.setBackground(catchResources.getDrawable(shootButtonId));
			  }
			  
			  
		  }

		  private class Uploader extends AsyncTask<Bitmap, Integer, Integer> {
				
				String serverAddress;
				
				public Uploader(String Address)
				{
					serverAddress=Address;
				}
				
			    @Override
			    protected void onProgressUpdate(Integer... progress) {
			    	super.onProgressUpdate(progress);
			    }
			    
			    
				@Override
			    protected void onPostExecute(Integer result) {
					super.onPostExecute(result);
					if(result>0)
					{
					     SendFailed();
					}
			    }
			   
			   @Override
			    protected Integer doInBackground(Bitmap... parameter) {
				   String resultString;
				   Integer result;
				   final String delimiter = "--";
				   final String boundary =  "SwA"+Long.toString(System.currentTimeMillis())+"SwA";
			       ByteArrayOutputStream baos = new ByteArrayOutputStream();
			       parameter[0].compress(CompressFormat.JPEG, 100, baos);

			       try {
			    	   HttpURLConnection con = (HttpURLConnection) ( new URL(serverAddress)).openConnection();
			   	       con.setRequestMethod("POST");
			   	       con.setDoInput(true);
			   	       con.setDoOutput(true);
			   	       con.setRequestProperty("Connection", "Keep-Alive");
			   	       con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
			           con.connect();
			           OutputStream os = con.getOutputStream();
			           os.write((delimiter + boundary + "\r\n").getBytes());
			           os.write(("Content-Disposition: form-data; name=\"uploadFile\"; filename=\"image.jpeg\"\r\n"  ).getBytes());
			           os.write("\r\n".getBytes());
			           os.write(baos.toByteArray());
			           os.write("\r\n".getBytes());
					   os.write((delimiter + boundary + delimiter + "\r\n").getBytes());
			           os.flush();
			           os.close();
			           int responseCode = con.getResponseCode();
			           String response= con.getResponseMessage();
			           Log.v("SHUREG", response);
			           publishProgress(responseCode);
			           if (responseCode == 200) {
			               InputStream in = con.getInputStream();

			               InputStreamReader isr = new InputStreamReader(in,
			                       "UTF-8");

			               StringBuffer data = new StringBuffer();
			               int c;
			               while ((c = isr.read()) != -1) {
			                   data.append((char) c);
			               }

			               resultString = new String(data.toString());
			               result=0;
			               Log.v("SHUREG", resultString);

			           } else {
			               resultString = "сервер не ответил";
			               result=1;
			           }
			           con.disconnect();
			       }
			       catch(Throwable t) {
			           t.printStackTrace();
			           result=2;
			           Log.v("SHUREG", t.toString());
			       }
			        
			        return result;
			    }
			   
			   }
		  @Override
		  protected void onCreate(Bundle savedInstanceState) {
		    super.onCreate(savedInstanceState);
		    //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		    setContentView(R.layout.activity_main);
		    catchResources=getResources();
		    mContext = this;
		    mCamera = openCamera (); //1
		    if (mCamera == null) { //2
		      Toast.makeText(this, "Opening camera failed", Toast.LENGTH_LONG).show();
		      return;
		    }
		        
		    preview = new CameraPreview (this, mCamera); //3
		    mFrame = (FrameLayout) findViewById(R.id.layout); //4
		    mFrame.addView(preview, 0);
		    
		    tomat=new flyingObject(R.drawable.tomat, R.drawable.tomat_x, R.drawable.tomat_anim, R.drawable.tomat_button, R.drawable.tomat_button_x);
		    flower=new flyingObject(R.drawable.flowers, R.drawable.flowers_x, R.drawable.flower_anim, R.drawable.flowers_button, R.drawable.flowers_button_x);
		    
		    flower.button = (ImageButton) findViewById(R.id.capture); //5
		    tomat.button = (ImageButton) findViewById(R.id.capture2); //5
		    
		    mBullet = (ImageView)findViewById(R.id.image);
		    preview.mTarget = (ImageView)findViewById(R.id.target);
		    
		    mFrame.setOnTouchListener(this);
	        		  
		    flower.button.setOnClickListener(Fire);
		    
		    tomat.button.setOnClickListener(Fire);
		    
		    ViewTreeObserver vto = mFrame.getViewTreeObserver(); 
		    vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() { 
		        @Override 
		        public void onGlobalLayout() { 
		        	tomat.setLayout(mFrame.getWidth()-tomat.button.getWidth()+mBullet.getWidth()/2, mFrame.getHeight()-2*tomat.button.getHeight()/3, mBullet.getWidth());
		        	flower.setLayout(flower.button.getWidth()/3, mFrame.getHeight()-flower.button.getHeight(), mBullet.getWidth()*2);
					preview.target_x = mFrame.getWidth()/2;   
					preview.target_y = mFrame.getHeight()/2;  
				    preview.mTarget.setX(preview.target_x-preview.mTarget.getWidth()/2);
				    preview.mTarget.setY(preview.target_y-preview.mTarget.getHeight()/2);
		        	mFrame.getViewTreeObserver().removeOnGlobalLayoutListener(this);
		        } 
		    }); 
		  }
		  
		  
		  View.OnClickListener Fire=new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switch (v.getId())
				{
					case R.id.capture:
						activeObject = flower;
						settingDefault=true;
			        	break;
					case R.id.capture2:
						activeObject = tomat;
						settingDefault=false;
					    break;
					}
				activeObject.setButtonShoot();
	        	Shoot();
			}
				
			};
		  
		  @Override
		  public boolean onCreateOptionsMenu(Menu menu) {
		  	getMenuInflater().inflate(R.menu.main, menu);
		  	return true;
		  }
		  
		  AnimationListener animationFlyListener = new AnimationListener() {

				@Override
				public void onAnimationEnd(Animation arg0) {
					try {

					mBullet.setBackground(catchResources.getDrawable(activeObject.hitId)); 
					mCamera.takePicture(null, null, null, mPictureCallback);
					activeObject.setButtonWait();
					}
					catch (Exception e) {
						Log.v("SHUREG", "Ошибко1");
					}
		   
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub
				}

				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
				}
			};
			
			public static Bitmap RotateBitmap(Bitmap source, float angle)
			{
			      Matrix matrix = new Matrix();
			      matrix.postRotate(angle);
			      return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
			}
			
			public static Bitmap PrepareBitmap(byte[] data, Bitmap hit, int size, int target_x, int target_y, int width, int height, int rotate)
			{
			  Bitmap b;
			  int w,h, x, y;
			  switch (rotate) {
				case Surface.ROTATION_0:
					b= RotateBitmap(BitmapFactory.decodeByteArray(data, 0, data.length), 90);
					x=(int)(b.getWidth()*(target_x)/width);
					y=(int)(b.getHeight()*(target_y)/height);
					break;
				case Surface.ROTATION_90:
					b = BitmapFactory.decodeByteArray(data, 0, data.length);
					x=(int)(b.getWidth()*(target_x)/width);
					y=(int)(b.getHeight()*(target_y)/height);
					break;
				case Surface.ROTATION_270:
					b= RotateBitmap(BitmapFactory.decodeByteArray(data, 0, data.length), 180);
					x=(int)(b.getWidth()*(target_x)/width);
					y=(int)(b.getHeight()*(target_y)/height);
					break;
				default:
					b = BitmapFactory.decodeByteArray(data, 0, data.length);
					x=(int)(b.getWidth()*(target_x)/width);
					y=(int)(b.getHeight()*(target_y)/height);
				}
			  
		      Bitmap cs = Bitmap.createBitmap(b.getWidth(), b.getHeight(), Bitmap.Config.RGB_565);
		      Canvas comboImage = new Canvas(cs);
		      comboImage.drawBitmap(b, 0f, 0f, null);
		      w=(int)(b.getWidth()*size/width);
		      h=(int)(b.getHeight()*size/height);
		      Bitmap bmHalf = Bitmap.createScaledBitmap(hit, w,  h, false);
		      comboImage.drawBitmap(bmHalf, x, y, null);
		      return cs; 
			}
		  
			 @Override
			  public boolean onTouch(View v, MotionEvent event) {
				 if(!settingAuto)
				 {
					float x = event.getX();
			    	float y = event.getY();
			    
			    	preview.target_y=(int)(y);
			    	preview.target_x=(int)(x);
			    	preview.mTarget.setX(preview.target_x-preview.mTarget.getWidth()/2);
			    	preview.mTarget.setY(preview.target_y-preview.mTarget.getHeight()/2);
	        		if(activeObject!=null){
					 if(startTime>0 && clickCount==1 && (System.currentTimeMillis() - startTime) > WAIT_CLICK_DURATION)
						 {
						 clickCount--;
						 }

				 switch(event.getAction() & MotionEvent.ACTION_MASK)
			        {
			        	case MotionEvent.ACTION_DOWN:
			        		if((++clickCount)<2)
			        		{
			        			startTime = System.currentTimeMillis();
			        		    Log.v("SHUREG", "aaaa"+clickCount);
			        		}
			        		else
			        		{
			        			duration= System.currentTimeMillis() - startTime;
			        			Log.v("SHUREG", "a"+duration);
			        			if(duration<= DBL_CLICK_DURATION)
			        			{
			        				Shoot();
			        			}
			        			clickCount=0;
			        		}
			        		break;
			        /*	case MotionEvent.ACTION_UP:
			        		long time = System.currentTimeMillis() - startTime;
			        		duration=  duration + time;
			        			if(duration>= DBL_CLICK_DURATION)
			        			{
			        				activeObject=defaultObject;
			        				Shoot();
			        			}
			        			duration = 0;
		        			break;*/
			        }
				 }
				}
			    return true;
			  }
			
		  private PictureCallback mPictureCallback = new PictureCallback() {

			  @Override
			  public void onPictureTaken(byte[] data, Camera camera) {
			      try 
			      {
			    	  if(settingSave || settingServer)
			    	  {
			    		  BitmapFactory.Options options = new BitmapFactory.Options();
						    options.inSampleSize = 2;
						    options.outWidth=activeObject.Size;
						    options.outHeight=activeObject.Size;
			    		  Bitmap cs=PrepareBitmap(data, BitmapFactory.decodeResource(catchResources, activeObject.hitId, options), activeObject.Size, preview.target_x-activeObject.Size/2, preview.target_y-activeObject.Size/2, preview.previewSurfaceWidth, preview.previewSurfaceHeight, preview.rotate);
			    		  if(settingSave)
			    		  	{
			    			  Uri pictureFile = generateFile(); 
			    			  savePhotoInFile(cs, pictureFile);
			    		  	}
			    		  if(settingServer)
			    		  {
			    			  Uploader u=new Uploader(catchResources.getString(R.string.server_address));
			    			  u.execute(cs);
			    		  }
			    		  cs=null;
				      }
				   }
				   catch (Exception e) {
					Log.v("SHUREG", "Ошибко");
				 }
			     mCamera.startPreview(); 
				 mCamera.startFaceDetection();
			     mBullet.setVisibility(View.INVISIBLE);
			     mBullet.clearAnimation();
			  }
			};
			
	/*		private PictureCallback mPictureCallbackNew = new PictureCallback() {

				  @Override
				  public void onPictureTaken(byte[] data, Camera camera) {

				    try {
				         /*Bitmap b= RotateBitmap(BitmapFactory.decodeByteArray(data, 0, data.length),90);
				         Bitmap mfoto=b.copy(Bitmap.Config.RGB_565, true);
					     FaceDetector mface= new FaceDetector(mfoto.getWidth(),mfoto.getHeight(),1);
					     FaceDetector.Face [] face= new FaceDetector.Face[1];
					     int count = mface.findFaces(mfoto, face);
				         PointF midpoint = new PointF();
				    	 if (count > 0) 
				    	 {
					       face[0].getMidPoint(midpoint); 
					       preview.target_x=(int)(mFrame.getWidth()*midpoint.x/preview.previewSurfaceHeight);    // middle pint of  the face in x.
					       preview.target_y= (int)(mFrame.getHeight()*midpoint.y/preview.previewSurfaceWidth);    // middle point of the face in y.
				    	 }
				    	 else
				    	 {
				    		 preview.target_x = mFrame.getWidth()/2;   
				    		 preview.target_y = mFrame.getHeight()/2;
				    	 }
				    	 preview.mTarget.setX(preview.target_x-preview.mTarget.getWidth()/2);
				    	 preview.mTarget.setY(preview.target_y-preview.mTarget.getHeight()/2);
				    	 Shoot();
				    }
				    catch (Exception e) {
				      Log.v("SHUREG",e.getMessage());
				    }
				    mCamera.startPreview(); //3
				  }
				  
			};*/
			
			private void Shoot()
			{
				mBullet.setBackgroundResource(activeObject.animationId);
				LayoutParams lp = mBullet.getLayoutParams();
				lp.width = activeObject.Size;
				lp.height = activeObject.Size;
				mBullet.setLayoutParams(lp);
				animation = (AnimationDrawable)mBullet.getBackground();
				 final Animation animationFly = new TranslateAnimation(Animation.ABSOLUTE, activeObject.start_x, Animation.ABSOLUTE, 
						 preview.target_x-activeObject.Size/2, Animation.ABSOLUTE, activeObject.start_y, Animation.ABSOLUTE, preview.target_y-activeObject.Size/2);
				    animationFly.setDuration(500);
				    animationFly.setFillAfter(true);
				    animationFly.setRepeatCount(0);
				    animationFly.setAnimationListener(animationFlyListener);
		            mBullet.setVisibility(View.VISIBLE);
		            mBullet.bringToFront();
		            animation.start();
		            mBullet.startAnimation(animationFly);
			}
			
			private void savePhotoInFile(Bitmap bm, Uri pictureFile) throws Exception {
			    
				  if (pictureFile == null)
				    throw new Exception();

				  ByteArrayOutputStream baos = new ByteArrayOutputStream();  
				  bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object   
				  byte[] b = baos.toByteArray(); 
		          
				  OutputStream os = getContentResolver().openOutputStream(pictureFile);
				  os.write(b);
				  os.close();
				}
			
			private Uri generateFile() {
				  if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				    return null;

				  File path = new File (Environment.getExternalStorageDirectory(), "CameraTest");
				  if (! path.exists()){
				    if (! path.mkdirs()){
				      return null;
				    }
				  }
				            
				  String timeStamp = String.valueOf(System.currentTimeMillis());
				  File newFile = new File(path.getPath() + File.separator + timeStamp + ".jpg");
				  return Uri.fromFile(newFile);
				}

		  private Camera openCamera() { 
		    if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
		      return null;
		        
		    Camera cam = null;
		    if (Camera.getNumberOfCameras() > 0) {
		      try {
		        cam = Camera.open();
		      }
		      catch (Exception exc) {
		        //
		      }
		    }
		    return cam; 
		  }
		  
		  

		 

		  @Override
		  protected void onPause() { //6
		    super.onPause();
		    if (mCamera != null){
		      mFrame.removeView(preview);
		      preview=null;	
		      mCamera.release();
		      mCamera = null;
		    }
		    if(mPreferences==null)mPreferences = getSharedPreferences(CATCH_PREFERENCES, Context.MODE_PRIVATE);
  			Editor editor = mPreferences.edit();
  			editor.putBoolean(CATCH_PREFERENCES_DEF, settingDefault);
  			editor.apply();
		    mPreferences=null;
		  }
		  
		  @Override
		  protected void onResume() {
		    super.onResume();
		    if(mPreferences==null)mPreferences = getSharedPreferences(CATCH_PREFERENCES, Context.MODE_PRIVATE);
			
			settingServer = mPreferences.getBoolean(CATCH_PREFERENCES_SERVER, true);
			settingSave = mPreferences.getBoolean(CATCH_PREFERENCES_SAVE, true);
			settingDefault = mPreferences.getBoolean(CATCH_PREFERENCES_DEF, false);
		    if (mCamera == null)
		    {
		    	mCamera = openCamera();
		    	preview = new CameraPreview (this, mCamera); //3
		    	preview.mTarget = (ImageView)findViewById(R.id.target);
			    mFrame.addView(preview, 0);
		    };
		    settingAuto = mPreferences.getBoolean(CATCH_PREFERENCES_AUTO, false);
		    preview.settingAuto=settingAuto;
        	if(!settingAuto)
        	{
        		preview.target_x = mFrame.getWidth()/2;   
        		preview.target_y = mFrame.getHeight()/2;
			    preview.mTarget.setX(preview.target_x-preview.mTarget.getWidth()/2);
			    preview.mTarget.setY(preview.target_y-preview.mTarget.getHeight()/2);
        	}
        	else
        	{
           	}
		  }
		  
	  
		  private void runSettings()
		  {
			  Intent intent = new Intent(MainActivity.this, Settings.class);
			  intent.putExtra(CATCH_PREFERENCES_AUTO, settingAuto);
			  intent.putExtra(CATCH_PREFERENCES_SERVER, settingServer);
			  intent.putExtra(CATCH_PREFERENCES_SAVE, settingSave);
			  startActivityForResult(intent, CATCH_SETTING_REQUEST_CODE);
		  }
		  
		  private void runAbout()
		  {
			  Intent intent = new Intent(MainActivity.this, About.class);
			  startActivity(intent);
		  }
		  
	  
	  
		  public void onPreferencesClick(View view)
		  {
			  runSettings();
		  }
		  
		  public void onMenuItemClick(MenuItem item)
		  {
			  switch(item.getItemId())
			  {
			  case R.id.action_settings :
				  runSettings();
				  break;
			  case R.id.action_about:
				  runAbout();
				  break;
			  }
		  }
		  
		  @Override
		  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		  	// TODO Auto-generated method stub
		  	super.onActivityResult(requestCode, resultCode, data);

		  	if (requestCode == CATCH_SETTING_REQUEST_CODE) {
		  		if(resultCode==RESULT_OK)
		  		{
		  			if(mPreferences==null)mPreferences= getSharedPreferences(CATCH_PREFERENCES, Context.MODE_PRIVATE);
		  			Editor editor = mPreferences.edit();
		  			editor.putBoolean(CATCH_PREFERENCES_AUTO, data.getExtras().getBoolean(CATCH_PREFERENCES_AUTO));
		  			editor.putBoolean(CATCH_PREFERENCES_SERVER, data.getExtras().getBoolean(CATCH_PREFERENCES_SERVER));
		  			editor.putBoolean(CATCH_PREFERENCES_SAVE, data.getExtras().getBoolean(CATCH_PREFERENCES_SAVE));
		  			editor.apply();
		  		}
		  	}
		  }
		   
		  public void SendFailed()
		  {
		      Toast.makeText(this, "Send picture failed", Toast.LENGTH_LONG).show();
		  }
		  
		  
		  
 
		}