/* ============================================================================
   01_schema_v3.sql - CSDL YHCT, phien ban 3 (12 bang, 3 sequence)
   Chay trong SSMS, chon database YHCT. Chay lai duoc nhieu lan (xoa du lieu cu).

   THAY DOI SO VOI v2
   1. VanBanNguon : NgayBanHanh/SoHieu cho phep rong khi la sach/giao trinh
                    (them LoaiNguon + CHECK: van ban phap ly van bat buoc co so hieu, ngay).
   2. ViThuoc, BaiTDhuoc : MaVanBan cho phep rong (ban ghi do quan tri vien tu them).
   3. NhatKyHeThong : bo ON DELETE SET NULL (khong mat "ai da sua"); them TomTat.
   4. BaiThuoc, ViThuoc, CayThuoc : them NguoiTao, NguoiCapNhat (FK -> TaiKhoanAdmin).
   5. Ma tu sinh cho ban ghi tu them: 3 SEQUENCE bat dau o so lon, khong dung voi ma BYT.
   6. TaiKhoanAdmin : co du lieu mau VaiTro (xem cuoi file).
   7. Doi ten: DuocLieu -> CayThuoc, MaDuocLieuTuNhien -> MaCayThuoc (tranh nham voi
      ViThuoc.MaDuocLieu la ma duoc lieu cua Bo Y te); TenGoiKhacDuocLieu -> TenGoiKhac.
   8. TenGoiKhac : them nguon (MaVanBan, Trang), chi muc duy nhat loc theo lien ket.
   9. CayThuoc.BoPhanDung rut gon NVARCHAR(500); them TenLatinDuocLieu (tach tu BoPhanDung).
   10. ThanhPhanBaiThuoc : UngVienReview NVARCHAR(1000); CHECK lien ket TrangThai <-> ma vi thuoc.
   ============================================================================ */
USE YHCT;
GO

DROP TABLE IF EXISTS dbo.ThanhPhanBaiThuoc;
DROP TABLE IF EXISTS dbo.BaiThuoc;
DROP TABLE IF EXISTS dbo.TheLamSang;
DROP TABLE IF EXISTS dbo.BenhDanh;
DROP TABLE IF EXISTS dbo.TenGoiKhacDuocLieu;  
DROP TABLE IF EXISTS dbo.TenGoiKhac;
DROP TABLE IF EXISTS dbo.ViThuoc;
DROP TABLE IF EXISTS dbo.DuocLieu;            
DROP TABLE IF EXISTS dbo.CayThuoc;
DROP TABLE IF EXISTS dbo.HoDuocLieu;
DROP TABLE IF EXISTS dbo.NhatKyHeThong;
DROP TABLE IF EXISTS dbo.TaiKhoanAdmin;
DROP TABLE IF EXISTS dbo.VaiTro;
DROP TABLE IF EXISTS dbo.VanBanNguon;
DROP SEQUENCE IF EXISTS dbo.Seq_BaiThuoc;
DROP SEQUENCE IF EXISTS dbo.Seq_ViThuoc;
DROP SEQUENCE IF EXISTS dbo.Seq_CayThuoc;
GO

/* ---- Sequence cap ma cho ban ghi do quan tri vien tu them ----
   Ma cua Bo Y te co dang 658xxxx (khoang 6,5 trieu) va STT cua sach la 1..~3000,
   nen bat dau o so lon hon de khong bao gio trung.
   Khi nap du lieu co san, truyen ma ro rang (INSERT ... gia tri ma) -> bo qua gia tri mac dinh. */
CREATE SEQUENCE dbo.Seq_BaiThuoc AS BIGINT START WITH 9000001 INCREMENT BY 1;
CREATE SEQUENCE dbo.Seq_ViThuoc  AS BIGINT START WITH 9000001 INCREMENT BY 1;
CREATE SEQUENCE dbo.Seq_CayThuoc AS INT    START WITH 100001  INCREMENT BY 1;
GO

/* ============================================================================
   NHOM 1: NGUON DU LIEU, PHAN QUYEN, NHAT KY
   ============================================================================ */

