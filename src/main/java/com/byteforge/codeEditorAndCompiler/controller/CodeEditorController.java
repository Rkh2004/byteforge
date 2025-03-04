package com.byteforge.codeEditorAndCompiler.controller;

import com.byteforge.codeEditorAndCompiler.dto.CompileRequest;
import com.byteforge.codeEditorAndCompiler.dto.CompileResponse;
import com.byteforge.codeEditorAndCompiler.service.CodeEditorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CodeEditorController {

    private final CodeEditorService codeEditorService;

    public CodeEditorController(CodeEditorService codeEditorService) {
        this.codeEditorService = codeEditorService;
    }

    // Endpoint to compile and run Java code.
    // This endpoint is secured by JWT (as per your security configuration).
    @PostMapping("/compile")
    public ResponseEntity<CompileResponse> compileAndRun(@RequestBody CompileRequest request) {
        CompileResponse response = codeEditorService.compileAndRun(request);
        return ResponseEntity.ok(response);
    }
}
