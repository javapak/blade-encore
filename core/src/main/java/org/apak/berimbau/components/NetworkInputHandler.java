package org.apak.berimbau.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import org.apak.berimbau.network.NetworkManager;
import org.apak.berimbau.network.NetworkPacket;

public class NetworkInputHandler implements InputHandler {
    private Vector3 moveDirection = new Vector3();
    private boolean isWalking;
    private boolean isShuffling;
    private boolean isAttacking;
    private boolean isBlocking;
    private boolean isAirborne;
    private boolean isStanceSwitching;

    private final int playerID;
    private final NetworkManager networkManager;

    public NetworkInputHandler(int playerID, NetworkManager networkManager) {
        this.playerID = playerID;
        this.networkManager = networkManager;
    }

    /**
     * Capture inputs like a local input handler but send them to the server.
     */
    public void captureAndSendInput(Vector3 position) {
        float x = (Gdx.input.isKeyPressed(Input.Keys.D) ? 1 : 0) - (Gdx.input.isKeyPressed(Input.Keys.A) ? 1 : 0);
        float z = (Gdx.input.isKeyPressed(Input.Keys.W) ? 1 : 0) - (Gdx.input.isKeyPressed(Input.Keys.S) ? 1 : 0);
        moveDirection.set(x, 0, z).nor();
        isWalking = Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT);
        isShuffling = detectShuffle();
        isAttacking = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        isBlocking = Gdx.input.isButtonPressed(Input.Buttons.RIGHT);
        isAirborne = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
        isStanceSwitching = Gdx.input.isKeyJustPressed(Input.Keys.Q);


        // Package the input state and send to server
        NetworkPacket packet = new NetworkPacket(playerID);
        packet.put("position", position);
        packet.put("moveDirection", moveDirection);
        packet.put("isWalking", isWalking);
        packet.put("isShuffling", isShuffling);
        packet.put("isAttacking", isAttacking);
        packet.put("isBlocking", isBlocking);
        packet.put("attackStance", AttackStance.FAST); 
        packet.put("isAirborne", isAirborne);
        packet.put("isStanceSwitching", isStanceSwitching);

        networkManager.sendData(packet); // Send to server for validation
    }

    /**
     * Called when a new network update is received from the server.
     */
    public void updateFromNetwork(NetworkPacket packet) {
        if (packet.getPlayerID() == this.playerID) {
            this.moveDirection.set(packet.getVector3("moveDirection"));
            this.isWalking = packet.getBoolean("isWalking");
            this.isShuffling = packet.getBoolean("isShuffling");
            this.isAttacking = packet.getBoolean("isAttacking");
            this.isBlocking = packet.getBoolean("isBlocking");
            this.isAirborne = packet.getBoolean("isAirborne");
            this.isStanceSwitching = packet.getBoolean("isStanceSwitching");
        }
    }

    private boolean detectShuffle() {
        return Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT);
    }

    @Override
    public Vector3 getMoveDirection() { return moveDirection; }
    @Override
    public boolean isWalking() { return Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT); }
    @Override
    public boolean isShuffling() { return detectShuffle(); }
    @Override
    public boolean isAttacking() { return isAttacking; }
    @Override
    public boolean isBlocking() { return Gdx.input.isButtonPressed(Input.Buttons.RIGHT); }
    @Override
    public boolean isAirborne() {
        return Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
    }
    @Override
    public boolean isStanceSwitching() { return isStanceSwitching; }
}
