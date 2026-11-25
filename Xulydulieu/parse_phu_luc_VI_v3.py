from pathlib import Path
import re
import pandas as pd

RAW = Path("data/official/processed/phu_luc_VI_tables_raw.csv")
OUT_DIR = Path("data/official/processed")
OUT = OUT_DIR / "bai_thuoc_byt_qd3080_v3.csv"
AUDIT = OUT_DIR / "bai_thuoc_phu_luc_VI_placeholder_audit.csv"
REPORT = OUT_DIR / "bai_thuoc_byt_qd3080_v3_validation.txt"

CODE_RE = re.compile(r"(?<!\d)(\d{7})(?!\d)")
MA_RE = re.compile(r"U\d{2}(?:\.\d+)+(?:\.(?:CP|ĐP|NP)\d+)?", re.I)

def s(x):
    if x is None:
        return ""
    x = str(x)
    if x.lower() == "nan":
        return ""
    return re.sub(r"\s+", " ", x).strip()

def split_cells(v):
    parts = [s(x) for x in str(v).split(" || ")]
    if len(parts) < 6:
        parts += [""] * (6 - len(parts))
    elif len(parts) > 6:
        # Không làm mất dữ liệu: gộp phần dư vào cột cuối.
        parts = parts[:5] + [" | ".join(parts[5:])]
    return parts

def extract_code(text):
    m = CODE_RE.search(s(text))
    return m.group(1) if m else ""

def clean_watermark_text(text):
    x = s(text)
    if not x:
        return ""

    # Các mảnh watermark/ký số quan sát được trong PDF QĐ 3080.
    markers = [
        "0 9/ 2 _ n a u T",
        "0 9/ 2",
        "_ n a u T",
        "n a M n e y u g N",
        "n e y u g N",
        "_ ct d y",
        "2 3 7: 5 7:",
    ]
    positions = [x.find(m) for m in markers if x.find(m) >= 0]
    if positions:
        x = x[:min(positions)]

    # Mảnh ký tự đơn lẻ ở đầu ô do watermark.
    x = re.sub(r"^[a-z]\s+(?=[A-ZÀ-ỴĐ])", "", x)
    return s(x)

def extract_ma_bai_thuoc(text):
    x = s(text)

    # PDF đôi khi chèn thêm 1 chữ số trước phần mã, ví dụ U262... thay vì U62...
    x = re.sub(r"U(\d{3})(?=\.)", lambda m: "U" + m.group(1)[-2:], x)

    m = MA_RE.search(x)
    return m.group(0) if m else ""

def add_unique(lst, value):
    value = clean_watermark_text(value)
    if value and value not in lst:
        lst.append(value)

if not RAW.exists():
    raise FileNotFoundError(f"Khong tim thay: {RAW}")

df = pd.read_csv(RAW, dtype=str, keep_default_na=False).fillna("")
for c in ["Page", "Table", "Row"]:
    if c in df.columns:
        df[c] = pd.to_numeric(df[c], errors="coerce")
df = df.sort_values([c for c in ["Page", "Table", "Row"] if c in df.columns]).reset_index(drop=True)

records = []
placeholders = []
current = None
pending_prefix = {"ingredients": [], "clinical": [], "disease": []}
placeholder_active = False

def flush_current():
    global current
    if current is not None:
        records.append(current)
        current = None

