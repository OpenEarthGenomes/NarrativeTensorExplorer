package com.meaning.app

enum class AppView {
    THREE_D_MAP,          // 3D Narratív térkép
    DIMENSION_FOREST,     // Dimenzió erdő
    SEARCH_RESULTS,       // Keresési eredmények
    ENTITY_DETAIL,        // Entitás részletes nézet
    CONNECTION_GRAPH,     // Kapcsolati gráf
    METRICS_DASHBOARD,    // Teljesítmény metrikák
    SETTINGS              // Beállítások
}

data class ViewState(
    val currentView: AppView = AppView.THREE_D_MAP,
    val previousView: AppView? = null,
    val viewParams: Map<String, Any> = emptyMap(),
    val transitionInProgress: Boolean = false
) {
    fun navigateTo(view: AppView, params: Map<String, Any> = emptyMap()): ViewState {
        return ViewState(
            currentView = view,
            previousView = this.currentView,
            viewParams = params
        )
    }
    
    fun goBack(): ViewState {
        return if (previousView != null) {
            ViewState(
                currentView = previousView,
                previousView = null,
                viewParams = emptyMap()
            )
        } else this
    }
}
