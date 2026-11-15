import sys, numpy as np
sys.path.insert(0, "../props")
_src = open("../props/build_more.py").read(); _argv = sys.argv; sys.argv = ["x", "../props", ".", "."]
exec(_src[:_src.index("specs = json.load")]); sys.argv = _argv
def load_trees(p):
    a, mgx = load(p); fg = mgx < 120
    cand = fg.mean(1) > 0.97
    rows = np.nonzero(cand)[0]; out = []
    i = 0
    while i < len(rows):
        j = i
        while j + 1 < len(rows) and rows[j + 1] == rows[j] + 1: j += 1
        if rows[j] - rows[i] + 1 <= 6:
            mgx[max(0, rows[i] - 2):rows[j] + 3] = 255; out.append((int(rows[i]), int(rows[j])))
        i = j + 1
    return a, mgx, out
