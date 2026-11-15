"""Campus_Trees: trees cut from the sheets, purple (magenta-blended) shadows turned into translucent dark shadows,
sized by type, trunk base centred on a tile column; bottom trunk tile = solid, rest = above-player."""
import sys, json, math, numpy as np
from PIL import Image
sys.path.insert(0, "."); from treelib import *
OUT = sys.argv[1]; COLS = 16; T = 16
SHADOW = (14, 28, 20, 110)
SHEETS = [("t1", None, ["english_oak", "copper_beech", "horse_chestnut", "london_plane", "weeping_willow", "sycamore"],
           [64] * 6),
          ("ta", 6, ["scots_pine", "spruce", "yew", "holly", "cedar", "young_pine",
                     "cherry_blossom", "magnolia", "rowan", "silver_birch", "japanese_maple", "laburnum",
                     "blossom_tree", "summer_tree", "autumn_yellow", "autumn_orange", "winter_bare", "snowy_evergreen"],
           [48, 40, 56, 32, 64, 28, 56, 48, 36, 32, 48, 48, 48, 48, 48, 48, 48, 48]),
          ("tb", 6, ["scots_pine", "spruce", "yew", "holly", "cedar", "young_pine",
                     "cherry_blossom", "magnolia", "rowan", "silver_birch", "japanese_maple", "DROP_lilac",
                     "white_blossom_tree", "summer_tree", "autumn_yellow", "autumn_orange", "winter_bare", "snowy_evergreen"],
           [48, 40, 56, 32, 64, 28, 56, 48, 36, 32, 48, 48, 48, 48, 48, 48, 48, 48])]
trees = []
for sheet, skip, names, widths in SHEETS:
    a, mg, _ = load_trees(f"in/{sheet}.png"); objs, lab = objects(mg)
    objs = objs[skip:] if skip else objs
    assert len(objs) == len(names), (sheet, len(objs))
    suffix = "" if sheet == "t1" else ("_a" if sheet == "ta" else "_b")
    for (s, l), name, wt in zip(objs, names, widths):
        mask = (lab[s] == l) & (mg[s] < 60); rgb = a[s].copy(); h, w = mask.shape
        R, G, B = rgb[..., 0], rgb[..., 1], rgb[..., 2]
        bottom = np.zeros_like(mask); bottom[int(h * 0.78):] = True
        shadow = mask & bottom & (R - G > 20) & (B - G > 20) & (rgb.mean(-1) < 130)
        tree = mask & ~shadow
        sc = w / wt; tw, th = max(1, round(w / sc)), max(1, round(h / sc))
        col, m = shrink_mode(rgb, tree, tw, th, k=24); spr = finish(col, m)
        # shadow: box-shrink coverage, keep only where the tree itself is transparent
        sh = np.asarray(Image.fromarray((shadow * 255).astype(np.uint8)).resize((tw, th), Image.BOX)) > 100
        sh &= spr[..., 3] == 0
        spr[sh] = SHADOW
        # trunk base centre: opaque tree pixels in the lowest rows above the shadow
        al = spr[..., 3] == 255; rows = np.nonzero(al.any(1))[0]; low = rows.max()
        band = al[max(0, low - max(3, th // 10)):low + 1]
        cols = np.nonzero(band.any(0))[0]; bx = (cols.min() + cols.max() + 1) / 2
        left_t = max(0, math.ceil((bx - 8) / 16)); right_t = max(0, math.ceil((tw - bx - 8) / 16))
        W = 1 + left_t + right_t; H = math.ceil(th / 16)
        canvas = np.zeros((H * 16, W * 16, 4), np.uint8)
        ox = int(round(left_t * 16 + 8 - bx)); oy = H * 16 - th
        x0 = max(0, ox); sx0 = x0 - ox; x1 = min(W * 16, ox + tw)
        canvas[oy:oy + th, x0:x1] = spr[:, sx0:sx0 + (x1 - x0)]
        if not name.startswith("DROP"): trees.append((name + suffix, sheet, W, H, left_t, canvas))
# skyline pack, biggest first so rows fill well
trees.sort(key=lambda t: (-t[3], -t[2]))
sky = [0] * COLS; placed = []
for name, sheet, W, H, pc, canvas in trees:
    y, x = min((max(sky[x:x + W]), x) for x in range(COLS - W + 1))
    for i in range(x, x + W): sky[i] = y + H
    placed.append((name, sheet, W, H, pc, canvas, x, y))
rows = max(sky); img = Image.new("RGBA", (COLS * T, rows * T), (0, 0, 0, 0)); index = []
for name, sheet, W, H, pc, canvas, x, y in placed:
    img.alpha_composite(Image.fromarray(canvas), (x * T, y * T))
    ids = [(y + r) * COLS + x + c for r in range(H) for c in range(W)]
    trunk = (y + H - 1) * COLS + x + pc
    index.append({"tile_id": ids[0], "name": name, "tile_x": x, "tile_y": y, "w": W, "h": H, "trunk_column": pc,
                  "tile_ids": ids, "solid_tiles": [trunk], "above_tiles": [i for i in ids if i != trunk]})
img.save(f"{OUT}/Campus_Trees.png"); json.dump(index, open(f"{OUT}/Campus_Trees_index.json", "w"), indent=1)
open(f"{OUT}/Campus_Trees.tsx", "w").write(
    f'<?xml version="1.0" encoding="UTF-8"?>\n<tileset version="1.10" tiledversion="1.12.2" name="Campus_Trees" tilewidth="16" tileheight="16" '
    f'tilecount="{COLS*rows}" columns="{COLS}">\n <image source="Campus_Trees.png" width="{COLS*T}" height="{rows*T}"/>\n</tileset>\n')
print(len(placed), "trees,", rows, "rows")
for name, sheet, W, H, pc, *_ in placed[:6]: print(" ", name, f"{W}x{H}", "trunk col", pc)
