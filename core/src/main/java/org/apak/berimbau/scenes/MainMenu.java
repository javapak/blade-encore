package org.apak.berimbau.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import org.apak.berimbau.components.CustomButton;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import de.eskalon.commons.screen.ManagedScreen;
import de.eskalon.commons.screen.ScreenManager;
import de.eskalon.commons.screen.transition.ScreenTransition;
import de.eskalon.commons.screen.transition.impl.SlidingDirection;
import de.eskalon.commons.screen.transition.impl.PushTransition;


public class MainMenu extends ManagedScreen {
    private Stage stage;
    private Skin skin;
    private ScreenManager<ManagedScreen, ScreenTransition> screenManager;
    public MainMenu(ScreenManager<ManagedScreen, ScreenTransition> screenManager) {
        this.screenManager = screenManager;
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("blade-encore/assets/blade-encore-ui/blade-encore.json"));
        Gdx.input.setInputProcessor(stage);

        // Example usage:
        Table table = new Table();
        table.setFillParent(true);
        table.bottom();
        table.padBottom(100f);

        CustomButton play = new CustomButton("Play", 350f, 70f);
        play.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Switching to Gameplay screen...");
                screenManager.pushScreen(new TestScene(screenManager), new PushTransition(new SpriteBatch(), SlidingDirection.RIGHT, 0.2f));
        }});
        CustomButton settings = new CustomButton("Settings", 350f, 70f);
        CustomButton character = new CustomButton("Customize", 350f, 70f);
        character.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Switching to Character Select screen...");

                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        screenManager.pushScreen(new CharacterSelectScreen(screenManager), new PushTransition(new SpriteBatch(), SlidingDirection.RIGHT, 0.2f));
                    }
                });
            }
        });
        table.add(play).width(200f).pad(20f);
        table.add(settings).width(200f).pad(20f);
        table.add(character).width(200f).pad(20f);
        stage.addActor(table);

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        
        for (Actor actor : stage.getActors()) {
            if (actor instanceof CustomButton) {
                ((CustomButton) actor).updateSize(width, height); // Resize custom buttons
            }
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
