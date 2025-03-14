package org.apak.berimbau.components;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;

public class MovementComponent {
    private final btRigidBody rigidBody;
    private final float moveSpeed = 10f;
    private final Vector3 position = new Vector3();
    private final Vector3 velocity = new Vector3();

    public MovementComponent(btRigidBody rigidBody) {
        this.rigidBody = rigidBody;
        Matrix4 transform = new Matrix4();
        rigidBody.getWorldTransform(transform);
        transform.getTranslation(position);
    }

    public void move(Vector3 moveDirection, float deltaTime) {
        if (!moveDirection.isZero()) {
            moveDirection.nor().scl(moveSpeed);
            rigidBody.applyCentralForce(moveDirection);
            rigidBody.activate(); // ✅ Prevents Bullet from deactivating the character
            velocity.set(moveDirection); // ✅ Store movement velocity
            System.out.println("🛠 Applying force: " + moveDirection);
        } else {
            velocity.setZero(); // ✅ Stop movement if no input
        }
    }

    public void update() {
        Matrix4 transform = new Matrix4();
        rigidBody.getWorldTransform(transform);
        transform.getTranslation(position); 
   }

    public Vector3 getVelocity() {
        return velocity;
    }

    public void setPosition(Vector3 position) {
        this.position.set(position);
    }

    public Vector3 getPosition() {
        return position;
    
    }

    public boolean isAirborne() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isAirborne'");
    }
}