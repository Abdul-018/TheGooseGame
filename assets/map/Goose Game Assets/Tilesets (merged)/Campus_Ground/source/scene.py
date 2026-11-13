"""Paint a test map using the corner-terrain rules (like Tiled's terrain brush) to check seams and kerbs."""
import sys, json, random, numpy as np
from PIL import Image
OUT = sys.argv[1]; GRASS = sys.argv[2]
sheet = np.asarray(Image.open(f"{OUT}/Campus_Ground.png").convert("RGBA")); idx = json.load(open(f"{OUT}/Campus_Ground_index.json"))
lay = json.load(open(f"{OUT}/_layout.json")); COLS = 16; T = 16
def tile(tid): r, c = divmod(tid, COLS); return sheet[r*T:(r+1)*T, c*T:(c+1)*T]
lookup = {}; fills = {}
for e in idx:
    if "material" in e:
        st = tuple(e["corners_TL_TR_BL_BR"])
        if st == (1, 1, 1, 1): fills.setdefault(e["material"], []).append(e["tile_id"])
        else: lookup[(e["material"], st)] = e["tile_id"]
byname = {e["name"]: e["tile_id"] for e in idx}
W, H = 30, 20
corner = [[None] * (W + 1) for _ in range(H + 1)]       # material at each tile corner
def rect(m, x0, y0, x1, y1):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1): corner[y][x] = m
rect("concrete_slabs", 2, 2, 9, 7); rect("concrete_slabs", 2, 7, 5, 11)        # L-shaped plaza (inner corner)
rect("york_stone", 12, 2, 19, 4); rect("brick_paving", 22, 2, 27, 7)
rect("granite_setts", 12, 7, 18, 10); rect("gravel", 21, 10, 27, 12)
rect("tarmac", 1, 14, 28, 17)                                                      # road, 3 tiles wide
rect("tarmac", 8, 11, 10, 14)                                                      # side road joining it
random.seed(3)
grass = Image.open(GRASS).convert("RGBA")
img = Image.new("RGBA", (W * T, H * T))
for y in range(H):
    for x in range(W): img.alpha_composite(grass, (x * T, y * T))
over = []
for y in range(H):
    for x in range(W):
        cs = [corner[y][x], corner[y][x + 1], corner[y + 1][x], corner[y + 1][x + 1]]
        for m in set(c for c in cs if c):
            st = tuple(int(c == m) for c in cs)
            if st == (1, 1, 1, 1):
                ids = fills[m]; tid = ids[0] if random.random() < 0.5 else random.choice(ids)
            else: tid = lookup[(m, st)]
            img.alpha_composite(Image.fromarray(tile(tid)), (x * T, y * T))
for x in range(2, 28): over.append(("road_dash_horizontal", x, 15))
for x in range(1, 28):
    if not 8 <= x <= 10: over.append(("double_yellow_kerb_above", x, 13))
over += [("manhole_cover", 20, 16), ("drain_grate", 4, 16), ("tactile_paving", 6, 12), ("tactile_paving", 12, 12)]
for nm, x, y in over: img.alpha_composite(Image.fromarray(tile(byname[nm])), (x * T, y * T))
img.resize((img.width * 3, img.height * 3), Image.NEAREST).save(f"{OUT}/scene.png")
s = Image.fromarray(sheet); bg = Image.new("RGBA", s.size, (99, 171, 63, 255)); bg.alpha_composite(s)
bg.resize((s.width * 4, s.height * 4), Image.NEAREST).save(f"{OUT}/sheet_preview.png")
