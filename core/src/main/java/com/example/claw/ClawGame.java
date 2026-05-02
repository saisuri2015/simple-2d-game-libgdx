package com.example.claw;

import com.badlogic.gdx.Game;

public class ClawGame extends Game {
    @Override
    public void create() {
        setScreen(new GameScreen());
    }
}
