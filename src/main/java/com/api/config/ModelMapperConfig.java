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


/**
 * Class ModelMapperConfig
 *
 * Configuration class for creating and setting up a ModelMapper bean.
 * Defines custom mappings between Card entity and its DTOs.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ModelMapperConfig {

    private final EncryptionUtil encryptionUtil;


    /**
     * Creates and configures a ModelMapper bean.
     * Adds custom converter to decrypt and mask card numbers.
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

    /**
     * Masks a card number, keeping only the last 4 digits.
     *
     * @param number decrypted card number
     */
    private String mask(String number) {
        if (number == null || number.length() < 4) return "****";
        return "****-****-****-" + number.substring(number.length() - 4);
    }
}
