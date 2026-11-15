"""Letter the v2 (plain-panel) signs, centring text exactly inside the measured panel, and replace the old
signs in their existing tile slots (tile IDs unchanged)."""
import sys, json, numpy as np
from PIL import Image
from scipy import ndimage
P, D, OUT = sys.argv[1:4]
sys.argv = ["x", P, D, OUT]
src = open(f"{P}/build_more.py").read(); exec(src[:src.index("specs = json.load")])
fs = open(f"{P}/signs.py").read(); exec(fs[fs.index("F = {"):fs.index("SIGNS = [")])      # font + text_mask
F["I"] = ["#", "#", "#", "#", "#"]
def line_mask(s, extra_space=0):
    if extra_space and " " in s:
        F2 = dict(F); F2[" "] = [".." + "." * extra_space] * 5
        g = F; globals()["F"] = F2; m = text_mask(s); globals()["F"] = g; return m
    return text_mask(s)
WIDE = {"O": [".##.","#..#","#..#","#..#",".##."], "A": [".##.","#..#","####","#..#","#..#"],
        "U": ["#..#","#..#","#..#","#..#",".##."], "D": ["###.","#..#","#..#","#..#","###."],
        "C": [".###","#...","#...","#...",".###"], "G": [".###","#...","#.##","#..#",".###"], "H": ["#..#","#..#","####","#..#","#..#"]}
WHITE, DARK = (0xf2, 0xf4, 0xee), (0x2c, 0x35, 0x4d)
# design index in the sheet (reading order) for each building
SIGNS = [("heslington_hall", ["HESLINGTON", "HALL"], 3, WHITE), ("central_hall", ["CENTRAL", "HALL"], 0, WHITE),
         ("ron_cooke_hub", ["RON COOKE", "HUB"], 2, WHITE), ("ian_wand", ["IAN WAND", "BUILDING"], 5, DARK),
         ("jb_morrell_library", ["JB MORRELL", "LIBRARY"], 4, WHITE), ("derwent_college", ["DERWENT", "COLLEGE"], 6, WHITE),
         ("piazza_building", ["PIAZZA", "BUILDING"], 1, WHITE), ("university_of_york", ["UNIVERSITY", "OF YORK"], 7, WHITE)]
a, mg = load(f"{P}/in/building_signs_v2.png"); objs, lab = objects(mg); assert len(objs) == 8, len(objs)
TW, TH = 4, 2; made = {}; report = []; debug = []
sprites = {}
for k, (s, l) in enumerate(objs):
    mask = (lab[s] == l) & (mg[s] < 60); rgb = a[s]
    if k in (3, 4):                                   # wall signs: cut brick rows above/below the dark panel
        L = rgb.mean(-1); cw = mask.shape[1]; band = L[:, int(cw * .35):int(cw * .65)]
        dark = np.nonzero((band < 75).mean(1) > 0.6)[0]; p0, p1 = dark.min(), dark.max() + 1
        cop = int(mask.shape[0] * 0.17)                   # keep the coping on top
        keep = list(range(0, cop)) + list(range(max(cop, p0 - 10), min(mask.shape[0], p1 + 10))) + list(range(mask.shape[0] - 8, mask.shape[0]))
        keep = sorted(set(keep)); mask = mask[keep]; rgb = rgb[keep]
    h, w = mask.shape
    sc = max(w / (TW * 16), h / (TH * 16)); tw, th = round(w / sc), round(h / sc)
    tw = min(TW * 16, round(tw * 1.25))                # stretch a little wider: panels get side margin
    col, m = shrink_mode(rgb, mask, tw, th, k=20); sprites[k] = finish(col, m)
def panel_rect(spr):
    th, tw = spr.shape[:2]; best = None
    for fy in (0.4, 0.45, 0.5, 0.55, 0.6):            # seed a few points down the centre line, keep the biggest flat region
        sx, sy = tw // 2, int(fy * th); c0 = spr[sy, sx, :3].astype(int)
        sim = (np.abs(spr[..., :3].astype(int) - c0).sum(-1) < 60) & (spr[..., 3] > 0)
        labs, _ = ndimage.label(sim); reg = labs == labs[sy, sx]
        if best is None or reg.sum() > best.sum(): best = reg
    rows = np.nonzero(best.mean(1) > 0.5 * best.mean(1).max())[0]; cols = np.nonzero(best.mean(0) > 0.5 * best.mean(0).max())[0]
    x0, x1 = cols.min(), cols.max() + 1; half = min(tw / 2 - x0, x1 - tw / 2) if False else max(tw / 2 - x0, x1 - tw / 2)
    x0, x1 = int(round(tw / 2 - half)), int(round(tw / 2 + half))      # signs are symmetric: mirror the panel
    return max(0, x0), min(tw, x1), rows.min(), rows.max() + 1
