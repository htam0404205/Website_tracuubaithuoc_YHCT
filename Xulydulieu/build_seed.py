"""
build_seed.py - Làm sạch 3 file CSV của Bộ Y tế và sinh script 02_seed_data.sql cho SQL Server.

Cách chạy:  python build_seed.py <thư mục chứa 3 file csv> <thư mục xuất>
Cần:        pip install pandas
"""
import re, sys, unicodedata, datetime, pathlib
import pandas as pd

SRC = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else ".")
OUT = pathlib.Path(sys.argv[2] if len(sys.argv) > 2 else ".")
log = []          # nội dung báo cáo làm sạch

def nfc(s): return unicodedata.normalize("NFC", s)
def norm(v):
    if v is None or (isinstance(v, float) and pd.isna(v)): return None
    s = re.sub(r"\s+", " ", nfc(str(v))).strip()
    return s or None

RESIDUE = re.compile(r"^.*?\|\s*\(\d+\)\s*\|\s*")           # "Tiêu đề cột | (5) | giá trị" -> "giá trị"
CODE_TAIL = re.compile(r"\s*\(U[0-9.]+\)\s*$")               # "Thể hàn thấp (U62.392.5.01)" -> "Thể hàn thấp"
HEADER_TOKEN = re.compile(r"^(Thành phần \(nếu có\)|\(\d+\))$")

def strip_residue(s):
    s = norm(s)
    if s and RESIDUE.match(s): s = RESIDUE.sub("", s, count=1)
    return s
def clean_name(s):
    s = strip_residue(s)
    return CODE_TAIL.sub("", s).strip() if s else s

ENTRY_CODE = re.compile(r"\((U[0-9.]+)\)\s*$")
def clean_entry(cell, code):
    """Ô có thể chứa nhiều mục 'Tên (mã) | Tên (mã)' do lỗi trích xuất PDF: lấy đúng mục có mã khớp cột mã."""
    s = strip_residue(cell)
    if not s: return s
    parts = [p.strip() for p in s.split("|")]
    if len(parts) > 1:
        match = [p for p in parts if (ENTRY_CODE.search(p) or [None, None])[1] == code]
        if match: return CODE_TAIL.sub("", match[0]).strip()
    return CODE_TAIL.sub("", s).strip()

bt = pd.read_csv(SRC / "bai_thuoc.csv")
vt = pd.read_csv(SRC / "vi_thuoc.csv")
mp = pd.read_csv(SRC / "mapping.csv")

# ---------- Văn bản nguồn ----------
assert bt["NguonVanBan"].nunique() == 1 and vt["NguonVanBan"].nunique() == 1
assert bt["NgayVanBan"].nunique() == 1 and vt["NgayVanBan"].nunique() == 1
VAN_BAN = dict(so_hieu="Quyết định 3080/QĐ-BYT",
               ten="Ban hành Danh mục mã dùng chung thuật ngữ y học cổ truyền (Đợt 2)",
               co_quan="Bộ Y tế", ngay=datetime.date.fromisoformat(str(bt["NgayVanBan"].iloc[0])))

# ---------- Bệnh danh / Thể lâm sàng ----------
bt["TenBaiThuoc"] = bt["TenBaiThuoc"].map(norm)

# ---------- Sửa tên bài thuốc bị lẫn chữ rác (watermark của PDF) ----------
VOWELS = set("aeiouyàáảãạăắằẳẵặâấầẩẫậèéẻẽẹêếềểễệìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵ")
LEGIT1 = set("ôýyưêâơă")                       # chữ cái đơn có thể là từ thật trong tiếng Việt
def tok_strong(w): return w.endswith(".") or bool(re.search(r"[_\d/]", w)) or (len(w) == 2 and not (set(w.lower()) & VOWELS))
def tok_single(w): return len(w) == 1 and w.isalpha() and w.isascii() and w.lower() not in LEGIT1
def is_noisy(t):
    ws = t.split() if t else []
    return any(tok_strong(w) for w in ws) or sum(tok_single(w) for w in ws) >= 2
def squash(t): return re.sub(r"\s+", "", t.lower())
def is_subseq(a, b):
    it = iter(b); return all(ch in it for ch in a)
