from pathlib import Path
import pandas as pd

SRC = Path("data/official/processed/bai_thuoc_byt_qd3080.csv")
OUT = Path("data/official/processed/bai_thuoc_byt_qd3080_clean.csv")
AUDIT = Path("data/official/processed/bai_thuoc_blank_source_rows.csv")

df = pd.read_csv(SRC, dtype=str).fillna("")

# Theo Phụ lục VI, mã 6586594 là một dòng trống trong nguồn.
# Không điền tên, không suy diễn. Giữ lại dòng này trong audit, loại khỏi
# dataset xử lý vì không phải một bản ghi bài thuốc có nội dung.
mask_blank_source = (
    df["MaBaiThuocDungChung"].eq("6586594")
    & df["TenBaiThuoc"].str.strip().eq("")
    & df["MaBaiThuoc"].str.strip().eq("")
)

audit = df.loc[mask_blank_source].copy()
audit["LyDo"] = "Dong trong trong Phu luc VI - QD 3080/QD-BYT; khong co ten/ma bai thuoc"
audit.to_csv(AUDIT, index=False, encoding="utf-8-sig")

clean = df.loc[~mask_blank_source].copy()
clean.to_csv(OUT, index=False, encoding="utf-8-sig")

print("=" * 78)
print("CLEAN DATASET BAI THUOC - BO Y TE")
print("=" * 78)
print("Dataset goc:", SRC)
print("So dong goc:", len(df))
print("Dong trong nguon loai khoi dataset chinh:", len(audit))
print("So dong bai thuoc sau clean:", len(clean))
print("Ma 6586594 co trong dataset chinh:", (clean["MaBaiThuocDungChung"]=="6586594").sum())
print("Ma trung:", clean["MaBaiThuocDungChung"].duplicated().sum())
print("Ten bai thuoc rong:", clean["TenBaiThuoc"].str.strip().eq("").sum())
print("Thanh phan 6586595:")
print(clean.loc[clean["MaBaiThuocDungChung"]=="6586595",
                ["MaBaiThuocDungChung","TenBaiThuoc","MaBaiThuoc","ThanhPhan_NguyenVan",
                 "TheLamSang","BenhDanhYHCT"]].to_string(index=False))
print("OUT:", OUT)
print("AUDIT:", AUDIT)
