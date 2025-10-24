package com.example.TelegramBotGrupo2.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PdIDTO {
    private String id;
    private String hechoId;
    private String descripcion;
    private String lugar;
    private String momento;
    private String contenido;
    private String urlImagen;
    private List<String> etiquetas;
}