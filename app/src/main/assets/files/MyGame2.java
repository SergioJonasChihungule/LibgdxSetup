package $packagename$;

import com.badlogic.gdx.Game;
import $packagename$.screens.FirstScreen;

public class MyGame extends Game {

    @Override
    public void create() {
        setScreen(new FirstScreen(this));
    }
}
