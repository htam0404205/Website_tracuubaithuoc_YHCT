"""
EXTRACTOR DATA YHCT CHINH THUC - BO Y TE
Nguon:
- QD 3080/QD-BYT ngay 26/09/2025
- Phu luc V: Danh muc duoc lieu, vi thuoc co truyen
- Phu luc VI: Danh muc bai thuoc va ma bai thuoc theo the lam sang YHCT

Muc tieu:
1) Tai PDF truc tiep tu ydct.moh.gov.vn
2) Trich bang goc, khong suy dien
3) Luu raw tables de audit
4) Xuat CSV vi thuoc va bai thuoc
5) Khong sua/suy dien ten vi thuoc trong thanh phan bai thuoc
"""

from pathlib import Path
import sys
import re
import time
import hashlib

import requests
import pandas as pd
import ssl
from requests.adapters import HTTPAdapter
from urllib3.poolmanager import PoolManager

BASE = Path("data") / "official"
RAW = BASE / "raw"
OUT = BASE / "processed"
RAW.mkdir(parents=True, exist_ok=True)
OUT.mkdir(parents=True, exist_ok=True)

URL_V = "https://ydct.moh.gov.vn/static/files/uploads/dc130ff6d984bd105e58ec97f3f3fb779f883a426849f650f9fd9f57398779c9.pdf"
URL_VI = "https://ydct.moh.gov.vn/static/files/uploads/3db67f925f5b27e505c035a6d79009db5b1d159bfc36f10a5ac20c95f41114fc.pdf"

PDF_V = RAW / "QD3080_PhuLucV.pdf"
PDF_VI = RAW / "QD3080_PhuLucVI.pdf"

OUT_V = OUT / "vi_thuoc_byt_qd3080.csv"
OUT_B = OUT / "bai_thuoc_byt_qd3080.csv"
OUT_RAW_V = OUT / "phu_luc_V_tables_raw.csv"
OUT_RAW_B = OUT / "phu_luc_VI_tables_raw.csv"
REPORT = OUT / "official_dataset_validation.txt"


HEADERS = {
    "User-Agent": (
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
        "AppleWebKit/537.36 (KHTML, like Gecko) "
        "Chrome/140.0 Safari/537.36"
    )
}


class WeakDHAdapter(HTTPAdapter):
    def init_poolmanager(self, connections, maxsize, block=False, **pool_kwargs):
        ctx = ssl.create_default_context()
        ctx.set_ciphers("DEFAULT:@SECLEVEL=1")
        self.poolmanager = PoolManager(
            num_pools=connections,
            maxsize=maxsize,
            block=block,
            ssl_context=ctx,
            **pool_kwargs
        )

    def proxy_manager_for(self, proxy, **proxy_kwargs):
        ctx = ssl.create_default_context()
        ctx.set_ciphers("DEFAULT:@SECLEVEL=1")
        proxy_kwargs["ssl_context"] = ctx
        return super().proxy_manager_for(proxy, **proxy_kwargs)


def make_session():
    session = requests.Session()
    session.headers.update(HEADERS)
    adapter = WeakDHAdapter(
        max_retries=0,
        pool_connections=4,
        pool_maxsize=4
    )
    session.mount("https://", adapter)
    return session


SESSION = make_session()


def download(url, target):
    if target.exists() and target.stat().st_size > 10000:
        print(f"Da co file: {target}")
        return

    print(f"Tai: {url}")
    last = None

    for attempt in range(1, 6):
        try:
            r = SESSION.get(
                url,
                timeout=(30, 180),
                allow_redirects=True,
            )
            r.raise_for_status()

            content = r.content
            if not content.startswith(b"%PDF"):
                raise RuntimeError(
                    "Phan hoi khong phai PDF. "
                    f"Content-Type={r.headers.get('Content-Type')}"
                )

            target.write_bytes(content)
            print(
                f"  OK: {target} "
                f"({target.stat().st_size/1024/1024:.2f} MB)"
            )
            return

        except Exception as e:
            last = e
            print(f"  Lan {attempt}/5 loi: {e}")
            time.sleep(2 * attempt)

    raise RuntimeError(f"Khong tai duoc PDF: {url}\n{last}")


def clean(v):
    if v is None:
        return ""
    v = str(v)
    v = v.replace("\xa0", " ")
    v = re.sub(r"\s+", " ", v)
    return v.strip()


def try_pdfplumber(pdf_path, raw_output):
    try:
        import pdfplumber
    except ImportError:
        return None

    all_rows = []

    print(f"\nDoc bang bang pdfplumber: {pdf_path.name}")

    with pdfplumber.open(pdf_path) as pdf:
        print("  So trang:", len(pdf.pages))

        for pno, page in enumerate(pdf.pages, start=1):
            tables = page.extract_tables()

            for tno, table in enumerate(tables, start=1):
                if not table:
                    continue

                for rowno, row in enumerate(table, start=1):
                    vals = [clean(x) for x in row]
                    if not any(vals):
                        continue

                    all_rows.append({
                        "Page": pno,
                        "Table": tno,
                        "Row": rowno,
                        "Cells": " || ".join(vals),
                    })

    raw_df = pd.DataFrame(all_rows)
    raw_df.to_csv(
        raw_output,
        index=False,
        encoding="utf-8-sig"
    )

    return raw_df


