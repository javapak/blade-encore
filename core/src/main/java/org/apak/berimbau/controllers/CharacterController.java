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
        this.input = new NetworkInputHandler(playerID, NetworkManager.getInstance());
        camera = new PerspectiveCamera(120, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        

        this.camera.position.set(0, 1, 0);
        this.updateCamera();
    }

    public int getPlayerID() {
        return playerID;
    }


    private void setupPhysics() {
        rigidBody.setRestitution(0.0f);
        rigidBody.setFriction(0.9f);
        rigidBody.setDamping(0.05f, 0.05f);
        rigidBody.setGravity(new Vector3(0, -9.81f, 0));
        rigidBody.setActivationState(Collision.ACTIVE_TAG); 
        rigidBody.setCollisionFlags(rigidBody.getCollisionFlags() & ~btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
    
        transform.setTranslation(0, 2, 0);
        rigidBody.setWorldTransform(transform);
    
        System.out.println("Physics Initialized! RigidBody set up.");
    }
    @Override
    public void getWorldTransform(Matrix4 worldTrans) {
        worldTrans.set(transform);
    }

    public ModelInstance getModelInstance() {
        return modelInstance;
    }

    @Override
    public void setWorldTransform(Matrix4 worldTrans) {
        transform.set(worldTrans);
    
        //  Update MovementComponent position
        movement.setPosition(transform.getTranslation(movement.getPosition()));
    
        //  Sync ModelInstance with physics
        modelInstance.transform.set(transform);
    
        //  Debugging: Confirm correct position updates
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
    
        Vector3 moveDirection = new Vector3();
        if (Gdx.input.isKeyPressed(Input.Keys.W)) moveDirection.z -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) moveDirection.z += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) moveDirection.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) moveDirection.x += 1;
    
        if (!moveDirection.isZero()) {
            movement.move(moveDirection, deltaTime);
            System.out.println("Moving: " + moveDirection);
        }
    
        movement.update(); // Sync position and velocity with physics
    
        rigidBody.getWorldTransform(transform);
        modelInstance.transform.set(transform); // Sync ModelInstance
    
        updateCamera();
    
        if (network != null) {
            network.sync(this);
        }
    
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
