//package com.example.websiteyhct;
//
//
//import com.example.websiteyhct.entity.BaiThuoc;
//import com.example.websiteyhct.repository.BaiThuocRepository;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Component;
//
//import org.springframework.data.domain.Pageable;
//@Component
//public class KiemTraTamThoi implements CommandLineRunner {
//
//    private final BaiThuocRepository repo;
//
//    public KiemTraTamThoi(BaiThuocRepository repo) {
//        this.repo = repo;
//    }
//
//    @Override
//    public void run(String... args) {
//        thu("thử nghiệm", "");               // mong đợi 2: A, B
//        thu("", "");                          // mong đợi 3: tất cả
//        thu("khác", "");                      // mong đợi 1: C
//        thu("", "U62.392.5.02");              // mong đợi 1: C (lọc theo thể)
//        thu("THỬ NGHIỆM", "");                // chữ HOA: có ra 2 không?
//        thu("thu nghiem", "");                // không dấu: có ra 2 không?
//    }
//
//    private void thu(String q, String the) {
//        Page<BaiThuoc> kq = repo.timTheoTen(q, the, (Pageable) PageRequest.of(0, 10, Sort.by("tenBaiThuoc")));
//        System.out.println(">>> q='" + q + "' the='" + the + "' -> " + kq.getTotalElements() + " ket qua");
//        kq.forEach(b -> System.out.println("      " + b.getTenBaiThuoc()
//                + " | " + b.getTheLamSang().getTenTheLamSang()
//                + " | " + b.getTheLamSang().getBenhDanh().getTenBenhDanh()));
//    }
//}