def parse_v_rows(raw_df):
    """
    Phu luc V co 7 cot:
    Ma dung chung | Ten vi thuoc | Ma duoc lieu, vi thuoc |
    Nguon goc Nam | Bac | Ten KH duoc lieu/vi thuoc |
    Ten KH cay/con/khoang vat
    """
    rows = []

    if raw_df is None or raw_df.empty:
        return pd.DataFrame()

    for _, r in raw_df.iterrows():
        cells = [clean(x) for x in str(r["Cells"]).split("||")]

        # Loai header
        joined = " | ".join(cells).lower()
        if (
            "mã dùng chung" in joined
            or "ten vi thuoc" in joined
            or "tên vị thuốc" in joined
        ):
            continue

        # Tim ma 7 chu so trong cell dau
        code_index = None
        for i, c in enumerate(cells):
            if re.fullmatch(r"\d{7}", c):
                code_index = i
                break

        if code_index is None:
            continue

        cells = cells[code_index:]

        # Co the bi tach thanh 7 cot; neu nhieu hon, gom phan cuoi
        if len(cells) < 6:
            continue

        ma_dung_chung = cells[0]
        ten_vi_thuoc = cells[1] if len(cells) > 1 else ""
        ma_duoc_lieu = cells[2] if len(cells) > 2 else ""
        nam = cells[3] if len(cells) > 3 else ""
        bac = cells[4] if len(cells) > 4 else ""
        ten_kh_duoc_lieu = cells[5] if len(cells) > 5 else ""
        ten_kh_nguon = " | ".join(cells[6:]) if len(cells) > 6 else ""

        rows.append({
            "MaDungChung": ma_dung_chung,
            "TenViThuoc": ten_vi_thuoc,
            "MaDuocLieu_ViThuoc": ma_duoc_lieu,
            "NguonGoc_Nam": nam,
            "NguonGoc_Bac": bac,
            "TenKhoaHoc_DuocLieu": ten_kh_duoc_lieu,
            "TenKhoaHoc_Nguon": ten_kh_nguon,
            "NguonVanBan": "Quyet dinh 3080/QD-BYT",
            "NgayVanBan": "2025-09-26",
            "NguonURL": URL_V,
        })

    df = pd.DataFrame(rows)

    if df.empty:
        return df

    # Loai trung theo ma dung chung trong cung bang
    df = df.drop_duplicates(
        subset=["MaDungChung"],
        keep="first"
    )

    return df


def parse_b_rows(raw_df):
    """
    Phu luc VI co cac cot bien doi theo bang PDF.
    Khong co gang map thanh phan vao ma vi thuoc.
    Chi giu nguyen van cac dong thanh phan duoi bai thuoc.
    """
    rows = []

    if raw_df is None or raw_df.empty:
        return pd.DataFrame()

    current = None

    for _, r in raw_df.iterrows():
        cells = [clean(x) for x in str(r["Cells"]).split("||")]
        if not cells:
            continue

        joined = " | ".join(cells).lower()

        # Bo qua header
        if "mã bài thuốc" in joined and "tên bài thuốc" in joined:
            continue

        # Ma dung chung bai thuoc co dang 658xxxx
        code = None
        for i, c in enumerate(cells):
            if re.fullmatch(r"658\d{4}", c):
                code = c
                code_index = i
                break

        if code:
            # Neu gap bai moi, luu bai truoc
            if current:
                rows.append(current)

            tail = cells[code_index + 1:]

            # Tim chuoi ma bai thuoc U...
            ma_bai = ""
            ma_the = ""
            ten_bai = ""

            for x in tail:
                if re.fullmatch(r"U[\w.]+", x):
                    ma_bai = x
                elif not ten_bai and x:
                    ten_bai = x

            # Ten bai thuoc thuong nam ngay sau ma dung chung.
            if tail:
                candidates = [
                    x for x in tail
                    if x and not re.fullmatch(r"U[\w.]+", x)
                ]
                if candidates:
                    ten_bai = candidates[0]

            current = {
                "MaBaiThuocDungChung": code,
                "TenBaiThuoc": ten_bai,
                "MaBaiThuoc": ma_bai,
                "ThanhPhan_NguyenVan": "",
                "TheLamSang": "",
                "MaTheLamSang": "",
                "BenhDanhYHCT": "",
                "MaBenhDanhYHCT": "",
                "NguonVanBan": "Quyet dinh 3080/QD-BYT",
                "NgayVanBan": "2025-09-26",
                "NguonURL": URL_VI,
            }

            # Thu thap cac cell con lai trong dong bai
            if len(tail) > 1:
                current["ThanhPhan_NguyenVan"] = " | ".join(
                    x for x in tail[1:] if x
                )

            # Tim ma the U va cac ma con lai
            us = [x for x in tail if re.fullmatch(r"U[\w.]+", x)]
            if us:
                current["MaBaiThuoc"] = us[0]

            continue

        # Dong tiep theo cua cung bai: bo sung thanh phan/the/benh
        if current:
            nonempty = [x for x in cells if x]

            for x in nonempty:
                if re.fullmatch(r"U[\w.]+", x):
                    if not current["MaBaiThuoc"]:
                        current["MaBaiThuoc"] = x
                elif (
                    not current["TheLamSang"]
                    and "thể " in x.lower()
                ):
                    current["TheLamSang"] = x
                elif (
                    not current["BenhDanhYHCT"]
                    and (
                        "chứng " in x.lower()
                        or "bệnh " in x.lower()
                    )
                ):
                    current["BenhDanhYHCT"] = x
                else:
                    if x not in [
                        current["TenBaiThuoc"],
                        current["MaBaiThuoc"],
                    ]:
                        old = current["ThanhPhan_NguyenVan"]
                        current["ThanhPhan_NguyenVan"] = (
                            f"{old} | {x}" if old else x
                        )

    if current:
        rows.append(current)

    df = pd.DataFrame(rows)

    if df.empty:
        return df

    df = df.drop_duplicates(
        subset=["MaBaiThuocDungChung"],
        keep="first"
    )

    return df


