package org.apak.berimbau.scenes;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import org.apak.berimbau.components.CustomButton;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import de.eskalon.commons.core.ManagedGame;
import de.eskalon.commons.screen.ManagedScreen;
import de.eskalon.commons.screen.ScreenManager;
import de.eskalon.commons.screen.transition.ScreenTransition;
import de.eskalon.commons.screen.transition.impl.SlidingDirection;
import de.eskalon.commons.screen.transition.impl.SlidingInTransition;
import de.eskalon.commons.screen.transition.impl.PushTransition;

import org.apak.berimbau.Main;

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
        table.center();

        CustomButton play = new CustomButton("Play");
        play.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Switching to Gameplay screen...");
                screenManager.pushScreen(new TestScene(screenManager), new PushTransition(new SpriteBatch(), SlidingDirection.RIGHT, 0.2f));
        }});
        CustomButton settings = new CustomButton("Settings");
        CustomButton character = new CustomButton("Character");
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
        table.add(play).width(200).pad(10);
        table.add(settings).width(200).pad(10);
        table.add(character).width(200).pad(10);
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
