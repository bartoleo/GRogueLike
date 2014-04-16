package com.bartoleo.groguelike.main

import com.bartoleo.groguelike.entity.Entity
import com.bartoleo.groguelike.game.Game
import com.bartoleo.groguelike.game.GameState
import com.bartoleo.groguelike.graphic.MessageLog
import com.bartoleo.groguelike.graphic.RenderConfig
import com.bartoleo.groguelike.graphic.StatusBar
import com.bartoleo.groguelike.input.CharacterInputListener
import com.bartoleo.groguelike.map.LevelMap
import squidpony.squidcolor.SColor
import squidpony.squidcolor.SColorFactory
import squidpony.squidgrid.fov.BasicRadiusStrategy
import squidpony.squidgrid.fov.EliasLOS
import squidpony.squidgrid.fov.TranslucenceWrapperFOV
import squidpony.squidgrid.gui.awt.event.SGMouseListener
import squidpony.squidgrid.gui.swing.SwingPane
import squidpony.squidgrid.util.Direction

import javax.swing.*
import javax.swing.event.MouseInputListener
import java.awt.*

public class Main {


    public SwingPane display
    public JFrame frame
    public LevelMap levelMap
    public Entity player

    public int selectX = 0
    public int selectY = 0


    public static void main(String[] args) {
        Main helloDungeon = new Main()
    }

    public Main() {

        // Setup window
        RenderConfig.fov = new TranslucenceWrapperFOV()
        RenderConfig.los = new EliasLOS()
        RenderConfig.strat = BasicRadiusStrategy.CIRCLE
        SColorFactory.addPallet("light", SColorFactory.asGradient(RenderConfig.litNear, RenderConfig.litFarDay));
        SColorFactory.addPallet("dark", SColorFactory.asGradient(RenderConfig.litNear, RenderConfig.litFarNight));

        // Generate map
        //MapGenerator mapGen = new CityMapGenerator()

        //levelMap = mapGen.reGenerate()

        player = new Entity()

        // set up display
        frame = new JFrame("Groovy RogueLike")
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        frame.setLayout(new BorderLayout())

        display = new SwingPane()
        display.initialize(RenderConfig.screenWidth, RenderConfig.screenHeight, new Font("Ariel", Font.BOLD, 12))
        clear(display)

        frame.add(display, BorderLayout.SOUTH)
        frame.setVisible(true)

        frame.pack()
        frame.setLocationRelativeTo(null)
        frame.repaint()

        frame.requestFocusInWindow()

        CharacterInputListener dil = new CharacterInputListener(this, player)

        int cellWidth = display.getCellDimension().width
        int cellHeight = display.getCellDimension().height
        MouseInputListener mil = new SGMouseListener(cellWidth, cellHeight, dil)
        display.addMouseListener(mil) //listens for clicks and releases
        display.addMouseMotionListener(mil) //listens for movement based events
        frame.addKeyListener(dil)

        render()

        Game.state = GameState.playing

    }


    public void render() {


        if (Game.state == GameState.selecting) {
            levelMap.render(display: display, player: player, viewX: selectX, viewY: selectY)
        } else {
            levelMap.render(display: display, player: player)
        }

        //render stats
        StatusBar.render(display, 0, (2 * RenderConfig.windowRadiusY) + 2, 10, 'hp', player?.fighter?.hp ?: 0, player?.fighter?.maxHP ?: 1, SColor.RED)
        StatusBar.render(display, 12, (2 * RenderConfig.windowRadiusY) + 2, 10, 'sta', player?.fighter?.stamina ?: 0, player?.fighter?.maxStamina ?: 1, SColor.YELLOW)
        StatusBar.render(display, 24, (2 * RenderConfig.windowRadiusY) + 2, 10, 'inf', player?.fighter?.infection ?: 0, player?.fighter?.maxInfection ?: 1, SColor.GREEN)

        MessageLog.render(display, player)

        (0..RenderConfig.surroundingWidth).each { x ->
            (0..RenderConfig.surroundingHeight).each { y ->
                display.clearCell(x + RenderConfig.surroundingX, y + RenderConfig.surroundingY)
            }
        }
        def names = (levelMap.getEntitiesAtLocation(player.x, player.y) - player).sort { Entity entity -> entity.priority }.name

        names.eachWithIndex { String name, int i ->
            if (i < RenderConfig.surroundingHeight) {
                display.placeHorizontalString(RenderConfig.surroundingX, RenderConfig.surroundingY + i, name)
            }
        }

        //done rendering this frame
        display.refresh();
    }


    public void clear(SwingPane display) {
        for (int x = 0; x < RenderConfig.screenWidth; x++) {
            for (int y = 0; y < RenderConfig.screenHeight; y++) {
                display.clearCell(x, y)
            }
        }
        display.refresh()
    }

    public void stepSim() {
        //Run sim
        levelMap.noiseMap.spread()
        levelMap.noiseMap.fade()
        levelMap.noiseMap.regenerateDirection()

        //This is not an efficient way to do this..
        levelMap.objects.toArray().each { Entity entity ->
            if (entity.ai)
                entity.ai.takeTurn()
        }

        levelMap.objects.sort({ it.priority })
        render()
        Game.passTime()
    }


    public void move(Direction dir, boolean shift = false) {
        if (Game.state == GameState.playing) {

            if (shift && player.fighter.stamina) {
                player.fighter.stamina--
                int x = player.x + dir.deltaX
                int y = player.y + dir.deltaY

                //check for legality of move based solely on map boundary
                if (x >= 0 && x < levelMap.xSize && y >= 0 && y < levelMap.ySize) {
                    player.moveOrAttack(dir.deltaX, dir.deltaY)
                }
            }

            int x = player.x + dir.deltaX
            int y = player.y + dir.deltaY

            //check for legality of move based solely on map boundary
            if (x >= 0 && x < levelMap.xSize && y >= 0 && y < levelMap.ySize) {
                player.moveOrAttack(dir.deltaX, dir.deltaY)
                stepSim()
            }

        } else if (Game.state == GameState.selecting) {
            selectX += dir.deltaX
            selectY += dir.deltaY
            render()
        }
    }

    public void fire() {
        if (Game.state == GameState.playing) {
            MessageLog.send("You can't fire... function not implemented yet!.", SColor.RED, [player])
            render()
        }
    }

    public void grab() {
        if (Game.state == GameState.playing) {
            //To change body of created methods use File | Settings | File Templates.
            player.grab()
            stepSim()
        }

    }

    public void standStill() {
        if (Game.state == GameState.playing) {
            stepSim()
        }
    }

    public void drop() {
        if (Game.state == GameState.playing) {
            player.drop()
            stepSim()
        }
    }

    public void useItem(int id) {
        if (Game.state == GameState.playing) {
            if (player.inventory.useById(id - 1)) {
                stepSim()
            } else {
                render()
            }
        }
    }

    private def oldState

    public void inspect() {

        if (Game.state != GameState.selecting) {
            oldState = Game.state
            Game.state = GameState.selecting
            selectX = player.x
            selectY = player.y
            display.setCellBackground(RenderConfig.windowRadiusX, RenderConfig.windowRadiusY, SColor.EDO_BROWN)
            render()

        } else {
            Game.state = oldState
            display.setCellBackground(RenderConfig.windowRadiusX, RenderConfig.windowRadiusY, SColor.BLACK)
            render()

        }
    }

    public void reload() {
        //doesnt do anything
    }
}