def main():
    print("=" * 78)
    print("EXTRACT DATA YHCT CHINH THUC - BO Y TE")
    print("=" * 78)

    print("\n[1] KIEM TRA THU VIEN...")
    try:
        import pdfplumber  # noqa
        print("  pdfplumber: OK")
    except ImportError:
        print("  THIEU pdfplumber.")
        print("  Chay: pip install pdfplumber")
        sys.exit(1)

    print("\n[2] TAI PDF CHINH THUC...")
    print("  Dung SSL compatibility mode SECLEVEL=1 cho ydct.moh.gov.vn.")
    download(URL_V, PDF_V)
    download(URL_VI, PDF_VI)

    print("\n[3] TRICH BANG PHU LUC V...")
    raw_v = try_pdfplumber(PDF_V, OUT_RAW_V)
    if raw_v is None:
        raise RuntimeError("Khong doc duoc PDF Phu luc V.")

    vi = parse_v_rows(raw_v)
    vi.to_csv(
        OUT_V,
        index=False,
        encoding="utf-8-sig"
    )

    print("  So dong vi thuoc trich duoc:", len(vi))

    print("\n[4] TRICH BANG PHU LUC VI...")
    raw_b = try_pdfplumber(PDF_VI, OUT_RAW_B)
    if raw_b is None:
        raise RuntimeError("Khong doc duoc PDF Phu luc VI.")

    bai = parse_b_rows(raw_b)
    bai.to_csv(
        OUT_B,
        index=False,
        encoding="utf-8-sig"
    )

    print("  So dong bai thuoc trich duoc:", len(bai))

    print("\n[5] VALIDATE...")
    report = []
    report.append("=" * 78)
    report.append("VALIDATE DATASET YHCT - BO Y TE")
    report.append("=" * 78)
    report.append(f"Vi thuoc: {len(vi)}")
    report.append(f"Bai thuoc: {len(bai)}")

    if not vi.empty:
        report.append(
            f"MaDungChung vi thuoc trung: "
            f"{vi['MaDungChung'].duplicated().sum()}"
        )
        report.append(
            f"TenViThuoc rong: "
            f"{vi['TenViThuoc'].eq('').sum()}"
        )

    if not bai.empty:
        report.append(
            f"MaBaiThuocDungChung trung: "
            f"{bai['MaBaiThuocDungChung'].duplicated().sum()}"
        )
        report.append(
            f"TenBaiThuoc rong: "
            f"{bai['TenBaiThuoc'].eq('').sum()}"
        )

    report.append("")
    report.append("NGUYEN TAC:")
    report.append("- Nguon chi tu ydct.moh.gov.vn.")
    report.append("- Khong suy dien du lieu.")
    report.append("- Khong tu gan ma vi thuoc vao thanh phan bai thuoc.")
    report.append("- ThanhPhan_NguyenVan duoc giu de audit.")
    report.append("")
    report.append(f"FILE VI THUOC: {OUT_V}")
    report.append(f"FILE BAI THUOC: {OUT_B}")
    report.append(f"RAW V: {OUT_RAW_V}")
    report.append(f"RAW VI: {OUT_RAW_B}")

    REPORT.write_text(
        "\n".join(report),
        encoding="utf-8"
    )

    print("\n" + "\n".join(report))
    print("\nHOAN TAT EXTRACTION.")


if __name__ == "__main__":
    main()
