from pathlib import Path
import pandas as pd
import re

BASE = Path("data/official/processed")

SRC = BASE / "bai_thuoc_vi_thuoc_FINAL_v2_mapping.csv"

OUT = BASE / "bai_thuoc_vi_thuoc_FINAL_v5_mapping.csv"
AUDIT = BASE / "review_apply_V5_fix_audit.csv"
REMAIN = BASE / "review_final_remaining_v5.csv"


df = pd.read_csv(
    SRC,
    dtype=str,
    keep_default_na=False
).fillna("")


# mã chuẩn sau khi đã đối chiếu Bộ Y tế
MERGE = {
    "Xa tiền tử": "6581819",
    "Xuyên khung": "6581825",
    "Xuyên luyện tử": "6581826",
    "Xích thược": "6581822",
}


audit = []


for idx, row in df.iterrows():

    name = row["ThanhPhan_NguyenVan"]
    status = row["TrangThaiMapping"]

    if status != "REVIEW":
        continue


    if name in MERGE:

        old = row["UngVienReview"]

        new_code = MERGE[name]


        audit.append({
            "Dong": idx,
            "ThanhPhan": name,
            "MaCu": old,
            "MaMoi": new_code,
            "LyDo":
            "Trung ma duoc lieu + ten khoa hoc trong danh muc BYT"
        })


        df.at[idx,"MaDungChungViThuoc"] = new_code

        df.at[idx,"TrangThaiMapping"] = "OFFICIAL_ALIAS"

        df.at[idx,"DoTinCay"] = "1.000"



df.to_csv(
    OUT,
    index=False,
    encoding="utf-8-sig"
)


pd.DataFrame(audit).to_csv(
    AUDIT,
    index=False,
    encoding="utf-8-sig"
)


df[df["TrangThaiMapping"]=="REVIEW"].to_csv(
    REMAIN,
    index=False,
    encoding="utf-8-sig"
)


print("==============================")
print("REVIEW APPLY V5 FIX")
print("==============================")

print("So dong ap dung:", len(audit))

print(df["TrangThaiMapping"].value_counts())

print("")
print("OUT:", OUT)
print("AUDIT:", AUDIT)
print("REMAIN:", REMAIN)