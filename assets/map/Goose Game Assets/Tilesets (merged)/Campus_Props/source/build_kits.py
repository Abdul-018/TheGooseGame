"""Append area-kit objects from the single overview image to Campus_Props (new rows only)."""
import sys, json, numpy as np
from PIL import Image
from scipy import ndimage
P, base, out = sys.argv[1], sys.argv[2], sys.argv[3]
sys.path.insert(0, P)
from modeshrink import shrink_mode
from segments import finish
specs = json.load(open(f"{P}/specs_kits.json")); det = {o["id"]: o for o in json.load(open(f"{P}/out10/kits_labels.png.json"))}
a = np.asarray(Image.open(f"{P}/in/kits_overview.png").convert("RGB")).astype(float); R, G, B = a[...,0], a[...,1], a[...,2]
bg = (R > 175) & (B > 175) & (G < 95)
for x in (383,384,385,386,766,767,768,769,1150,1151,1152,1153): bg[:, x] = True
for y in (259,260,261,262,263,504,505,506,507,508,759,760,761,762): bg[y] = True
bg[:, :3] = bg[:, -3:] = True; bg[:3] = bg[-3:] = True
lab, _ = ndimage.label(ndimage.binary_dilation(~bg, iterations=2))
near = ndimage.binary_dilation(bg, iterations=2) & ~bg
fringe = near & (np.minimum(R, B) - G > 50) & (np.abs(R - B) < 70); a[fringe] = (0x21, 0x18, 0x1b)
tint = ~bg & (R - G > 55) & (B - G > 55) & (np.abs(R - B) < 90); dark = tint & (np.maximum(R, B) < 120)
a[dark] = (0x21, 0x18, 0x1b); bg |= tint & ~dark
fg = ~bg
NM, COLS = "Campus_Props", 16
old = Image.open(f"{base}/{NM}.png"); index = json.load(open(f"{base}/{NM}_index.json"))
start_row = old.height // 16; made = []
for group, items in specs.items():
    for name, src, (W, H), *rest in items:
        mode = rest[0] if rest else "fit"
        if isinstance(src, int):
            o = det[src]; x0, y0, x1, y1 = o["x"], o["y"], o["x"] + o["w"], o["y"] + o["h"]
            mask = np.zeros(fg.shape, bool); mask[y0:y1, x0:x1] = (lab[y0:y1, x0:x1] == o["label"]) & fg[y0:y1, x0:x1]
        else:
            x0, y0, x1, y1 = src; mask = np.zeros(fg.shape, bool); mask[y0:y1, x0:x1] = fg[y0:y1, x0:x1]
        ys, xs = np.nonzero(mask); y0, y1, x0, x1 = ys.min(), ys.max() + 1, xs.min(), xs.max() + 1
        rgb, op = a[y0:y1, x0:x1], mask[y0:y1, x0:x1]; h, w = op.shape
        if mode == "fillw": sc = w / (W * 16)
        elif mode.startswith("sc:"): sc = float(mode[3:])
        else: sc = max(w / (W * 16), h / (H * 16))
        tw, th = min(W * 16, max(1, round(w / sc))), min(H * 16, max(1, round(h / sc)))
        col, m = shrink_mode(rgb, op, tw, th, k=20)
        spr = finish(col, m)
        canvas = np.zeros((H * 16, W * 16, 4), np.uint8)
        cx = (W * 16 - tw) // 2; cy = H * 16 - th
        canvas[cy:cy + th, cx:cx + tw] = spr
        made.append((group, name, W, H, canvas, (w, h, tw, th)))
sky = [0] * COLS; placed = []
for group, name, W, H, canvas, dims in made:
    best = None
    for x in range(COLS - W + 1):
        y = max(sky[x:x + W])
        if best is None or y < best[1]: best = (x, y)
    x, y = best
    for i in range(x, x + W): sky[i] = y + H
    placed.append((group, name, W, H, canvas, x, y))
rows = start_row + max(sky)
img = np.zeros((rows * 16, COLS * 16, 4), np.uint8); img[:old.height] = np.asarray(old.convert("RGBA"))
for group, name, W, H, canvas, x, y in placed:
    ty = start_row + y; reg = img[ty * 16:(ty + H) * 16, x * 16:(x + W) * 16]
    reg[canvas[..., 3] > 0] = canvas[canvas[..., 3] > 0]
    ids = [(ty + r) * COLS + x + c for r in range(H) for c in range(W)]
    index.append({"tile_id": ids[0], "name": name, "group": group, "tile_x": x, "tile_y": ty, "w": W, "h": H, "tile_ids": ids,
                  "solid_tiles": ids[-W:] if H > 1 else ids, "above_tiles": ids[:-W] if H > 1 else []})
Image.fromarray(img).save(f"{out}/{NM}.png")
json.dump(index, open(f"{out}/{NM}_index.json", "w"), indent=1)
open(f"{out}/{NM}.tsx", "w").write(
    f'<?xml version="1.0" encoding="UTF-8"?>\n<tileset version="1.10" tiledversion="1.12.2" name="{NM}" tilewidth="16" tileheight="16" '
    f'tilecount="{COLS*rows}" columns="{COLS}">\n <image source="{NM}.png" width="{COLS*16}" height="{rows*16}"/>\n</tileset>\n')
pv = Image.new("RGBA", (COLS * 16, rows * 16), (99, 171, 63, 255)); pv.alpha_composite(Image.fromarray(img))
pv.resize((pv.width * 4, pv.height * 4), Image.NEAREST).save(f"{out}/{NM}_preview.png")
nv = pv.crop((0, start_row * 16, COLS * 16, rows * 16)); nv.resize((nv.width * 4, nv.height * 4), Image.NEAREST).save(f"{out}/new_rows.png")
print("rows", start_row, "->", rows, "| new", len(placed))
for g, n, W, H, c, d in made:
    if d[2] == W * 16 and d[3] == H * 16: pass