/* 1. Van ban / sach / giao trinh lam nguon du lieu */
CREATE TABLE dbo.VanBanNguon (
    MaVanBan       INT IDENTITY(1,1) CONSTRAINT PK_VanBanNguon PRIMARY KEY,
    LoaiNguon      VARCHAR(20)   NOT NULL CONSTRAINT DF_VanBanNguon_Loai DEFAULT 'VAN_BAN'
        CONSTRAINT CK_VanBanNguon_Loai CHECK (LoaiNguon IN ('VAN_BAN','SACH','GIAO_TRINH')),
    SoHieu         NVARCHAR(80)  NULL,
    TenVanBan      NVARCHAR(500) NOT NULL,
    CoQuanBanHanh  NVARCHAR(200) NULL,            -- co quan ban hanh / nha xuat ban
    NgayBanHanh    DATE          NULL,
    GhiChu         NVARCHAR(500) NULL,
    NgayTao        DATETIME2     NOT NULL CONSTRAINT DF_VanBanNguon_NgayTao DEFAULT SYSUTCDATETIME(),
    NgayCapNhat    DATETIME2     NOT NULL CONSTRAINT DF_VanBanNguon_NgayCapNhat DEFAULT SYSUTCDATETIME(),
    -- Van ban phap ly bat buoc co so hieu va ngay ban hanh; sach/giao trinh thi khong
    CONSTRAINT CK_VanBanNguon_VanBan CHECK (LoaiNguon <> 'VAN_BAN' OR (SoHieu IS NOT NULL AND NgayBanHanh IS NOT NULL)),
    CONSTRAINT UQ_VanBanNguon_Loai_Ten UNIQUE (LoaiNguon, TenVanBan)
);
CREATE UNIQUE INDEX UX_VanBanNguon_SoHieu ON dbo.VanBanNguon(SoHieu) WHERE SoHieu IS NOT NULL;

/* 2. Vai tro phan quyen */
CREATE TABLE dbo.VaiTro (
    MaVaiTro       VARCHAR(20)   NOT NULL CONSTRAINT PK_VaiTro PRIMARY KEY,
    TenVaiTro      NVARCHAR(100) NOT NULL,
    MoTa           NVARCHAR(300) NULL,
    NgayTao        DATETIME2     NOT NULL CONSTRAINT DF_VaiTro_NgayTao DEFAULT SYSUTCDATETIME(),
    NgayCapNhat    DATETIME2     NOT NULL CONSTRAINT DF_VaiTro_NgayCapNhat DEFAULT SYSUTCDATETIME()
);

/* 3. Tai khoan quan tri */
CREATE TABLE dbo.TaiKhoanAdmin (
    MaTK             INT IDENTITY(1,1) CONSTRAINT PK_TaiKhoanAdmin PRIMARY KEY,
    TenDangNhap      VARCHAR(50)   NOT NULL CONSTRAINT UQ_TaiKhoanAdmin_Ten UNIQUE,
    MatKhauHash      VARCHAR(100)  NOT NULL,      -- chuoi BCrypt (60 ky tu), khong luu mat khau tho
    HoTen            NVARCHAR(100) NOT NULL,
    MaVaiTro         VARCHAR(20)   NOT NULL CONSTRAINT DF_TaiKhoanAdmin_VaiTro DEFAULT 'ADMIN',
    TrangThai        BIT           NOT NULL CONSTRAINT DF_TaiKhoanAdmin_TrangThai DEFAULT 1,  -- 1 = hoat dong, 0 = khoa
    LanDangNhapCuoi  DATETIME2     NULL,
    NgayTao          DATETIME2     NOT NULL CONSTRAINT DF_TaiKhoanAdmin_NgayTao DEFAULT SYSUTCDATETIME(),
    NgayCapNhat      DATETIME2     NOT NULL CONSTRAINT DF_TaiKhoanAdmin_NgayCapNhat DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_TaiKhoanAdmin_VaiTro FOREIGN KEY (MaVaiTro) REFERENCES dbo.VaiTro(MaVaiTro)
);

/* 4. Nhat ky he thong: ai lam gi, tren ban ghi nao, luc nao
   - MaTK de rong chi danh cho thao tac he thong (vd IMPORT_SEED).
   - KHONG dat khoa ngoai toi ban ghi bi xoa (KhoaChinhBanGhi la chu) de nhat ky con nguyen khi ban ghi bi xoa.
   - Khong cho xoa tai khoan da co nhat ky (khoa tai khoan bang TrangThai = 0). */
