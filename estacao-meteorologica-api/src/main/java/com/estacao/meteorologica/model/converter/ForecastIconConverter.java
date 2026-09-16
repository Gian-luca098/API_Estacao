package com.estacao.meteorologica.model.converter;

import com.estacao.meteorologica.model.enums.ForecastIcon;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converte {@link ForecastIcon} para o texto da coluna {@code forecasts.icon} e vice-versa,
 * usando sempre o valor de fio (ex.: "cloud-rain"), nunca o nome da constante Java.
 * {@code autoApply = true} aplica esse conversor a todo campo do tipo {@link ForecastIcon}
 * sem precisar anotar cada um com {@code @Convert}.
 */
@Converter(autoApply = true)
public class ForecastIconConverter implements AttributeConverter<ForecastIcon, String> {

    @Override
    public String convertToDatabaseColumn(ForecastIcon attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public ForecastIcon convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ForecastIcon.fromValue(dbData);
    }
}
