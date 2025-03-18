package org.apak.berimbau.components;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;

public class MovementComponent {
    protected final btRigidBody rigidBody;
    private final float moveSpeed = 10f;
    protected final Vector3 position = new Vector3();
    private final Vector3 velocity = new Vector3();

    public MovementComponent(btRigidBody rigidBody) {
        this.rigidBody = rigidBody;
        Matrix4 transform = new Matrix4();
        rigidBody.getWorldTransform(transform);
        transform.getTranslation(position);
    }
    
    public btRigidBody getRigidBody() {
        return rigidBody;
    }

    public void syncPositionFromPhysics() {
        Matrix4 transform = new Matrix4();
        rigidBody.getWorldTransform(transform);
        transform.getTranslation(position);
    }

    public void syncPhysicsFromPosition() {
        Matrix4 transform = new Matrix4();
        rigidBody.getWorldTransform(transform);
        transform.setTranslation(position);
        rigidBody.setWorldTransform(transform);
    }
    public void move(Vector3 moveDirection) {
        if (!moveDirection.isZero()) {
            // Use a reasonable force - scale by deltaTime to make it frame-rate independent
            moveDirection.nor().scl(moveSpeed * 100);
            // System.out.println("Applying force: " + moveDirection);
            rigidBody.applyCentralForce(moveDirection);
            rigidBody.activate();
            velocity.set(moveDirection);
        } else {
            velocity.setZero();
        }
    }

    public void update() {
        Vector3 oldPosition = new Vector3(position);
        
        Matrix4 transform = new Matrix4();
        rigidBody.getWorldTransform(transform);
        
        Vector3 newPosition = new Vector3();
        transform.getTranslation(newPosition);
        
        // Print the transform details
        
        // Update the position
        position.set(newPosition);
        
        if (!oldPosition.equals(position)) {
        }
    }

    public Vector3 getVelocity() {
        return velocity;
    }

    public void setPosition(Vector3 position) {
        this.position.set(position);
        updatePhysicsBodyPosition();
    }

    public Vector3 getPosition() {
        return position;
    
    }

    public boolean isAirborne() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isAirborne'");
    }

    public void updatePhysicsBodyPosition() {
        Matrix4 transform = new Matrix4();
        rigidBody.getWorldTransform(transform);
        transform.setTranslation(position);
        rigidBody.setWorldTransform(transform);
        rigidBody.activate(); // Ensure the body is active
    }
}
