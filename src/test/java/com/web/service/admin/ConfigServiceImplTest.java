package com.web.service.admin;

import com.web.dto.admin.ConfigResponse;
import com.web.dto.admin.ConfigUpdateRequest;
import com.web.entity.Config;
import com.web.entity.User;
import com.web.exception.ResourceNotFoundException;
import com.web.repository.ConfigRepository;
import com.web.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ConfigServiceImplTest {

    @Mock
    private ConfigRepository configRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ConfigServiceImpl configService;

    private User admin;
    private Config config;

    @BeforeEach
    void setUp() {
        admin = User.builder()
                .id(1L)
                .name("Admin")
                .email("admin@example.com")
                .role(User.Role.ADMIN)
                .build();

        config = Config.builder()
                .id(1L)
                .configKey("hold.duration.minutes")
                .configValue("10")
                .dataType(Config.DataType.INTEGER)
                .build();
    }

    @Test
    void shouldGetConfig_ReturnAllConfigValues() {
        // Given
        when(configRepository.findByConfigKey(anyString())).thenReturn(Optional.empty());

        // When
        ConfigResponse result = configService.getConfig();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.holdDurationMinutes()).isEqualTo(10);
        assertThat(result.overbookingMaxPercentage()).isEqualTo(0.05);
    }

    @Test
    void shouldUpdateConfig_WithValidRequest_UpdateValues() {
        // Given
        ConfigUpdateRequest request = new ConfigUpdateRequest(
                15, 10, 5, new HashMap<>(), BigDecimal.valueOf(25.0),
                BigDecimal.valueOf(6000), BigDecimal.valueOf(15000), 0.1,
                BigDecimal.valueOf(95), BigDecimal.valueOf(75), BigDecimal.valueOf(55),
                BigDecimal.valueOf(35), BigDecimal.ZERO,
                BigDecimal.valueOf(55000), BigDecimal.valueOf(1.2),
                BigDecimal.valueOf(1.3), BigDecimal.valueOf(1.15)
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(configRepository.findByConfigKey(anyString())).thenReturn(Optional.of(config));
        when(configRepository.save(any(Config.class))).thenReturn(config);

        // When
        ConfigResponse result = configService.updateConfig(request, 1L);

        // Then
        assertThat(result).isNotNull();
        verify(configRepository, atLeastOnce()).save(any(Config.class));
    }

    @Test
    void shouldGetRefundPercentage48Hours_ReturnConfiguredValue() {
        // Given
        Config refundConfig = Config.builder()
                .configKey("refund.policy.48hours.percentage")
                .configValue("95")
                .dataType(Config.DataType.DECIMAL)
                .build();

        when(configRepository.findByConfigKey("refund.policy.48hours.percentage"))
                .thenReturn(Optional.of(refundConfig));

        // When
        BigDecimal result = configService.getRefundPercentage48Hours();

        // Then
        assertThat(result).isEqualTo(BigDecimal.valueOf(95));
    }

    @Test
    void shouldGetTicketBasePrice_ReturnConfiguredValue() {
        // Given
        Config priceConfig = Config.builder()
                .configKey("ticket.base.price")
                .configValue("55000")
                .dataType(Config.DataType.DECIMAL)
                .build();

        when(configRepository.findByConfigKey("ticket.base.price"))
                .thenReturn(Optional.of(priceConfig));

        // When
        BigDecimal result = configService.getTicketBasePrice();

        // Then
        assertThat(result).isEqualTo(BigDecimal.valueOf(55000));
    }

    @Test
    void shouldGetOverbookingMaxPercentage_ReturnConfiguredValue() {
        // Given
        Config overbookingConfig = Config.builder()
                .configKey("overbooking.max.percentage")
                .configValue("0.1")
                .dataType(Config.DataType.DECIMAL)
                .build();

        when(configRepository.findByConfigKey("overbooking.max.percentage"))
                .thenReturn(Optional.of(overbookingConfig));

        // When
        Double result = configService.getOverbookingMaxPercentage();

        // Then
        assertThat(result).isEqualTo(0.1);
    }
}

