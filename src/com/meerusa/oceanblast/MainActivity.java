package com.meerusa.oceanblast;

import java.io.IOException;
import java.util.Random;

import org.andengine.audio.music.Music;
import org.andengine.audio.music.MusicFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
import org.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.modifier.ScaleModifier;
import org.andengine.entity.modifier.SequenceEntityModifier;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.ButtonSprite.OnClickListener;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.entity.util.FPSLogger;
import org.andengine.input.touch.controller.MultiTouch;

import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.debug.Debug;

import android.graphics.Typeface;
import android.opengl.GLES20;
import android.widget.Toast;

public class MainActivity extends SimpleBaseGameActivity{
	//constants
	public static int CAMERA_WIDTH=800;
	public static int CAMERA_HEIGHT = 480;
	
	private boolean mPlaceOnScreenControlsAtDifferentVerticalLocations = false;	
	
	//fields
	private Music mMusic;
	private Camera mCamera;
	private Scene mMainScene;
	private Scene mPlayScene;
	private Font mFont;
	public SceneType currentScene = SceneType.MENU;
	
	//images
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TiledTextureRegion mPlayerTileTextureRegion;
	
	private BitmapTextureAtlas mBubbleTextureAtlas;
	private TiledTextureRegion mBubbleTileTextureRegion;
		
	private BitmapTextureAtlas mButtonTextureAtlas;
	private TextureRegion mPlayButtonRegion;
	private TextureRegion mPlayHoverButtonRegion;
	private TextureRegion mOptButtonRegion;
	private TextureRegion mOptHoverButtonRegion;
	private TextureRegion mPauseButtonRegion;
	private BitmapTextureAtlas mBackgroundTextureAtlas; 
	private TextureRegion mBackgroundTextureRegion;
	
	private BuildableBitmapTextureAtlas mShootTextureAtlas;
	private ITextureRegion mFace1TextureRegion;
	private ITextureRegion mFace2TextureRegion;
	private ITextureRegion mFace3TextureRegion;
	
	//parallax background
	private BitmapTextureAtlas mAutoParallaxBackgroundTexture;
	private ITextureRegion mParallaxLayerBack;
	private ITextureRegion mParallaxLayerFront;
	
	//animated sprite
	private BuildableBitmapTextureAtlas mAnimatedAtlas;
	private TiledTextureRegion mGoldfishTextureRegion;
	
				
	// controls
	private BitmapTextureAtlas mOnScreenControlTexture;
	private ITextureRegion mOnScreenControlBaseTextureRegion;
	private ITextureRegion mOnScreenControlKnobTextureRegion;
			
		
	public EngineOptions onCreateEngineOptions() {
		this.mCamera = new Camera(0,0,CAMERA_WIDTH, CAMERA_HEIGHT);
		EngineOptions engineOption = new EngineOptions(true,ScreenOrientation.LANDSCAPE_FIXED,
								new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
		engineOption.getAudioOptions().setNeedsMusic(true);
		
		engineOption.getTouchOptions().setNeedsMultiTouch(true);

		if(MultiTouch.isSupported(this)) {
			if(MultiTouch.isSupportedDistinct(this)) {
				Toast.makeText(this, "MultiTouch detected --> Both controls will work properly!", Toast.LENGTH_SHORT).show();
			} else {
				this.mPlaceOnScreenControlsAtDifferentVerticalLocations = true;
				Toast.makeText(this, "MultiTouch detected, but your device has problems distinguishing between fingers." +
						"\n\nControls are placed at different vertical locations.", Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(this, "Sorry your device does NOT support MultiTouch!\n\n(Falling back to SingleTouch.)" +
					"\n\nControls are placed at different vertical locations.", Toast.LENGTH_LONG).show();
			}		
		return engineOption;
		}
	
	@Override
	protected void onCreateResources() {
		
		//the font
		this.mFont = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32);
		this.mFont.load();
		
		
		// the images
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");	
		mBubbleTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(),100,100);
			mBubbleTileTextureRegion = BitmapTextureAtlasTextureRegionFactory.
						createTiledFromAsset(this.mBubbleTextureAtlas,this,"bubbles.png",0,0,1,1);
		mBubbleTextureAtlas.load();
		
		mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(),200,200);
		mPlayerTileTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas,
									this,"submarine.png",0,0,1,1);
		mBitmapTextureAtlas.load();
		
