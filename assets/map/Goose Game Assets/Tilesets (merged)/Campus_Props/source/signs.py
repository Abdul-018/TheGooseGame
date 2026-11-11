"""Building name signs: cut, shrink to a 4x3-tile footprint, find the blank panel, letter the name in a 3x5 pixel font.
Appends lettered + blank versions to Campus_Props (existing tiles untouched)."""
import sys, json, numpy as np
from PIL import Image
from scipy import ndimage
P, D, OUT = sys.argv[1:4]
sys.argv = ["x", P, D, OUT]
src = open(f"{P}/build_more.py").read(); exec(src[:src.index("specs = json.load")])
F = {"A": [".#.","#.#","###","#.#","#.#"], "B": ["##.","#.#","##.","#.#","##."], "C": [".##","#..","#..","#..",".##"],
     "D": ["##.","#.#","#.#","#.#","##."], "E": ["###","#..","##.","#..","###"], "F": ["###","#..","##.","#..","#.."],
     "G": [".##","#..","#.#","#.#",".##"], "H": ["#.#","#.#","###","#.#","#.#"], "I": ["#","#","#","#","#"],
     "J": ["..#","..#","..#","#.#",".#."], "K": ["#.#","#.#","##.","#.#","#.#"], "L": ["#..","#..","#..","#..","###"],
     "M": ["#...#","##.##","#.#.#","#...#","#...#"], "N": ["#..#","##.#","#.##","#..#","#..#"], "O": [".#.","#.#","#.#","#.#",".#."],
     "P": ["##.","#.#","##.","#..","#.."], "Q": [".#.","#.#","#.#","##.",".##"], "R": ["##.","#.#","##.","#.#","#.#"],
     "S": [".##","#..",".#.","..#","##."], "T": ["###",".#.",".#.",".#.",".#."], "U": ["#.#","#.#","#.#","#.#","###"],
     "V": ["#.#","#.#","#.#","#.#",".#."], "W": ["#...#","#...#","#.#.#","##.##","#...#"], "X": ["#.#","#.#",".#.","#.#","#.#"],
     "Y": ["#.#","#.#",".#.",".#.",".#."], "Z": ["###","..#",".#.","#..","###"], " ": ["..","..","..","..",".."]}
def text_mask(s):
    cols = []
    for i, ch in enumerate(s):
        g = F[ch]; w = len(g[0])
        for x in range(w): cols.append([g[y][x] == "#" for y in range(5)])
        if i < len(s) - 1: cols.append([False] * 5)
    return np.array(cols, bool).T                     # 5 x W
SIGNS = [  # name, lines, text colour, panel seed (fraction of sprite w, h)
 ("heslington_hall", ["HESLINGTON", "HALL"], (0x7d, 0x38, 0x33), (.5, .55)),
 ("central_hall", ["CENTRAL", "HALL"], (0x2c, 0x35, 0x4d), (.5, .45)),
 ("ron_cooke_hub", ["RON COOKE", "HUB"], (0xf5, 0xff, 0xe8), (.55, .45)),
 ("ian_wand", ["IAN WAND", "BUILDING"], (0x40, 0x49, 0x73), (.5, .55)),
 ("jb_morrell_library", ["JB MORRELL", "LIBRARY"], (0x52, 0x33, 0x3f), (.5, .62)),
 ("derwent_college", ["DERWENT", "COLLEGE"], (0xf5, 0xff, 0xe8), (.5, .65)),
 ("piazza_building", ["PIAZZA", "BUILDING"], (0x5b, 0x2a, 0x86), (.5, .5)),
 ("university_of_york", ["UNIVERSITY", "OF YORK"], (0xf0, 0xb5, 0x41), (.5, .55))]
