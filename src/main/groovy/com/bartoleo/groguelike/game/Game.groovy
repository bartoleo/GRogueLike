package com.bartoleo.groguelike.game

class Game {
    public static GameState state = GameState.playing
    public static int gameTurn = 1


    public static passTime() {
        gameTurn++
    }

}
