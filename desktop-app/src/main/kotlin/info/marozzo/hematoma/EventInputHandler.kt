/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma

import com.copperleaf.ballast.InputHandlerScope
import info.marozzo.hematoma.contract.Input
import info.marozzo.hematoma.contract.EventState


typealias EventInputHandlerScope = InputHandlerScope<Input, Nothing, EventState>
typealias AcceptFun = (Input) -> Unit

