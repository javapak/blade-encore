package org.apak.berimbau.components;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class CombatComponent {
    private float attackCooldown = 0.5f;
    private float lastAttackTime = 0;

    public void update(InputHandler input, StateMachine state, MovementComponent movement, float deltaTime) {
        if (input.isAttacking() && canAttack()) {
            performAttack(state.getAttackStance());
            lastAttackTime = System.currentTimeMillis();
        }

        if (movement.isAirborne()) {
            state.setAttackStance(AttackStance.AIR);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            cycleAttackStance(state);
        }
    }

    private boolean canAttack() {
        return (System.currentTimeMillis() - lastAttackTime) > attackCooldown * 1000;
    }

    private void performAttack(AttackStance stance) {
        System.out.println("Performed attack in stance: " + stance);
        // Handle attack animations, hit detection, networking, etc.
    }

    private void cycleAttackStance(StateMachine state) {
        switch (state.getAttackStance()) {
            case FAST -> state.setAttackStance(AttackStance.BALANCED);
            case BALANCED -> state.setAttackStance(AttackStance.HEAVY);
            case HEAVY -> state.setAttackStance(AttackStance.FAST);
            case AIR -> state.setAttackStance(AttackStance.BALANCED); // Air stance resets after landing
        }
    }
}
