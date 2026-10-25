package com.crevolab.testseries.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TestPricingValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTestPricing {
    String message() default "Invalid pricing configuration";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