VCENTRE = {"derwent_college": 0.53}
a, mg = load(f"{P}/in/building_signs.png"); objs, lab = objects(mg)
assert len(objs) == 8, len(objs)
TW, TH = 4, 3; out = []; report = []
for (s, l), (name, lines, tc, seed) in zip(objs, SIGNS):
    mask = (lab[s] == l) & (mg[s] < 60); rgb = a[s]; h, w = mask.shape
    sc = max(w / (TW * 16), h / (TH * 16)); tw, th = round(w / sc), round(h / sc)
    col, m = shrink_mode(rgb, mask, tw, th, k=20); spr = finish(col, m)
    # panel = flood region of similar colour around the seed
    sx, sy = int(seed[0] * tw), int(seed[1] * th); c0 = spr[sy, sx, :3].astype(int)
    sim = (np.abs(spr[..., :3].astype(int) - c0).sum(-1) < 90) & (spr[..., 3] > 0)
    lab2, _ = ndimage.label(ndimage.binary_closing(sim, iterations=1)); reg = lab2 == lab2[sy, sx]
    ys, xs = np.nonzero(reg); px0, px1, py0, py1 = xs.min(), xs.max() + 1, ys.min(), ys.max() + 1
    blank = spr.copy()
    tm = [text_mask(t) for t in lines]; tot_h = 5 * len(tm) + 2 * (len(tm) - 1)
    pw, ph = px1 - px0, py1 - py0; ty = py0 + (ph - tot_h) // 2
    if name in VCENTRE: ty = int(VCENTRE[name] * th) - tot_h // 2
    fits = all(t.shape[1] <= pw - 2 for t in tm) and tot_h <= ph
    for t in tm:
        tx = (tw - t.shape[1]) // 2            # signs are symmetric: centre on the sign
        reg_ = spr[ty:ty + 5, tx:tx + t.shape[1]]; reg_[t, :3] = tc; reg_[t, 3] = 255; ty += 7
    report.append(f"{name:20s} sprite {tw}x{th} panel {pw}x{ph} text {max(t.shape[1] for t in tm)}x{tot_h} {'OK' if fits else 'TOO BIG'}")
    for nm, im in ((f"sign_{name}", spr), (f"sign_{name}_blank", blank)):
        canvas = np.zeros((TH * 16, TW * 16, 4), np.uint8); cx = (TW * 16 - tw) // 2; cy = TH * 16 - th
        canvas[cy:cy + th, cx:cx + tw] = im; out.append((nm, canvas))
print("\n".join(report))
# append: lettered signs first (2 rows of 4), then blanks
NM = "Campus_Props"; COLS = 16
img = Image.open(f"{D}/{NM}.png").convert("RGBA"); index = json.load(open(f"{D}/{NM}_index.json"))
start = img.height // 16
order = [o for o in out if not o[0].endswith("_blank")] + [o for o in out if o[0].endswith("_blank")]
rows = start + (len(order) // 4) * TH
new = Image.new("RGBA", (COLS * 16, rows * 16), (0, 0, 0, 0)); new.alpha_composite(img)
for i, (nm, canvas) in enumerate(order):
    x = (i % 4) * TW; y = start + (i // 4) * TH
    new.alpha_composite(Image.fromarray(canvas), (x * 16, y * 16))
    ids = [(y + r) * COLS + x + c for r in range(TH) for c in range(TW)]
    index.append({"tile_id": ids[0], "name": nm, "group": "building_signs", "tile_x": x, "tile_y": y, "w": TW, "h": TH,
                  "tile_ids": ids, "solid_tiles": ids[-TW:], "above_tiles": ids[:-TW]})
new.save(f"{OUT}/{NM}.png"); json.dump(index, open(f"{OUT}/{NM}_index.json", "w"), indent=1)
open(f"{OUT}/{NM}.tsx", "w").write(
    f'<?xml version="1.0" encoding="UTF-8"?>\n<tileset version="1.10" tiledversion="1.12.2" name="{NM}" tilewidth="16" tileheight="16" '
    f'tilecount="{COLS*rows}" columns="{COLS}">\n <image source="{NM}.png" width="{COLS*16}" height="{rows*16}"/>\n</tileset>\n')
# preview of the lettered signs on grass, 5x
pv = Image.new("RGBA", (4 * TW * 16 + 40, 2 * TH * 16 + 24), (99, 171, 63, 255))
for i, (nm, canvas) in enumerate([o for o in out if not o[0].endswith("_blank")]):
    pv.alpha_composite(Image.fromarray(canvas), (8 + (i % 4) * (TW * 16 + 8), 8 + (i // 4) * (TH * 16 + 8)))
pv.resize((pv.width * 5, pv.height * 5), Image.NEAREST).save(f"{OUT}/signs_preview.png")
print("rows", start, "->", rows)
