package com.web.service.admin;

import com.web.dto.admin.ConfigResponse;
import com.web.dto.admin.ConfigUpdateRequest;
import com.web.entity.Config;
import com.web.entity.User;
import com.web.exception.ResourceNotFoundException;
import com.web.repository.ConfigRepository;
import com.web.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final ConfigRepository configRepository;
    private final UserRepository userRepository;

    //Obtener toda la configuracion del sistema
    @Override
    @Transactional(readOnly = true)
    public ConfigResponse getConfig() {

        Integer holdDuration = getIntegerConfig("hold.duration.minutes", 10);
        Integer overbookingPercentage = getIntegerConfig("overbooking.percentage", 5);
        Integer noShowFeePercentage = getIntegerConfig("no.show.fee.percentage", 10);
        BigDecimal baggageWeightLimit = getDecimalConfig("baggage.weight.limit", BigDecimal.valueOf(23.0));
        BigDecimal baggagePricePerKg = getDecimalConfig("baggage.price.per.kg", BigDecimal.valueOf(5000));

        // Configuraciones adicionales
        BigDecimal noShowFee = getNoShowFee();
        Double overbookingMaxPercentage = getOverbookingMaxPercentage();

        // Políticas de Reembolso
        BigDecimal refund48Hours = getRefundPercentage48Hours();
        BigDecimal refund24Hours = getRefundPercentage24Hours();
        BigDecimal refund12Hours = getRefundPercentage12Hours();
        BigDecimal refund6Hours = getRefundPercentage6Hours();
        BigDecimal refundLess6Hours = getRefundPercentageLess6Hours();

        // Precios de Tickets
        BigDecimal ticketBasePrice = getTicketBasePrice();
        BigDecimal ticketMultiplierPeakHours = getTicketPriceMultiplierPeakHours();
        BigDecimal ticketMultiplierHighDemand = getTicketPriceMultiplierHighDemand();
        BigDecimal ticketMultiplierMediumDemand = getTicketPriceMultiplierMediumDemand();

        // Descuentos desde configuración
        Map<String, Integer> discounts = getDiscountPercentages();

        return new ConfigResponse(
                holdDuration,
                noShowFeePercentage,
                overbookingPercentage,
                discounts,
                baggageWeightLimit,
                baggagePricePerKg,
                noShowFee,
                overbookingMaxPercentage,
                refund48Hours,
                refund24Hours,
                refund12Hours,
                refund6Hours,
                refundLess6Hours,
                ticketBasePrice,
                ticketMultiplierPeakHours,
                ticketMultiplierHighDemand,
                ticketMultiplierMediumDemand,
                LocalDateTime.now());
    }

    //Actualizar la configuracion
    @Override
    @Transactional
    public ConfigResponse updateConfig(ConfigUpdateRequest request, Long adminUserId) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", adminUserId));

        if (request.holdDurationMinutes() != null) {
            updateConfigValue("hold.duration.minutes", String.valueOf(request.holdDurationMinutes()), admin);
        }

        if (request.overbookingPercentage() != null) {
            updateConfigValue("overbooking.percentage", String.valueOf(request.overbookingPercentage()), admin);
        }

        if (request.noShowFeePercentage() != null) {
            updateConfigValue("no.show.fee.percentage", String.valueOf(request.noShowFeePercentage()), admin);
        }

        if (request.baggageWeightLimit() != null) {
            updateConfigValue("baggage.weight.limit", String.valueOf(request.baggageWeightLimit()), admin);
        }

        if (request.baggagePricePerKg() != null) {
            updateConfigValue("baggage.price.per.kg", String.valueOf(request.baggagePricePerKg()), admin);
        }

        // Descuentos
        if (request.discountPercentages() != null && !request.discountPercentages().isEmpty()) {
            for (Map.Entry<String, Integer> entry : request.discountPercentages().entrySet()) {
                String discountKey = "discount.percentage." + entry.getKey().toLowerCase();
                updateConfigValue(discountKey, String.valueOf(entry.getValue()), admin);
            }
        }

        // Configuraciones adicionales
        if (request.noShowFee() != null) {
            updateConfigValue("no.show.fee", String.valueOf(request.noShowFee()), admin);
        }

        if (request.overbookingMaxPercentage() != null) {
            updateConfigValue("overbooking.max.percentage", String.valueOf(request.overbookingMaxPercentage()), admin);
        }

        // Políticas de Reembolso
        if (request.refundPercentage48Hours() != null) {
            updateConfigValue("refund.policy.48hours.percentage",
                    String.valueOf(request.refundPercentage48Hours()), admin);
        }

        if (request.refundPercentage24Hours() != null) {
            updateConfigValue("refund.policy.24hours.percentage",
                    String.valueOf(request.refundPercentage24Hours()), admin);
        }

        if (request.refundPercentage12Hours() != null) {
            updateConfigValue("refund.policy.12hours.percentage",
                    String.valueOf(request.refundPercentage12Hours()), admin);
        }

        if (request.refundPercentage6Hours() != null) {
            updateConfigValue("refund.policy.6hours.percentage",
                    String.valueOf(request.refundPercentage6Hours()), admin);
        }

        if (request.refundPercentageLess6Hours() != null) {
            updateConfigValue("refund.policy.less.6hours.percentage",
                    String.valueOf(request.refundPercentageLess6Hours()), admin);
        }

        // Precios de Tickets
        if (request.ticketBasePrice() != null) {
            updateConfigValue("ticket.base.price",
                    String.valueOf(request.ticketBasePrice()), admin);
        }

        if (request.ticketPriceMultiplierPeakHours() != null) {
            updateConfigValue("ticket.price.multiplier.peak.hours",
                    String.valueOf(request.ticketPriceMultiplierPeakHours()), admin);
        }

        if (request.ticketPriceMultiplierHighDemand() != null) {
            updateConfigValue("ticket.price.multiplier.high.demand",
                    String.valueOf(request.ticketPriceMultiplierHighDemand()), admin);
        }

        if (request.ticketPriceMultiplierMediumDemand() != null) {
            updateConfigValue("ticket.price.multiplier.medium.demand",
                    String.valueOf(request.ticketPriceMultiplierMediumDemand()), admin);
        }

        return getConfig();
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getHoldDurationMinutes() {
        return getIntegerConfig("hold.duration.minutes", 10);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getBaggageWeightLimit() {
        return getDoubleConfig("baggage.weight.limit", 23.0);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getExcessFeePerKg() {
        return getDecimalConfig("baggage.price.per.kg", BigDecimal.valueOf(5000));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getNoShowFee() {
        return getDecimalConfig("no.show.fee", BigDecimal.valueOf(10000));
    }

    @Override
    @Transactional(readOnly = true)
    public Double getOverbookingMaxPercentage() {
        return getDoubleConfig("overbooking.max.percentage", 0.05);
    }

    // Políticas de Reembolso
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getRefundPercentage48Hours() {
        return getDecimalConfig("refund.policy.48hours.percentage", BigDecimal.valueOf(90));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getRefundPercentage24Hours() {
        return getDecimalConfig("refund.policy.24hours.percentage", BigDecimal.valueOf(70));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getRefundPercentage12Hours() {
        return getDecimalConfig("refund.policy.12hours.percentage", BigDecimal.valueOf(50));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getRefundPercentage6Hours() {
        return getDecimalConfig("refund.policy.6hours.percentage", BigDecimal.valueOf(30));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getRefundPercentageLess6Hours() {
        return getDecimalConfig("refund.policy.less.6hours.percentage", BigDecimal.ZERO);
    }

    // Precios de Tickets
    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTicketBasePrice() {
        return getDecimalConfig("ticket.base.price", BigDecimal.valueOf(50000));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTicketPriceMultiplierPeakHours() {
        return getDecimalConfig("ticket.price.multiplier.peak.hours", BigDecimal.valueOf(1.15));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTicketPriceMultiplierHighDemand() {
        return getDecimalConfig("ticket.price.multiplier.high.demand", BigDecimal.valueOf(1.2));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTicketPriceMultiplierMediumDemand() {
        return getDecimalConfig("ticket.price.multiplier.medium.demand", BigDecimal.valueOf(1.1));
    }

    private Integer getIntegerConfig(String key, Integer fallback) {
        return configRepository.findByConfigKey(key)
                .map(config -> {
                    try {
                        return Integer.parseInt(config.getConfigValue());
                    } catch (NumberFormatException e) {
                        return fallback;
                    }
                })
                .orElse(fallback);
    }

    private Double getDoubleConfig(String key, Double fallback) {
        return configRepository.findByConfigKey(key)
                .map(config -> {
                    try {
                        return Double.parseDouble(config.getConfigValue());
                    } catch (NumberFormatException e) {
                        return fallback;
                    }
                })
                .orElse(fallback);
    }

    private BigDecimal getDecimalConfig(String key, BigDecimal fallback) {
        return configRepository.findByConfigKey(key)
                .map(config -> {
                    try {
                        return new BigDecimal(config.getConfigValue());
                    } catch (NumberFormatException e) {
                        return fallback;
                    }
                })
                .orElse(fallback);
    }

    private void updateConfigValue(String key, String value, User updatedBy) {
        Config config = configRepository.findByConfigKey(key)
                .orElseGet(() -> {

                    return Config.builder()
                            .configKey(key)
                            .dataType(Config.DataType.STRING)
                            .build();
                });

        config.setConfigValue(value);
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy(updatedBy);

        configRepository.save(config);

    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Integer> getDiscountPercentages() {
        Map<String, Integer> discounts = new HashMap<>();
        // Valores por defecto
        discounts.put("STUDENT", getDiscountPercentage("STUDENT", 20));
        discounts.put("SENIOR", getDiscountPercentage("SENIOR", 15));
        discounts.put("CHILD", getDiscountPercentage("CHILD", 50));
        return discounts;
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getDiscountPercentage(String discountType, Integer fallback) {
        String key = "discount.percentage." + discountType.toLowerCase();
        return getIntegerConfig(key, fallback);
    }
}
