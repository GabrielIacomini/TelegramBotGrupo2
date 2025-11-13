package com.example.TelegramBotGrupo2.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusquedaReqDTO {
    private List<String> terminos;
    private Integer pageIdx;
    private Integer pageSize;
}
