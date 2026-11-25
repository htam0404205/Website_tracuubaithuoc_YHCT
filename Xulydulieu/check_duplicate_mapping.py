import pandas as pd

file="data/official/processed/bai_thuoc_vi_thuoc_FINAL_v5_mapping.csv"

df=pd.read_csv(file,dtype=str).fillna("")

dup=df[
    df.duplicated(
        subset=[
            "MaBaiThuocDungChung",
            "MaDungChungViThuoc"
        ],
        keep=False
    )
]

print("="*70)
print("DUPLICATE DETAIL")
print("="*70)

print(
    dup[
        [
        "MaBaiThuocDungChung",
        "ThanhPhan_NguyenVan",
        "MaDungChungViThuoc",
        "TrangThaiMapping"
        ]
    ].to_string(index=False)
)

dup.to_csv(
    "data/official/processed/mapping_duplicate_check.csv",
    index=False,
    encoding="utf-8-sig"
)