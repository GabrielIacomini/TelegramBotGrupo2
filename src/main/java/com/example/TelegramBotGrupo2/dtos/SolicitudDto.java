package com.example.TelegramBotGrupo2.dtos;

import com.example.TelegramBotGrupo2.Enums.EstadoSolicitudBorradoEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SolicitudDto {
    String id;
    String descripcion;
    EstadoSolicitudBorradoEnum estado;
    String hechoId;
}
