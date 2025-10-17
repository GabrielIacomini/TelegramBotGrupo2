package com.example.TelegramBotGrupo2.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HechoDTO {
    String id;
    String nombreColeccion;
    String titulo;
    List<String> etiquetas;
    String categoria;
    String ubicacion;
    String fecha;
    String origen;
}
