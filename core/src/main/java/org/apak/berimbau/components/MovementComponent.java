package org.apak.berimbau.components;
import com.badlogic.gdx.math.Vector3;

public class MovementComponent {
    private Vector3 position = new Vector3(0, 0, 0);
    private float velocityY = 0;
    private boolean isGrounded = true;

    public void update(InputHandler input, float deltaTime) {
        Vector3 direction = input.getMoveDirection();
        position.add(direction.scl(6f * deltaTime));

        applyGravity(deltaTime);
    }

    private void applyGravity(float deltaTime) {
        if (!isGrounded) {
            velocityY -= 9.8f * deltaTime;
            position.y += velocityY * deltaTime;
        }
        
        if (position.y <= 0) {
            position.y = 0;
            velocityY = 0;
            isGrounded = true;
        }
    }

    public boolean isAirborne() {
        return !isGrounded;
    }

    public Vector3 getPosition() {
        return position;
    }
    // For networking purposes
    public void setPosition(Vector3 newPosition) {
        position = newPosition;
    }
}

