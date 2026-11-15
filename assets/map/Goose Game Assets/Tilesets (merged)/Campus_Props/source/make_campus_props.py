"""Cut objects from a magenta sheet, downscale to game size, clean colours/outline, snap to 16px tiles."""
import sys, json, math, numpy as np
from PIL import Image, ImageEnhance
from scipy import ndimage

OUTLINE = np.array([0x21, 0x18, 0x1b], float)

def load(p):
    a = np.asarray(Image.open(p).convert("RGB")).astype(float)
    mg = np.minimum(a[..., 0], a[..., 2]) - a[..., 1]
    return a, mg

def objects(mg):
    fg = mg < 120
    lab, n = ndimage.label(ndimage.binary_dilation(fg, iterations=4))
    out = []
    for i, s in enumerate(ndimage.find_objects(lab)):
        if (lab[s] == i + 1).sum() < 2000: continue
        out.append((s, i + 1))
    # rows: cluster by vertical centre
    out.sort(key=lambda t: (t[0][0].start + t[0][0].stop) / 2)
    rows, cur = [], []
    for o in out:
        c = (o[0][0].start + o[0][0].stop) / 2
        if cur and c - cur[-1][2] > 150: rows.append(cur); cur = []
        cur.append((o[0], o[1], c))
    rows.append(cur)
    return [(s, l) for r in rows for (s, l, _) in sorted(r, key=lambda t: t[0][1].start)], lab

def shrink(rgb, opaque, scale):
    h, w = opaque.shape
    tw, th = max(1, round(w / scale)), max(1, round(h / scale))
    pm = np.dstack([rgb * opaque[..., None], opaque.astype(float)])
    small = np.stack([np.asarray(Image.fromarray(pm[..., c].astype(np.float32), "F").resize((tw, th), Image.BOX)) for c in range(4)], -1)
    al = small[..., 3]
    col = small[..., :3] / np.maximum(al, 1e-6)[..., None]
    return col, al >= 0.5

def outline(col, m):
    pad = np.pad(m, 1)
    edge = m & ~(pad[:-2, 1:-1] & pad[2:, 1:-1] & pad[1:-1, :-2] & pad[1:-1, 2:])
    lum = col.mean(-1)
    darken = edge & (lum > 70)
    col[darken] = col[darken] * 0.3 + OUTLINE * 0.7
    return col

def run(src, scale, names, ncol=32, sheet_sat=1.25):
    a, mg = load(src)
    objs, lab = objects(mg)
    assert len(objs) == len(names), (src, len(objs), len(names))
    res = []
    for (s, l), name in zip(objs, names):
        opaque = (lab[s] == l) & (mg[s] < 60)
        col, m = shrink(a[s], opaque, scale)
        col = outline(col, m)
        sat = sheet_sat
        col = np.asarray(ImageEnhance.Color(Image.fromarray(col.clip(0, 255).astype(np.uint8))).enhance(sat)).astype(float)
        res.append([name, col, m])
    out = []
    for name, col, m in res:
        px = col[m].clip(0, 255).astype(np.uint8)
        pal_img = Image.fromarray(px[None]).quantize(min(ncol, max(2, len(np.unique(px.reshape(-1, 3), axis=0)))), method=Image.Quantize.MEDIANCUT)
        q = np.asarray(Image.fromarray(col.clip(0, 255).astype(np.uint8)).quantize(palette=pal_img, dither=Image.Dither.NONE).convert("RGB"))
        rgba = np.dstack([q, (m * 255).astype(np.uint8)])
        h, w = m.shape
        tw, th = math.ceil(w / 16), math.ceil(h / 16)
        canvas = np.zeros((th * 16, tw * 16, 4), np.uint8)
        x0 = (tw * 16 - w) // 2; y0 = th * 16 - h
        canvas[y0:y0 + h, x0:x0 + w] = rgba
        out.append((name, Image.fromarray(canvas), (w, h)))
    return out

if __name__ == "__main__":
    cfg = json.load(open(sys.argv[1]))
    allout = []
    for sheet in cfg["sheets"]:
        for name, im, wh in run(sheet["src"], sheet["scale"], sheet["names"], sheet_sat=sheet.get("sat", 1.25)):
            allout.append({"name": name, "sheet": sheet["id"], "img": im, "px": wh})
            print(f'{name:22s} {wh[0]:3d}x{wh[1]:<3d}px -> {im.width//16}x{im.height//16} tiles')
    # shelf-pack into 16 columns
    COLS = 16; rows_used = []; place = []
    skyline = [0] * COLS
    for o in allout:
        tw, th = o["img"].width // 16, o["img"].height // 16
        best = None
        for x in range(COLS - tw + 1):
            y = max(skyline[x:x + tw])
            if best is None or y < best[1]: best = (x, y)
        x, y = best
        for i in range(x, x + tw): skyline[i] = y + th
        place.append((o, x, y, tw, th))
    H = max(skyline)
    sheet = Image.new("RGBA", (COLS * 16, H * 16), (0, 0, 0, 0))
    index = []
    for n, (o, x, y, tw, th) in enumerate(place, 1):
        sheet.alpha_composite(o["img"], (x * 16, y * 16))
        index.append({"n": n, "name": o["name"], "group": o["sheet"], "tile_x": x, "tile_y": y, "w": tw, "h": th,
                      "first_tile_id": y * COLS + x,
                      "solid_tiles": [(y + th - 1) * COLS + x + i for i in range(tw)],
                      "above_tiles": [(y + r) * COLS + x + i for r in range(th - 1) for i in range(tw)]})
    od = cfg["out"]; nm = cfg["name"]
    sheet.save(f"{od}/{nm}.png")
    json.dump(index, open(f"{od}/{nm}_index.json", "w"), indent=1)
    open(f"{od}/{nm}.tsx", "w").write(
        f'<?xml version="1.0" encoding="UTF-8"?>\n<tileset version="1.10" tiledversion="1.12.2" name="{nm}" tilewidth="16" tileheight="16" '
        f'tilecount="{COLS*H}" columns="{COLS}">\n <image source="{nm}.png" width="{COLS*16}" height="{H*16}"/>\n</tileset>\n')
    print("packed", COLS, "x", H, "tiles")
