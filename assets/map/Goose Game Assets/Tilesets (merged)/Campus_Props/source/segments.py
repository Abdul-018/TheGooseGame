"""Helpers for props that are too wide for one tile: single hoops, repeatable runs, 2-tile splits."""
import numpy as np
from PIL import Image, ImageEnhance
from process import load, objects, outline

def shrink_wh(rgb, opaque, tw, th):
    pm = np.dstack([rgb * opaque[..., None], opaque.astype(float)])
    small = np.stack([np.asarray(Image.fromarray(pm[..., c].astype(np.float32), "F").resize((tw, th), Image.BOX)) for c in range(4)], -1)
    al = small[..., 3]
    return small[..., :3] / np.maximum(al, 1e-6)[..., None], al >= 0.5

def _unused(): pass

def finish(col, m, sat=1.0, ncol=24):
    col = outline(col, m)
    col = np.asarray(ImageEnhance.Color(Image.fromarray(col.clip(0, 255).astype(np.uint8))).enhance(sat)).astype(float)
    px = col[m].clip(0, 255).astype(np.uint8)
    pal = Image.fromarray(px[None]).quantize(min(ncol, max(2, len(np.unique(px, axis=0)))), method=Image.Quantize.MEDIANCUT)
    q = np.asarray(Image.fromarray(col.clip(0, 255).astype(np.uint8)).quantize(palette=pal, dither=Image.Dither.NONE).convert("RGB"))
    return np.dstack([q, (m * 255).astype(np.uint8)])

def get(src, k):
    a, mg = load(src); objs, lab = objects(mg); s, l = objs[k]
    return a[s], (lab[s] == l) & (mg[s] < 60)

def col_profile(rgba, rows):
    return (rgba[rows, :, 3] > 0).sum(0)

def runs(mask1d):
    out, start = [], None
    for i, v in enumerate(list(mask1d) + [False]):
        if v and start is None: start = i
        if not v and start is not None: out.append((start, i)); start = None
    return out

def place(tile_rgba, sprite, x=None, bottom=0):
    h, w = sprite.shape[:2]
    if x is None: x = (16 - w) // 2
    y = 16 - h - bottom
    reg = tile_rgba[y:y + h, x:x + w]
    a = sprite[..., 3:4] > 0
    reg[:] = np.where(a, sprite, reg)
    return tile_rgba
