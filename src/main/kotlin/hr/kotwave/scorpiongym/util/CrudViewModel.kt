package hr.kotwave.scorpiongym.util

import androidx.compose.runtime.mutableStateListOf

/**
 * Base ViewModel for simple CRUD entities backed by a DAO.
 *
 * Subclasses pass DAO method references as the four lambdas and may expose
 * domain-specific aliases (e.g. `memberships`, `addMembership`) on top of
 * the protected [items] / [add] / [update] / [remove] surface.
 */
abstract class CrudViewModel<T : Identifiable>(
    private val loader: () -> List<T>,
    private val inserter: (T) -> Int,
    private val updater: (T) -> Unit,
    private val deleter: (T) -> Unit,
) {
    private val _items = mutableStateListOf<T>()
    protected val items: List<T> get() = _items

    init {
        _items.addAll(loader())
    }

    protected fun add(item: T) {
        item.id = inserter(item)
        _items.add(item)
    }

    protected fun update(item: T) {
        updater(item)
        val index = _items.indexOfFirst { it.id == item.id }
        if (index != -1) {
            _items[index] = item
        }
    }

    protected fun remove(item: T) {
        deleter(item)
        _items.remove(item)
    }
}
