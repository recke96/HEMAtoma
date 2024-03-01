/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.domain.errors

import arrow.core.EitherNel

typealias Validated<T> = EitherNel<ValidationError, T>

data class ValidationError(val message: String, val property: String) : DomainError