def is_noise_token(w): return tok_strong(w) or tok_single(w)
canon = sorted({t for t in bt["TenBaiThuoc"] if t and not is_noisy(t)}, key=lambda x: -len(squash(x)))
def strip_noise(t):
    out = []
    for w in t.split():
        if re.fullmatch(r"[\d/_.]+", w): continue
        w2 = re.sub(r"^[\d_/]+|[\d_/]+$", "", w)
        if not w2 or tok_strong(w2) or tok_single(w2): continue
        out.append(w2)
    return " ".join(out)
def repair_name(t):
    hit = next((c for c in canon if is_subseq(squash(c), squash(t))), None)
    if hit: return hit, "khớp tên chuẩn"
    r = strip_noise(t)
    if r and r != t and any(len(x.strip("()")) >= 3 for x in r.split()): return r, "xóa ký tự rác"
    return None, None
PLACEHOLDER = "Bài thuốc (chưa rõ tên)"
fixed, lost = [], []
for i, t in bt["TenBaiThuoc"].items():
    if is_noisy(t):
        code, page = int(bt.at[i, "MaBaiThuocDungChung"]), int(bt.at[i, "TrangBatDau"])
        new, how = repair_name(t)
        if how:
            bt.at[i, "TenBaiThuoc"] = new; fixed.append((code, page, t, new, how))
        else:
            bt.at[i, "TenBaiThuoc"] = PLACEHOLDER; lost.append((code, page, t))
still = [(int(bt.at[i, "MaBaiThuocDungChung"]), t) for i, t in bt["TenBaiThuoc"].items() if is_noisy(t)]
assert not still, f"Còn tên nhiễu chưa sửa: {still}"
log.append(f"[Bài thuốc] tự sửa {len(fixed)} tên bị lẫn chữ rác (watermark PDF). Cần rà lại với PDF gốc:")
for code, page, old, new, how in fixed:
    log.append(f"      {code} (trang {page}): '{old}' -> '{new}' ({how})")
log.append(f"[Bài thuốc] {len(lost)} tên mất hẳn do chữ rác, đã tạm đặt '{PLACEHOLDER}' -> PHẢI sửa tay theo PDF gốc:")
for code, page, old in lost:
    log.append(f"      {code} (trang {page}): nguyên văn trong CSV = '{old}'")

for c in ["MaBaiThuoc", "MaTheLamSang", "MaBenhDanhYHCT"]: bt[c] = bt[c].map(norm)
bt["_tls"] = [clean_entry(a, c) for a, c in zip(bt["TheLamSang"], bt["MaTheLamSang"])]
bt["_bd"] = [clean_entry(a, c) for a, c in zip(bt["BenhDanhYHCT"], bt["MaBenhDanhYHCT"])]
multi = bt[bt["TheLamSang"].str.contains(r"\|", na=False) & ~bt["TheLamSang"].str.contains(r"\| \(\d+\) \|", na=False)]
for r in multi.itertuples():
    log.append(f"[Bài thuốc] {r.MaBaiThuocDungChung} (trang {r.TrangBatDau}): ô thể lâm sàng/bệnh danh chứa 2 mục do lỗi PDF -> lấy mục khớp mã {r.MaTheLamSang}")
bt["ThanhPhan_NguyenVan"] = bt["ThanhPhan_NguyenVan"].map(strip_residue)

def pick_names(df, code, name, label):
    rows = []
    for k, g in df.groupby(code):
        names = g[name].value_counts()
        if len(names) > 1:
            log.append(f"[{label}] mã {k} có {len(names)} tên khác nhau { {k: int(v) for k, v in names.items()} } -> chọn tên xuất hiện nhiều nhất: '{names.index[0]}'")
        rows.append((k, names.index[0]))
    return rows
benh_danh = pick_names(bt, "MaBenhDanhYHCT", "_bd", "Bệnh danh")
the_ls = []
for k, g in bt.groupby("MaTheLamSang"):
    the_ls.append((k, g["_tls"].value_counts().index[0], g["MaBenhDanhYHCT"].iloc[0]))
assert all(t[0].startswith(t[2] + ".") for t in the_ls), "Mã thể lâm sàng không thuộc mã bệnh danh"

# ---------- Vị thuốc: bỏ bản ghi trùng hoàn toàn (cùng tên + cùng mã dược liệu) ----------
for c in ["TenViThuoc", "MaDuocLieu_ViThuoc", "NguonGoc_Nam", "NguonGoc_Bac", "TenKhoaHoc_DuocLieu", "TenKhoaHoc_Nguon", "NguonURL"]:
    vt[c] = vt[c].map(norm)