CREATE TABLE dbo.NhatKyHeThong (
    MaNhatKy         BIGINT IDENTITY(1,1) CONSTRAINT PK_NhatKyHeThong PRIMARY KEY,
    MaTK             INT           NULL,
    HanhDong         VARCHAR(30)   NOT NULL
        CONSTRAINT CK_NhatKy_HanhDong CHECK (HanhDong IN ('INSERT','UPDATE','DELETE','APPROVE_MAPPING','IMPORT_SEED','LOGIN')),
    TenBang          VARCHAR(60)   NOT NULL,
    KhoaChinhBanGhi  NVARCHAR(60)  NULL,
    TomTat           NVARCHAR(300) NULL,          -- vd ten bai thuoc tai thoi diem thao tac
    NoiDungCu        NVARCHAR(MAX) NULL,
    NoiDungMoi       NVARCHAR(MAX) NULL,
    ThoiGian         DATETIME2     NOT NULL CONSTRAINT DF_NhatKyHeThong_ThoiGian DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_NhatKyHeThong_TaiKhoan FOREIGN KEY (MaTK) REFERENCES dbo.TaiKhoanAdmin(MaTK)
);
CREATE INDEX IX_NhatKy_ThoiGian ON dbo.NhatKyHeThong(ThoiGian DESC);
CREATE INDEX IX_NhatKy_TenBang  ON dbo.NhatKyHeThong(TenBang, KhoaChinhBanGhi);
CREATE INDEX IX_NhatKy_TaiKhoan ON dbo.NhatKyHeThong(MaTK, ThoiGian DESC);

/* ============================================================================
   NHOM 2: PHAN CAP BENH DANH - THE LAM SANG
   Ma phan cap: benh danh U62.392.5 -> the lam sang U62.392.5.01 -> bai thuoc U62.392.5.01.DP001
   ============================================================================ */

/* 5. Benh danh YHCT (cap 1) */
CREATE TABLE dbo.BenhDanh (
    MaBenhDanh     VARCHAR(30)   NOT NULL CONSTRAINT PK_BenhDanh PRIMARY KEY,
    TenBenhDanh    NVARCHAR(200) NOT NULL,
    NgayTao        DATETIME2     NOT NULL CONSTRAINT DF_BenhDanh_NgayTao DEFAULT SYSUTCDATETIME(),
    NgayCapNhat    DATETIME2     NOT NULL CONSTRAINT DF_BenhDanh_NgayCapNhat DEFAULT SYSUTCDATETIME()
);
CREATE INDEX IX_BenhDanh_Ten ON dbo.BenhDanh(TenBenhDanh);

/* 6. The lam sang (cap 2) */
CREATE TABLE dbo.TheLamSang (
    MaTheLamSang   VARCHAR(30)   NOT NULL CONSTRAINT PK_TheLamSang PRIMARY KEY,
    TenTheLamSang  NVARCHAR(300) NOT NULL,
    MaBenhDanh     VARCHAR(30)   NOT NULL,
    NgayTao        DATETIME2     NOT NULL CONSTRAINT DF_TheLamSang_NgayTao DEFAULT SYSUTCDATETIME(),
    NgayCapNhat    DATETIME2     NOT NULL CONSTRAINT DF_TheLamSang_NgayCapNhat DEFAULT SYSUTCDATETIME(),
    CONSTRAINT FK_TheLamSang_BenhDanh FOREIGN KEY (MaBenhDanh) REFERENCES dbo.BenhDanh(MaBenhDanh)
);
CREATE INDEX IX_TheLamSang_BenhDanh ON dbo.TheLamSang(MaBenhDanh);

/* ============================================================================
   NHOM 3: CAY THUOC (tu sach) VA VI THUOC (QD 3080/QD-BYT)
   ============================================================================ */

/* 7. Ho thuc vat. Latin co the thieu (khoang 23% dong cua sach chi con ten Viet),
   nen chi bat buoc ten Viet; ten Latin da co thi duy nhat. */
