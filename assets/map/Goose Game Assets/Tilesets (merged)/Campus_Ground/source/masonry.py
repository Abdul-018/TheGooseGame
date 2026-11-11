"""Seamless masonry fills: cut real unit faces (bricks / setts) out of the source patch, shrink each to a small face,
then lay them on a fixed grid that repeats exactly every 16px (so all variants and edge tiles share the same joints)."""
import numpy as np, random
from scipy import ndimage

def unit_faces(a, box, kt, row_px, unit_px, face_wh, to_tile, inset=3):
    """Find rows by darkest-joint period, then units in each row by joint minima; return shrunk faces + mortar colour."""
    y0, y1, x0, x1 = box; I = a[y0 + kt + 2:y1 - kt - 2, x0 + kt + 2:x1 - kt - 2]; L = I.mean(-1)
    prof = L.mean(1); per = row_px
    ph = int(np.argmin([prof[i::per].mean() for i in range(per)]))
    rows = [(ph + k * per, ph + (k + 1) * per) for k in range(-1, len(prof) // per + 1)]
    faces, mortar = [], []
    for ra, rb in rows:
        ra, rb = max(ra, 0), min(rb, I.shape[0])
        if rb - ra < per * 0.8: continue
        band = L[ra + inset:rb - inset]; cp = ndimage.uniform_filter1d(band.mean(0), 3)
        thr = np.median(cp) - 0.8 * cp.std()
        joints = []; i = 0
        while i < len(cp):
            if cp[i] < thr:
                j = i
                while j < len(cp) and cp[j] < thr: j += 1
                joints.append((i + j) // 2); i = j
            else: i += 1
        for ja, jb in zip(joints, joints[1:]):
            if 0.6 * unit_px <= jb - ja <= 1.5 * unit_px:
                face = I[ra + inset:rb - inset, ja + inset:jb - inset]
                faces.append(to_tile(face, np.ones(face.shape[:2], bool), face_wh[0], face_wh[1]))
        mortar.append(I[ra:ra + 2].reshape(-1, 3))
    m = np.concatenate(mortar); dark = m[m.mean(-1) < np.percentile(m.mean(-1), 30)]
    return faces, np.median(dark, 0).astype(np.uint8)

def lay(faces, mortar, unit_w, unit_h, offset, seed, T=16):
    """Fill a TxT tile: courses of height unit_h (face + 1px joint below), units of width unit_w (face + 1px joint right),
    every other course shifted by `offset`. Wraps horizontally, so it tiles seamlessly."""
    rnd = random.Random(seed); t = np.zeros((T, T, 4), np.uint8); t[..., :3] = mortar; t[..., 3] = 255
    for r in range(T // unit_h):
        sh = (r % 2) * offset
        for k in range(T // unit_w):
            f = rnd.choice(faces); fh, fw = f.shape[:2]
            for yy in range(fh):
                for xx in range(fw):
                    if f[yy, xx, 3]: t[r * unit_h + yy, (sh + k * unit_w + xx) % T] = f[yy, xx]
    return t
