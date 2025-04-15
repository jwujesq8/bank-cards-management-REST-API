package com.api.config;

import com.api.dto.CardDto;
import com.api.dto.CardDtoNoId;
import com.api.entity.Card;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ModelMapperConfig {

    private final EncryptionUtil encryptionUtil;

    /**
     * Creates and returns a {@link ModelMapper} instance as a Spring bean.
     * This bean is used for mapping between different object types, simplifying the process of converting one object
     * to another, especially in scenarios like DTOs to entities or vice versa.
     *
     * @return a new {@link ModelMapper} instance.
     */
    @Bean
    public ModelMapper modelMapper(){

        ModelMapper modelMapper = new ModelMapper();
        Converter<String, String> decryptAndMaskConverter = ctx -> {
            String encrypted = ctx.getSource();
            if (encrypted == null) return "****";
            try {
                String decrypted = encryptionUtil.decrypt(encrypted);

                return mask(decrypted);
            } catch (Exception e) {
                return "****";
            }
        };
        modelMapper.typeMap(Card.class, CardDto.class).addMappings(mapper -> {
            mapper.using(decryptAndMaskConverter).map(Card::getNumber, CardDto::setNumber);
        });
        modelMapper.typeMap(Card.class, CardDtoNoId.class).addMappings(mapper -> {
            mapper.using(decryptAndMaskConverter).map(Card::getNumber, CardDtoNoId::setNumber);
        });
        return modelMapper;
    }

    private String mask(String number) {
        if (number == null || number.length() < 4) return "****";
        return "****-****-****-" + number.substring(number.length() - 4);
    }
}
