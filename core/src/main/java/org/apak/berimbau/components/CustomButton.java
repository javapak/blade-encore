package org.apak.berimbau.components;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

    public class CustomButton extends Actor {
        private ShapeRenderer shapeRenderer;
        private BitmapFont font;
        private GlyphLayout layout;
    
        private float shadowOffset = 4;
        private float foldSize = 10;
        private String text;
        private Color activeTextColorRef = Color.WHITE;
        private Color activeBackgroundColorRef = Color.BLACK;
        private Color activeDropShadowColorRef = Color.DARK_GRAY;
        private Color activeAccentColorRef = Color.WHITE;
        private float textScale = 0.4f;
    
        private float baseWidth, baseHeight; // Store original size
    
        public CustomButton(String text, float width, float height) {
            this.text = text;
            this.baseWidth = width;  // Store original width
            this.baseHeight = height; // Store original height
    
            shapeRenderer = new ShapeRenderer();
            font = new BitmapFont(Gdx.files.internal("blade-encore/assets/blade-encore-ui/blade-encore.fnt"));
            font.getData().setScale(textScale);
            font.getRegion().getTexture().setFilter(
                Texture.TextureFilter.Linear, 
                Texture.TextureFilter.Linear
            );
            layout = new GlyphLayout();
            layout.setText(font, text);
            
            setSize(baseWidth, baseHeight); // Set size based on stored width & height

            
    
            // Add input listener
            this.addListener(new InputListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    CustomButton target = (CustomButton) (event.getTarget());
                    target.activeBackgroundColorRef = Color.WHITE;
                    target.activeTextColorRef = Color.BLACK;
                    target.activeDropShadowColorRef = Color.BLACK;
                    target.activeAccentColorRef = Color.BLACK;
                }
    
                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    CustomButton target = (CustomButton) (event.getTarget());
                    target.activeBackgroundColorRef = Color.BLACK;
                    target.activeTextColorRef = Color.WHITE;
                    target.activeDropShadowColorRef = Color.DARK_GRAY;
                    target.activeAccentColorRef = Color.WHITE;
                }
    
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    CustomButton target = (CustomButton) (event.getTarget());
                    target.layout.width -= shadowOffset;
                    target.setX(target.getX() + shadowOffset);
                    return true;
                }
    
                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    CustomButton target = (CustomButton) (event.getTarget());
                    target.layout.width += shadowOffset;
                    target.setX(target.getX() - shadowOffset);
                }
            });
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            batch.end(); // End batch before ShapeRenderer

            float x = getX();
            float y = getY();
            float width = getWidth();
            float height = getHeight();

            shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix()); // ✅ Fix projection

            // ✅ Draw shadow rectangle
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(activeDropShadowColorRef);
            shapeRenderer.rect(x + shadowOffset, y - shadowOffset, width, height);
            shapeRenderer.end();

            // ✅ Draw main button rectangle
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(activeBackgroundColorRef);
            shapeRenderer.rect(x, y, width, height);
            shapeRenderer.end();

            // ✅ Draw right triangle accent
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(activeAccentColorRef);
            shapeRenderer.triangle(
            x + width - 5, y + height - 5,
            x + width - 5 - foldSize, y + height - 5,
            x + width - 5, y + height - 5 - foldSize
            );
            shapeRenderer.end();

            batch.begin(); // Restart batch for text

            // ✅ Centered text
            layout.setText(font, text);
            float textX = x + (width - layout.width) / 2;
            float textY = y + (height + layout.height) / 2;
            font.setColor(activeTextColorRef);
            font.getData().setScale(textScale); // ✅ Apply dynamic text scaling
            font.draw(batch, layout, textX, textY);
        }

        public void updateSize(float screenWidth, float screenHeight) {
            // Scale button size based on original resolution
            float newWidth = screenWidth * (baseWidth / 1920f);  
            float newHeight = screenHeight * (baseHeight / 1080f); 
        
            // Recenter button
            float newX = (screenWidth - newWidth) / 2;  
            float newY = (screenHeight - newHeight) / 2; 
        
            setBounds(newX, newY, newWidth, newHeight);
        
            // Scale text proportionally
            textScale = Math.min(newWidth / baseWidth, newHeight / baseHeight);
            font.getData().setScale(textScale);
        
            layout.setText(font, text);
        }    
    }
    
