package com.jbeer.dev.roguelike;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Roquelike implements ApplicationListener {
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Texture avatarTexture;
	private Rectangle avatar;

	@Override
	public void create() {		
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera(1, h/w);
		camera.setToOrtho(false, 800, 480);
		batch = new SpriteBatch();

		avatarTexture = new Texture(Gdx.files.internal("data/avatar.png"));

		avatar = new Rectangle();
		avatar.x = w / 2 - 128 / 2;
		avatar.y = 20;
		avatar.width = 32;
		avatar.height = 32;

	}

	@Override
	public void dispose() {
		batch.dispose();
		avatarTexture.dispose();
	}

	@Override
	public void render() {		
		Gdx.gl.glClearColor(1, 1, 1f, 1);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		camera.update();

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(avatarTexture, avatar.x, avatar.y);
		batch.end();

		if(Gdx.input.isKeyPressed(Keys.LEFT)) avatar.x -= 32 * Gdx.graphics.getDeltaTime();
		if(Gdx.input.isKeyPressed(Keys.UP)) avatar.y += 32 * Gdx.graphics.getDeltaTime();
		if(Gdx.input.isKeyPressed(Keys.DOWN)) avatar.y -= 32 * Gdx.graphics.getDeltaTime();
		if(Gdx.input.isKeyPressed(Keys.RIGHT)) avatar.x += 32 * Gdx.graphics.getDeltaTime();

		if(avatar.x < 0) avatar.x = 0;
		if(avatar.x > 800 - 128) avatar.x = 800 - 128;
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
}
