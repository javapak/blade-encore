package org.apak.berimbau.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btStaticPlaneShape;

import de.eskalon.commons.screen.ManagedScreen;
import de.eskalon.commons.screen.ScreenManager;
import de.eskalon.commons.screen.transition.ScreenTransition;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apak.berimbau.components.RemotePlayer;
import org.apak.berimbau.controllers.CharacterController;
import org.apak.berimbau.network.GeneralUtils;

public class TestScene extends ManagedScreen {
    private btDiscreteDynamicsWorld dynamicsWorld;
    private btRigidBody groundBody;
    private CharacterController player;
    private ModelBatch modelBatch;
    private Environment environment;
    private ModelInstance groundModel;
    private btCollisionConfiguration collisionConfig;
    private btDispatcher dispatcher;
    private btBroadphaseInterface broadphase;
    private btConstraintSolver solver;
    private btCollisionShape groundShape;
    private Set<String> addedPlayerKeys = new HashSet<>();


    public TestScene(ScreenManager<ManagedScreen, ScreenTransition> screenManager) {
        setupPhysics();
        setupGround();
<<<<<<< HEAD
        player = new CharacterController(4, "10.0.0.61", 7777);
=======
        player = new CharacterController(3, GeneralUtils.getBestLocalIP().getHostAddress(), 7777);
        player.getRemotePlayers().forEachValue(1000, remotePlayer -> 
        dynamicsWorld.addRigidBody(remotePlayer.getRigidBody()));        
     
        
>>>>>>> f8b0f08afa854598984754bddc86492b8b9d1dd7
        player.setupPhysics();
        dynamicsWorld.addRigidBody(player.getRigidBody());

        modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.Diffuse, 0.8f, 0.8f, 0.8f, 1f)); // Make textures more visible
        environment.add(new DirectionalLight().set(1f, 1f, 1f, -1f, -0.8f, -0.2f)); 
    }

private void setupPhysics() {
    collisionConfig = new btDefaultCollisionConfiguration();
    dispatcher = new btCollisionDispatcher(collisionConfig);
    broadphase = new btDbvtBroadphase();
    solver = new btSequentialImpulseConstraintSolver();
    
    dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfig);
    dynamicsWorld.setGravity(new Vector3(0, -9.81f, 0));


    System.out.println(" Bullet Physics World Initialized!");
}

private void checkForNewRemotePlayers() {
    // Get all remote players
    ConcurrentHashMap<String, RemotePlayer> remotePlayers = player.getRemotePlayers();
    
    // Check for any players not yet added to physics world
    remotePlayers.forEach((key, remotePlayer) -> {
        if (!addedPlayerKeys.contains(key)) {
            // Add to physics world
            dynamicsWorld.addRigidBody(remotePlayer.getRigidBody());
            addedPlayerKeys.add(key);
            System.out.println("Added new remote player to physics world: " + key);
        }
    });
}

private void setupGround() {
    //  Create a visual model for the ground
    System.out.println(Gdx.files.internal("blade-encore/assets/textures/512blue.png").exists());
    Texture groundTexture = new Texture(Gdx.files.internal("blade-encore/assets/textures/512blue.png"));

    Material groundMaterial = new Material(TextureAttribute.createDiffuse(groundTexture));

    //  Use ModelBuilder to manually scale UVs
    ModelBuilder modelBuilder = new ModelBuilder();
    modelBuilder.begin();
    MeshPartBuilder meshBuilder = modelBuilder.part(
        "ground", GL20.GL_TRIANGLES,
        VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
        groundMaterial
    );

    float size = 50f;  // Ground size
    float uvScale = 10f;  // Repeat texture 10 times
    meshBuilder.rect(
        -size, 0f,  size,   // Top-left
         size, 0f,  size,   // Top-right
         size, 0f, -size,   // Bottom-right
        -size, 0f, -size,   // Bottom-left
        0f, 1f, 0f        
    );


    Model groundVisual = modelBuilder.end();
    groundModel = new ModelInstance(groundVisual);
    groundModel.transform.setToTranslation(0, -0.05f, 0); 

    // Create a static physics body for the ground
    btCollisionShape groundShape = new btStaticPlaneShape(new Vector3(0, 1, 0), 0);
    btRigidBodyConstructionInfo groundInfo = new btRigidBodyConstructionInfo(0, null, groundShape);
    groundBody = new btRigidBody(groundInfo);
    groundBody.setCollisionFlags(btCollisionObject.CollisionFlags.CF_STATIC_OBJECT);
    dynamicsWorld.addRigidBody(groundBody);

    System.out.println(" Ground model created with repeated texture!");
}
@Override
public void render(float delta) {
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    
    // Add teleport test on key press
    checkForNewRemotePlayers();

    if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
        Matrix4 resetTransform = new Matrix4();
        resetTransform.setToTranslation(0, 2, 0);
        player.getRigidBody().setWorldTransform(resetTransform);
        player.getMovement().update();
        System.out.println("Reset player position to origin");
        
        // Also reset velocity
        player.getRigidBody().setLinearVelocity(new Vector3(0, 0, 0));
    }

    // Physics and update
    dynamicsWorld.stepSimulation(delta, 5, 1f / 60f);
    player.update(delta);
    player.getRemotePlayers().forEachValue(1000, remotePlayer -> remotePlayer.update(delta));
    player.getCamera().update();
    
    // Debug output current position every few frames
    if (Gdx.graphics.getFrameId() % 60 == 0) {
    }
    
    modelBatch.begin(player.getCamera());
    if (groundModel != null) {
        modelBatch.render(groundModel, environment);
    }
    else {
        System.err.println("ERROR: groundModel is NULL!");
    }

    if (player.getModelInstance() != null) {
        modelBatch.render(player.getModelInstance(), environment);
    } else {
        System.err.println("ERROR: player.getModelInstance() is NULL!");
    }

    if (player.getRemotePlayers().size() > 0) {
        for (RemotePlayer remotePlayer : player.getRemotePlayers().values()) {
            if (remotePlayer.getModelInstance() != null) {
                modelBatch.render(remotePlayer.getModelInstance(), environment);
            } else {
                System.err.println("ERROR: remotePlayer.getModelInstance() is NULL!");
            }
        }
    }

    modelBatch.end();
}
@Override
    public void show() {
        Gdx.input.setInputProcessor(null); // Reset input for the scene
    }

    @Override
    public void resize(int width, int height) {
        player.getCamera().viewportWidth = width;
        player.getCamera().viewportHeight = height;
        player.getCamera().update();
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        dynamicsWorld.dispose();
        player.getRigidBody().dispose();
        if (groundBody != null) {
            dynamicsWorld.removeRigidBody(groundBody);
            groundBody.dispose();
            groundBody = null;
        }
    
        if (groundShape != null) {
            groundShape.dispose();
            groundShape = null;
        }
    
        if (dynamicsWorld != null) {
            dynamicsWorld.dispose();
            dynamicsWorld = null;
        }
    
        if (solver != null) {
            solver.dispose();
            solver = null;
        }
    
        if (broadphase != null) {
            broadphase.dispose();
            broadphase = null;
        }
    
        if (dispatcher != null) {
            dispatcher.dispose();
            dispatcher = null;
        }
    
        if (collisionConfig != null) {
            collisionConfig.dispose();
            collisionConfig = null;
        }
    
    }
}
