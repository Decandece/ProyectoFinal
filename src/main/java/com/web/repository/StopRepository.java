package com.web.repository;

import com.web.entity.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StopRepository extends JpaRepository<Stop, Long> {

    // Buscar paradas de una ruta ordenadas por secuencia
    List<Stop> findByRouteIdOrderByOrderAsc(Long routeId);

    // Validar si una parada pertenece a una ruta
    boolean existsByIdAndRouteId(Long stopId, Long routeId);

    // Obtener el orden de una parada para validación de tramos
    @Query("""
        SELECT s.order FROM Stop s
        WHERE s.id = :stopId
    """)
    Integer getStopOrder(@Param("stopId") Long stopId);

    // Validar que un tramo sea válido (fromStop debe venir antes que toStop en la misma ruta)
    @Query("""
        SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END
        FROM Stop fs, Stop ts
        WHERE fs.id = :fromStopId
        AND ts.id = :toStopId
        AND fs.route.id = ts.route.id
        AND fs.order < ts.order
    """)
    Boolean isValidSegment(
        @Param("fromStopId") Long fromStopId,
        @Param("toStopId") Long toStopId
    );
}
