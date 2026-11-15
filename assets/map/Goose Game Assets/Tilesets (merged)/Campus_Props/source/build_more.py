"""Append new sheets to Campus_Props as new rows (existing tile IDs never change)."""
import sys, json, numpy as np
from PIL import Image
sys.path.insert(0, sys.argv[1])
from process import objects
from scipy import ndimage
def load(p):
    """Background = only true magenta (R,B high, G low). Purple art survives. Pink-ish fringe next to the
    background is repainted as outline."""
    a = np.asarray(Image.open(p).convert("RGB")).astype(float)
    R, G, B = a[..., 0], a[..., 1], a[..., 2]
    bg = (R > 175) & (B > 175) & (G < 95)
    near = ndimage.binary_dilation(bg, iterations=2) & ~bg
    fringe = near & (np.minimum(R, B) - G > 50) & (np.abs(R - B) < 70)
    a[fringe] = (0x21, 0x18, 0x1b)
    return a, np.where(bg, 255.0, 0.0)
from modeshrink import shrink_mode
from segments import finish
P = sys.argv[1]; base = sys.argv[2]; out = sys.argv[3]
specs = json.load(open(sys.argv[4] if len(sys.argv) > 4 else f"{P}/specs_batch2.json"))
NM = "Campus_Props"; COLS = 16
old = Image.open(f"{base}/{NM}.png"); index = json.load(open(f"{base}/{NM}_index.json"))
for e in index: e.setdefault("w", 1); e.setdefault("h", 1); e.setdefault("tile_ids", [e["tile_id"]])
start_row = old.height // 16
objs_out = []
for sheet, items in specs.items():
    a, mg = load(f"{P}/in/{sheet}.png"); objs, lab = objects(mg)
    for it in items:
        name, comps, (W, H) = it[0], it[1], it[2]; mode = it[3] if len(it) > 3 else "fit"; flat = len(it) > 4
        mask = np.zeros(mg.shape, bool)
        for k in comps: s, l = objs[k]; mask[s] |= (lab[s] == l)
        mask &= mg < 60
        ys, xs = np.nonzero(mask); y0, y1, x0, x1 = ys.min(), ys.max() + 1, xs.min(), xs.max() + 1
        rgb, op = a[y0:y1, x0:x1], mask[y0:y1, x0:x1]; h, w = op.shape
        if mode == "fill": tw, th = W * 16, H * 16
        else:
            sc = max(w / (W * 16), h / (H * 16)); tw, th = max(1, round(w / sc)), max(1, round(h / sc))
        col, m = shrink_mode(rgb, op, tw, th, k=20)
        if flat:
            spr = np.dstack([col.clip(0, 255).astype(np.uint8), (m * 255).astype(np.uint8)])
        else:
            spr = finish(col, m)
        canvas = np.zeros((H * 16, W * 16, 4), np.uint8)
        cx = (W * 16 - tw) // 2; cy = H * 16 - th
        canvas[cy:cy + th, cx:cx + tw] = spr
        objs_out.append((sheet, name, W, H, canvas))
# skyline-pack new objects below the existing rows, keeping each sheet's items together in order
sky = [0] * COLS; placed = []
for sheet, name, W, H, canvas in objs_out:
    best = None
    for x in range(COLS - W + 1):
        y = max(sky[x:x + W])
        if best is None or y < best[1]: best = (x, y)
    x, y = best
    for i in range(x, x + W): sky[i] = y + H
    placed.append((sheet, name, W, H, canvas, x, y))
rows = start_row + max(sky)
img = Image.new("RGBA", (COLS * 16, rows * 16), (0, 0, 0, 0)); img.alpha_composite(old.convert("RGBA"), (0, 0))
for sheet, name, W, H, canvas, x, y in placed:
    ty = start_row + y
    img.alpha_composite(Image.fromarray(canvas), (x * 16, ty * 16))
    ids = [(ty + r) * COLS + x + c for r in range(H) for c in range(W)]
    index.append({"tile_id": ids[0], "name": name, "group": sheet, "tile_x": x, "tile_y": ty, "w": W, "h": H, "tile_ids": ids,
                  "solid_tiles": ids[-W:] if H > 1 else ids, "above_tiles": ids[:-W] if H > 1 else []})
img.save(f"{out}/{NM}.png")
json.dump(index, open(f"{out}/{NM}_index.json", "w"), indent=1)
open(f"{out}/{NM}.tsx", "w").write(
    f'<?xml version="1.0" encoding="UTF-8"?>\n<tileset version="1.10" tiledversion="1.12.2" name="{NM}" tilewidth="16" tileheight="16" '
    f'tilecount="{COLS*rows}" columns="{COLS}">\n <image source="{NM}.png" width="{COLS*16}" height="{rows*16}"/>\n</tileset>\n')
# preview on grass, 4x
pv = Image.new("RGBA", img.size, (99, 171, 63, 255)); pv.alpha_composite(img)
pv.resize((pv.width * 4, pv.height * 4), Image.NEAREST).save(f"{out}/{NM}_preview.png")
print("rows", start_row, "->", rows, "| new objects", len(placed), "| total entries", len(index))