ref = mp["MaDungChungViThuoc"].dropna().astype(int).value_counts()
vt["_ref"] = vt["MaDungChung"].map(ref).fillna(0).astype(int)
remap, drop = {}, set()
for (ten, ma), g in vt.groupby(["TenViThuoc", "MaDuocLieu_ViThuoc"], dropna=False):
    if len(g) > 1:
        keep = g.sort_values(["_ref", "MaDungChung"], ascending=[False, True]).iloc[0]["MaDungChung"]
        for code in g["MaDungChung"]:
            if code != keep: remap[int(code)] = int(keep); drop.add(int(code))
        log.append(f"[Vị thuốc] '{ten}' ({ma}) lặp {len(g)} lần -> giữ {int(keep)}, bỏ {sorted(set(map(int, g['MaDungChung'])) - {int(keep)})}")
vt_clean = vt[~vt["MaDungChung"].isin(drop)].copy()
for ten, g in vt_clean.groupby("TenViThuoc"):
    if len(g) > 1:
        log.append(f"[Vị thuốc] '{ten}' có {len(g)} bản ghi khác mã dược liệu {list(g['MaDuocLieu_ViThuoc'])} -> GIỮ CẢ HAI, cần đối chiếu PDF gốc")

# ---------- Thành phần (mapping) ----------
mp["TenGoc"] = mp["ThanhPhan_NguyenVan"].map(norm)
hdr = mp["TenGoc"].fillna("").map(lambda s: bool(HEADER_TOKEN.match(s)))
log.append(f"[Thành phần] bỏ {int(hdr.sum())} dòng rác là chữ tiêu đề cột: {mp.loc[hdr, 'TenGoc'].tolist()}")
mp = mp[~hdr].copy()
mp = mp.sort_values(["MaBaiThuocDungChung", "ViTriThanhPhan"])
mp["ViTri"] = mp.groupby("MaBaiThuocDungChung").cumcount() + 1
mp["MaVT"] = pd.Series([(remap.get(int(x), int(x)) if pd.notna(x) else None) for x in mp["MaDungChungViThuoc"]],
                       index=mp.index, dtype=object)
def fix_cand(s):
    s = norm(s)
    if not s: return None
    for old, new in remap.items(): s = s.replace(f"{old}:", f"{new}:")
    return s
mp["UngVien"] = mp["UngVienReview"].map(fix_cand)
valid_vt = set(vt_clean["MaDungChung"].astype(int))
assert all(pd.isna(x) or int(x) in valid_vt for x in mp["MaVT"]), "Thành phần trỏ tới vị thuốc không tồn tại"
assert mp["MaBaiThuocDungChung"].isin(bt["MaBaiThuocDungChung"]).all()

# ---------- Sinh SQL ----------
def lit(v):
    if v is None or (not isinstance(v, (str, datetime.date)) and pd.isna(v)): return "NULL"
    if isinstance(v, (bool,)): return "1" if v else "0"
    if isinstance(v, int) or hasattr(v, "item") and isinstance(v.item(), int): return str(int(v))
    if isinstance(v, float) or hasattr(v, "item") and isinstance(v.item(), float): return repr(float(v))
    if isinstance(v, datetime.date): return f"'{v.isoformat()}'"
    if str(v) == "@vb": return "@vb"
    return "N'" + str(v).replace("'", "''") + "'"

def insert(table, cols, rows, size=500):
    out = []
    for i in range(0, len(rows), size):
        chunk = rows[i:i + size]
        vals = ",\n".join("  (" + ", ".join(lit(x) for x in r) + ")" for r in chunk)
        out.append(f"INSERT INTO dbo.{table} ({', '.join(cols)}) VALUES\n{vals};")
    return "\n".join(out)

