"""Replace Campus_Ground gravel tiles (ids 128-142, 144-148) in place with a seamless gravel path.
All fill variants share an identical 3px border ring (16px-periodic), so any tile meets any other cleanly."""
import sys, numpy as np
from PIL import Image
GD, SHEET, OUTP = sys.argv[1], sys.argv[2], sys.argv[3]
src = open(f"{GD}/build_ground.py").read().split("g1a, g1fg, g1 = patches")[0]
sys.argv = ["x", GD, "/dev/null"]; ns = {"__name__": "gb"}; exec(src, ns)
g1a, g1fg, g1 = ns["patches"](f"{GD}/in/g1.png")
P = ns["kerb_parts"](g1a, g1fg, g1[4], 29)
OUTL = np.array([0x21, 0x18, 0x1b, 255], np.uint8)
for k, v in P.items():                      # magenta-tinted fringe pixels -> outline
    c = v[..., :3].astype(int); bad = (v[..., 3] > 0) & (c[..., 0] - c[..., 1] > 50) & (c[..., 2] - c[..., 1] > 50)
    v[bad] = OUTL
T, RING = 16, 3
BED = np.array([[170, 160, 146], [160, 150, 137], [178, 169, 155], [150, 141, 129]], float)
BEDP = [0.42, 0.28, 0.18, 0.12]
STONES = [dict(hi=(222, 216, 204), mid=(196, 189, 176), lo=(146, 138, 127)),   # pale limestone
          dict(hi=(205, 196, 182), mid=(176, 166, 151), lo=(126, 117, 106)),   # buff
          dict(hi=(188, 184, 180), mid=(156, 152, 148), lo=(108, 104, 101)),   # grey
          dict(hi=(214, 196, 172), mid=(186, 164, 138), lo=(132, 114, 94))]    # warm sandstone
SHAPES = [["mh", "ml"], ["hm", "ml"], ["hmm", "mml"], ["hm", "mm", "ll"], ["h", "l"], ["hml"], ["hmm", "mml", ".l."]]
GAP = np.array([118, 110, 101], float)
def ring_mask():
    y, x = np.mgrid[:T, :T]; return np.minimum(np.minimum(x, y), np.minimum(T - 1 - x, T - 1 - y)) < RING
RM = ring_mask()
def bed(rng):
    idx = rng.choice(len(BED), size=(T, T), p=BEDP); return BED[idx]
def stamp(img, occ, rng, x, y, wrap, allow):
    sh = SHAPES[rng.integers(len(SHAPES))]; st = STONES[rng.choice(4, p=[0.35, 0.3, 0.2, 0.15])]
    cells = [(dy, dx, ch) for dy, row in enumerate(sh) for dx, ch in enumerate(row) if ch != "."]
    pts = [((y + dy) % T if wrap else y + dy, (x + dx) % T if wrap else x + dx, ch) for dy, dx, ch in cells]
    if any(not (0 <= py < T and 0 <= px < T) or occ[py, px] or not allow[py, px] for py, px, _ in pts): return False
    sy, sx = (y + len(sh)) % T if wrap else y + len(sh), (x + 1) % T if wrap else x + 1   # little shadow gap under stone
    for py, px, ch in pts:
        img[py, px] = st[{"h": "hi", "m": "mid", "l": "lo"}[ch]]; occ[py, px] = True
    if 0 <= sy < T and 0 <= sx < T and not occ[sy, sx] and allow[sy, sx]: img[sy, sx] = GAP; occ[sy, sx] = True
    return True
# fixed ring: same bed grain + same stones in every variant (stones may wrap across tile edges)
rng0 = np.random.default_rng(7); ring_img = bed(rng0); ring_occ = np.zeros((T, T), bool)
for _ in range(400):
    x, y = rng0.integers(T), rng0.integers(T)
    if RM[y, x]: stamp(ring_img, ring_occ, rng0, x, y, True, np.ones((T, T), bool))
    if ring_occ.mean() > 0.42: break
def variant(seed):
    rng = np.random.default_rng(100 + seed); img = bed(rng); occ = ring_occ.copy()
    img[RM] = ring_img[RM]; img[ring_occ] = ring_img[ring_occ]
    inner = ~RM
    for _ in range(300):
        stamp(img, occ, rng, rng.integers(T), rng.integers(T), False, inner & ~ring_occ)
        if occ[inner].mean() > 0.45: break
    return np.dstack([img.clip(0, 255).astype(np.uint8), np.full((T, T), 255, np.uint8)])
fills = [variant(s) for s in range(6)]
sheet = np.array(Image.open(SHEET).convert("RGBA"))
COMBOS = [(tl, tr, bl, br) for tl in (0, 1) for tr in (0, 1) for bl in (0, 1) for br in (0, 1) if (tl, tr, bl, br) != (0, 0, 0, 0)]
r0 = 8
for ci, st in enumerate(COMBOS):
    tile = fills[0] if st == (1, 1, 1, 1) else ns["compose"](st, fills[0], P)
    sheet[r0 * T:(r0 + 1) * T, ci * T:(ci + 1) * T] = tile
for vi, f in enumerate(fills[1:]):
    sheet[(r0 + 1) * T:(r0 + 2) * T, vi * T:(vi + 1) * T] = f
Image.fromarray(sheet).save(OUTP)
# preview: 8x5 gravel patch with edges using random fills, plus 4x4 fills-only test, on grass
C = {st: (r0 * 16 + ci) for ci, st in enumerate(COMBOS)}; fid = [C[(1, 1, 1, 1)]] + [144 + i for i in range(5)]
def tile(tid): r, c = divmod(tid, 16); return Image.fromarray(sheet[r * T:(r + 1) * T, c * T:(c + 1) * T])
W, H = 10, 7; grid = np.zeros((H + 1, W + 1), int); grid[1:H, 1:W] = 1; grid[1:3, 7:W] = 0
pv = Image.open(f"{GD}/grass.png").convert("RGBA").resize((W * 16 + 96, H * 16)) if False else Image.new("RGBA", (W * 16 + 96, H * 16), (99, 171, 63, 255))
rng = np.random.default_rng(3)
for y in range(H):
    for x in range(W):
        st = (grid[y, x], grid[y, x + 1], grid[y + 1, x], grid[y + 1, x + 1])
        if st == (0, 0, 0, 0): continue
        tid = int(rng.choice(fid)) if st == (1, 1, 1, 1) else C[st]
        pv.alpha_composite(tile(tid), (x * 16, y * 16))
for y in range(H - 1):
    for x in range(5):
        pv.alpha_composite(tile(int(rng.choice(fid))), (W * 16 + 8 + x * 16 + (0 if x < 5 else 0), 8 + y * 16) if x < 5 else (0, 0))
pv.resize((pv.width * 5, pv.height * 5), Image.NEAREST).save(OUTP.replace(".png", "_preview.png"))
print("ok")
