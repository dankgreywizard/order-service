package com.skmcore.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skmcore.orderservice.dto.ProductRequest;
import com.skmcore.orderservice.dto.ProductResponse;
import com.skmcore.orderservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ProductControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private ProductService productService;

    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private UUID productId;
    private ProductResponse productResponse;
    private ProductRequest productRequest;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        productId = UUID.randomUUID();
        productRequest = new ProductRequest(
            "PROD-001",
            "Test Product",
            "A test product",
            new BigDecimal("29.99"),
            "Electronics",
            100,
            true
        );
        productResponse = new ProductResponse(
            productId,
            "PROD-001",
            "Test Product",
            "A test product",
            new BigDecimal("29.99"),
            "Electronics",
            100,
            true,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    @Test
    void getAllProducts_ShouldReturnListOfProducts() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(productResponse));

        mockMvc.perform(get("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(productId.toString()))
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$[0].productCode").value("PROD-001"));
    }

    @Test
    void getAvailableProducts_ShouldReturnAvailableProducts() throws Exception {
        when(productService.getAvailableProducts()).thenReturn(List.of(productResponse));

        mockMvc.perform(get("/api/v1/products/available")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    void getProductById_ShouldReturnProduct() throws Exception {
        when(productService.getProductById(productId)).thenReturn(productResponse);

        mockMvc.perform(get("/api/v1/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void getProductByCode_ShouldReturnProduct() throws Exception {
        when(productService.getProductByCode("PROD-001")).thenReturn(productResponse);

        mockMvc.perform(get("/api/v1/products/code/{productCode}", "PROD-001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productCode").value("PROD-001"));
    }

    @Test
    void getProductsByCategory_ShouldReturnProductsInCategory() throws Exception {
        when(productService.getProductsByCategory("Electronics")).thenReturn(List.of(productResponse));

        mockMvc.perform(get("/api/v1/products/category/{category}", "Electronics")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Electronics"));
    }

    @Test
    void getAvailableProductsByCategory_ShouldReturnAvailableProductsInCategory() throws Exception {
        when(productService.getAvailableProductsByCategory("Electronics")).thenReturn(List.of(productResponse));

        mockMvc.perform(get("/api/v1/products/category/{category}/available", "Electronics")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Electronics"))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    void createProduct_ShouldReturnCreated() throws Exception {
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(productResponse);

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productCode").value("PROD-001"))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void updateProduct_ShouldReturnUpdatedProduct() throws Exception {
        ProductRequest updateRequest = new ProductRequest(
            "PROD-001",
            "Updated Product",
            "Updated description",
            new BigDecimal("39.99"),
            "Electronics",
            150,
            true
        );
        ProductResponse updatedResponse = new ProductResponse(
            productId,
            "PROD-001",
            "Updated Product",
            "Updated description",
            new BigDecimal("39.99"),
            "Electronics",
            150,
            true,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        when(productService.updateProduct(eq(productId), any(ProductRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/v1/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.price").value(39.99));
    }

    @Test
    void deleteProduct_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(productId);
    }
}
