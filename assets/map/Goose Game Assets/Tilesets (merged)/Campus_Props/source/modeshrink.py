"""Colour-preserving downscale: quantise the full-res object to its own palette, then each output pixel takes
the dominant palette colour of its source block, preferring a clearly darker colour (outlines, gaps, detail
lines) when it covers a fair share of the block. No averaged/blended colours are ever created."""
import numpy as np
from PIL import Image

def lum(c): return c[..., 0] * 0.299 + c[..., 1] * 0.587 + c[..., 2] * 0.114

def shrink_mode(rgb, opaque, tw, th, k=16, dark_share=0.28, dark_gap=45):
    h, w = opaque.shape
    px = rgb[opaque].clip(0, 255).astype(np.uint8)
    pal_img = Image.fromarray(px[None]).quantize(min(k, max(2, len(np.unique(px, axis=0)))), method=Image.Quantize.MEDIANCUT)
    pal = np.array(pal_img.getpalette()[:3 * 256], float).reshape(-1, 3)
    idx = np.asarray(Image.fromarray(rgb.clip(0, 255).astype(np.uint8)).quantize(palette=pal_img, dither=Image.Dither.NONE))
    idx = np.where(opaque, idx.astype(np.int16), -1)
    ys = np.linspace(0, h, th + 1).round().astype(int); xs = np.linspace(0, w, tw + 1).round().astype(int)
    col = np.zeros((th, tw, 3)); m = np.zeros((th, tw), bool)
    L = lum(pal)
    for j in range(th):
        for i in range(tw):
            b = idx[ys[j]:max(ys[j + 1], ys[j] + 1), xs[i]:max(xs[i + 1], xs[i] + 1)].ravel()
            n = b.size; b = b[b >= 0]
            if b.size < 0.5 * n: continue
            cnt = np.bincount(b, minlength=len(pal)); share = cnt / b.size
            best = int(cnt.argmax())
            dark = np.where((share >= dark_share) & (L < L[best] - dark_gap))[0]
            if dark.size: best = int(dark[np.argmin(L[dark])])
            col[j, i] = pal[best]; m[j, i] = True
    return col, m

def shrink_mode_scale(rgb, opaque, scale, **kw):
    h, w = opaque.shape
    return shrink_mode(rgb, opaque, max(1, round(w / scale)), max(1, round(h / scale)), **kw)
