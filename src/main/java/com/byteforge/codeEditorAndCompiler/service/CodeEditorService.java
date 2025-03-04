package com.byteforge.codeEditorAndCompiler.service;

import com.byteforge.codeEditorAndCompiler.dto.CompileRequest;
import com.byteforge.codeEditorAndCompiler.dto.CompileResponse;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

@Service
public class CodeEditorService {

    public CompileResponse compileAndRun(CompileRequest request) {
        Path tempDir = null;
        try {
            // Create a temporary directory for the compilation process.
            tempDir = Files.createTempDirectory("codeeditor_");
            Path sourceFile = tempDir.resolve("Main.java");

            // Write the user-provided Java code into Main.java.
            Files.write(sourceFile, request.getCode().getBytes());

            // 1. Compile the Java code using Docker.
            ProcessBuilder compilePb = new ProcessBuilder(
                    "docker",
                    "run", "--rm",
                    "-v", tempDir.toAbsolutePath() + ":/code",
                    "openjdk:11",
                    "javac", "/code/Main.java"
            );
            Process compileProcess = compilePb.start();
            int compileExitCode = compileProcess.waitFor();

            // Capture compile error (if any).
            String compileError = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()))
                    .lines().collect(Collectors.joining("\n"));

            if (compileExitCode != 0) {
                // Compilation failed; return compile error.
                return new CompileResponse("", "Compilation Error:\n" + compileError);
            }

            // 2. Run the compiled code using Docker.

            ProcessBuilder runPb = new ProcessBuilder(
                    "docker", "run", "-i", "--rm",
                    "-v", tempDir.toAbsolutePath() + ":/code",
                    "openjdk:11",
                    "java", "-cp", "/code", "Main"
            );
            Process runProcess = runPb.start();

            // If input is provided, write it to the process's output stream.
            if (request.getInput() != null && !request.getInput().isEmpty()) {
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream()))) {
                    writer.write(request.getInput());
                    writer.newLine();  // Ensure a newline is sent.
                    writer.flush();
                }
            }

            // Wait for the process to complete.
            int runExitCode = runProcess.waitFor();

            // Read the standard output.
            String output = new BufferedReader(new InputStreamReader(runProcess.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));

            // Read the runtime error stream.
            String runtimeError = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()))
                    .lines().collect(Collectors.joining("\n"));

            if (runExitCode != 0) {
                return new CompileResponse(output, "Runtime Error:\n" + runtimeError);
            } else {
                return new CompileResponse(output, runtimeError);
            }
        } catch (Exception e) {
            return new CompileResponse("", "Exception: " + e.getMessage());
        } finally {
            // Clean up the temporary directory.
            if (tempDir != null) {
                try {
                    Files.walk(tempDir)
                            .sorted((a, b) -> b.compareTo(a)) // delete children first
                            .forEach(path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException e) {
                                    // Log error during cleanup
                                }
                            });
                } catch (IOException e) {
                    // Log error during directory deletion
                }
            }
        }
    }
}
