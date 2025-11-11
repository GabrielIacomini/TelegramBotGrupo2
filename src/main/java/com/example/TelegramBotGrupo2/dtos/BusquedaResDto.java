package com.example.TelegramBotGrupo2.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.TelegramBotGrupo2.dtos.HechoDTO;

import java.util.List;
import java.util.Optional;

public class BusquedaResDto {
    public List<HechoDTO> hechos;
    public Integer total;
}