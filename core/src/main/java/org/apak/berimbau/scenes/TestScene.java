package org.apak.berimbau.scenes;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

import de.eskalon.commons.screen.ManagedScreen;
import de.eskalon.commons.screen.ScreenManager;

import org.apak.berimbau.controllers.CharacterController;
import org.apak.berimbau.components.*;

public class TestScene extends ManagedScreen {
    private final ScreenManager screenManager;
    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;
    private ModelInstance ground, character;
    private CharacterController characterController;

    public TestScene(ScreenManager screenManager) {
        this.screenManager = screenManager;
    }

    @Override
    public void show() {
        setupEnvironment();
        setupCamera();
        setupTestObjects();
        setupCharacter();
    }

    private void setupEnvironment() {
        modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.5f, 0.5f, 0.5f, 1f));
        environment.add(new DirectionalLight().set(Color.WHITE, -1f, -0.8f, -0.2f));
    }

    private void setupCamera() {
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0, 5, 10);
        camera.lookAt(0, 1, 0);
        camera.near = 0.1f;
        camera.far = 300f;
        camera.update();
    }

    private void setupTestObjects() {
        ModelBuilder modelBuilder = new ModelBuilder();
        Model groundModel = modelBuilder.createBox(20f, 0.2f, 20f,
                new Material(ColorAttribute.createDiffuse(Color.GRAY)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        ground = new ModelInstance(groundModel);
    }

    private void setupCharacter() {
        // Create a placeholder box for the player character
        ModelBuilder modelBuilder = new ModelBuilder();
        Model characterModel = modelBuilder.createBox(1f, 2f, 1f,
                new Material(ColorAttribute.createDiffuse(Color.RED)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        character = new ModelInstance(characterModel);

        // Initialize character controller, random player id for now since only two (maybe a few more but whatever) players are going to be used.
        characterController = new CharacterController((int)Math.floor(Math.random() * 100), "10.0.0.61" , 7777);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        handleCameraControls(delta);
        characterController.update(delta);

        modelBatch.begin(camera);
        modelBatch.render(ground, environment);
        modelBatch.render(character, environment);
        modelBatch.end();
    }

    private void handleCameraControls(float delta) {
        float moveSpeed = 10f * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) camera.position.add(camera.direction.cpy().scl(moveSpeed));
        if (Gdx.input.isKeyPressed(Input.Keys.S)) camera.position.sub(camera.direction.cpy().scl(moveSpeed));
        if (Gdx.input.isKeyPressed(Input.Keys.A)) camera.position.add(camera.direction.cpy().crs(Vector3.Y).scl(-moveSpeed));
        if (Gdx.input.isKeyPressed(Input.Keys.D)) camera.position.add(camera.direction.cpy().crs(Vector3.Y).scl(moveSpeed));
        camera.update();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
    }

    @Override public void pause() {}
    @Override public void resume() {}
}
