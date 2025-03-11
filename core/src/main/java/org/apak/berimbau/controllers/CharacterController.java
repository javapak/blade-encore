package org.apak.berimbau.controllers;

import org.apak.berimbau.components.*;
import org.apak.berimbau.network.NetworkPacket;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;

public class CharacterController {
    private InputHandler input;
    private MovementComponent movement;
    private CombatComponent combat;
    private StateMachine state;
    private final ChatComponent chatComponent; // Nullabe (only for networked players)
    private NetworkingComponent network; // Nullable (only for networked players)

    // Constructor for Local Players (No Networking)
    public CharacterController() {
        this.input = new LocalInputHandler();
        this.movement = new MovementComponent();
        this.combat = new CombatComponent();
        this.state = new StateMachine();
        this.chatComponent = null; // No chat component needed
        this.network = null; // No networking needed
    }

    // Constructor for Networked Players
    public CharacterController(int playerID, String serverIP, int serverPort) {
        this.movement = new MovementComponent();
        this.combat = new CombatComponent();
        this.state = new StateMachine();
        this.network = new NetworkingComponent(playerID, serverIP, serverPort);
        this.input = new NetworkInputHandler(playerID, this.network.getNetworkManager());
        this.chatComponent = new ChatComponent(this.network.getNetworkManager(), playerID);

    }

    public Vector3 getPosition() {
        return movement.getPosition();
    }

    public String getState() {
        return state.getCurrentState();
    }

    public AttackStance getAttackStance() {
        return state.getAttackStance();
    }

    public StateMachine getStateMachine() {
        return state;
    }

    public MovementComponent getMovement() {
        return movement;
    }

    public void update(float deltaTime) {
        if (input instanceof NetworkInputHandler networkInput) {
            networkInput.captureAndSendInput(movement.getPosition()); // Capture and send input to server
        }
    
        movement.update(input, deltaTime);
        combat.update(input, state, movement, deltaTime);
    
        if (input.isAirborne()) {
            state.setAttackStance(AttackStance.AIR);
        } else if (input.isStanceSwitching()) {
            cycleAttackStance();
        }
    
        if (network != null) {
            network.sync(this);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.Y)) {
            chatComponent.toggleChat();
        }

        chatComponent.render();
    }
    

    private void cycleAttackStance() {
        switch (state.getAttackStance()) {
            case FAST -> state.setAttackStance(AttackStance.BALANCED);
            case BALANCED -> state.setAttackStance(AttackStance.HEAVY);
            case HEAVY -> state.setAttackStance(AttackStance.FAST);
            case AIR -> state.setAttackStance(AttackStance.BALANCED);
        }
    }

    public void dispose() {
        chatComponent.dispose();
    }

    public void handleNetworkData(NetworkPacket packet) {
        if (network != null) {
            network.applyNetworkData(this, packet);
        }
    }
}
