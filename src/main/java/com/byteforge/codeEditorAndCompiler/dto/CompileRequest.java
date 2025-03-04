package com.byteforge.codeEditorAndCompiler.dto;

import lombok.Data;

@Data
public class CompileRequest {
    // The Java source code. Expecting a public class Main with a main() method.
    private String code;

    // Optional input for the program (if any).
    private String input;
}

