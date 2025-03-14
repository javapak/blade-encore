package org.apak.berimbau.components;

import java.io.IOException;

import java.io.Serializable;

import org.apak.berimbau.controllers.CharacterState;

public class StateMachine<T extends Enum<T>> implements Serializable {
    private AttackStance attackStance = AttackStance.BALANCED; // Default stance
    private boolean isAirborne = false;
    private boolean isWalking = false;
    private boolean isShuffling = false;
    private boolean isAttacking = false;
    private boolean isBlocking = false;
    private boolean isStanceSwitching = false;




    public void setState(StateMachine<CharacterState> newStates) {
        if (!this.equals(newStates)) {
            System.out.println("State changed to: " + newStates);
            this.attackStance = newStates.getAttackStance();
            this.isAirborne = newStates.isAirborne();
            this.isWalking = newStates.isWalking();
            this.isShuffling = newStates.isShuffling();
            this.isAttacking = newStates.isAttacking();
            this.isBlocking = newStates.isBlocking();
            this.isStanceSwitching = newStates.isStanceSwitching();             
        }

    }

     private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();       
    }

    public void setAttackStance(AttackStance stance) {
        if (this.attackStance != stance) {
            System.out.println("Switched to stance: " + stance);
            this.attackStance = stance;
        }
    }

    public StateMachine<CharacterState> getCurrentState() {
        return (StateMachine<CharacterState>) this;
    }

    public AttackStance getAttackStance() {
        return attackStance;
    }

    public void isAttacking(boolean isAttacking) {
        if (isAttacking) {
            this.isAttacking = true;
        } else {
            this.isAttacking = false;
        }

    }
    public void isBlocking(boolean isBlocking) {
        if (isBlocking) {
            this.isBlocking = true;
        } else {
            this.isBlocking = false;
        }
    }
    public void isAirborne(boolean isAirborne) {
        if (isAirborne) {
            this.isAirborne = true;
        } else {
            this.isAirborne = false;
        }
    }
    public void isShuffling(boolean isShuffling) {
        if (isShuffling) {
            this.isShuffling = true;
        } else {
            this.isShuffling = false;
        }
    }
    public boolean isAttacking() {
        return isAttacking;
    }
    public boolean isBlocking() {
        return isBlocking;
    }
    public boolean isAirborne() {
        return isAirborne;
    }
    public boolean isShuffling() {
        return isShuffling;
    }
    public boolean isStanceSwitching() {
        return isStanceSwitching;
    }

    public void isStanceSwitching(boolean isStanceSwitching) {
        if (isStanceSwitching) {
            this.isStanceSwitching = true;
        } else {
            this.isStanceSwitching = false;
        }
    }
    public void isWalking(boolean isWalking) {
        if (isWalking) {
            this.isWalking = true;
        } else {
            this.isWalking = false;
        }
    }

    public boolean isWalking() {
        return isWalking; 
    }
}
