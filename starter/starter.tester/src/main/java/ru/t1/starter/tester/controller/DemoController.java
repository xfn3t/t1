package ru.t1.starter.tester.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.starter.tester.service.DemoService;

@RestController
@RequestMapping("/demo")
public class DemoController {

    private final DemoService svc;

    public DemoController(DemoService svc) {
        this.svc = svc;
    }

    /** Первый вызов – займет ~300 мс и сохранит лог (metric > 200), следующий – из кеша (~0 мс) */
    @GetMapping("/expensive/{input}")
    public ResponseEntity<String> expensive(@PathVariable String input) throws InterruptedException {
        String result = svc.expensiveOperation(input);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/fail")
    public ResponseEntity<Void> fail() {
        svc.alwaysFail();
        return ResponseEntity.ok().build();
    }
}
