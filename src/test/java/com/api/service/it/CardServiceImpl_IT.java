package com.api.service.it;

import com.api.config.enums.CardStatus;
import com.api.config.enums.Role;
import com.api.dto.CardDto;
import com.api.dto.CardDtoNoId;
import com.api.dto.UserDto;
import com.api.entity.User;
import com.api.repository.UserRepository;
import com.api.service.interfaces.CardService;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class CardServiceImpl_IT {

    @Autowired
    private CardService cardService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Test
    public void addCard_shouldReturnMaskedNumber(){
        User user = userRepository.save(User.builder()
                .fullName("test user")
                .email("test@gmail.com")
                .password("test123user")
                .role(Role.USER)
                .build());

        CardDto cardDto = cardService.addCard(CardDtoNoId.builder()
                .number("4444-4444-4444-4444")
                .owner(modelMapper.map(user, UserDto.class))
                .expirationDate(LocalDateTime.of(2028,12,31,00,00,00))
                .status(CardStatus.active)
                .balance(new BigDecimal("1500.00"))
                .transactionLimitPerDay(new BigDecimal("1000.00"))
                .build());

        assertEquals("****-****-****-4444", cardDto.getNumber());

    }
    // TODO:
    @Test
    public void addCard_EncryptedNumberIsSavedInDB(){


    }
}