def n(x): return None if pd.isna(x) else int(x)
sql = ["/* 02_seed_data.sql - sinh tự động bởi build_seed.py. Chạy SAU 01_schema.sql. */",
       "USE YHCT;", "SET NOCOUNT ON; SET XACT_ABORT ON;", "BEGIN TRAN;", "DECLARE @vb INT;",
       insert("VanBanNguon", ["SoHieu", "TenVanBan", "CoQuanBanHanh", "NgayBanHanh"],
              [(VAN_BAN["so_hieu"], VAN_BAN["ten"], VAN_BAN["co_quan"], VAN_BAN["ngay"])]),
       "SET @vb = SCOPE_IDENTITY();",
       insert("BenhDanh", ["MaBenhDanh", "TenBenhDanh"], sorted(benh_danh)),
       insert("TheLamSang", ["MaTheLamSang", "TenTheLamSang", "MaBenhDanh"], sorted(the_ls)),
       insert("ViThuoc", ["MaDungChung", "TenViThuoc", "MaDuocLieu", "NguonGocNam", "NguonGocBac", "TenKhoaHocDuocLieu",
                          "TenKhoaHocNguon", "MaVanBan", "NguonUrl", "TrangNguon", "DongNguon"],
              [(int(r.MaDungChung), r.TenViThuoc, r.MaDuocLieu_ViThuoc, r.NguonGoc_Nam, r.NguonGoc_Bac, r.TenKhoaHoc_DuocLieu,
                r.TenKhoaHoc_Nguon, "@vb", r.NguonURL, n(r.TrangNguon), n(r.DongNguon)) for r in vt_clean.sort_values("MaDungChung").itertuples()]),
       insert("BaiThuoc", ["MaBaiThuocDungChung", "MaBaiThuoc", "TenBaiThuoc", "ThanhPhanNguyenVan", "MaTheLamSang",
                           "MaVanBan", "NguonUrl", "TrangBatDau", "TrangKetThuc"],
              [(int(r.MaBaiThuocDungChung), r.MaBaiThuoc, r.TenBaiThuoc, r.ThanhPhan_NguyenVan, r.MaTheLamSang, "@vb",
                norm(r.NguonURL), n(r.TrangBatDau), n(r.TrangKetThuc)) for r in bt.sort_values("MaBaiThuocDungChung").itertuples()]),
       insert("ThanhPhanBaiThuoc", ["MaBaiThuocDungChung", "ViTri", "TenGoc", "MaDungChungViThuoc", "TrangThai", "DoTinCay", "UngVienReview"],
              [(int(r.MaBaiThuocDungChung), int(r.ViTri), r.TenGoc, r.MaVT, r.TrangThaiMapping,
                None if pd.isna(r.DoTinCay) else round(float(r.DoTinCay), 3), r.UngVien) for r in mp.itertuples()]),
       "COMMIT;",
       "SELECT 'BaiThuoc' AS Bang, COUNT(*) AS SoDong FROM dbo.BaiThuoc UNION ALL SELECT 'ViThuoc', COUNT(*) FROM dbo.ViThuoc",
       "UNION ALL SELECT 'ThanhPhanBaiThuoc', COUNT(*) FROM dbo.ThanhPhanBaiThuoc UNION ALL SELECT 'TheLamSang', COUNT(*) FROM dbo.TheLamSang",
       "UNION ALL SELECT 'BenhDanh', COUNT(*) FROM dbo.BenhDanh UNION ALL SELECT 'VanBanNguon', COUNT(*) FROM dbo.VanBanNguon;"]
OUT.mkdir(parents=True, exist_ok=True)
(OUT / "02_seed_data.sql").write_text("\n".join(sql) + "\n", encoding="utf-8-sig")

counts = dict(BaiThuoc=len(bt), ViThuoc=len(vt_clean), ThanhPhanBaiThuoc=len(mp), TheLamSang=len(the_ls), BenhDanh=len(benh_danh), VanBanNguon=1)
stat = mp["TrangThaiMapping"].value_counts().to_dict()
log.append("[Cảnh báo nguồn] 'Xích đồng nam' (05V.57) mang tên khoa học Semen Plantaginis - đây là tên của Xa tiền tử. Nhiều khả năng sai từ nguồn hoặc lỗi trích xuất, cần đối chiếu PDF gốc.")
log.append(f"[Thành phần chưa khớp] {stat.get('NOT_FOUND',0)} NOT_FOUND + {stat.get('REVIEW',0)} REVIEW -> xử lý ở trang 'Rà soát liên kết'.")
rep = ["BÁO CÁO LÀM SẠCH DỮ LIỆU", "=" * 40, "Số dòng sẽ có trong CSDL sau khi chạy 02_seed_data.sql:"] + \
      [f"  {k}: {v}" for k, v in counts.items()] + ["", f"Trạng thái liên kết thành phần: {stat}", "", "Chi tiết xử lý:"] + [" - " + x for x in log]
(OUT / "bao_cao_lam_sach.txt").write_text("\n".join(rep) + "\n", encoding="utf-8")
print("\n".join(rep))
