package com.byteforge.codeEditorAndCompiler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompileResponse {
    // Standard output from running the code.
    private String output;

    // Any compilation or runtime error messages.
    private String error;
}
