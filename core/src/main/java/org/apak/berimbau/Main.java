package org.apak.berimbau;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

import de.eskalon.commons.core.ManagedGame;
import de.eskalon.commons.screen.ManagedScreen;
import de.eskalon.commons.screen.transition.ScreenTransition;

import org.apak.berimbau.scenes.CharacterSelectScreen;
import org.apak.berimbau.scenes.MainMenu;
import org.apak.berimbau.scenes.ScreenType;



/** {@link com.badlogic.gdx.Game} implementation shared by all platforms. */
public class Main extends ManagedGame<ManagedScreen, ScreenTransition> {
    @Override
    public void create() {
        super.create();
        this.screenManager.pushScreen(new MainMenu(this.screenManager), null);
    }



    @Override
    public void render() {
        super.render(); // Delegate to current Screen
    }

    @Override
    public void dispose() {
        this.screenManager.dispose(); // Dispose current Screen when application closes
    }
}
