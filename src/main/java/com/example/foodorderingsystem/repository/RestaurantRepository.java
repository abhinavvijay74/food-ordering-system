package com.example.foodorderingsystem.repository;

import com.example.foodorderingsystem.dto.model.Restaurant;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT r FROM Restaurant r WHERE r.id = :id")
    Optional<Restaurant> findByIdWithPessimisticReadLock(@Param("id") Long id);

    boolean existsByName(String name);

    @Query(value = """ 
        SELECT r.* FROM restaurants r
        JOIN menu_items m ON r.id = m.restaurant_id
        WHERE m.name = :itemName AND m.status = 'ACTIVE' AND r.capacity >= :minCapacity
        ORDER BY r.rating DESC
        """,
            nativeQuery = true)
    List<Restaurant> findRestaurantsByItemNameSortedByRating(
            @Param("itemName") String itemName,
            @Param("minCapacity") int minCapacity
    );

    @Query(value = """
        SELECT r.* FROM restaurants r
        JOIN menu_items m ON r.id = m.restaurant_id
        WHERE m.name = :itemName AND m.status = 'ACTIVE' AND r.capacity >= :minCapacity
        ORDER BY m.price ASC
        """,
            nativeQuery = true)
    List<Restaurant> findRestaurantsByItemNameSortedByPrice(
            @Param("itemName") String itemName,
            @Param("minCapacity") int minCapacity
    );

    @Query(value = """ 
        SELECT r.* FROM restaurants r
        JOIN menu_items m ON r.id = m.restaurant_id
        WHERE m.name = :itemName AND m.status = 'ACTIVE'
        """,
            nativeQuery = true)
    Page<Restaurant> findRestaurantsByItemNameWithPagination(
            @Param("itemName") String itemName,
            PageRequest pageRequest
    );
}