CREATE TABLE dbo.HoDuocLieu (
    MaHo           INT IDENTITY(1,1) CONSTRAINT PK_HoDuocLieu PRIMARY KEY,
    TenHoViet      NVARCHAR(200) NOT NULL,
    TenHoKhoaHoc   VARCHAR(150)  NULL,
    NgayTao        DATETIME2     NOT NULL CONSTRAINT DF_HoDuocLieu_NgayTao DEFAULT SYSUTCDATETIME(),
    NgayCapNhat    DATETIME2     NOT NULL CONSTRAINT DF_HoDuocLieu_NgayCapNhat DEFAULT SYSUTCDATETIME(),
    CONSTRAINT UQ_HoDuocLieu_Viet_Latin UNIQUE (TenHoViet, TenHoKhoaHoc)
);
CREATE UNIQUE INDEX UX_HoDuocLieu_Latin ON dbo.HoDuocLieu(TenHoKhoaHoc) WHERE TenHoKhoaHoc IS NOT NULL;

/* 8. Cay thuoc (truoc day la DuocLieu) - Tuyen tap 3033 cay thuoc.
   MaCayThuoc = STT trong sach; cay do quan tri vien them lay tu Seq_CayThuoc. */
CREATE TABLE dbo.CayThuoc (
    MaCayThuoc          INT            NOT NULL
        CONSTRAINT DF_CayThuoc_Ma DEFAULT (NEXT VALUE FOR dbo.Seq_CayThuoc)
        CONSTRAINT PK_CayThuoc PRIMARY KEY,
    TrangPDF            INT            NULL,
    TenCayThuoc         NVARCHAR(300)  NOT NULL,   -- da tach sach tien to / lap ten
    TenKhoaHoc          NVARCHAR(400)  NULL,       -- ten loai
    MaHo                INT            NULL,
    TenLatinDuocLieu    NVARCHAR(300)  NULL,       -- vd "Radix Adenophorae tetraphyllae" (tach tu BoPhanDung)
    BoPhanDung          NVARCHAR(500)  NULL,       -- vd "Re", "Toan cay"
    BoPhanDungSuyDien   BIT            NOT NULL CONSTRAINT DF_CayThuoc_BoPhanSuyDien DEFAULT 0,  -- 1 = suy tu mo ta
    MoTa                NVARCHAR(MAX)  NULL,
    NoiSongThuHai       NVARCHAR(MAX)  NULL,
    ThanhPhanHoaHoc     NVARCHAR(MAX)  NULL,
    TinhViTacDung       NVARCHAR(MAX)  NULL,
    CongDung            NVARCHAR(MAX)  NULL,
    CachDungDonThuoc    NVARCHAR(MAX)  NULL,       -- thong tin y te: khong hien thi cong khai neu khong kem luu y
    MaVanBan            INT            NOT NULL,   -- sach nguon (bat buoc)
    NgayTao             DATETIME2      NOT NULL CONSTRAINT DF_CayThuoc_NgayTao DEFAULT SYSUTCDATETIME(),
    NgayCapNhat         DATETIME2      NOT NULL CONSTRAINT DF_CayThuoc_NgayCapNhat DEFAULT SYSUTCDATETIME(),
    NguoiTao            INT            NULL,       -- NULL = nap tu sach
    NguoiCapNhat        INT            NULL,
    CONSTRAINT FK_CayThuoc_Ho          FOREIGN KEY (MaHo)         REFERENCES dbo.HoDuocLieu(MaHo),
    CONSTRAINT FK_CayThuoc_VanBan      FOREIGN KEY (MaVanBan)     REFERENCES dbo.VanBanNguon(MaVanBan),
    CONSTRAINT FK_CayThuoc_NguoiTao    FOREIGN KEY (NguoiTao)     REFERENCES dbo.TaiKhoanAdmin(MaTK),
    CONSTRAINT FK_CayThuoc_NguoiCapNhat FOREIGN KEY (NguoiCapNhat) REFERENCES dbo.TaiKhoanAdmin(MaTK)
);
CREATE INDEX IX_CayThuoc_Ten      ON dbo.CayThuoc(TenCayThuoc);
CREATE INDEX IX_CayThuoc_KhoaHoc  ON dbo.CayThuoc(TenKhoaHoc);
CREATE INDEX IX_CayThuoc_Ho       ON dbo.CayThuoc(MaHo);

