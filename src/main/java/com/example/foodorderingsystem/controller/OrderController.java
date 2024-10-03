package com.example.foodorderingsystem.controller;

import com.example.foodorderingsystem.dto.request.OrderRequest;
import com.example.foodorderingsystem.service.OrderHistoryService;
import com.example.foodorderingsystem.service.OrderService;
import com.example.foodorderingsystem.utils.ResponseUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.foodorderingsystem.constants.SuccessConstants.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderServiceImpl;
    private final OrderHistoryService orderHistoryServiceImpl;

    @Autowired
    public OrderController(OrderService orderServiceImpl, OrderHistoryService orderHistoryServiceImpl) {
        this.orderServiceImpl = orderServiceImpl;
        this.orderHistoryServiceImpl = orderHistoryServiceImpl;
    }

    @PostMapping("/place")
    public ResponseEntity<Object> placeOrder(
            @RequestParam(value = "sortBy", required = false, defaultValue = "price") String sortBy,
            @RequestBody @Valid OrderRequest orderRequest
    ) {
        try {
            orderServiceImpl.placeOrder(orderRequest,sortBy);
            return ResponseUtils.successResponse(PLACE_ORDER);
        } catch (Exception e) {
            return ResponseUtils.exceptionResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/complete/{orderId}")
    public ResponseEntity<Object> completeOrder(@PathVariable Long orderId) {
        try {
            orderServiceImpl.completeOrder(orderId);
            return ResponseUtils.successResponse(COMPLETE_ORDER);
        } catch (Exception e) {
            return ResponseUtils.exceptionResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Object> fetchOrderDetails(@PathVariable Long orderId) {
        try {
            return  ResponseUtils.successResponse(
                    orderHistoryServiceImpl.getOrderDetails(orderId),
                    FETCH_ORDER_DETAILS,
                    SUCCESS
            );
        } catch (Exception e) {
            return ResponseUtils.exceptionResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
