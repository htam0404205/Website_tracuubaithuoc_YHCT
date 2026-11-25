//package com.example.websiteyhct.controller;
//
//
//import com.example.websiteyhct.service.TraCuuService;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//@Controller
//public class TraCuuController {
//
//    private final TraCuuService service;
//
//    public TraCuuController(TraCuuService service) {
//        this.service = service;
//    }
//
//    @GetMapping("/")
//    public String trangChu(@RequestParam(defaultValue = "") String q,
//                           @RequestParam(defaultValue = "") String benhDanh,
//                           @RequestParam(defaultValue = "") String theLamSang,
//                           @RequestParam(defaultValue = "0") int page,
//                           Model model) {
//        model.addAttribute("q", q);
//        model.addAttribute("benhDanh", benhDanh);
//        model.addAttribute("theLamSang", theLamSang);
//        model.addAttribute("dsTheLamSang", service.danhSachTheLamSang());
//        model.addAttribute("trang", service.timBaiThuoc(q, benhDanh, theLamSang, page));
//        return "index";
//    }
//}