package com.example.foodorderingsystem.service.impl;

import com.example.foodorderingsystem.dto.PaginatedResponse;
import com.example.foodorderingsystem.dto.model.Restaurant;
import com.example.foodorderingsystem.dto.request.RestaurantRequestDto;
import com.example.foodorderingsystem.dto.response.RestaurantResponseDto;
import com.example.foodorderingsystem.exception.*;
import com.example.foodorderingsystem.repository.MenuItemRepository;
import com.example.foodorderingsystem.repository.RestaurantRepository;
import com.example.foodorderingsystem.service.RestaurantSelectionStrategy;
import com.example.foodorderingsystem.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.example.foodorderingsystem.constants.ExceptionConstants.*;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantStrategyFactory restaurantStrategyFactory;

    @Autowired
    public RestaurantServiceImpl(
            RestaurantRepository restaurantRepository,
            MenuItemRepository menuItemRepository,
            RestaurantStrategyFactory restaurantStrategyFactory
    ) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantStrategyFactory = restaurantStrategyFactory;
    }

    public void addRestaurant(RestaurantRequestDto restaurantRequestDto) throws Exception {
        if (restaurantRepository.existsByName(restaurantRequestDto.getName())) {
            throw new RestaurantAlreadyExistsException(RESTAURANT_ALREADY_EXISTS);
        }
        Restaurant restaurant = Restaurant.builder()
                .name(restaurantRequestDto.getName())
                .capacity(restaurantRequestDto.getCapacity())
                .rating(restaurantRequestDto.getRating())
                .build();
        restaurantRepository.save(restaurant);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateRestaurant(Long id, RestaurantRequestDto restaurantRequestDto) throws Exception {
        int retries = 3; // Maximum number of retries

        while (retries > 0) {
            try {
                Optional<Restaurant> existingRestaurantOpt = restaurantRepository.findByIdWithPessimisticReadLock(id);
                if (existingRestaurantOpt.isEmpty()) {
                    throw new RestaurantNotFoundException(String.format(RESTAURANT_NOT_FOUND, id));
                }

                Restaurant existingRestaurant = existingRestaurantOpt.get();
                existingRestaurant.setName(restaurantRequestDto.getName());
                existingRestaurant.setCapacity(restaurantRequestDto.getCapacity());
                existingRestaurant.setRating(restaurantRequestDto.getRating());
                restaurantRepository.save(existingRestaurant);
                // Break the loop when the save is successful
                break;
            } catch (OptimisticLockingFailureException e) {
                retries--;
                if (retries == 0) {
                    // Throw the custom exception if all retries are exhausted
                    throw new RestaurantUpdateFailedException(
                            RESTAURANT_UPDATE_FAILED
                    );
                }
                Thread.sleep(5);
            }
        }
    }

    public RestaurantResponseDto getRestaurantById(Long id) throws Exception {
        Restaurant restaurant =  restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(
                        String.format(RESTAURANT_NOT_FOUND,id)
                ));
        return RestaurantResponseDto.builder()
                .name(restaurant.getName())
                .capacity(restaurant.getCapacity())
                .id(restaurant.getId())
                .rating(restaurant.getRating())
                .createAt(restaurant.getCreatedAt())
                .updateAt(restaurant.getUpdatedAt())
                .build();
    }

    public PaginatedResponse<RestaurantResponseDto> getRestaurantsByMenuItemName(
            String menuItemName,
            Integer page,
            Integer size
    ) {
        Page<Restaurant> restaurants = restaurantRepository.findRestaurantsByItemNameWithPagination(
                menuItemName,PageRequest.of(page,size));
        List<RestaurantResponseDto> restaurantResponse = restaurants.stream()
                .map(restaurant -> RestaurantResponseDto.builder()
                        .id(restaurant.getId())
                        .name(restaurant.getName())
                        .capacity(restaurant.getCapacity())
                        .rating(restaurant.getRating())
                        .updateAt(restaurant.getUpdatedAt())
                        .createAt(restaurant.getCreatedAt())
                        .build())
                .toList();
        return PaginatedResponse.<RestaurantResponseDto>builder()
                .content(restaurantResponse)
                .pageNumber(restaurants.getNumber())
                .pageSize(restaurants.getSize())
                .totalElements(restaurants.getTotalElements())
                .totalPages(restaurants.getTotalPages())
                .isLast(restaurants.isLast())
                .build();

    }

    public List<Restaurant> findRestaurantsByItemNameWithSort(String sortBy,String itemName,Integer minCapacity)
            throws Exception
    {
        RestaurantSelectionStrategy restaurantSelectionStrategy = restaurantStrategyFactory.getStrategy(
                sortBy.toLowerCase()
        );
        if(restaurantSelectionStrategy == null) {
            throw new SelectionStrategyNotFoundException(
                    String.format(RESTAURANT_SELECTION_STRATEGY_NOT_FOUND,sortBy)
            );
        }
        List<Restaurant> restaurants = restaurantSelectionStrategy.findRestaurants(itemName,minCapacity);
        if (restaurants.isEmpty()) {
            throw new RestaurantNotFoundException(
                    String.format(NO_RESTAURANT_SERVES_ITEM,itemName)
            );
        }
        return restaurants;
    }

    public PaginatedResponse<RestaurantResponseDto> getAllRestaurants(String menuItemName,Integer page,Integer size) {
        if(menuItemName!=null) {
            return getRestaurantsByMenuItemName(menuItemName,page,size);
        }
        Page<Restaurant> restaurants = restaurantRepository.findAll(PageRequest.of(page,size));
        List<RestaurantResponseDto> restaurantResponse = restaurants.stream()
                .map(this::convertToRestaurantResponseDto)
                .toList();
        return PaginatedResponse.<RestaurantResponseDto>builder()
                .content(restaurantResponse)
                .pageNumber(restaurants.getNumber())
                .pageSize(restaurants.getSize())
                .totalElements(restaurants.getTotalElements())
                .totalPages(restaurants.getTotalPages())
                .isLast(restaurants.isLast())
                .build();

    }

    // Helper method to convert Restaurant entity to RestaurantResponseDto
    private RestaurantResponseDto convertToRestaurantResponseDto(Restaurant restaurant) {
        return RestaurantResponseDto.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .capacity(restaurant.getCapacity())
                .rating(restaurant.getRating())
                .createAt(restaurant.getCreatedAt())
                .updateAt(restaurant.getUpdatedAt())
                .build();

    }

}