/* 9. Vi thuoc (Phu luc V, QD 3080/QD-BYT) */
CREATE TABLE dbo.ViThuoc (
    MaDungChung          BIGINT        NOT NULL
        CONSTRAINT DF_ViThuoc_Ma DEFAULT (NEXT VALUE FOR dbo.Seq_ViThuoc)
        CONSTRAINT PK_ViThuoc PRIMARY KEY,
    TenViThuoc           NVARCHAR(200) NOT NULL,
    MaDuocLieu           VARCHAR(30)   NULL,     -- ma duoc lieu theo Bo Y te (vd 05V.1)
    MaCayThuoc           INT           NULL,     -- lien ket sang cay thuoc trong sach (noi theo ten loai)
    NguonGocNam          CHAR(1)       NULL CONSTRAINT CK_ViThuoc_NguonGocNam CHECK (NguonGocNam = 'N'),
    NguonGocBac          CHAR(1)       NULL CONSTRAINT CK_ViThuoc_NguonGocBac CHECK (NguonGocBac = 'B'),
    TenKhoaHocDuocLieu   NVARCHAR(300) NULL,     -- ten Latin duoc lieu (Radix ..., Herba ...)
    TenKhoaHocNguon      NVARCHAR(500) NULL,     -- "ten loai - ho", giu nguyen chuoi goc
    MaVanBan             INT           NULL,     -- NULL = ban ghi do quan tri vien tu them
    NguonUrl             NVARCHAR(500) NULL,
    TrangNguon           INT           NULL,
    DongNguon            INT           NULL,
    NgayTao              DATETIME2     NOT NULL CONSTRAINT DF_ViThuoc_NgayTao DEFAULT SYSUTCDATETIME(),
    NgayCapNhat          DATETIME2     NOT NULL CONSTRAINT DF_ViThuoc_NgayCapNhat DEFAULT SYSUTCDATETIME(),
    NguoiTao             INT           NULL,
    NguoiCapNhat         INT           NULL,
    CONSTRAINT FK_ViThuoc_CayThuoc      FOREIGN KEY (MaCayThuoc)   REFERENCES dbo.CayThuoc(MaCayThuoc),
    CONSTRAINT FK_ViThuoc_VanBan        FOREIGN KEY (MaVanBan)     REFERENCES dbo.VanBanNguon(MaVanBan),
    CONSTRAINT FK_ViThuoc_NguoiTao      FOREIGN KEY (NguoiTao)     REFERENCES dbo.TaiKhoanAdmin(MaTK),
    CONSTRAINT FK_ViThuoc_NguoiCapNhat  FOREIGN KEY (NguoiCapNhat) REFERENCES dbo.TaiKhoanAdmin(MaTK)
);
CREATE INDEX IX_ViThuoc_Ten      ON dbo.ViThuoc(TenViThuoc);
CREATE INDEX IX_ViThuoc_CayThuoc ON dbo.ViThuoc(MaCayThuoc);

/* 10. Ten goi khac cua cay thuoc / vi thuoc */
CREATE TABLE dbo.TenGoiKhac (
    MaTenKhac            BIGINT IDENTITY(1,1) CONSTRAINT PK_TenGoiKhac PRIMARY KEY,
    MaCayThuoc           INT           NULL,
    MaDungChungViThuoc   BIGINT        NULL,
    TenGoiKhac           NVARCHAR(250) NOT NULL,
    LoaiTen              VARCHAR(30)   NOT NULL CONSTRAINT DF_TenGoiKhac_LoaiTen DEFAULT 'TEN_DAN_GIAN'
        CONSTRAINT CK_TenGoiKhac_LoaiTen CHECK (LoaiTen IN ('TEN_DAN_GIAN','OFFICIAL_ALIAS','TEN_HAN_VIET')),
    MaVanBan             INT           NULL,     -- nguon cua ten nay
    Trang                INT           NULL,
    NgayTao              DATETIME2     NOT NULL CONSTRAINT DF_TenGoiKhac_NgayTao DEFAULT SYSUTCDATETIME(),
    CONSTRAINT CK_TenGoiKhac_CoLienKet CHECK (MaCayThuoc IS NOT NULL OR MaDungChungViThuoc IS NOT NULL),
    CONSTRAINT FK_TenGoiKhac_CayThuoc FOREIGN KEY (MaCayThuoc)         REFERENCES dbo.CayThuoc(MaCayThuoc)  ON DELETE CASCADE,
    CONSTRAINT FK_TenGoiKhac_ViThuoc  FOREIGN KEY (MaDungChungViThuoc) REFERENCES dbo.ViThuoc(MaDungChung)  ON DELETE CASCADE,
    CONSTRAINT FK_TenGoiKhac_VanBan   FOREIGN KEY (MaVanBan)           REFERENCES dbo.VanBanNguon(MaVanBan)
);
CREATE INDEX IX_TenGoiKhac_Ten ON dbo.TenGoiKhac(TenGoiKhac);
CREATE UNIQUE INDEX UX_TenGoiKhac_CayThuoc ON dbo.TenGoiKhac(MaCayThuoc, TenGoiKhac)         WHERE MaCayThuoc IS NOT NULL;
CREATE UNIQUE INDEX UX_TenGoiKhac_ViThuoc  ON dbo.TenGoiKhac(MaDungChungViThuoc, TenGoiKhac) WHERE MaDungChungViThuoc IS NOT NULL;

