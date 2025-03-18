package org.apak.berimbau.components;

import org.apak.berimbau.controllers.CharacterState;

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

public class RemotePlayer extends btMotionState {
    public final String clientKey; // Format: "IP:PORT"
    private ModelInstance modelInstance;
    private btCollisionShape collisionShape = new btBoxShape(new Vector3(0.5f, 1f, 0.5f)); // Character Hitbox
    private btRigidBodyConstructionInfo bodyInfo = new btRigidBodyConstructionInfo(
        70f,  
        null, 
        collisionShape,
        Vector3.Zero
    );
    private btRigidBody rigidBody = new btRigidBody(bodyInfo);
    private final MovementComponent movement;
    private final CombatComponent combat;
    private final StateMachine<CharacterState> state;    
    private final Matrix4 transform = new Matrix4();

    public boolean isWalking, isShuffling, isAttacking, isBlocking, isAirborne, isStanceSwitching;

    public RemotePlayer(String clientKey) {
        System.out.println("New remote player being created and rendered...");
        this.setupModel();
        this.clientKey = clientKey;
        this.movement = new MovementComponent(rigidBody);
        this.combat = new CombatComponent();
        this.state = new StateMachine<>();
        this.setupPhysics();

    }

    @Override
    public void setWorldTransform(Matrix4 worldTrans) {
        transform.set(worldTrans);
        
        // Explicitly sync position with the MovementComponent
        movement.syncPositionFromPhysics();
        
        // Sync ModelInstance with physics
        modelInstance.transform.set(transform);
    }

    public void updatePosition(Vector3 moveDirection) {
        movement.move(moveDirection);
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

    public btRigidBody getRigidBody() {
        return rigidBody;
    }

    public ModelInstance getModelInstance() {
        return modelInstance;
    }

    public void teleportTo(Vector3 position) {
        transform.setTranslation(position);
        rigidBody.setWorldTransform(transform);
        modelInstance.transform.setTranslation(position);
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

    }

    public void update(float deltaTime) {
        rigidBody.activate(); // Prevent sleeping
        movement.update();
    }

    
}
