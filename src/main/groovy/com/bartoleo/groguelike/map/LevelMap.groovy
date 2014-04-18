package com.bartoleo.groguelike.map

import com.bartoleo.groguelike.entity.Entity
import com.bartoleo.groguelike.game.Game
import com.bartoleo.groguelike.graphic.RenderConfig
import squidpony.squidcolor.SColor
import squidpony.squidcolor.SColorFactory
import squidpony.squidgrid.gui.swing.SwingPane

class LevelMap {

    public int xSize
    public int ySize

    public Tile[][] ground
    public ArrayList<Entity> objects


    public LevelMap(int x, int y) {
        xSize = x
        ySize = y

        ground = new Tile[x][y]
        objects = new ArrayList<Entity>()
    }

    public boolean isBlocked(int x, int y) {

        if (x < 0 || x >= xSize || y < 0 || y >= ySize) {
            return true
        }

        if (ground[x][y] && ground[x][y].isBlocked) {
            return true
        }

        for (Entity entity : objects) {
            if (entity.x == x && entity.y == y && entity.blocks)
                return true
        }

        return false
    }

    public List<Entity> getEntitiesAtLocation(int x, int y) {
        return objects.findAll { it.x == x && it.y == y }
    }

    /**
     * Performs the Field of View process
     *
     * @param startx
     * @param starty
     */
    public void render(params) {

        SwingPane display = params.display
        int viewX = params.viewX ?: params.player.x
        int viewY = params.viewY ?: params.player.y
        Entity player = params.player

        //first we figure out where the real translation center is
        int worldLowX = viewX - RenderConfig.windowRadiusX //low is upper left corner
        int worldHighX = viewX + RenderConfig.windowRadiusX

        if (worldLowX<0){
            worldHighX = worldHighX-worldLowX
            worldLowX = 0
        }

        int worldLowY = viewY - RenderConfig.windowRadiusY
        int worldHighY = viewY + RenderConfig.windowRadiusY

        int xRange = worldHighX - worldLowX + 1 // this is the total size of the box
        int yRange = worldHighY - worldLowY + 1

        //player.ai.calculateSight()

        //repaint the level with new light map -- Note that in normal use you'd limit this to just elements that changed
        for (int x = 0; x < xRange; x++) {
            for (int y = 0; y < yRange; y++) {
                int originalX = x + worldLowX
                int originalY = y + worldLowY

                if (originalX < 0 || originalX >= ground.length || originalY < 0 || originalY >= ground[0].length) {
                    display.clearCell(x, y); //off the map

                } else  {
                    display.placeCharacter(x, y, ground[originalX][originalY].representation, SColor.DARK_GRAY)
                    //display.clearCell(x, y);
                }
            }
        }
        xRange.times { x ->
            display.placeCharacter(x, yRange, '-' as char, SColor.DARK_GRAY)
        }
        yRange.times { y ->
            display.placeCharacter(xRange, y, '|' as char, SColor.DARK_GRAY)
        }
        display.placeCharacter(xRange, yRange, '+' as char, SColor.DARK_GRAY)

//
        objects.each { Entity entity ->

            int screenPositionX = entity.x - worldLowX
            int screenPositionY = entity.y - worldLowY

            if (screenPositionX >= 0 && screenPositionX < xRange && screenPositionY >= 0 && screenPositionY < yRange) {
                //put the player at the origin of the FOV
                float bright = 1;
                SColor cellLight = SColorFactory.fromPallet("light", bright);
                SColor objectLight = SColorFactory.blend(entity.color, cellLight, getTint(0f));
                display.placeCharacter(screenPositionX, screenPositionY, entity.ch, objectLight);
            }
        }

    }

    /**
     * Custom method to determine tint based on radius as well as general tint
     * factor.
     *
     * @param radius
     * @return
     */
    private float getTint(double radius) {
        return (float) (0f + RenderConfig.lightTintPercentage * radius);//adjust tint based on distance
    }

    public generate(){
        for (int x = 0; x < xSize; x++) {
            for (int y = 0; y < ySize; y++) {
                if (x==0||x==xSize-1||y==0||y==ySize-1){
                    ground[x][y] = new Tile(true, 1f, (char)'#', SColor.WHITE)
                } else {
                    ground[x][y] = new Tile(false, 1f, (char)' ', SColor.WHITE)
                }
            }
        }
    }


}
