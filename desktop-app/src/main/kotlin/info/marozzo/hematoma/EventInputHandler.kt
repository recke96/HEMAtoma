/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma

import com.copperleaf.ballast.InputHandlerScope
import info.marozzo.hematoma.contract.Event
import info.marozzo.hematoma.contract.EventState
import info.marozzo.hematoma.contract.Input


typealias EventInputHandlerScope = InputHandlerScope<Input, Event, EventState>
typealias AcceptFun = (Input) -> Unit