/* ============================================================================
   NHOM 4: BAI THUOC VA THANH PHAN
   ============================================================================ */

/* 11. Bai thuoc (Phu luc VI, QD 3080/QD-BYT) */
CREATE TABLE dbo.BaiThuoc (
    MaBaiThuocDungChung  BIGINT         NOT NULL
        CONSTRAINT DF_BaiThuoc_Ma DEFAULT (NEXT VALUE FOR dbo.Seq_BaiThuoc)
        CONSTRAINT PK_BaiThuoc PRIMARY KEY,
    MaBaiThuoc           NVARCHAR(40)   NOT NULL CONSTRAINT UQ_BaiThuoc_Ma UNIQUE,  -- vd U62.392.5.01.DP001
    TenBaiThuoc          NVARCHAR(300)  NOT NULL,                                    -- ten KHONG duy nhat
    ThanhPhanNguyenVan   NVARCHAR(MAX)  NULL,
    MaTheLamSang         VARCHAR(30)    NOT NULL,
    MaVanBan             INT            NULL,     -- NULL = ban ghi do quan tri vien tu them
    NguonUrl             NVARCHAR(500)  NULL,
    TrangBatDau          INT            NULL,
    TrangKetThuc         INT            NULL,
    NgayTao              DATETIME2      NOT NULL CONSTRAINT DF_BaiThuoc_NgayTao DEFAULT SYSUTCDATETIME(),
    NgayCapNhat          DATETIME2      NOT NULL CONSTRAINT DF_BaiThuoc_NgayCapNhat DEFAULT SYSUTCDATETIME(),
    NguoiTao             INT            NULL,
    NguoiCapNhat         INT            NULL,
    CONSTRAINT CK_BaiThuoc_Trang CHECK (TrangBatDau IS NULL OR TrangKetThuc IS NULL OR TrangKetThuc >= TrangBatDau),
    CONSTRAINT FK_BaiThuoc_TheLamSang   FOREIGN KEY (MaTheLamSang) REFERENCES dbo.TheLamSang(MaTheLamSang),
    CONSTRAINT FK_BaiThuoc_VanBan       FOREIGN KEY (MaVanBan)     REFERENCES dbo.VanBanNguon(MaVanBan),
    CONSTRAINT FK_BaiThuoc_NguoiTao     FOREIGN KEY (NguoiTao)     REFERENCES dbo.TaiKhoanAdmin(MaTK),
    CONSTRAINT FK_BaiThuoc_NguoiCapNhat FOREIGN KEY (NguoiCapNhat) REFERENCES dbo.TaiKhoanAdmin(MaTK)
);
CREATE INDEX IX_BaiThuoc_Ten        ON dbo.BaiThuoc(TenBaiThuoc);
CREATE INDEX IX_BaiThuoc_TheLamSang ON dbo.BaiThuoc(MaTheLamSang);

/* 12. Thanh phan bai thuoc - bang trung gian N-N
   - Xoa bai thuoc -> xoa thanh phan (CASCADE).
   - Xoa vi thuoc dang duoc dung bi CSDL TU CHAN (khong CASCADE, khong SET NULL).
   - TrangThai gan voi ma vi thuoc: da doi chieu thi co ma, REVIEW/NOT_FOUND thi khong. */
