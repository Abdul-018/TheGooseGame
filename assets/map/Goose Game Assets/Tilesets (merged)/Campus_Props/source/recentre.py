"""Pole-type props: line the centre of the BASE up with the centre of a tile column. Same footprint -> redraw in
place (tile IDs unchanged); wider footprint -> append a new *_centred object. Never moves existing tiles."""
import sys, json, math, numpy as np
from PIL import Image
P, D, OUT = sys.argv[1:4]
sys.argv = ["x", P, D, OUT]
src = open(f"{P}/build_more.py").read(); exec(src[:src.index("specs = json.load")])
specs = json.load(open(f"{P}/specs_batch2.json"))
POLES = ["lamp_victorian", "lamp_modern", "lamp_double", "light_bollard", "brick_pillar_lamp", "fingerpost",
         "sign_geese_warning", "sign_goose_crossing", "ev_charger"]
NM = "Campus_Props"; COLS = 16
img = Image.open(f"{D}/{NM}.png").convert("RGBA"); index = json.load(open(f"{D}/{NM}_index.json"))
byname = {e["name"]: e for e in index}
cache = {}
def sprite(sheet, comps, W, H):
    if sheet not in cache: cache[sheet] = (lambda a_mg: (a_mg, objects(a_mg[1])))(load(f"{P}/in/{sheet}.png"))
    (a, mg), (objs, lab) = cache[sheet]
    mask = np.zeros(mg.shape, bool)
    for k in comps: s, l = objs[k]; mask[s] |= (lab[s] == l)
    mask &= mg < 60
    ys, xs = np.nonzero(mask); y0, y1, x0, x1 = ys.min(), ys.max() + 1, xs.min(), xs.max() + 1
    rgb, op = a[y0:y1, x0:x1], mask[y0:y1, x0:x1]; h, w = op.shape
    sc = max(w / (W * 16), h / (H * 16)); tw, th = max(1, round(w / sc)), max(1, round(h / sc))
    col, m = shrink_mode(rgb, op, tw, th, k=20)
    return finish(col, m)
report = []; appended = []
for sheet, items in specs.items():
    for it in items:
        name, comps, (W, H) = it[0], it[1], it[2]
        if name not in POLES: continue
        spr = sprite(sheet, comps, W, H); th, tw = spr.shape[:2]
        alpha = spr[..., 3] > 0
        rows = max(2, th // 6)
        cols = np.nonzero(alpha[th - rows:].any(0))[0]
        bx = (cols.min() + cols.max() + 1) / 2                      # base centre (px from sprite left)
        left_t = max(0, math.ceil((bx - 8) / 16)); right_t = max(0, math.ceil((tw - bx - 8) / 16))
        NW = 1 + left_t + right_t
        canvas = np.zeros((H * 16, NW * 16, 4), np.uint8)
        ox = int(round(left_t * 16 + 8 - bx)); oy = H * 16 - th
        x0 = max(0, ox); sx0 = x0 - ox; x1 = min(NW * 16, ox + tw)
        canvas[oy:oy + th, x0:x1] = spr[:, sx0:sx0 + (x1 - x0)]
        e = byname[name]
        if NW == e["w"]:
            region = Image.fromarray(canvas)
            box = (e["tile_x"] * 16, e["tile_y"] * 16, (e["tile_x"] + e["w"]) * 16, (e["tile_y"] + e["h"]) * 16)
            img.paste(Image.new("RGBA", region.size, (0, 0, 0, 0)), box[:2]); img.paste(region, box[:2])
            report.append(f"{name:22s} redrawn in place ({e['w']}x{e['h']}), pole in tile column {left_t}")
        else:
            appended.append((name + "_centred", sheet, NW, H, canvas, left_t))
            report.append(f"{name:22s} needs {NW}x{H} -> new {name}_centred, pole in column {left_t}")
# append new objects in a fresh row block (skyline pack below current rows)
start = img.height // 16; sky = [0] * COLS; placed = []
for name, sheet, W, H, canvas, pc in appended:
    best = min(((max(sky[x:x + W]), x) for x in range(COLS - W + 1)))
    y, x = best
    for i in range(x, x + W): sky[i] = y + H
    placed.append((name, sheet, W, H, canvas, x, start + y, pc))
rows = start + max(sky) if placed else start
new = Image.new("RGBA", (COLS * 16, rows * 16), (0, 0, 0, 0)); new.alpha_composite(img)
for name, sheet, W, H, canvas, x, y, pc in placed:
    new.alpha_composite(Image.fromarray(canvas), (x * 16, y * 16))
    ids = [(y + r) * COLS + x + c for r in range(H) for c in range(W)]
    index.append({"tile_id": ids[0], "name": name, "group": sheet, "tile_x": x, "tile_y": y, "w": W, "h": H, "tile_ids": ids,
                  "pole_column": pc, "solid_tiles": [ (y + H - 1) * COLS + x + pc ], "above_tiles": [i for i in ids if i != (y + H - 1) * COLS + x + pc]})
new.save(f"{OUT}/{NM}.png"); json.dump(index, open(f"{OUT}/{NM}_index.json", "w"), indent=1)
open(f"{OUT}/{NM}.tsx", "w").write(
    f'<?xml version="1.0" encoding="UTF-8"?>\n<tileset version="1.10" tiledversion="1.12.2" name="{NM}" tilewidth="16" tileheight="16" '
    f'tilecount="{COLS*rows}" columns="{COLS}">\n <image source="{NM}.png" width="{COLS*16}" height="{rows*16}"/>\n</tileset>\n')
print("\n".join(report)); print("rows", start, "->", rows)
