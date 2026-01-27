package $packagename$;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MyGame implements ApplicationListener {

    Texture bg;
    SpriteBatch batch;
    public static float width, height;
    private OrthographicCamera camera;

    @Override
    public void create() {

        bg = new Texture(Gdx.files.internal("ic_bg.jpg"));
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        configureCamera(800);
    }

    @Override
    public void resize(int arg0, int arg1) {}

    @Override
    public void render() {
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        ScreenUtils.clear(0.1f, 0.3f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(bg, 0, 0, camera.viewportWidth, camera.viewportHeight);
        batch.end();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        batch.dispose();
        bg.dispose();
    }

    private void configureCamera(int size) {
        if (Gdx.graphics.getHeight() < Gdx.graphics.getWidth())
            camera.setToOrtho(
                    false, size, size * Gdx.graphics.getHeight() / Gdx.graphics.getWidth());
        else
            camera.setToOrtho(
                    false, size * Gdx.graphics.getWidth() / Gdx.graphics.getHeight(), size);
    }
}
