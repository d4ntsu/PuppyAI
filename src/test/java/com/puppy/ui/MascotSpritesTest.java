package com.puppy.ui;

import com.puppy.enums.MascotState;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the mascot artwork contract: the spritesheet must divide into the grid
 * the animator expects, and every state must own one row with real artwork.
 */
class MascotSpritesTest {

    @Test
    void everyStateUsesItsOwnRow() {
        Set<Integer> rows = new HashSet<>();
        for (MascotState state : MascotState.values()) {
            MascotSprites.Animation animation = MascotSprites.animationFor(state);
            assertEquals(MascotSprites.COLUMNS, animation.frameCount(),
                    "Expected every row of the sheet to be used: " + state);
            assertTrue(rows.add(animation.row()),
                    "Two states share a spritesheet row: " + state);
        }
        assertEquals(MascotSprites.ROWS, rows.size(),
                "The sheet declares " + MascotSprites.ROWS + " rows but states mapped " + rows);
    }

    @Test
    void spritesheetDividesIntoTheFrameGrid() throws IOException {
        BufferedImage sheet = loadSheet();
        assertEquals(0, sheet.getWidth() % MascotSprites.COLUMNS,
                "Spritesheet width must divide into " + MascotSprites.COLUMNS + " frames");
        assertEquals(0, sheet.getHeight() % MascotSprites.ROWS,
                "Spritesheet height must divide into " + MascotSprites.ROWS + " rows");
    }

    @Test
    void everyFrameCellContainsArtwork() throws IOException {
        BufferedImage sheet = loadSheet();
        int frameWidth = sheet.getWidth() / MascotSprites.COLUMNS;
        int frameHeight = sheet.getHeight() / MascotSprites.ROWS;

        for (int row = 0; row < MascotSprites.ROWS; row++) {
            for (int column = 0; column < MascotSprites.COLUMNS; column++) {
                assertTrue(cellHasVisiblePixels(sheet, column * frameWidth, row * frameHeight,
                                frameWidth, frameHeight),
                        "Empty sprite cell at row " + row + ", column " + column);
            }
        }
    }

    @Test
    void iconIsPackaged() throws IOException {
        try (InputStream stream = MascotSpritesTest.class.getResourceAsStream(MascotSprites.ICON_RESOURCE)) {
            assertNotNull(stream, "Missing application icon: " + MascotSprites.ICON_RESOURCE);
            assertNotNull(ImageIO.read(stream), "Application icon is not a readable image");
        }
    }

    private static BufferedImage loadSheet() throws IOException {
        try (InputStream stream = MascotSpritesTest.class.getResourceAsStream(MascotSprites.SHEET_RESOURCE)) {
            assertNotNull(stream, "Missing mascot spritesheet: " + MascotSprites.SHEET_RESOURCE);
            BufferedImage sheet = ImageIO.read(stream);
            assertNotNull(sheet, "Mascot spritesheet is not a readable image");
            return sheet;
        }
    }

    private static boolean cellHasVisiblePixels(BufferedImage sheet,
                                                int x, int y, int width, int height) {
        for (int py = y; py < y + height; py++) {
            for (int px = x; px < x + width; px++) {
                if ((sheet.getRGB(px, py) >>> 24) != 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
