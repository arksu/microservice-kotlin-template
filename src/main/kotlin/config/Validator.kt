package com.company.config

import com.company.config.validation.accessors.firstName
import com.company.config.validation.accessors.id
import com.company.config.validation.accessors.lastName
import dev.nesk.akkurate.constraints.builders.hasLengthGreaterThan
import dev.nesk.akkurate.constraints.builders.isGreaterThan
import dev.nesk.akkurate.constraints.builders.isNotEmpty


val validateCustomer = dev.nesk.akkurate.Validator<Customer> {
    id.isGreaterThan(10)
    firstName.isNotEmpty()
    lastName.hasLengthGreaterThan(3)
}