CREATE TABLE dbo.ThanhPhanBaiThuoc (
    Id                   BIGINT IDENTITY(1,1) CONSTRAINT PK_ThanhPhanBaiThuoc PRIMARY KEY,
    MaBaiThuocDungChung  BIGINT        NOT NULL,
    ViTri                INT           NOT NULL CONSTRAINT CK_ThanhPhan_ViTri CHECK (ViTri > 0),
    TenGoc               NVARCHAR(300) NOT NULL,         -- giu nguyen van tu tai lieu nguon
    MaDungChungViThuoc   BIGINT        NULL,
    TrangThai            VARCHAR(20)   NOT NULL
        CONSTRAINT CK_ThanhPhan_TrangThai CHECK (TrangThai IN ('EXACT','OFFICIAL_ALIAS','NOT_FOUND','REVIEW','MANUAL')),
    DoTinCay             DECIMAL(4,3)  NULL CONSTRAINT CK_ThanhPhan_DoTinCay CHECK (DoTinCay BETWEEN 0 AND 1),
    UngVienReview        NVARCHAR(1000) NULL,
    NguoiDuyet           INT           NULL,             -- ai duyet ghep thu cong (MANUAL)
    NgayCapNhat          DATETIME2     NOT NULL CONSTRAINT DF_ThanhPhan_NgayCapNhat DEFAULT SYSUTCDATETIME(),
    CONSTRAINT CK_ThanhPhan_TrangThai_Ma CHECK (
        (TrangThai IN ('EXACT','OFFICIAL_ALIAS','MANUAL') AND MaDungChungViThuoc IS NOT NULL)
        OR (TrangThai IN ('REVIEW','NOT_FOUND') AND MaDungChungViThuoc IS NULL)
    ),
    CONSTRAINT UQ_ThanhPhan_ViTri      UNIQUE (MaBaiThuocDungChung, ViTri),
    CONSTRAINT FK_ThanhPhan_BaiThuoc   FOREIGN KEY (MaBaiThuocDungChung) REFERENCES dbo.BaiThuoc(MaBaiThuocDungChung) ON DELETE CASCADE,
    CONSTRAINT FK_ThanhPhan_ViThuoc    FOREIGN KEY (MaDungChungViThuoc)  REFERENCES dbo.ViThuoc(MaDungChung),
    CONSTRAINT FK_ThanhPhan_NguoiDuyet FOREIGN KEY (NguoiDuyet)          REFERENCES dbo.TaiKhoanAdmin(MaTK)
);
CREATE INDEX IX_ThanhPhan_ViThuoc   ON dbo.ThanhPhanBaiThuoc(MaDungChungViThuoc) INCLUDE (MaBaiThuocDungChung);
CREATE INDEX IX_ThanhPhan_TrangThai ON dbo.ThanhPhanBaiThuoc(TrangThai);
GO

/* ============================================================================
   DU LIEU MAU (chay duoc nhieu lan vi bang vua duoc tao lai)
   ============================================================================ */

INSERT INTO dbo.VaiTro (MaVaiTro, TenVaiTro, MoTa) VALUES
    ('ADMIN',  N'Quản trị viên',  N'Thêm, sửa, xóa dữ liệu; quản lý tài khoản; xem nhật ký'),
    ('EDITOR', N'Biên tập viên',  N'Thêm, sửa dữ liệu; không xóa, không quản lý tài khoản');

INSERT INTO dbo.VanBanNguon (LoaiNguon, SoHieu, TenVanBan, CoQuanBanHanh, NgayBanHanh, GhiChu) VALUES
    ('VAN_BAN', N'3080/QĐ-BYT',
        N'Quyết định 3080/QĐ-BYT - Phụ lục V (danh mục dược liệu, vị thuốc cổ truyền) và Phụ lục VI (danh mục bài thuốc, mã bài thuốc theo thể lâm sàng YHCT)',
        N'Bộ Y tế', '2025-09-26', N'Tải từ ydct.moh.gov.vn'),
    ('SACH', NULL, N'Tuyển tập 3033 cây thuốc đông y', NULL, NULL,
        N'Bổ sung tác giả, nhà xuất bản, năm xuất bản khi có thông tin');

/* Tai khoan quan tri dau tien: KHONG ghi mat khau vao file. Tao ma BCrypt tren may roi chay:
   INSERT INTO dbo.TaiKhoanAdmin (TenDangNhap, MatKhauHash, HoTen, MaVaiTro)
   VALUES ('ten_dang_nhap', '<CHUOI_BCRYPT>', N'Họ tên', 'ADMIN');                           */
GO
