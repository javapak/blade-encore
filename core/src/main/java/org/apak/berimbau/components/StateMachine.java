package org.apak.berimbau.components;

public class StateMachine {
    private String currentState = "IDLE";
    private AttackStance attackStance = AttackStance.BALANCED; // Default stance
    private boolean isAirborne = false;
    private boolean isWalking = false;
    private boolean isShuffling = false;
    private boolean isAttacking = false;
    private boolean isBlocking = false;
    private boolean isStanceSwitching = false;




    public void setState(String newState) {
        if (!currentState.equals(newState)) {
            System.out.println("State changed to: " + newState);
            currentState = newState;
        }
    }

    public void setAttackStance(AttackStance stance) {
        if (this.attackStance != stance) {
            System.out.println("Switched to stance: " + stance);
            this.attackStance = stance;
        }
    }

    public String getCurrentState() {
        return currentState;
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
