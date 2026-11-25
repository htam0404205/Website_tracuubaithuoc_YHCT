/* =====================================================================
   03_fulltext.sql  -  Full-Text Search (TÙY CHỌN)
   Chỉ chạy được nếu SQL Server đã cài tính năng Full-Text Search.
   Kiểm tra trước:  SELECT FULLTEXTSERVICEPROPERTY('IsFullTextInstalled');  -- phải trả về 1
   ===================================================================== */
USE YHCT;
GO
CREATE FULLTEXT CATALOG ftc_yhct WITH ACCENT_SENSITIVITY = OFF;   -- tìm "yeu thong" vẫn ra "Yêu thống"
GO
CREATE FULLTEXT INDEX ON dbo.BaiThuoc (TenBaiThuoc LANGUAGE 0, ThanhPhanNguyenVan LANGUAGE 0)
    KEY INDEX PK_BaiThuoc ON ftc_yhct WITH CHANGE_TRACKING AUTO;
CREATE FULLTEXT INDEX ON dbo.ViThuoc (TenViThuoc LANGUAGE 0, TenKhoaHocDuocLieu LANGUAGE 0)
    KEY INDEX PK_ViThuoc ON ftc_yhct WITH CHANGE_TRACKING AUTO;
GO
/* Thử nghiệm sau khi index xong:
   SELECT TenBaiThuoc FROM dbo.BaiThuoc WHERE CONTAINS(ThanhPhanNguyenVan, N'"can khương"');
*/
