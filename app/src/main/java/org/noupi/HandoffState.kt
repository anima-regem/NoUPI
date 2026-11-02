package org.noupi

/** No callback can establish payment success. A consumed request never launches again. */
class HandoffState(initial: Stage = Stage.REVIEW) {
    enum class Stage { REVIEW, OPENED, UNVERIFIED }
    var stage = initial
        private set
    fun begin(): Boolean {
        if (stage != Stage.REVIEW) return false
        stage = Stage.OPENED
        return true
    }
    fun unavailable() { if (stage == Stage.OPENED) stage = Stage.REVIEW }
    fun returned() { if (stage == Stage.OPENED) stage = Stage.UNVERIFIED }
    fun restored() { returned() }
}