for name, lines, k, tc in SIGNS:
    spr = sprites[k].copy(); blank = sprites[k].copy(); th, tw = spr.shape[:2]
    x0, x1, y0, y1 = panel_rect(spr); pw, ph = x1 - x0, y1 - y0
    tms = []
    NARROW = {"N": ["##.","#.#","#.#","#.#","#.#"], "M": ["#.#","###","#.#","#.#","#.#"], "W": ["#.#","#.#","#.#","###","#.#"]}
    def build(glyphs):
        cols = []
        for j, g in enumerate(glyphs):
            for x in range(len(g[0])): cols.append([g[y][x] == "#" for y in range(5)])
            if j < len(glyphs) - 1: cols.append([False] * 5)
        return np.array(cols, bool).T
    for line in lines:
        gl = [F[c] for c in line]
        if build(gl).shape[1] > pw - 2:
            gl = [NARROW.get(c, F[c]) for c in line]
        m = build(gl)
        if (pw - m.shape[1]) % 2:
            done = False
            for i, c in enumerate(line):                   # widen a round letter if there is room
                if c in WIDE and m.shape[1] + 1 <= pw - 2:
                    g2 = list(gl); g2[i] = WIDE[c]; m = build(g2); done = True; break
            if not done:
                for i, c in enumerate(line):               # otherwise narrow one N/M/W
                    if c in NARROW and gl[i] is not NARROW[c]:
                        g2 = list(gl); g2[i] = NARROW[c]; m = build(g2); done = True; break
            if not done and " " in line:
                F2 = dict(F); F2[" "] = ["..."] * 5; m = build([F2[c] if c == " " else g for c, g in zip(line, gl)])
        tms.append(m)
    fits = all(m.shape[1] <= pw - 2 for m in tms)
    opts = [g for g in (2, 1) if (ph - 10 - g) % 2 == 0 and ph - 10 - g >= 2] or [g for g in (2, 1) if ph - 10 - g >= 2] or [1]
    gap = opts[0]
    tot = 10 + gap; ty = y0 + (ph - tot) // 2
    for m in tms:
        tx = x0 + (pw - m.shape[1]) // 2
        r = spr[ty:ty + 5, tx:tx + m.shape[1]]; r[m, :3] = tc; r[m, 3] = 255; ty += 5 + gap
    lm = [(pw - m.shape[1]) / 2 for m in tms]
    report.append(f"{'OK ' if fits else 'TIGHT'} {name:20s} design {k} panel {pw}x{ph} at x{x0}-{x1} y{y0}-{y1}; side margins {lm}; top/bottom {(ph - tot) / 2}")
    for nm, im in ((f"sign_{name}", spr), (f"sign_{name}_blank", blank)):
        c = np.zeros((TH * 16, TW * 16, 4), np.uint8); cx = (TW * 16 - tw) // 2; cy = TH * 16 - th
        c[cy:cy + th, cx:cx + tw] = im; made[nm] = c
    debug.append((spr, (x0, x1, y0, y1)))
print("\n".join(report))
NM = "Campus_Props"; img = Image.open(f"{D}/{NM}.png").convert("RGBA"); index = json.load(open(f"{D}/{NM}_index.json"))
n = 0
for e in index:
    if e["name"] in made:
        box = (e["tile_x"] * 16, e["tile_y"] * 16)
        img.paste(Image.new("RGBA", (e["w"] * 16, e["h"] * 16), (0, 0, 0, 0)), box); img.paste(Image.fromarray(made[e["name"]]), box)
        x, y = e["tile_x"], e["tile_y"]; ids = [(y + r) * 16 + x + c for r in range(TH) for c in range(TW)]
        e.update({"w": TW, "h": TH, "tile_id": ids[0], "tile_ids": ids, "solid_tiles": ids[-TW:], "above_tiles": ids[:-TW]}); n += 1
print("replaced", n, "sign slots")
img.save(f"{OUT}/{NM}.png"); json.dump(index, open(f"{OUT}/{NM}_index.json", "w"), indent=1)
# previews: lettered signs on grass (5x) and a debug view with the measured panel outlined
pv = Image.new("RGBA", (4 * 72 + 8, 2 * 40 + 8), (99, 171, 63, 255)); dv = pv.copy()
for i, (name, *_ ) in enumerate(SIGNS):
    pv.alpha_composite(Image.fromarray(made[f"sign_{name}"]), (8 + (i % 4) * 72, 8 + (i // 4) * 40))
pv.resize((pv.width * 5, pv.height * 5), Image.NEAREST).save(f"{OUT}/signs_preview.png")
from PIL import ImageDraw
big = pv.resize((pv.width * 5, pv.height * 5), Image.NEAREST); d = ImageDraw.Draw(big)
for i, (name, lines, k, tc) in enumerate(SIGNS):
    spr, (x0, x1, y0, y1) = debug[i]; th, tw = spr.shape[:2]
    ox = 8 + (i % 4) * 72 + (64 - tw) // 2; oy = 8 + (i // 4) * 40 + 32 - th
    d.rectangle([(ox + x0) * 5, (oy + y0) * 5, (ox + x1) * 5 - 1, (oy + y1) * 5 - 1], outline=(255, 0, 255), width=2)
    cx = (ox + x0 + (x1 - x0) / 2) * 5; d.line([(cx, (oy + y0) * 5), (cx, (oy + y1) * 5)], fill=(255, 255, 0), width=1)
big.save(f"{OUT}/signs_debug.png")