	   //Buttons	
		mButtonTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(),120,280);
			mPlayButtonRegion =BitmapTextureAtlasTextureRegionFactory.
						createFromAsset(this.mButtonTextureAtlas, this,"play.png",0,0);
			mPlayHoverButtonRegion = BitmapTextureAtlasTextureRegionFactory.
						createFromAsset(this.mButtonTextureAtlas,this,"playHover.png",0,40);
				
			mOptButtonRegion =BitmapTextureAtlasTextureRegionFactory.
						createFromAsset(this.mButtonTextureAtlas, this,"option.png",0,80);
			mOptHoverButtonRegion = BitmapTextureAtlasTextureRegionFactory.
						createFromAsset(this.mButtonTextureAtlas,this,"optionHover.png",0,120);	
			mPauseButtonRegion = BitmapTextureAtlasTextureRegionFactory.
						createFromAsset(this.mButtonTextureAtlas, this, "pause.png", 0,185);
			
			mButtonTextureAtlas.load();
			
			//control button for shooting
			this.mShootTextureAtlas = new BuildableBitmapTextureAtlas(this.getTextureManager(), 512, 512);
			this.mFace1TextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mShootTextureAtlas, this, "face_box_tiled.png");
			this.mFace2TextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mShootTextureAtlas, this, "face_circle_tiled.png");
			this.mFace3TextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mShootTextureAtlas, this, "face_hexagon_tiled.png");
			
			try {
				this.mShootTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 0));
				this.mShootTextureAtlas.load();
			} catch (TextureAtlasBuilderException e) {
				Debug.e(e);
			}
		
		//background for the menu
		mBackgroundTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(),CAMERA_WIDTH, 1000 );
			mBackgroundTextureRegion = BitmapTextureAtlasTextureRegionFactory.
						createFromAsset(this.mBackgroundTextureAtlas,	this, "background.png",0,0);
		mBackgroundTextureAtlas.load();
		
		//background for the play Scene
		this.mAutoParallaxBackgroundTexture = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024);
		this.mParallaxLayerFront = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "parallax_background_layer_front.png", 0, 0);
		this.mParallaxLayerBack = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "background.png", 0, 188);
		//this.mParallaxLayerMid = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "parallax_background_layer_mid.png", 0, 669);
		this.mAutoParallaxBackgroundTexture.load();		
		
		//control images
		this.mOnScreenControlTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 128, TextureOptions.BILINEAR);
			this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.
				createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_base.png", 0, 0);
			this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.
				createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_knob.png", 128, 0);
		this.mOnScreenControlTexture.load();
		
		//animated sprite
		this.mAnimatedAtlas = new BuildableBitmapTextureAtlas(this.getTextureManager(), 512, 256, TextureOptions.NEAREST);
		this.mGoldfishTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mAnimatedAtlas, this, "goldfish_tiled.png",2,1);
		
		try {
			this.mAnimatedAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 1));
			this.mAnimatedAtlas.load();
		} catch (TextureAtlasBuilderException e) {
			Debug.e(e);
		}
		
		//add music
		MusicFactory.setAssetBasePath("musix/");
		try
		{
			this.mMusic = MusicFactory.createMusicFromAsset(mEngine.getMusicManager(), this, "mainMenuMusic.mid");
		this.mMusic.setVolume(0.5f);
			this.mMusic.setLooping(true);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}	
	}
	
	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());	
		this.mMainScene = new Scene();
		
		final int centerX=(CAMERA_WIDTH - mButtonTextureAtlas.getWidth())/2;
		final int centerY=(CAMERA_HEIGHT - mButtonTextureAtlas.getHeight())/2;
		
		//listeners
		OnClickListener musicListener = new OnClickListener() {
			
			//play Music
			public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX,
					float pTouchAreaLocalY) {
				if(MainActivity.this.mMusic.isPlaying())
					MainActivity.this.mMusic.pause();
				else
					MainActivity.this.mMusic.play();	
			}
		};
		
		OnClickListener playListener = new OnClickListener(){
			//play Music
			public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX,
					float pTouchAreaLocalY) {		
				MainActivity.this.createPlayScene();		
				/* Attach the menu. */
			mMainScene.setChildScene(mPlayScene, false, true, true);
			}
		};	
		
		//sprite for the background
				final Sprite oBackground = new Sprite(0,0,this.mBackgroundTextureRegion,getVertexBufferObjectManager());
				this.mMainScene.attachChild(oBackground); 

		//add the menu choices
		final Sprite playButton = new ButtonSprite(centerX,centerY,this.mPlayButtonRegion,
					this.mPlayHoverButtonRegion,getVertexBufferObjectManager(), playListener);		
		this.mMainScene.registerTouchArea(playButton);
		this.mMainScene.attachChild(playButton);
		this.mMainScene.setTouchAreaBindingOnActionDownEnabled(true);
				
		final Sprite optButton = new ButtonSprite(centerX,centerY+60,this.mOptButtonRegion,
					this.mOptHoverButtonRegion,getVertexBufferObjectManager(),musicListener);
		this.mMainScene.registerTouchArea(optButton);
		this.mMainScene.attachChild(optButton);
		this.mMainScene.setTouchAreaBindingOnActionDownEnabled(true); 
		
		
		//sprite to where the bubble image is placed
		final Player oPlayer = new Player(centerX,centerY,this.mBubbleTileTextureRegion,getVertexBufferObjectManager(),-100);
			this.mMainScene.attachChild(oPlayer);
		final Player oPlayerTwo = new Player(centerX-20,centerY,this.mBubbleTileTextureRegion,getVertexBufferObjectManager(),-50);
			this.mMainScene.attachChild(oPlayerTwo);		
		final Player oPlayerThree = new Player(centerX+100,centerY,this.mBubbleTileTextureRegion,getVertexBufferObjectManager(),-85);
			this.mMainScene.attachChild(oPlayerThree);		
		
		return this.mMainScene;
	}
	
	
	protected void createPlayScene(){
			//center the player on the camera
			final int centerX=(CAMERA_WIDTH - mBitmapTextureAtlas.getWidth())/2;
			final int centerY=(CAMERA_HEIGHT - mBitmapTextureAtlas.getHeight())/2;		
			
			this.mPlayScene = new Scene();
			final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
			final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
			autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(0.0f, new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerBack.getHeight(), 
					this.mParallaxLayerBack, vertexBufferObjectManager)));
			//autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-5.0f, new Sprite(0, 80, this.mParallaxLayerMid, vertexBufferObjectManager)));
			autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-10.0f, new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerFront.getHeight(),
					this.mParallaxLayerFront, vertexBufferObjectManager)));
			mPlayScene.setBackground(autoParallaxBackground);
			
			final Sprite pauseButton= new ButtonSprite(CAMERA_WIDTH-40, 0, this.mPauseButtonRegion, this.getVertexBufferObjectManager());
			this.mPlayScene.registerTouchArea(pauseButton);
			this.mPlayScene.attachChild(pauseButton);
			this.mPlayScene.setTouchAreaBindingOnActionDownEnabled(true);
			
			final Sprite face = new Sprite(centerX, centerY, this.mPlayerTileTextureRegion,this.getVertexBufferObjectManager());
			final PhysicsHandler physicsHandler = new PhysicsHandler(face);
			face.registerUpdateHandler(physicsHandler);
			this.mPlayScene.attachChild(face);
									
			final AnalogOnScreenControl analogOnScreenControl = new AnalogOnScreenControl(15, CAMERA_HEIGHT -  
					this.mOnScreenControlBaseTextureRegion.getHeight()-15,this.mCamera, 
					this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 
					0.1f, 200, this.getVertexBufferObjectManager(), new IAnalogOnScreenControlListener() {

						public void onControlChange(BaseOnScreenControl pBaseOnScreenControl,float pValueX, float pValueY) {
							physicsHandler.setVelocity(pValueX * 250, pValueY * 250);
							
							// setting boundaries
							int zeroA = 0;
							int zeroB = (int) face.getHeight();
							float velX = physicsHandler.getVelocityX();
							float velY = physicsHandler.getVelocityY();
            
							if (face.getX() < zeroA) {
                    
								if (face.getY() < zeroA) {
									if (velX < 0)
										physicsHandler.setVelocityX(0.0f);
									if (velY < 0)
										physicsHandler.setVelocityY(0.0f);
								} 
								else if (face.getY() > CAMERA_HEIGHT - zeroB) {
									if (velX < 0)
										physicsHandler.setVelocityX(0.0f);
									if (velY > 0)
										physicsHandler.setVelocityY(0.0f);
								} else {
									if (velX < 0)
										physicsHandler.setVelocityX(0.0f);
								}
								
							} else if (face.getX() > CAMERA_WIDTH - zeroB) {
								
								if (face.getY() < zeroA) {
									if (velX > 0)
										physicsHandler.setVelocityX(0.0f);
									if (velY < 0)
										physicsHandler.setVelocityY(0.0f);

								} else if (face.getY() > CAMERA_HEIGHT - zeroB) {
									if (velX > 0)
										physicsHandler.setVelocityX(0.0f);
									if (velY > 0)
										physicsHandler.setVelocityY(0.0f);

								} else {
                                    if (velX > 0)
                                    physicsHandler.setVelocityX(0.0f);
								  }
							
							} else {
									if (face.getY() < zeroA) {
                          
										if (velY < 0)
											physicsHandler.setVelocityY(0.0f);

									} else if (face.getY() > CAMERA_HEIGHT - zeroB) {
                                         if (velY > 0)
                                        	 physicsHandler.setVelocityY(0.0f);

									} 
							}		
					} // end of onControlChange


						public void onControlClick(AnalogOnScreenControl pAnalogOnScreenControl) {
							face.registerEntityModifier(new SequenceEntityModifier(new ScaleModifier(0.25f, 1, 1.5f), new ScaleModifier(0.25f, 1.5f, 1)));
							
						}});
						
			analogOnScreenControl.getControlBase().setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
			analogOnScreenControl.getControlBase().setAlpha(0.5f);
			analogOnScreenControl.getControlBase().setScaleCenter(0, 128);
			analogOnScreenControl.getControlBase().setScale(1.25f);
			analogOnScreenControl.getControlKnob().setScale(1.25f);
			analogOnScreenControl.refreshControlKnobPosition();
			
			this.mPlayScene.setChildScene(analogOnScreenControl);
			
			//enemy
			final Enemy goldfish = new Enemy(650,50,this.mGoldfishTextureRegion,this.getVertexBufferObjectManager(),-100);
			goldfish.animation(200);
			final PhysicsHandler physicsHand = new PhysicsHandler(goldfish);
			goldfish.registerUpdateHandler(physicsHand);
			mPlayScene.attachChild(goldfish);
			
			
		
			// shoot bullet listener
			OnClickListener shootListener = new OnClickListener() {
				
				public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX,
						float pTouchAreaLocalY) {
					//the bullet
					final Rectangle bullet = new Rectangle(face.getX()+100,face.getY()+50, 32, 32, vertexBufferObjectManager);
					bullet.setColor(1,0,0);
					PhysicsHandler bulletHandler = new PhysicsHandler(bullet);
					bulletHandler.setVelocityX(100);
					bullet.registerUpdateHandler(bulletHandler);
					mPlayScene.attachChild(bullet);
					
					mPlayScene.registerUpdateHandler(new IUpdateHandler() {
						public void reset() { 
												}

						public void onUpdate(final float pSecondsElapsed) {							
							if(bullet.collidesWith(goldfish)){
									bullet.detachSelf();								
									mPlayScene.detachChild(bullet);
									final Text hitText = new Text(bullet.getX(), bullet.getY(), mFont, "Hit!!", 
											new TextOptions(HorizontalAlign.CENTER), vertexBufferObjectManager);										
									mPlayScene.attachChild(hitText);
								}
						}
					});					
				}
			};
			
			//the shoot button
			final Sprite fire = new ButtonSprite(700, 420, this.mFace1TextureRegion, 
					this.mFace2TextureRegion, this.mFace3TextureRegion, vertexBufferObjectManager,shootListener);
			mPlayScene.registerTouchArea(fire);
			mPlayScene.attachChild(fire);
			mPlayScene.setTouchAreaBindingOnActionDownEnabled(true);
			
					
			/* The actual collision-checking. */
			mPlayScene.registerUpdateHandler(new IUpdateHandler() {
				public void reset() {
					
				}

				public void onUpdate(final float pSecondsElapsed) {
					final Text centerText = new Text(350, 240, mFont, "Game Over!!", new TextOptions(HorizontalAlign.CENTER), vertexBufferObjectManager);				
					
					if(goldfish.collidesWith(face)) {
						mPlayScene.attachChild(centerText);
					}else
						mPlayScene.detachChild(centerText);					
					
					
					if(!mCamera.isRectangularShapeVisible(face)) {
						//nothing
					}
				}
			});
			
			
		}
			
} 