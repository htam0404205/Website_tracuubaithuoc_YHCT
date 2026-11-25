import pandas as pd

file = "data/official/processed/bai_thuoc_vi_thuoc_FINAL_v5_mapping.csv"

df = pd.read_csv(file, dtype=str).fillna("")

print("="*70)
print("FINAL VALIDATION MAPPING")
print("="*70)

print("So dong:", len(df))

print("\nTrangThaiMapping:")
print(df["TrangThaiMapping"].value_counts())

print("\nMa vi thuoc rong:")
print(
    (df["MaDungChungViThuoc"]=="").sum()
)

print("\nThanh phan rong:")
print(
    (df["ThanhPhan_NguyenVan"]=="").sum()
)

print("\nDuplicate mapping:")
print(
    df.duplicated(
        subset=[
            "MaBaiThuocDungChung",
            "MaDungChungViThuoc"
        ]
    ).sum()
)

print("="*70)