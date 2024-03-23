/*
 * Copyright Jakob Ecker, 2024
 * Licensed under the EUPL-1.2-or-later
 *
 */

package info.marozzo.hematoma

import androidx.compose.runtime.*
import com.darkrockstudios.libraries.mpfilepicker.DirectoryPicker
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import com.darkrockstudios.libraries.mpfilepicker.MPFile
import com.darkrockstudios.libraries.mpfilepicker.MultipleFilePicker
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.nio.file.Path
import kotlin.coroutines.resume
import kotlin.io.path.absolutePathString

class FilePickerHostState {

    private val mutex = Mutex()

    var currentFilePicker by mutableStateOf<Picker?>(null)
        private set

    suspend fun pickFile(
        title: String? = null,
        initialDirectory: Path? = null,
        extensions: List<String> = emptyList()
    ): PickerResult = pick(title, initialDirectory, Picker.File(extensions))

    suspend fun pickFiles(
        title: String? = null,
        initialDirectory: Path? = null,
        extensions: List<String> = emptyList()
    ): PickerResult = pick(title, initialDirectory, Picker.Files(extensions))

    suspend fun pickDirectory(
        title: String? = null,
        initialDirectory: Path? = null,
    ): PickerResult = pick(title, initialDirectory, Picker.Directory)

    private suspend fun pick(title: String?, initialDirectory: Path?, kind: Picker.Kind): PickerResult =
        mutex.withLock {
            try {
                return suspendCancellableCoroutine { cont ->
                    currentFilePicker = PickerImpl(title, initialDirectory, kind, cont)
                }
            } finally {
                currentFilePicker = null
            }
        }

    private class PickerImpl(
        override val title: String?,
        override val initialDirectory: Path?,
        override val kind: Picker.Kind,
        private val continuation: CancellableContinuation<PickerResult>
    ) : Picker {

        override fun select(files: List<Path>) {
            if (continuation.isActive) continuation.resume(
                if (kind is Picker.Files) PickerResult.Files(files.toImmutableList())
                else PickerResult.File(files.single())
            )
        }

        override fun dismiss() {
            if (continuation.isActive) continuation.resume(PickerResult.Dismissed)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as PickerImpl

            if (title != other.title) return false
            if (initialDirectory != other.initialDirectory) return false
            if (kind != other.kind) return false
            if (continuation != other.continuation) return false

            return true
        }

        override fun hashCode(): Int {
            var result = title.hashCode()
            result = 31 * result + (initialDirectory?.hashCode() ?: 0)
            result = 31 * result + kind.hashCode()
            result = 31 * result + continuation.hashCode()
            return result
        }
    }
}

interface Picker {
    val title: String?
    val initialDirectory: Path?
    val kind: Kind

    fun select(files: List<Path>)
    fun dismiss()

    sealed interface Kind
    data object Directory : Kind
    data class File(val extensions: List<String> = emptyList()) : Kind
    data class Files(val extensions: List<String> = emptyList()) : Kind
}


sealed interface PickerResult {
    val files: ImmutableList<Path>

    data object Dismissed : PickerResult {
        override val files: ImmutableList<Path> = persistentListOf()
    }

    data class File(val file: Path) : PickerResult {
        override val files: ImmutableList<Path> by lazy { persistentListOf(file) }
    }

    data class Files(override val files: ImmutableList<Path>) : PickerResult
}

@Composable
fun FilePickerHost(
    hostState: FilePickerHostState
) {
    val currentPicker = hostState.currentFilePicker
    val kind = currentPicker?.kind
    val (show, setShow) = remember { mutableStateOf(false) }
    LaunchedEffect(currentPicker) {
        setShow(currentPicker != null)
    }

    when (kind) {
        is Picker.File -> FilePicker(
            show = show,
            title = currentPicker.title,
            initialDirectory = currentPicker.initialDirectory?.absolutePathString(),
            fileExtensions = kind.extensions
        ) { file ->
            if (file != null) currentPicker.select(listOf(Path.of(file.path)))
            else currentPicker.dismiss()
        }

        is Picker.Files -> MultipleFilePicker(
            show = show,
            title = currentPicker.title,
            initialDirectory = currentPicker.initialDirectory?.absolutePathString(),
            fileExtensions = kind.extensions
        ) { files ->
            if (files != null) currentPicker.select(files.map(MPFile<Any>::path).map(Path::of))
            else currentPicker.dismiss()
        }

        is Picker.Directory -> DirectoryPicker(
            show = show,
            title = currentPicker.title,
            initialDirectory = currentPicker.initialDirectory?.absolutePathString()
        ) { dir ->
            if (dir != null) currentPicker.select(listOf(Path.of(dir)))
            else currentPicker.dismiss()
        }

        null -> Unit
    }
}
