package org.apak.berimbau.controllers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody.btRigidBodyConstructionInfo;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import org.apak.berimbau.components.*;
import org.apak.berimbau.network.NetworkManager;
import org.apak.berimbau.network.NetworkPacket;

public class CharacterController extends btMotionState {
    private final InputHandler input;
    private final MovementComponent movement;
    private int playerID;
    private final CombatComponent combat;
    private final StateMachine<CharacterState> state;    
    private final NetworkingComponent network; // Nullable (only for networked players)
    private ModelInstance modelInstance;

    private final Matrix4 transform = new Matrix4();
    private btCollisionShape collisionShape = new btBoxShape(new Vector3(0.5f, 1f, 0.5f)); // Character Hitbox
    
    private btRigidBodyConstructionInfo bodyInfo = new btRigidBody.btRigidBodyConstructionInfo( // ✅ Store this object
        70f,  // Mass (Must be > 0 for dynamic physics)
        this, // MotionState (Handles position updates)
        collisionShape
    );
    private btRigidBody rigidBody = new btRigidBody(bodyInfo);
    

    private final PerspectiveCamera camera;
    private final Vector3 cameraOffset = new Vector3(0, 2f, 6f); // Third-person offset

    // Constructor for Local Players

    public CharacterController() {
        this.setupModel();
        this.input = new LocalInputHandler();
        this.combat = new CombatComponent();
        this.state = new StateMachine<CharacterState>();
        this.network = null;
        
        setupPhysics();
        this.movement = new MovementComponent(rigidBody);
        this.camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        updateCamera();
    }

    private void setupModel() {
        ModelBuilder modelBuilder = new ModelBuilder();
        Model cubeModel = modelBuilder.createBox(
            1f, 2f, 1f,
            new com.badlogic.gdx.graphics.g3d.Material(),
            com.badlogic.gdx.graphics.VertexAttributes.Usage.Position | com.badlogic.gdx.graphics.VertexAttributes.Usage.Normal
        );
    
        modelInstance = new ModelInstance(cubeModel);
        
        if (modelInstance == null) {
            throw new RuntimeException("Model instance failed to initialize!");
        }
    }
    // Constructor for Networked Players
    public CharacterController(int playerID, String serverIP, int serverPort) {
        setupPhysics();
        this.movement = new MovementComponent(rigidBody);
        this.setupModel();
        this.playerID = playerID;
        this.combat = new CombatComponent();
        this.state = new StateMachine<CharacterState>();
        this.network = new NetworkingComponent(playerID, serverIP, serverPort);
        
        // Pass movement component to the NetworkInputHandler
        this.input = new NetworkInputHandler(
            playerID,
            ClientNetworkManager.getClientNetworkManagerInstance(serverIP, serverPort),
            this.movement
        );
        
        this.network.getNetworkManager().startReceiveData();
        camera = new PerspectiveCamera(120, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        

        this.camera.position.set(0, 1, 0);
        this.updateCamera();
    }
    public int getPlayerID() {
        return playerID;
    }

    public void setupPhysics() {
        // Create a new Vector3 to store the linear factor
        Vector3 linearFactor = new Vector3(1f, 1f, 1f);
    
        // Set the linear factor to ensure movement is allowed on all axes
        rigidBody.setLinearFactor(linearFactor);
    
        System.out.println("Set linear factor to: " + linearFactor);
        rigidBody.setRestitution(0.1f);
        rigidBody.setFriction(2f);  // Lower friction (was 0.9f)

        
        // Add stronger damping to prevent runaway acceleration
        rigidBody.setDamping(0.0f, 0.0f); 
        
        rigidBody.setGravity(new Vector3(0, -9.81f, 0));
        rigidBody.setActivationState(Collision.ACTIVE_TAG);

        rigidBody.setCollisionFlags(rigidBody.getCollisionFlags() & ~btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
    
        transform.setTranslation(0, 2, 0);
        rigidBody.setWorldTransform(transform);

    }    @Override
    public void getWorldTransform(Matrix4 worldTrans) {
        worldTrans.set(transform);
    }

    public ModelInstance getModelInstance() {
        return modelInstance;
    }
    public StateMachine<CharacterState> getState() {
        return state.getCurrentState();
    }

    public AttackStance getAttackState() {
        return state.getAttackStance();
    }

    public StateMachine getStateMachine() {
        return state;
    }

    public MovementComponent getMovement() {
        return movement;
    }

    public void update(float deltaTime) {
        rigidBody.activate(); // Prevent sleeping
    
        // First check for network updates
        if (network != null) {
            // Process any waiting network packets
            NetworkPacket packet = network.getNetworkManager().getNextPacket();
            if (packet != null) {
                network.applyNetworkData(this, packet);
            }
        }
    
        // If we recently got a network update, skip local movement processing
        if (network != null && network.wasRecentlyUpdatedFromServer()) {
            System.out.println("Skipping local movement - using network position");
        } else {
            // Local movement processing
            Vector3 moveDirection = new Vector3();
            if (Gdx.input.isKeyPressed(Input.Keys.W)) moveDirection.z -= 1;
            if (Gdx.input.isKeyPressed(Input.Keys.S)) moveDirection.z += 1;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) moveDirection.x -= 1;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) moveDirection.x += 1;
    
            if (!moveDirection.isZero()) {
                movement.move(moveDirection, deltaTime);
            }
        }
    
        // Always update movement component
        movement.update();
    
        // Sync model with physics
        rigidBody.getWorldTransform(transform);
        modelInstance.transform.set(transform);
    
        // Update camera
        updateCamera();
    
        // Send our current state to the server
        if (network != null) {
            network.sync(this);
        }
    }
    @Override
    public void setWorldTransform(Matrix4 worldTrans) {
        transform.set(worldTrans);
        
        // Explicitly sync position with the MovementComponent
        movement.syncPositionFromPhysics();
        
        // Sync ModelInstance with physics
        modelInstance.transform.set(transform);
    }
    
    private void cycleAttackStance() {
        switch (state.getAttackStance()) {
            case FAST -> state.setAttackStance(AttackStance.BALANCED);
            case BALANCED -> state.setAttackStance(AttackStance.HEAVY);
            case HEAVY -> state.setAttackStance(AttackStance.FAST);
            case AIR -> state.setAttackStance(AttackStance.BALANCED);
        }
    }

    public void handleNetworkData(NetworkPacket packet) {
        if (network != null) {
            network.applyNetworkData(this, packet);
        }
    }

    public btRigidBody getRigidBody() {
        return rigidBody;
    }

    public Camera getCamera() {
        return camera;
    }

    private void updateCamera() {
        Vector3 cameraPos = new Vector3(movement.getPosition()).add(cameraOffset);
        camera.position.set(cameraPos);
        camera.lookAt(movement.getPosition());
        camera.update();
    }
}