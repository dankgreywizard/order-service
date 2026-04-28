package com.skmcore.orderservice.mapper;

import com.skmcore.orderservice.dto.OrderItemResponse;
import com.skmcore.orderservice.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface OrderItemMapper {

    OrderItemResponse toResponse(OrderItem orderItem);
}
