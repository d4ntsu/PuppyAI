# Mascot spritesheet guide

The animator slices the sheet into exact cells and moves a viewport from frame to
frame. It cannot correct artwork that is drawn in the wrong place, which is why
the current sheet wobbles and why the TALKING row shows paws from the row above.

- **Shaking:** every frame draws the dog at a slightly different offset or size,
  so the viewport shows it jumping between cells.
- **Bleed from the row above:** parts of the art cross the 68 px row boundary.
  Once sliced, a neighbouring row's paws appear inside the TALKING frames.
- **Clipped bubbles:** `?`, `!`, speech bubbles and `Zzz` reach the cell edges,
  so they get cut in half or pushed into the next frame.

All three are fixed by drawing on a fixed anchor grid with a clean gap around
every frame.

## The grid to follow

Recommended sheet: **768 × 768 px, 6 columns × 6 rows, 128 × 128 px cells.**

| Value | Where |
| --- | --- |
| Sheet size | 768 × 768 px |
| Cell size | 128 × 128 px |
| Columns | 6 frames per animation |
| Rows | 6 states, one per row |
| Safe area | 16 px inside every cell (96 × 96 px box) |
| Ground line | `y = 112` inside the cell (16 px above the cell's bottom edge) |
| Body centre | `x = 64` inside the cell |
| Empty margin | the bottom 16 px of the cell (it holds the template labels) |

`docs/spritesheet-guide.png` is a ready-to-trace template of exactly this grid:
magenta lines are cell borders, dashed cyan lines are the safe area and centre,
green is the ground line, and the small labels show the state and frame number.

**General rule for any size:** `inset = cellSize / 8`, ground line =
`cellSize - inset`, body centre = `cellSize / 2`, safe area = the inset box.
For 128 px cells that gives 16 / 112 / 64, as above. The animator reads the cell
size from the image, so a 768 × 768 sheet works with no code changes.

## The rules

1. **One dog per cell, same anchor.** Feet flat on the green ground line, body
   mass centred on the cyan centre line. Do not re-centre each frame by eye.
2. **Same scale everywhere.** The dog's head width should be identical (±1 px)
   in all 36 frames; a frame drawn larger will look like a zoom pop.
3. **Stay inside the dashed safe box.** Antennae, ears, tails, bubbles, `?`,
   `!`, `Zzz`, sparkles and motion lines all stay within it. The bottom 16 px
   margin stays empty.
4. **Effects hold their position.** Within a row, put `?` or the speech bubble in
   the same corner in every frame, so it does not jump around.
5. **Talk with the mouth, not the body.** In the TALKING row the head and body
   stay on their anchor; only the mouth (and maybe one ear) changes.
6. **Small, consistent hops are allowed.** IDLE and HAPPY may bob 2–4 px, but the
   whole body moves together and never leaves the safe box. THINKING, TALKING,
   STUDYING and SLEEPING keep the feet on the ground line.
7. **Frame 6 must loop to frame 1.** No large pose change at the wrap point.
8. **Transparent background, no grid lines.** Export a PNG with an alpha channel;
   the template lines must not be in the final file.

## Row order

| Row | State | What moves | Notes |
| --- | --- | --- | --- |
| 1 | IDLE | blink, tail, small bob, tongue | Sparkle in the last frame, top-right |
| 2 | HAPPY | ears, tail, hop, motion lines | Motion lines stay inside the safe box |
| 3 | THINKING | ears, `?` bubbles, then `!` / bulb | `?` in the same top-right spot each frame |
| 4 | TALKING | mouth only | Body and head absolutely still |
| 5 | STUDYING | eyes, page turn, book | Book stays in the same position in the paws |
| 6 | SLEEPING | breathing, ears, `Zzz` | `Zzz` drifts up-right inside the safe box |

## How to build the sheet

Aseprite / GIMP / Krita / Paint.NET all work:

1. New image, **768 × 768**, transparent background.
2. Add `docs/spritesheet-guide.png` as a layer on top, lock it, and set its
   opacity to about 40 % so you can see the guides over your art.
3. Turn on the pixel grid: cell size **128 × 128** (Aseprite: *Grid → Grid
   Settings*; GIMP: *Image → Configure Grid*), and enable snapping.
4. Draw or paste one frame into each cell, aligned to the guides. If you reuse
   your existing art, scale each dog to the same height (about 90 px from feet
   to head top) and place it once, then copy it into every cell and change only
   the moving parts. That alone removes the shaking.
5. Hide the template layer, export as PNG (RGBA), and overwrite
   `src/main/resources/sprites/mascot_spritesheet.png`.
6. Run the app and use right-click → **State → …** to check each row:
   - the dog should sit perfectly still in TALKING and STUDYING
   - no fragments of another row may appear at the top of any cell
   - bubbles and `Zzz` must stay whole
   - the last frame should flow back into the first without a jolt

## Prompt for an image generator

If you regenerate the sheet with an AI, give it the layout explicitly:

> A 6×6 sprite sheet on a 768×768 transparent canvas, six equal 128×128 cells
> per row, no grid lines or labels. Column 1 must start at x = 0 and each cell
> is exactly 128 px wide; row 1 starts at y = 0 and each row is exactly 128 px
> tall. In every cell draw the same small cartoon puppy at the same size,
> centred horizontally in the cell, with its paws on a common ground line
> 16 px above the cell's bottom edge, and with every part of the puppy and every
> effect kept at least 16 px inside the cell edges. Row 1: idle loop. Row 2:
> happy tail wag. Row 3: thinking with a question mark that turns into a
> lightbulb. Row 4: talking, only the mouth changes. Row 5: studying a book.
> Row 6: sleeping with Zzz. Do not move or rescale the body between frames.

Generators often ignore exact grid instructions. The reliable route is to let
one generate poses, then place them yourself using the template.

## Checking and troubleshooting

The build test `MascotSpritesTest` verifies that the sheet still divides into
6 × 6 and that every cell actually contains artwork. It cannot see alignment, so
check these by eye:

| Symptom | Cause | Fix |
| --- | --- | --- |
| Dog shakes in place | Frames drawn at different offsets | Align every frame to the centre and ground lines |
| Legs/paws from another animation | Art crossed a cell border | Keep 16 px inset; repaint the overflowing pixels |
| Bubble cut in half | Effect outside the safe box | Move/shrink it inside the dashed box |
| Dog changes size between frames | Inconsistent scale | Scale all frames to one head width |
| Visible jump at loop | Frame 6 ≠ approach to frame 1 | Make frame 6 a near-copy of frame 1 |
| Blank/black frame | Cell left empty | Fill all 36 cells (the test catches this) |
| Dog looks smaller than before | Square cells are shown at 136 px tall | Crop tighter inside the safe box, or ask for the display size to be raised |

Cell size must be a whole number of pixels and the sheet must stay exactly
6 columns × 6 rows; the tests fail otherwise.
