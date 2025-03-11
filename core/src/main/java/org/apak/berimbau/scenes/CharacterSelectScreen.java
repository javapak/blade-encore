package org.apak.berimbau.scenes;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import de.eskalon.commons.screen.ManagedScreen;
import de.eskalon.commons.screen.ScreenManager;
import de.eskalon.commons.screen.transition.ScreenTransition;
import de.eskalon.commons.screen.transition.impl.SlidingDirection;
import de.eskalon.commons.screen.transition.impl.SlidingInTransition;

public class CharacterSelectScreen extends ManagedScreen {
    private Stage stage;
    private Skin skin;

    // 3D Components
    private PerspectiveCamera cam;
    private ModelBatch modelBatch;
    private Environment environment;
    private ModelInstance[] characterModels;
    private Model placeholderModel;
    private ScreenManager<ManagedScreen, ScreenTransition> screenManager;

    private int selectedIndex = 0;

    public CharacterSelectScreen(ScreenManager<ManagedScreen, ScreenTransition> screenManager)
    {
        this.screenManager = screenManager;
    }

    @Override
    public void show() {
        setup3D();
        setupUI();
    }

    private void setup3D() {
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0, 2, 7);
        cam.lookAt(0, 1, 0);
        cam.near = 0.1f;
        cam.far = 300f;
        cam.update();

        modelBatch = new ModelBatch();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.5f, 0.5f, 0.5f, 1f));
        environment.add(new DirectionalLight().set(Color.WHITE, -1f, -0.8f, -0.2f));

        ModelBuilder builder = new ModelBuilder();
        placeholderModel = builder.createBox(1.5f, 2.5f, 1f,
            new Material(ColorAttribute.createDiffuse(Color.PURPLE)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        characterModels = new ModelInstance[5]; // 5 characters

        for (int i = 0; i < characterModels.length; i++) {
            characterModels[i] = new ModelInstance(placeholderModel);
            characterModels[i].transform.setToTranslation((i - 2) * 3f, 0, 0); // Spread out horizontally
        }
    }

    private void selectCharacter(int index) {
        selectedIndex = index;
        Vector3 position = new Vector3((index - 2) * 3f, 1f, 0f);
        cam.position.set(position.x, position.y + 1.5f, 6f);
        cam.lookAt(position);
        cam.update();
    }

    private void setupUI() {
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("blade-encore/assets/lgdxs/skin/lgdxs-ui.json"));
        Gdx.input.setInputProcessor(stage);

        Table table = new Table();
        table.bottom().center();
        table.setFillParent(true);

        String[] characters = {"Judgement", "Vanguard", "Ryoku", "Pure", "Phalanx"};

        for (int i = 0; i < characters.length; i++) {
            final int index = i;
            TextButton button = new TextButton(characters[i], skin);
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selectCharacter(index);
                }
            });
            table.add(button).pad(5);
        }

        // Add back button
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                screenManager.pushScreen(new MainMenu(screenManager), new SlidingInTransition(new SpriteBatch(), SlidingDirection.RIGHT, 0.1f));
            }
        });

        table.row();
        table.add(backButton).colspan(5).padTop(10);

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT | Gdx.gl.GL_DEPTH_BUFFER_BIT);

        // Render 3D models
        modelBatch.begin(cam);
        for (ModelInstance model : characterModels) {
            modelBatch.render(model, environment);
        }
        modelBatch.end();

        // Render 2D UI
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        cam.viewportWidth = width;
        cam.viewportHeight = height;
        cam.update();
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        modelBatch.dispose();
        placeholderModel.dispose();
    }

    @Override
    public void pause() {}
    @Override
    public void resume() {}
}
