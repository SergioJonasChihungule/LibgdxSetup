package $packagename$.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;
import $packagename$.MyGame;

public class FirstScreen extends ScreenAdapter {

    Texture bg;
    SpriteBatch batch;
    public static float width, height;
    private OrthographicCamera camera;

    public FirstScreen(MyGame game) {
        bg = new Texture(Gdx.files.internal("ic_bg.jpg"));
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void render(float delta) {
        super.render(delta);

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
    public void resize(int w, int h) {
        super.resize(w, h);
        configureCamera(800);
    }

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
