package com.byteforge.bot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequest {

    @NotBlank(message = "Query can't be blank")
    private String query;

    @NotBlank(message = "Static content context cannot be blank")
    private String staticContent;
}