for _, r in df.iterrows():
    cells = split_cells(r.get("Cells", ""))
    c0, c1, c2, c3, c4, c5 = cells
    code = extract_code(c0)

    if code:
        # Dòng chỉ có mã, còn 5 cột sau trống: coi là placeholder/page-break artifact.
        if not any([s(c1), s(c2), s(c3), s(c4), s(c5)]):
            flush_current()
            placeholders.append({
                "Page": r.get("Page", ""),
                "Table": r.get("Table", ""),
                "Row": r.get("Row", ""),
                "MaBaiThuocDungChung": code,
                "Cells": r.get("Cells", "")
            })
            pending_prefix = {"ingredients": [], "clinical": [], "disease": []}
            placeholder_active = True
            continue

        flush_current()

        name = clean_watermark_text(c1)
        ma = extract_ma_bai_thuoc(c2)

        current = {
            "MaBaiThuocDungChung": code,
            "TenBaiThuoc": name,
            "MaBaiThuoc": ma,
            "_ingredients": [],
            "_clinical": [],
            "_disease": [],
            "NguonVanBan": "Quyet dinh 3080/QD-BYT",
            "NgayVanBan": "2025-09-26",
            "NguonURL": "https://ydct.moh.gov.vn/static/files/uploads/3db67f925f5b27e505c035a6d79009db5b1d159bfc36f10a5ac20c95f41114fc.pdf",
            "_page_start": r.get("Page", "")
        }

        # Nếu trước dòng này có placeholder trống, các dòng thành phần tiếp theo
        # được xem là phần đầu của bản ghi thực bắt đầu ở trang sau.
        if placeholder_active:
            for x in pending_prefix["ingredients"]:
                add_unique(current["_ingredients"], x)
            for x in pending_prefix["clinical"]:
                add_unique(current["_clinical"], x)
            for x in pending_prefix["disease"]:
                add_unique(current["_disease"], x)
            pending_prefix = {"ingredients": [], "clinical": [], "disease": []}
            placeholder_active = False

        add_unique(current["_ingredients"], c3)
        add_unique(current["_clinical"], c4)
        add_unique(current["_disease"], c5)
        continue

    # Dòng không có mã: là continuation.
    if placeholder_active:
        add_unique(pending_prefix["ingredients"], c3)
        add_unique(pending_prefix["clinical"], c4)
        add_unique(pending_prefix["disease"], c5)
    elif current is not None:
        add_unique(current["_ingredients"], c3)
        add_unique(current["_clinical"], c4)
        add_unique(current["_disease"], c5)

flush_current()

out_rows = []
for rec in records:
    out_rows.append({
        "MaBaiThuocDungChung": rec["MaBaiThuocDungChung"],
        "TenBaiThuoc": rec["TenBaiThuoc"],
        "MaBaiThuoc": rec["MaBaiThuoc"],
        "ThanhPhan_NguyenVan": " | ".join(rec["_ingredients"]),
        "TheLamSang": " | ".join(rec["_clinical"]),
        "BenhDanhYHCT": " | ".join(rec["_disease"]),
        "NguonVanBan": rec["NguonVanBan"],
        "NgayVanBan": rec["NgayVanBan"],
        "NguonURL": rec["NguonURL"],
        "TrangBatDau": rec["_page_start"],
    })

out = pd.DataFrame(out_rows)
audit = pd.DataFrame(placeholders)

OUT_DIR.mkdir(parents=True, exist_ok=True)
out.to_csv(OUT, index=False, encoding="utf-8-sig")
audit.to_csv(AUDIT, index=False, encoding="utf-8-sig")

wm_re = re.compile(
    r"(n a u t|n a M|n e y u g N|_ ct d y|2 3 7: 5 7:|0 9/ 2)",
    re.I
)

dup_codes = int(out["MaBaiThuocDungChung"].duplicated().sum()) if not out.empty else 0
blank_names = int(out["TenBaiThuoc"].astype(str).str.strip().eq("").sum()) if not out.empty else 0
blank_ma = int(out["MaBaiThuoc"].astype(str).str.strip().eq("").sum()) if not out.empty else 0

wm_mask = pd.Series(False, index=out.index)
for col in ["TenBaiThuoc", "MaBaiThuoc", "ThanhPhan_NguyenVan", "TheLamSang", "BenhDanhYHCT"]:
    wm_mask = wm_mask | out[col].astype(str).str.contains(wm_re, na=False)
watermark_rows = int(wm_mask.sum())

nested_code_mask = out["ThanhPhan_NguyenVan"].astype(str).str.contains(r"(?<!\d)\d{7}(?!\d)", regex=True, na=False)
nested_code_rows = int(nested_code_mask.sum())

report = [
    "=" * 78,
    "VALIDATE BAI THUOC PHU LUC VI - PARSER V3",
    "=" * 78,
    f"So ban ghi: {len(out)}",
    f"Placeholder/page-break audit: {len(audit)}",
    f"Ma dung chung trung: {dup_codes}",
    f"Ten bai thuoc rong: {blank_names}",
    f"Ma bai thuoc rong: {blank_ma}",
    f"Dong con dau vet watermark: {watermark_rows}",
    f"Dong co ma 7 chu so chen trong ThanhPhan: {nested_code_rows}",
    "",
    f"OUT: {OUT}",
    f"AUDIT: {AUDIT}",
]
REPORT.write_text("\n".join(report), encoding="utf-8")

print("=" * 78)
print("PARSE PHU LUC VI - V3")
print("=" * 78)
for line in report[3:]:
    print(line)

print("\nKIEM TRA 6586595 / 6586621:")
print(out[out["MaBaiThuocDungChung"].isin(["6586595", "6586621"])].to_string(index=False))
