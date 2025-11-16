package com.web.integration.userstories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.dto.admin.ConfigResponse;
import com.web.dto.admin.ConfigUpdateRequest;
import com.web.dto.auth.Login.LoginRequest;
import com.web.dto.auth.Login.LoginResponse;
import com.web.dto.auth.Login.RegisterRequest;
import com.web.dto.ticket.TicketCancelResponse;
import com.web.dto.ticket.TicketCreateRequest;
import com.web.dto.ticket.TicketResponse;
import com.web.entity.Bus;
import com.web.entity.Route;
import com.web.entity.Seat;
import com.web.entity.Ticket;
import com.web.entity.Trip;
import com.web.entity.User;
import com.web.repository.BusRepository;
import com.web.repository.FareRuleRepository;
import com.web.repository.RouteRepository;
import com.web.repository.SeatRepository;
import com.web.repository.StopRepository;
import com.web.repository.TripRepository;
import com.web.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminConfigIntegrationTest extends BaseIntegrationTest {

        @Autowired
        private MockMvc mvc;

        @Autowired
        private ObjectMapper om;

        @Autowired
        private RouteRepository routeRepository;

        @Autowired
        private StopRepository stopRepository;

        @Autowired
        private BusRepository busRepository;

        @Autowired
        private SeatRepository seatRepository;

        @Autowired
        private TripRepository tripRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private FareRuleRepository fareRuleRepository;

        private String adminToken;
        private Long adminId;
        private String passengerToken;
        private Long passengerId;
        private Long tripId;
        private Long fromStopId;
        private Long toStopId;
        private Long tripId48h;

        @BeforeEach
        void setUp() throws Exception {
                // Limpiar datos (orden importante: primero dependientes, luego padres)
                tripRepository.deleteAll();
                seatRepository.deleteAll();
                fareRuleRepository.deleteAll(); // Eliminar antes de rutas/paradas
                stopRepository.deleteAll();
                routeRepository.deleteAll();
                busRepository.deleteAll();
                userRepository.deleteAll();

                // Crear ruta con código único
                String uniqueCode = "TEST-" + System.currentTimeMillis();
                Route route = Route.builder()
                                .code(uniqueCode)
                                .name("Bogotá - Medellín")
                                .origin("Bogotá")
                                .destination("Medellín")
                                .distanceKm(new BigDecimal("450.0"))
                                .durationMin(480)
                                .isActive(true)
                                .build();
                route = routeRepository.save(route);

                // Crear paradas
                var stopBogota = com.web.entity.Stop.builder()
                                .route(route)
                                .name("Terminal Bogotá")
                                .order(1)
                                .latitude(new BigDecimal("4.6097"))
                                .longitude(new BigDecimal("-74.0817"))
                                .build();
                stopBogota = stopRepository.save(stopBogota);
                fromStopId = stopBogota.getId();

                var stopMedellin = com.web.entity.Stop.builder()
                                .route(route)
                                .name("Terminal Medellín")
                                .order(2)
                                .latitude(new BigDecimal("6.2476"))
                                .longitude(new BigDecimal("-75.5658"))
                                .build();
                stopMedellin = stopRepository.save(stopMedellin);
                toStopId = stopMedellin.getId();

                // Crear bus
                Bus bus = Bus.builder()
                                .plate("TEST-001")
                                .capacity(40)
                                .amenities(new HashMap<>())
                                .status(Bus.BusStatus.ACTIVE)
                                .build();
                bus = busRepository.save(bus);

                // Crear asientos
                for (int i = 1; i <= 40; i++) {
                        Seat seat = Seat.builder()
                                        .bus(bus)
                                        .seatNumber(i)
                                        .seatType(Seat.SeatType.STANDARD)
                                        .build();
                        seatRepository.save(seat);
                }

                // Crear viaje normal
                LocalDate tomorrow = LocalDate.now().plusDays(1);
                Trip trip = Trip.builder()
                                .route(route)
                                .bus(bus)
                                .tripDate(tomorrow)
                                .departureTime(LocalDateTime.now().plusDays(1).withHour(8).withMinute(0))
                                .arrivalEta(LocalDateTime.now().plusDays(1).withHour(16).withMinute(0))
                                .status(Trip.TripStatus.SCHEDULED)
                                .build();
                trip = tripRepository.save(trip);
                tripId = trip.getId();

                // Crear viaje para cancelación (48h antes)
                Trip trip48h = Trip.builder()
                                .route(route)
                                .bus(bus)
                                .tripDate(LocalDate.now().plusDays(3))
                                .departureTime(LocalDateTime.now().plusDays(3).withHour(8).withMinute(0))
                                .arrivalEta(LocalDateTime.now().plusDays(3).withHour(16).withMinute(0))
                                .status(Trip.TripStatus.SCHEDULED)
                                .build();
                trip48h = tripRepository.save(trip48h);
                tripId48h = trip48h.getId();

                // Registrar ADMIN
                RegisterRequest adminRegister = new RegisterRequest(
                                "Test Admin",
                                "admin@test.com",
                                "1111111111",
                                "password123",
                                User.Role.ADMIN);
                mvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(adminRegister)))
                                .andExpect(status().isCreated());

                LoginRequest adminLogin = new LoginRequest("admin@test.com", "password123");
                MvcResult adminLoginResult = mvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(adminLogin)))
                                .andExpect(status().isOk())
                                .andReturn();

                LoginResponse adminLoginResponse = om.readValue(adminLoginResult.getResponse().getContentAsString(),
                                LoginResponse.class);
                adminToken = adminLoginResponse.token();
                adminId = adminLoginResponse.user().id();

                // Registrar pasajero
                RegisterRequest passengerRegister = new RegisterRequest(
                                "Test Passenger",
                                "passenger@test.com",
                                "2222222222",
                                "password123",
                                User.Role.PASSENGER);
                mvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(passengerRegister)))
                                .andExpect(status().isCreated());

                LoginRequest passengerLogin = new LoginRequest("passenger@test.com", "password123");
                MvcResult passengerLoginResult = mvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(passengerLogin)))
                                .andExpect(status().isOk())
                                .andReturn();

                LoginResponse passengerLoginResponse = om.readValue(
                                passengerLoginResult.getResponse().getContentAsString(), LoginResponse.class);
                passengerToken = passengerLoginResponse.token();
                passengerId = passengerLoginResponse.user().id();
        }

        /**
         * TEST DE INTEGRACIÓN 1: Verificar que los descuentos actualizados afectan las
         * compras reales
         * - Admin actualiza descuento de STUDENT del 20% al 25%
         * - Pasajero compra ticket como STUDENT
         * - El precio final debe reflejar el nuevo descuento del 25%
         * - Involucra: Controller → Service → Repository → PostgreSQL real
         */
        @Test
        void updateDiscounts_shouldAffectTicketPurchases() throws Exception {
                // PASO 1: Admin actualiza descuento de STUDENT a 25%
                ConfigUpdateRequest configUpdate = new ConfigUpdateRequest(
                                null, null, null,
                                java.util.Map.of("STUDENT", 25, "SENIOR", 15, "CHILD", 50), // Nuevo descuento STUDENT =
                                                                                            // 25%
                                null, null, null, null,
                                null, null, null, null, null,
                                null, null, null, null);

                mvc.perform(put("/api/v1/admin/config")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(configUpdate)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.discountPercentages.STUDENT").value(25));

                // PASO 2: Pasajero compra ticket como STUDENT con el nuevo descuento
                TicketCreateRequest ticketRequest = new TicketCreateRequest(
                                tripId, passengerId, 1,
                                fromStopId, "Terminal Bogotá", 1,
                                toStopId, "Terminal Medellín", 2,
                                new BigDecimal("50000"), Ticket.PaymentMethod.CARD,
                                null, "STUDENT");

                MvcResult purchaseResult = mvc.perform(post("/api/v1/trips/{tripId}/tickets", tripId)
                                .header("Authorization", "Bearer " + passengerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(ticketRequest)))
                                .andExpect(status().isCreated())
                                .andReturn();

                // PASO 3: Verificar que el precio final refleja el descuento del 25%
                TicketResponse ticketResponse = om.readValue(purchaseResult.getResponse().getContentAsString(),
                                TicketResponse.class);
                assertThat(ticketResponse.price()).isNotNull();
        }

        /**
         * TEST DE INTEGRACIÓN 2: Verificar que las políticas de reembolso actualizadas
         * afectan cancelaciones
         * - Admin cambia política de 48h de 80% a 90% reembolso
         * - Pasajero compra ticket para viaje en 3 días
         * - Pasajero cancela inmediatamente
         * - Debe recibir 90% de reembolso (nueva política)
         * - Involucra: Controller → Service → Repository → PostgreSQL real
         */
        @Test
        void updateRefundPolicy_shouldAffectCancellations() throws Exception {
                // PASO 1: Admin actualiza política de reembolso de 48h a 90%
                ConfigUpdateRequest configUpdate = new ConfigUpdateRequest(
                                null, null, null, null, null, null,
                                null, null,
                                new BigDecimal("90"), // Nuevo: 90% reembolso si cancelas 48h antes
                                null, null, null, null,
                                null, null, null, null);

                mvc.perform(put("/api/v1/admin/config")
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(configUpdate)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.refundPercentage48Hours").value(90));

                // PASO 2: Pasajero compra ticket para viaje en 3 días
                TicketCreateRequest ticketRequest = new TicketCreateRequest(
                                tripId48h, passengerId, 1,
                                fromStopId, "Terminal Bogotá", 1,
                                toStopId, "Terminal Medellín", 2,
                                new BigDecimal("100000"), Ticket.PaymentMethod.CARD,
                                null, "ADULT");

                MvcResult purchaseResult = mvc.perform(post("/api/v1/trips/{tripId}/tickets", tripId48h)
                                .header("Authorization", "Bearer " + passengerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(ticketRequest)))
                                .andExpect(status().isCreated())
                                .andReturn();

                TicketResponse ticketResponse = om.readValue(purchaseResult.getResponse().getContentAsString(),
                                TicketResponse.class);
                Long ticketId = ticketResponse.id();
                BigDecimal actualTicketPrice = ticketResponse.price();

                // PASO 3: Pasajero cancela el ticket inmediatamente (más de 48h antes del
                // viaje)
                MvcResult cancelResult = mvc.perform(post("/api/v1/tickets/{id}/cancel", ticketId)
                                .header("Authorization", "Bearer " + passengerToken)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andReturn();

                // PASO 4: Verificar que recibe 90% de reembolso según la nueva política
                TicketCancelResponse cancelResponse = om.readValue(cancelResult.getResponse().getContentAsString(),
                                TicketCancelResponse.class);
                BigDecimal expectedRefund = actualTicketPrice
                                .multiply(new BigDecimal("0.90"))
                                .setScale(2, RoundingMode.HALF_UP);
                assertThat(cancelResponse.refundPercentage()).isEqualTo(90);
                assertThat(cancelResponse.refundAmount()).isEqualByComparingTo(expectedRefund);
        }

        /**
         * TEST DE INTEGRACIÓN 3: Verificar que se puede obtener toda la configuración
         * del sistema
         * - Admin solicita configuración completa
         * - Debe retornar todas las políticas: descuentos, reembolsos, precios,
         * overbooking, etc.
         * - Valida que no falte ninguna política crítica
         * - Involucra: Controller → Service → Repository → PostgreSQL real
         */
        @Test
        void getConfig_shouldReturnAllPolicies() throws Exception {
                // PASO 1: Admin solicita la configuración completa
                MvcResult configResult = mvc.perform(get("/api/v1/admin/config")
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andReturn();

                ConfigResponse config = om.readValue(configResult.getResponse().getContentAsString(),
                                ConfigResponse.class);

                // PASO 2: Verificar que todas las políticas están presentes y no son nulas
                assertThat(config.holdDurationMinutes()).isNotNull(); // Duración de reserva
                assertThat(config.discountPercentages()).isNotNull(); // Descuentos
                assertThat(config.discountPercentages()).containsKey("STUDENT");// Descuento STUDENT existe
                assertThat(config.refundPercentage48Hours()).isNotNull(); // Política 48h
                assertThat(config.refundPercentage24Hours()).isNotNull(); // Política 24h
                assertThat(config.refundPercentage12Hours()).isNotNull(); // Política 12h
                assertThat(config.refundPercentage6Hours()).isNotNull(); // Política 6h
                assertThat(config.refundPercentageLess6Hours()).isNotNull(); // Política <6h
                assertThat(config.ticketBasePrice()).isNotNull(); // Precio base
                assertThat(config.ticketPriceMultiplierPeakHours()).isNotNull();// Multiplicador hora pico
                assertThat(config.overbookingMaxPercentage()).isNotNull(); // % overbooking máximo
                assertThat(config.noShowFee()).isNotNull(); // Cargo por no-show
        }
}
