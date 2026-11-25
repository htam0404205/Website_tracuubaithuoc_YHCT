from pathlib import Path
import re, unicodedata
from difflib import SequenceMatcher
import pandas as pd

BASE = Path("data/official/processed")
VI = BASE / "vi_thuoc_byt_qd3080_FINAL.csv"
BAI = BASE / "bai_thuoc_byt_qd3080_FINAL.csv"
OUT = BASE / "bai_thuoc_vi_thuoc_FINAL_mapping.csv"
REVIEW = BASE / "bai_thuoc_vi_thuoc_FINAL_review.csv"
REPORT = BASE / "bai_thuoc_vi_thuoc_FINAL_summary.txt"

def n(s):
    s = "" if s is None else str(s)
    if s.lower() == "nan": return ""
    s = unicodedata.normalize("NFC", s).lower().strip()
    s = re.sub(r"\s+", " ", s)
    return s.strip(" .;:,")

def aliases(name):
    name = str(name or "").strip()
    out = set()
    if not name: return out
    out.add(n(name))
    base = re.split(r"\s*\(", name, maxsplit=1)[0].strip()
    if base:
        for p in base.split("/"):
            if p.strip(): out.add(n(p))
    m = re.search(r"\(([^)]*)\)", name)
    if m:
        for p in re.split(r"[,;/]", m.group(1)):
            if p.strip(): out.add(n(p))
    return {x for x in out if x}

vi = pd.read_csv(VI, dtype=str, keep_default_na=False).fillna("")
bai = pd.read_csv(BAI, dtype=str, keep_default_na=False).fillna("")

full_index, alias_index = {}, {}
for _, r in vi.iterrows():
    code, name = r["MaDungChung"], r["TenViThuoc"]
    full_index.setdefault(n(name), []).append((code,name))
    for a in aliases(name):
        alias_index.setdefault(a, []).append((code,name))

rows=[]
for _, b in bai.iterrows():
    ingredients=[x.strip() for x in str(b["ThanhPhan_NguyenVan"]).split("|") if x.strip()]
    for pos, ing in enumerate(ingredients,1):
        key=n(ing)
        full=full_index.get(key,[])
        ah=alias_index.get(key,[])
        status="NOT_FOUND"; code=""; name=""; candidates=""; score=""

        if len(full)==1:
            status="EXACT"; code,name=full[0]; score="1.000"
        elif len(full)>1:
            status="REVIEW"
            candidates=" | ".join(f"{c}:{nm}" for c,nm in full)
            score="1.000"
        elif len(ah)==1:
            status="OFFICIAL_ALIAS"; code,name=ah[0]; score="1.000"
        elif len(ah)>1:
            status="REVIEW"
            candidates=" | ".join(f"{c}:{nm}" for c,nm in ah)
            score="1.000"
        else:
            scored=[]
            for _, vr in vi.iterrows():
                best=max([SequenceMatcher(None,key,a).ratio() for a in aliases(vr["TenViThuoc"])] or [0])
                if best>=0.72:
                    scored.append((best,vr["MaDungChung"],vr["TenViThuoc"]))
            scored=sorted(scored,reverse=True)[:5]
            if scored:
                candidates=" | ".join(f"{c}:{nm}({sc:.3f})" for sc,c,nm in scored)
                score=f"{scored[0][0]:.3f}"

        rows.append({
            "MaBaiThuocDungChung":b["MaBaiThuocDungChung"],
            "TenBaiThuoc":b["TenBaiThuoc"],
            "ViTriThanhPhan":pos,
            "ThanhPhan_NguyenVan":ing,
            "TrangThaiMapping":status,
            "MaDungChungViThuoc":code,
            "TenViThuoc_Khop":name,
            "DoTinCay":score,
            "UngVienReview":candidates,
        })

m=pd.DataFrame(rows)
r=m[m["TrangThaiMapping"].isin(["REVIEW","NOT_FOUND"])].copy()
m.to_csv(OUT,index=False,encoding="utf-8-sig")
r.to_csv(REVIEW,index=False,encoding="utf-8-sig")

counts=m["TrangThaiMapping"].value_counts()
summary=[
    "="*78,
    "FINAL MAPPING BAI THUOC <-> VI THUOC",
    "="*78,
    f"So bai thuoc: {len(bai)}",
    f"So vi thuoc: {len(vi)}",
    f"Tong dong thanh phan: {len(m)}",
]
for k in ["EXACT","OFFICIAL_ALIAS","REVIEW","NOT_FOUND"]:
    summary.append(f"{k}: {int(counts.get(k,0))}")
summary += [
    "",
    "NGUYEN TAC:",
    "- EXACT: trung toan bo TenViThuoc duy nhat.",
    "- OFFICIAL_ALIAS: trung dung mot alias duoc ghi ro trong TenViThuoc chinh thuc.",
    "- REVIEW: alias/ten trung nhieu ma; khong tu chon.",
    "- NOT_FOUND: khong tu fuzzy-map; ung vien chi de tham khao.",
    "",
    f"OUT: {OUT}",
    f"REVIEW: {REVIEW}",
]
REPORT.write_text("\n".join(summary), encoding="utf-8")
print("\n".join(summary))
