import sys, json, numpy as np
from PIL import Image
sys.path.insert(0, sys.argv[2])
from segments import *
from process import shrink
from modeshrink import shrink_mode, shrink_mode_scale
cfg = json.load(open(sys.argv[1])); od = sys.argv[3]
src = {s["id"]: s for s in cfg["sheets"]}
def one_tile(group, k, sat=1.25):
    rgb, op = get(src[group]["src"], k); h, w = op.shape
    s = max(w, h) / 16; col, m = shrink_mode_scale(rgb, op, s); spr = finish(col, m, sat)
    return place(np.zeros((16, 16, 4), np.uint8), spr)

T = {}
# --- bike stands: one hoop per tile, with/without bike -----------------------------------------
rgb, op = get(src["bike_racks"]["src"], 4)                 # rack5_empty (row 2 middle)
h, w = op.shape
big_col, big_m = shrink_mode(rgb, op, round(w / 9.0), 12)    # squash so one hoop period ~10px, height 12
rack = finish(big_col, big_m, 1.0)
prof = col_profile(rack, slice(0, 5))                       # hoop tops live in the top rows
hoops = runs(prof > 0)
print("hoop runs", hoops, "rack", rack.shape)
mid = hoops[len(hoops) // 2]; c = (mid[0] + mid[1]) // 2
hoop = rack[:, max(0, c - 6):c + 6]
empty = place(np.zeros((16, 16, 4), np.uint8), hoop)
T["bike_stand_empty"] = empty
for nm, k in (("red", 0), ("blue", 1), ("yellow", 2)):
    bike = one_tile("bikes", k, 1.15)
    t = empty.copy(); a = bike[..., 3:4] > 0; t = np.where(a, bike, t); T[f"bike_stand_{nm}"] = t
# --- parked bikes, scooter: one tile each --------------------------------------------------------
for nm, k in (("bike_red", 0), ("bike_blue", 1), ("bike_yellow_basket", 2), ("escooter", 5)):
    T[nm] = one_tile("bikes", k, 1.15)
# lying bikes: 2 tiles (left/right halves)
for nm, k in (("bike_green_lying", 3), ("bike_abandoned", 4)):
    rgb, op = get(src["bikes"]["src"], k); h, w = op.shape
    s = max(w / 32, h / 16); col, m = shrink_mode_scale(rgb, op, s); spr = finish(col, m, 1.15)
    canvas = np.zeros((16, 32, 4), np.uint8); hh, ww = spr.shape[:2]
    x0 = (32 - ww) // 2; canvas[16 - hh:, x0:x0 + ww] = spr
    T[nm + "_L"] = canvas[:, :16].copy(); T[nm + "_R"] = canvas[:, 16:].copy()
# --- benches, bins, posts: one tile each ---------------------------------------------------------
for g in ("benches", "bins"):
    for k, nm in enumerate(src[g]["names"]): T[nm] = one_tile(g, k)
for k, nm in enumerate(src["posts"]["names"][:6]): T[nm] = one_tile("posts", k)
# --- runs: post-and-chain and barrier as left / middle / right ----------------------------------
def run3(k, name):
    rgb, op = get(src["posts"]["src"], k); h, w = op.shape
    s = h / 16; col, m = shrink_mode_scale(rgb, op, s); spr = finish(col, m)
    W = spr.shape[1]
    if name == "chain":
        posts = runs(col_profile(spr, slice(0, 4)) > 0)       # post tops
        cs = [(a + b) // 2 for a, b in posts]
        print("chain posts", cs, W)
        mid = cs[len(cs) // 2]; L = cs[0]; R = cs[-1]
        pieces = {"left": (L - 4, L + 12), "middle": (mid - 8, mid + 8), "right": (R - 12, R + 4)}
    else:
        pieces = {"left": (0, 16), "middle": (W // 2 - 8, W // 2 + 8), "right": (W - 16, W)}
    for p, (a, b) in pieces.items():
        t = np.zeros((16, 16, 4), np.uint8); a0, b0 = max(a, 0), min(b, W)
        t[:, a0 - a:a0 - a + (b0 - a0)] = spr[:, a0:b0]; T[f"{name}_{p}"] = t
run3(6, "chain"); run3(7, "barrier")

ROWS = [("bike stands and bikes", ["bike_stand_empty", "bike_stand_red", "bike_stand_blue", "bike_stand_yellow",
                                    "bike_red", "bike_blue", "bike_yellow_basket", "escooter",
                                    "bike_green_lying_L", "bike_green_lying_R", "bike_abandoned_L", "bike_abandoned_R"]),
        ("benches", src["benches"]["names"]),
        ("bins", src["bins"]["names"]),
        ("posts and barriers", src["posts"]["names"][:6] + ["chain_left", "chain_middle", "chain_right",
                                                            "barrier_left", "barrier_middle", "barrier_right"])]
NM = cfg["name"]; COLS = 16
img = Image.new("RGBA", (COLS * 16, len(ROWS) * 16), (0, 0, 0, 0)); index = []
for r, (g, names) in enumerate(ROWS):
    for c, nm in enumerate(names):
        img.alpha_composite(Image.fromarray(T[nm]), (c * 16, r * 16))
        index.append({"tile_id": r * COLS + c, "name": nm, "group": g, "tile_x": c, "tile_y": r})
img.save(f"{od}/{NM}.png")
json.dump(index, open(f"{od}/{NM}_index.json", "w"), indent=1)
open(f"{od}/{NM}.tsx", "w").write(
    f'<?xml version="1.0" encoding="UTF-8"?>\n<tileset version="1.10" tiledversion="1.12.2" name="{NM}" tilewidth="16" tileheight="16" '
    f'tilecount="{COLS*len(ROWS)}" columns="{COLS}">\n <image source="{NM}.png" width="{COLS*16}" height="{len(ROWS)*16}"/>\n</tileset>\n')
# preview: the tileset + a little scene on grass
A = cfg["assets"]
grass = (99, 171, 63, 255)
pv = Image.new("RGBA", (COLS * 16 + 60, len(ROWS) * 16 + 4 + 40), grass)
pv.alpha_composite(img, (20, 2))
pv.alpha_composite(Image.open(A + "/CozyValley_Premium_1.3/Tilesets/Chests.png").crop((0, 0, 16, 16)), (2, 2))
y = len(ROWS) * 16 + 14; x = 20
def put(nm):
    global x
    pv.alpha_composite(Image.fromarray(T[nm]), (x, y)); x += 16
for nm in ["bike_stand_empty", "bike_stand_red", "bike_stand_blue", "bike_stand_empty", "bike_stand_yellow"]: put(nm)
x += 16
for nm in ["chain_left", "chain_middle", "chain_middle", "chain_right"]: put(nm)
x += 16
for nm in ["barrier_left", "barrier_middle", "barrier_right"]: put(nm)
x += 16
for nm in ["bike_green_lying_L", "bike_green_lying_R"]: put(nm)
pv.resize((pv.width * 5, pv.height * 5), Image.NEAREST).save(f"{od}/{NM}_preview.png")
print("ok", len(index), "tiles")
