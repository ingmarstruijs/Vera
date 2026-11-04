package net.moonbaseone.vera.shared.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory typed navigator backed by [StateFlow].
 *
 * Platforms may observe [destination] or call [current] / [navigate].
 * Full Decompose / Circuit adoption is deferred until product needs shared
 * multi-stack / deep-link orchestration beyond the unlock → shell IA —
 * keep [AppDestination] IDs stable so a ComponentContext / Circuit Presenter
 * can wrap this contract without route renames.
 */
class DefaultAppNavigator(
    initial: AppDestination = AppDestination.Unlock,
) : AppNavigator {
    private val _destination = MutableStateFlow(initial)
    val destination: StateFlow<AppDestination> = _destination.asStateFlow()

    override fun navigate(destination: AppDestination) {
        _destination.value = destination
    }

    override fun current(): AppDestination = _destination.value
}
