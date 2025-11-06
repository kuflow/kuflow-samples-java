/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.samples.temporal.worker.loan.activity;

import com.kuflow.samples.temporal.worker.loan.resource.DataSourceMocks;
import com.kuflow.samples.temporal.worker.loan.resource.DataSourceQueryRequest;
import com.kuflow.samples.temporal.worker.loan.resource.DataSourceQueryResponse;
import com.kuflow.samples.temporal.worker.loan.resource.DataSourceValidateValueRequest;
import com.kuflow.samples.temporal.worker.loan.resource.DataSourceValidateValueResponse;
import com.kuflow.samples.temporal.worker.loan.resource.DataSourceValidateValueResult;
import io.temporal.failure.ApplicationFailure;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DataSourceActivitiesImpl implements DataSourceActivities {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceActivitiesImpl.class);

    @Override
    public DataSourceQueryResponse runQuery(DataSourceQueryRequest request) {
        LOGGER.info("Started data source process {}", request.getCode());

        DataSourceQueryResponse response = this.queryDataSourceInternal(request);

        LOGGER.info("Finished data source process {}", request.getCode());

        return response;
    }

    @Override
    public DataSourceValidateValueResponse validateValue(DataSourceValidateValueRequest request) {
        LOGGER.info("Started data source validation {}", request.getCode());

        DataSourceValidateValueResponse response = new DataSourceValidateValueResponse();

        // If no values provided, validation passes (empty is ok)
        if (request.getValues().isEmpty()) {
            LOGGER.info("Finished data source validation - no values provided for validation for code: {}", request.getCode());

            return response;
        }

        // Extract all valid product IDs from mock data
        List<String> validProductIds = DataSourceMocks.MOCK_PRODUCTS.stream()
            .map(product -> product.get("id"))
            .filter(Objects::nonNull)
            .map(Object::toString)
            .toList();

        // Check if all values exist in the product data
        for (Object value : request.getValues()) {
            DataSourceValidateValueResult result = this.validateValue(value, validProductIds);

            response.addValidation(result);
        }

        LOGGER.info(
            "Finished data source validation {} - {} out of {} values valid",
            request.getCode(),
            response.getValidations().stream().filter(DataSourceValidateValueResult::isValid).count(),
            response.getValidations().size()
        );

        return response;
    }

    private DataSourceValidateValueResult validateValue(Object value, List<String> validProductIds) {
        DataSourceValidateValueResult result = new DataSourceValidateValueResult();

        if (!(value instanceof Map<?, ?> valueItem)) {
            String reason = "Invalid value type. Got %s".formatted(value == null ? "null" : value.getClass().getSimpleName());

            result.setValid(false);
            result.setMessage(reason);

            return result;
        }

        // Extract the id from the map
        String productId = "%s".formatted(valueItem.get("id"));

        if (StringUtils.isBlank(productId)) {
            String reason = "Value map does not contain a valid 'id' key";
            result.setValid(false);
            result.setMessage(reason);

            return result;
        }

        if (!validProductIds.contains(productId)) {
            String reason = "Product ID '%s' not found in data source".formatted(productId);
            result.setValid(false);
            result.setMessage(reason);

            return result;
        }

        result.setValid(true);

        return result;
    }

    private DataSourceQueryResponse queryDataSourceInternal(DataSourceQueryRequest workflowRequest) {
        int pageNumber = this.validateAndGetPageNumber(workflowRequest.getPageNumber());
        int pageSize = this.validateAndGetPageSize(workflowRequest.getPageSize());
        String query = workflowRequest.getQuery();

        // Filter products by query if provided
        List<Map<String, Object>> filteredProducts = this.filterProductsByQuery(DataSourceMocks.MOCK_PRODUCTS, query);

        // Calculate pagination metadata
        long totalElements = filteredProducts.size();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        // Calculate start and end index for pagination
        int startIndex = pageNumber * pageSize;
        int endIndex = Math.min(startIndex + pageSize, filteredProducts.size());

        List<Map<String, Object>> itemsToReturn;

        // If start index is out of range, return empty list
        if (startIndex >= filteredProducts.size()) {
            LOGGER.info("Page {} out of range, returning empty list", pageNumber);
            itemsToReturn = List.of();
        } else {
            itemsToReturn = filteredProducts.subList(startIndex, endIndex);
            LOGGER.info(
                "Returning page {} with size {} - {} items (from index {} to {}) - total filtered: {}",
                pageNumber,
                pageSize,
                itemsToReturn.size(),
                startIndex,
                endIndex - 1,
                totalElements
            );
        }

        DataSourceQueryResponse response = new DataSourceQueryResponse();
        response.setPageNumber(pageNumber);
        response.setPageSize(pageSize);
        response.setTotalElements(totalElements);
        response.setTotalPages(totalPages);
        response.setItems(itemsToReturn);
        return response;
    }

    /**
     * Validates and returns the page number from the request.
     *
     * @param pageNumber the page number to validate
     * @return the validated page number
     * @throws ApplicationFailure if validation fails
     */
    private int validateAndGetPageNumber(Integer pageNumber) {
        if (pageNumber == null) {
            String errorMsg = "Pagination is required: pageNumber must be specified";
            LOGGER.error(errorMsg);
            throw ApplicationFailure.newFailure(errorMsg, "validation");
        }

        if (pageNumber < 0) {
            String errorMsg = "Invalid pageNumber: must be >= 0, got " + pageNumber;
            LOGGER.error(errorMsg);
            throw ApplicationFailure.newFailure(errorMsg, "validation");
        }

        return pageNumber;
    }

    /**
     * Validates and returns the page size from the request.
     *
     * @param pageSize the page size to validate
     * @return the validated page size
     * @throws ApplicationFailure if validation fails
     */
    private int validateAndGetPageSize(Integer pageSize) {
        if (pageSize == null) {
            String errorMsg = "Pagination is required: pageSize must be specified";
            LOGGER.error(errorMsg);
            throw ApplicationFailure.newFailure(errorMsg, "validation");
        }

        if (pageSize <= 0) {
            String errorMsg = "Invalid pageSize: must be > 0, got " + pageSize;
            LOGGER.error(errorMsg);
            throw ApplicationFailure.newFailure(errorMsg, "validation");
        }

        return pageSize;
    }

    /**
     * Filters products by query string using case-insensitive label matching.
     * If query is null or empty, returns all products.
     *
     * @param products the list of products to filter
     * @param query the query string to filter by
     * @return filtered list of products
     */
    private List<Map<String, Object>> filterProductsByQuery(List<Map<String, Object>> products, String query) {
        if (query == null || query.trim().isEmpty()) {
            LOGGER.info("No query provided, returning all products");
            return products;
        }

        String normalizedQuery = query.toLowerCase().trim();
        List<Map<String, Object>> filtered = products
            .stream()
            .filter(item -> {
                Object labelObj = item.get("label");
                if (labelObj == null) {
                    return false;
                }
                String label = labelObj.toString();

                return label.toLowerCase().contains(normalizedQuery);
            })
            .toList();

        LOGGER.info("Filtered {} products from {} using query: '{}'", filtered.size(), products.size(), query);
        return filtered;
    }
}
