import sys, numpy as np, json
from PIL import Image, ImageDraw
from scipy import ndimage
src, outp = sys.argv[1], sys.argv[2]
a = np.asarray(Image.open(src).convert("RGB")).astype(int); R, G, B = a[...,0], a[...,1], a[...,2]
bg = (R > 175) & (B > 175) & (G < 95)
for x in (383,384,385,386,766,767,768,769,1150,1151,1152,1153): bg[:, x] = True
for y in (259,260,261,262,263,504,505,506,507,508,759,760,761,762): bg[y] = True
bg[:, :3] = bg[:, -3:] = True; bg[:3] = bg[-3:] = True
fg = ~bg
lab, n = ndimage.label(ndimage.binary_dilation(fg, iterations=2))
objs = []
for i, s in enumerate(ndimage.find_objects(lab)):
    m = (lab[s] == i + 1) & fg[s]
    h, w = m.shape
    if m.sum() < 150: continue
    if w > 250 and h < 50: continue   # header bar
    y0, x0 = s[0].start, s[1].start
    panel = (0 if y0 < 261 else 1 if y0 < 506 else 2 if y0 < 761 else 3) * 4 + (0 if x0 < 385 else 1 if x0 < 767 else 2 if x0 < 1151 else 3)
    objs.append(dict(panel=panel + 1, x=x0, y=y0, w=w, h=h, label=i + 1, area=int(m.sum())))
objs.sort(key=lambda o: (o["panel"], o["y"] // 60, o["x"]))
im = Image.open(src).convert("RGB"); d = ImageDraw.Draw(im)
for k, o in enumerate(objs):
    o["id"] = k
    d.rectangle([o["x"], o["y"], o["x"] + o["w"], o["y"] + o["h"]], outline=(0, 255, 255))
    d.text((o["x"] + 2, o["y"] + 2), str(k), fill=(255, 255, 0))
im.save(outp)
json.dump(objs, open(outp + ".json", "w"))
for o in objs: print(o["id"], "K%d" % o["panel"], o["x"], o["y"], o["w"], "x", o["h"], o["area"])
