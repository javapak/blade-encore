package org.apak.berimbau.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import org.apak.berimbau.network.NetworkManager;
import org.apak.berimbau.network.NetworkPacket;

public class ChatComponent {
    private final Stage stage;
    private final Skin skin;
    private final TextField chatInput;
    private final TextButton sendButton;
    private boolean isChatVisible = false;
    private final NetworkManager networkManager;
    private final int playerID;

    public ChatComponent(NetworkManager networkManager, int playerID) {
        this.networkManager = networkManager;
        this.playerID = playerID;
        
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("blade-encore/assets/lgdxs/skin/lgdxs-ui.json"));
    
        Table table = new Table();
        table.bottom().left(); // Move chat to bottom-left
        table.setFillParent(true);
    
        chatInput = new TextField("", skin);
        chatInput.setMessageText("");
        chatInput.setMaxLength(200); // Optional: Prevent long messages
    
        sendButton = new TextButton("Send", skin);
        sendButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                sendChatMessage();
            }
        });
    
        // Adjust padding & spacing for alignment
        table.pad(10).add(chatInput).width(300).padRight(5);
        table.add(sendButton).width(80);
        stage.addActor(table);
    
        Gdx.input.setInputProcessor(stage); // Set input focus to the chat UI
    }
    

    public void toggleChat() {
        isChatVisible = !isChatVisible;
        if (isChatVisible) {
            Gdx.input.setInputProcessor(stage);
        } else {
            Gdx.input.setInputProcessor(null); // Restore normal input when chat closes
        }
    }

    private void sendChatMessage() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty()) {
            NetworkPacket chatPacket = new NetworkPacket(playerID);
            chatPacket.put("chatMessage", message);
            networkManager.sendData(chatPacket);

            chatInput.setText(""); // Clear input after sending
            toggleChat(); // Hide chat after sending message
        }
    }

    public void render() {
        if (isChatVisible) {
            stage.act(Gdx.graphics.getDeltaTime());
            stage.draw();
        }
    }

    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
