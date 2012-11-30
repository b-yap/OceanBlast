package com.meerusa.oceanblast;

import org.andengine.opengl.texture.region.TiledTextureRegion;

import org.andengine.opengl.vbo.VertexBufferObjectManager;
import java.util.Random;
public class Player extends GameObject {
	private int speed;
	// constructors
	public Player(final float pX, final float pY, final TiledTextureRegion pTiledTextureRegion,
					final VertexBufferObjectManager pVertexBufferObjectManager, int velocity)
	{
		super(pX, pY, pTiledTextureRegion, pVertexBufferObjectManager);
		this.speed=velocity;
	}

	@Override
	public void move() {
		
		//this.mPhysicsHandler.setVelocityX(-AccelerometerHelper.TILT*100);
		//setRotation(-AccelerometerHelper.TILT*7);
		this.mPhysicsHandler.setVelocityY(this.speed);
		OutofScreenY();
	}

	public void animation(int frameDuration){}
	
	private void OutofScreenY() {

		if(mY> MainActivity.CAMERA_HEIGHT)
		{	mY=0;
			Random rand = new Random();
			int b=rand.nextInt(790);
			this.setX(b);
		}
		else if(mY<0)
		{	mY=MainActivity.CAMERA_HEIGHT;
		Random rand = new Random();
		int b=rand.nextInt(790);
		this.setX(b);
		}
	
	
	}
	
	public float getXPos(){
		return this.getX();		
	}
	public float getYPos(){
		return this.getY();
	}
	
}
