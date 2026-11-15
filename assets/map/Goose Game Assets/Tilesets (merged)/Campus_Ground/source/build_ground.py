"""Campus_Ground: seamless fill tiles + kerbed corner-terrain (wang) tiles per material, road-marking overlays,
manhole/drain. Output: Campus_Ground.png / .tsx (with Tiled wangsets) / _index.json / previews."""
import sys, json, math, numpy as np
from PIL import Image
from scipy import ndimage
sys.path.insert(0, sys.argv[1]); from modeshrink import shrink_mode
from masonry import unit_faces, lay
G = sys.argv[1]; OUT = sys.argv[2]
KW = 5; T = 16; S0 = 84 / 16                         # one concrete slab (~84px) == one tile

def load(p):
    a = np.asarray(Image.open(p).convert("RGB")).astype(float)
    bg = (a[..., 0] > 175) & (a[..., 2] > 175) & (a[..., 1] < 95)
    return a, ~bg
def patches(p):
    a, fg = load(p)
    lab, n = ndimage.label(ndimage.binary_dilation(fg, iterations=2))
    sl = [s for i, s in enumerate(ndimage.find_objects(lab)) if (lab[s] == i + 1).sum() > 1500]
    sl.sort(key=lambda s: (s[0].start // 150, s[1].start))
    return a, fg, [(s[0].start, s[0].stop, s[1].start, s[1].stop) for s in sl]
def to_tile(rgb, op, w=T, h=T, k=20):
    col, m = shrink_mode(rgb, op, w, h, k=k)
    return np.dstack([col.clip(0, 255).astype(np.uint8), (m * 255).astype(np.uint8)])
def lum(x): return x[..., :3].mean(-1)
def period(profile, lo, hi):
    hi = min(hi, len(profile) // 2)
    p = profile - profile.mean(); best, bv = lo, -1e18
    for d in range(lo, hi + 1):
        v = (p[:-d] * p[d:]).mean()
        if v > bv: best, bv = d, v
    return best
def phase(profile, per):
    folded = np.array([profile[i::per].mean() for i in range(per)]); return int(np.argmin(folded))

def kerb_parts(a, fg, box, kt):
    y0, y1, x0, x1 = box
    def strip(rgb, op, horiz):
        L = lum(rgb).mean(0 if horiz else 1); per = period(L, 40, 140); k = max(1, round(84 / per)); n = per * k
        n = min(n, len(L)); mid = (len(L) - n) // 2; ph = phase(L, per); s = mid - (mid - ph) % per
        if s < 0: s += per
        s = min(max(s, 0), len(L) - n)
        if horiz: return to_tile(rgb[:, s:s + n], op[:, s:s + n], T, KW)
        return to_tile(rgb[s:s + n], op[s:s + n], KW, T)
    m = 2 * kt  # keep away from corners
    parts = {"top": strip(a[y0:y0 + kt, x0 + m:x1 - m], fg[y0:y0 + kt, x0 + m:x1 - m], True),
             "bottom": strip(a[y1 - kt:y1, x0 + m:x1 - m], fg[y1 - kt:y1, x0 + m:x1 - m], True),
             "left": strip(a[y0 + m:y1 - m, x0:x0 + kt], fg[y0 + m:y1 - m, x0:x0 + kt], False),
             "right": strip(a[y0 + m:y1 - m, x1 - kt:x1], fg[y0 + m:y1 - m, x1 - kt:x1], False)}
    for nm, (ys, xs) in {"tl": (slice(y0, y0 + kt), slice(x0, x0 + kt)), "tr": (slice(y0, y0 + kt), slice(x1 - kt, x1)),
                         "bl": (slice(y1 - kt, y1), slice(x0, x0 + kt)), "br": (slice(y1 - kt, y1), slice(x1 - kt, x1))}.items():
        parts[nm] = to_tile(a[ys, xs], fg[ys, xs], KW, KW)
    return parts

def grid_fills(a, fg, box, kt, nx, ny):
    y0, y1, x0, x1 = box; ix0, ix1, iy0, iy1 = x0 + kt, x1 - kt, y0 + kt, y1 - kt
    L = lum(a[iy0:iy1, ix0:ix1])
    def joints(profile, n):
        step = len(profile) / n; js = [0]
        for i in range(1, n):
            c = int(i * step); lo, hi = c - 12, c + 12
            js.append(lo + int(np.argmin(profile[lo:hi])))
        js.append(len(profile)); return js
    jx = joints(L.mean(0), nx); jy = joints(L.mean(1), ny); cells = []
    for r in range(ny):
        for c in range(nx):
            ys, xs = slice(iy0 + jy[r] - (1 if r else 0), iy0 + jy[r + 1] - 1), slice(ix0 + jx[c] - (1 if c else 0), ix0 + jx[c + 1] - 1)
            cells.append(to_tile(a[ys, xs], np.ones(a[ys, xs].shape[:2], bool)))
    return cells

def periodic_fills(a, box, kt, n=6, pxr=(25, 140)):
    y0, y1, x0, x1 = box; I = a[y0 + kt + 4:y1 - kt - 4, x0 + kt + 4:x1 - kt - 4]; L = lum(I)
    py = period(L.mean(1), 18, 70); phy = phase(L.mean(1), py)
    band = L[phy:phy + py]; px = period(band.mean(0), *pxr); phx = phase(band.mean(0), px)
    kx = max(1, round(2 * py / px)); W, H = kx * px, 2 * py; out = []
    for j in range(n):
        yy = phy + (j % 2) * 2 * py; xx = phx + (j // 2) * px
        if yy + H <= I.shape[0] and xx + W <= I.shape[1]:
            c = I[yy:yy + H, xx:xx + W]; out.append(to_tile(c, np.ones(c.shape[:2], bool)))
    return out, (px, py, kx)

def random_fills(a, box, kt, n=6):
    y0, y1, x0, x1 = box; I = a[y0 + kt + 6:y1 - kt - 6, x0 + kt + 6:x1 - kt - 6]; c = int(16 * S0); out = []
    for j in range(n):
        yy = (j % 2) * (I.shape[0] - c); xx = int((j // 2) / 2 * (I.shape[1] - c))
        cr = I[yy:yy + c, xx:xx + c]; out.append(to_tile(cr, np.ones(cr.shape[:2], bool)))
    return out

def compose(states, fill, P):
    tl, tr, bl, br = states
    M = np.zeros((T, T), bool); M[:8, :8] = tl; M[:8, 8:] = tr; M[8:, :8] = bl; M[8:, 8:] = br
    def Me(x, y): return M[min(max(y, 0), T - 1), min(max(x, 0), T - 1)]
    out = np.zeros((T, T, 4), np.uint8); out[M] = fill[M]
    for y in range(T):
        for x in range(T):
            if not M[y, x]: continue
            def dist(dx, dy):
                for d in range(1, KW + 1):
                    if not Me(x + dx * d, y + dy * d): return d
                return None
            up, dn, lf, rt = dist(0, -1), dist(0, 1), dist(-1, 0), dist(1, 0)
            v = up or dn; h = lf or rt
            if v and h:
                key = ("t" if up else "b") + ("l" if lf else "r")
                piece = P[key]; ry = (up - 1) if up else (KW - dn); rx = (lf - 1) if lf else (KW - rt)
                out[y, x] = piece[ry, rx]
            elif up: out[y, x] = P["top"][up - 1, x]
            elif dn: out[y, x] = P["bottom"][KW - dn, x]
            elif lf: out[y, x] = P["left"][y, lf - 1]
            elif rt: out[y, x] = P["right"][y, KW - rt]
            else:   # concave (inner) corner: nearest empty lies diagonally
                best = None
                for sx in (-1, 1):
                    for sy in (-1, 1):
                        for a_ in range(1, KW + 1):
                            for b_ in range(1, KW + 1):
                                if not Me(x + sx * a_, y + sy * b_):
                                    if best is None or max(a_, b_) < max(best[0], best[1]): best = (a_, b_, sx, sy)
                if best:
                    a_, b_, sx, sy = best
                    if a_ <= b_: out[y, x] = P["left"][y, a_ - 1] if sx < 0 else P["right"][y, KW - a_]
                    else:        out[y, x] = P["top"][b_ - 1, x] if sy < 0 else P["bottom"][KW - b_, x]
    return out

g1a, g1fg, g1 = patches(f"{G}/in/g1.png"); g2a, g2fg, g2 = patches(f"{G}/in/g2.png")
KT = 29
MATS = []   # (name, fills[list], probs, parts)
def probs_for(cells):
    med = np.median(np.stack([c[..., :3].astype(float) for c in cells]), 0)
    d = [np.abs(c[..., :3] - med).mean() for c in cells]; order = np.argsort(d)
    pr = [0.0] * len(cells)
    for rank, i in enumerate(order): pr[i] = 1.0 if rank < max(3, len(cells) // 2) else 0.12
    return pr
for name, box, kind in [("concrete_slabs", g1[0], "grid"), ("york_stone", g1[1], "grid"), ("brick_paving", g1[2], "periodic"),
                        ("granite_setts", g1[3], "periodic"), ("gravel", g1[4], "random")]:
    parts = kerb_parts(g1a, g1fg, box, KT)
    if kind == "grid": fills = grid_fills(g1a, g1fg, box, KT, 5, 4)
    elif kind == "periodic":                      # lay real unit faces on a fixed 16px-repeating grid (seamless)
        if name == "brick_paving":
            faces, mortar = unit_faces(g1a, box, KT, 28, 62, (15, 7), to_tile); fills = [lay(faces, mortar, 16, 8, 8, s) for s in range(6)]
        else:
            faces, mortar = unit_faces(g1a, box, KT, 40, 41, (7, 7), to_tile); fills = [lay(faces, mortar, 8, 8, 4, s) for s in range(6)]
    else: fills = random_fills(g1a, box, KT)
    MATS.append((name, fills, probs_for(fills), parts))
box = g2[0]; KT2 = 21
MATS.append(("tarmac", random_fills(g2a, box, KT2), None, kerb_parts(g2a, g2fg, box, KT2)))
MATS[-1] = (MATS[-1][0], MATS[-1][1], [1.0] * len(MATS[-1][1]), MATS[-1][3])

# ---- extra tiles: tactile slabs, markings, manhole, drain
extra = []
y0, y1, x0, x1 = g1[5]; hx, hy = (x0 + x1) // 2, (y0 + y1) // 2
for ys, xs in [(slice(y0, hy), slice(x0, hx)), (slice(y0, hy), slice(hx, x1)), (slice(hy, y1), slice(x0, hx)), (slice(hy, y1), slice(hx, x1))]:
    extra.append(("tactile_paving", to_tile(g1a[ys, xs], g1fg[ys, xs])))
# white dashed centre line from the dashed road (g2[2]); keep only bright neutral paint between the kerbs
y0, y1, x0, x1 = g2[2]; band = g2a[y0 + 30:y1 - 30, x0 + 20:x1 - 20]
white = (band.min(-1) > 190) & (band.max(-1) - band.min(-1) < 40)
cols = white.any(0); runs = []; st = None
for i, v in enumerate(list(cols) + [False]):
    if v and st is None: st = i
    if not v and st is not None: runs.append((st, i)); st = None
d0 = runs[1]; per = runs[2][0] - runs[1][0]; c0 = d0[0] - (per - (d0[1] - d0[0])) // 2
rows = np.nonzero(white.any(1))[0]; ry0, ry1 = rows.min(), rows.max() + 1
dash_rgb = band[ry0:ry1, c0:c0 + per]; dash_m = white[ry0:ry1, c0:c0 + per]
dl = to_tile(dash_rgb, dash_m, T, 2, k=4); dash_h = np.zeros((T, T, 4), np.uint8); dash_h[7:9] = dl
extra.append(("road_dash_horizontal", dash_h)); extra.append(("road_dash_vertical", dash_h.transpose(1, 0, 2).copy()))
# double yellow lines: colour from the source, drawn as 2 crisp 1px lines along the kerb side of an edge tile
y0, y1, x0, x1 = g2[4]; band = g2a[y0:y1, x0:x1]
yel = (band[..., 0] > 170) & (band[..., 1] > 140) & (band[..., 2] < 90)
yc = np.median(band[yel], 0).astype(np.uint8); yd = (yc * 0.8).astype(np.uint8)
def dyl(rows_):
    t = np.zeros((T, T, 4), np.uint8)
    for r in rows_: t[r, :, :3] = yc; t[r, ::5, :3] = yd; t[r, :, 3] = 255
    return t
inner = 8 + KW            # first tarmac row below a kerb that sits at the tile's middle
extra += [("double_yellow_kerb_above", dyl([inner, inner + 2])),
          ("double_yellow_kerb_below", dyl([8 - KW - 1, 8 - KW - 3]))]
extra += [("double_yellow_kerb_left", dyl([inner, inner + 2]).transpose(1, 0, 2).copy()),
          ("double_yellow_kerb_right", dyl([8 - KW - 1, 8 - KW - 3]).transpose(1, 0, 2).copy())]
for name, b in (("manhole_cover", g2[5]), ("drain_grate", g2[6])):
    y0, y1, x0, x1 = b; rgb, op = g2a[y0:y1, x0:x1], g2fg[y0:y1, x0:x1]; h, w = op.shape
    sc = max(w, h) / 13; tw, th = round(w / sc), round(h / sc)
    t = np.zeros((T, T, 4), np.uint8); spr = to_tile(rgb, op, tw, th); oy, ox = (T - th) // 2, (T - tw) // 2
    t[oy:oy + th, ox:ox + tw] = spr; extra.append((name, t))

# ---- assemble: 2 rows per material (16 corner combos, then extra fill variants), then the extras row(s)
COLS = 16
COMBOS = [(tl, tr, bl, br) for tl in (0, 1) for tr in (0, 1) for bl in (0, 1) for br in (0, 1) if (tl, tr, bl, br) != (0, 0, 0, 0)]
rows_needed = 2 * len(MATS) + math.ceil(len(extra) / COLS)
sheet = np.zeros((rows_needed * T, COLS * T, 4), np.uint8); index = []; wangsets = []; tileprops = {}
for mi, (name, fills, pr, P) in enumerate(MATS):
    r0 = 2 * mi; wt = []
    for ci, st in enumerate(COMBOS):
        fill = fills[ci % len(fills)] if st != (1, 1, 1, 1) else fills[0]
        if st != (1, 1, 1, 1):          # edges/corners use the most typical fill so kerbs look consistent
            fill = fills[int(np.argmax(pr))]
        tile = compose(st, fill, P); tid = r0 * COLS + ci
        sheet[r0 * T:(r0 + 1) * T, ci * T:(ci + 1) * T] = tile; wt.append((tid, st))
        index.append({"tile_id": tid, "name": f"{name}_{''.join(map(str, st))}", "material": name, "corners_TL_TR_BL_BR": st})
    if (1, 1, 1, 1) in COMBOS: tileprops[r0 * COLS + COMBOS.index((1, 1, 1, 1))] = pr[0]
    for vi, (f, p) in enumerate(zip(fills[1:], pr[1:])):
        if vi >= COLS: break
        tid = (r0 + 1) * COLS + vi; sheet[(r0 + 1) * T:(r0 + 2) * T, vi * T:(vi + 1) * T] = f
        wt.append((tid, (1, 1, 1, 1))); tileprops[tid] = p
        index.append({"tile_id": tid, "name": f"{name}_fill_{vi + 2}", "material": name, "corners_TL_TR_BL_BR": (1, 1, 1, 1)})
    wangsets.append((name, wt))
r = 2 * len(MATS)
for i, (name, t) in enumerate(extra):
    rr, cc = r + i // COLS, i % COLS; sheet[rr * T:(rr + 1) * T, cc * T:(cc + 1) * T] = t
    index.append({"tile_id": rr * COLS + cc, "name": name})
Image.fromarray(sheet).save(f"{OUT}/Campus_Ground.png")
json.dump(index, open(f"{OUT}/Campus_Ground_index.json", "w"), indent=1)
cols_hex = ["#9aa0a6", "#d9a866", "#b4513e", "#5f6670", "#d8ccb0", "#3a3d44"]
x = ['<?xml version="1.0" encoding="UTF-8"?>',
     f'<tileset version="1.10" tiledversion="1.12.2" name="Campus_Ground" tilewidth="16" tileheight="16" tilecount="{rows_needed * COLS}" columns="{COLS}">',
     f' <image source="Campus_Ground.png" width="{COLS * T}" height="{rows_needed * T}"/>']
for tid, p in sorted(tileprops.items()): x.append(f' <tile id="{tid}" probability="{p:g}"/>')
x.append(" <wangsets>")
for (name, wt), hx in zip(wangsets, cols_hex):
    full = [t for t, s in wt if s == (1, 1, 1, 1)][0]
    x.append(f'  <wangset name="{name}" type="corner" tile="{full}">')
    x.append(f'   <wangcolor name="{name}" color="{hx}" tile="{full}" probability="1"/>')
    for tid, (tl, tr, bl, br) in wt: x.append(f'   <wangtile tileid="{tid}" wangid="0,{tr},0,{br},0,{bl},0,{tl}"/>')
    x.append("  </wangset>")
x += [" </wangsets>", "</tileset>"]
open(f"{OUT}/Campus_Ground.tsx", "w").write("\n".join(x) + "\n")
json.dump({"mats": [m[0] for m in MATS], "combos": COMBOS}, open(f"{OUT}/_layout.json", "w"))
print("ok", rows_needed, "rows,", len(index), "tiles")
