package com.crevolab.testseries.dto.validation;

import com.crevolab.testseries.domain.enums.AccessType;
import com.crevolab.testseries.dto.admin.TestRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

public class TestPricingValidator implements ConstraintValidator<ValidTestPricing, TestRequestDto> {

    @Override
    public boolean isValid(TestRequestDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        AccessType accessType = dto.getAccessType() != null ? dto.getAccessType() : AccessType.FREE;

        if (accessType == AccessType.PAID) {
            if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Price must be greater than 0 for PAID tests")
                        .addPropertyNode("price")
                        .addConstraintViolation();
                return false;
            }
        } else if (accessType == AccessType.FREE) {
            // FREE forces price = 0
            dto.setPrice(BigDecimal.ZERO);
        }

        return true;
    }
}
