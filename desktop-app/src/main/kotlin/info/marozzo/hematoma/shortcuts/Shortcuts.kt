/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma.shortcuts

import androidx.compose.ui.input.key.*
import info.marozzo.hematoma.contract.Input
import info.marozzo.hematoma.contract.Save
import info.marozzo.hematoma.contract.SaveAs
import info.marozzo.hematoma.input.AcceptFun
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf


sealed interface KeyEventMatcher {
    fun matches(evt: KeyEvent): Boolean
    operator fun plus(other: KeyEventMatcher): KeyEventMatcher
}

enum class ModifierKey(private val isModifier: (KeyEvent) -> Boolean) : KeyEventMatcher {
    Alt(KeyEvent::isAltPressed),
    Ctrl(KeyEvent::isCtrlPressed),
    Meta(KeyEvent::isMetaPressed),
    Shift(KeyEvent::isAltPressed);

    override fun matches(evt: KeyEvent): Boolean = isModifier(evt)
    override fun plus(other: KeyEventMatcher): KeyEventMatcher = CombinedMatcher(persistentListOf(this, other))
}

data class KeyMatch(private val key: Key) : KeyEventMatcher {
    override fun matches(evt: KeyEvent): Boolean = evt.key == key
    override fun plus(other: KeyEventMatcher): KeyEventMatcher = CombinedMatcher(persistentListOf(this, other))
}

operator fun Key.plus(matcher: KeyEventMatcher): KeyEventMatcher =
    CombinedMatcher(persistentListOf(KeyMatch(this), matcher))

operator fun KeyEventMatcher.plus(key: Key): KeyEventMatcher = key + this

class CombinedMatcher(private val matchers: PersistentList<KeyEventMatcher>) : KeyEventMatcher {
    override fun matches(evt: KeyEvent): Boolean = matchers.all { it.matches(evt) }
    override fun plus(other: KeyEventMatcher): KeyEventMatcher = CombinedMatcher(matchers.add(other))
}

data class KeyEventShortcut(val matcher: KeyEventMatcher, val inputFactory: () -> Input) : KeyEventMatcher by matcher

val shortcuts = persistentListOf(
    KeyEventShortcut(ModifierKey.Ctrl + Key.S) { Save },
    KeyEventShortcut(ModifierKey.Ctrl + ModifierKey.Alt + Key.S) { SaveAs },
)

fun ImmutableList<KeyEventShortcut>.handler(accept: AcceptFun): (KeyEvent) -> Boolean = { evt ->
    val inputs = this.filter { it.matches(evt) }.map { it.inputFactory() }
    if (inputs.isEmpty()) {
        false
    } else {
        inputs.forEach(accept)
        true
    }
}

