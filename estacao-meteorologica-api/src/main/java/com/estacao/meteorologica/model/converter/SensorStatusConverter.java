package com.estacao.meteorologica.model.converter;

import com.estacao.meteorologica.model.enums.SensorStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converte {@link SensorStatus} para o texto da coluna {@code sensors.status} (minúsculas)
 * e vice-versa. {@code autoApply = true} aplica esse conversor a todo campo do tipo
 * {@link SensorStatus} automaticamente.
 */
@Converter(autoApply = true)
public class SensorStatusConverter implements AttributeConverter<SensorStatus, String> {

    @Override
    public String convertToDatabaseColumn(SensorStatus attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public SensorStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : SensorStatus.fromValue(dbData);
    }
}
