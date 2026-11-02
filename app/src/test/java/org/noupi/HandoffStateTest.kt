package org.noupi
import org.junit.Assert.*
import org.junit.Test

class HandoffStateTest {
    @Test fun deliberateBeginOnlyOnce() { val flow = HandoffState(); assertTrue(flow.begin()); assertFalse(flow.begin()) }
    @Test fun fakeSuccessIsStillUnverified() { val flow = HandoffState(); flow.begin(); flow.returned(); assertEquals(HandoffState.Stage.UNVERIFIED, flow.stage); assertFalse(flow.begin()) }
    @Test fun cancelledResultIsStillUnverified() { val flow = HandoffState(); flow.begin(); flow.returned(); assertFalse(flow.begin()) }
    @Test fun missingCallbackStaysConsumed() { val flow = HandoffState(); flow.begin(); assertEquals(HandoffState.Stage.OPENED, flow.stage); assertFalse(flow.begin()) }
    @Test fun lifecycleRestoreNeverRelaunches() { val flow = HandoffState(HandoffState.Stage.OPENED); flow.restored(); assertEquals(HandoffState.Stage.UNVERIFIED, flow.stage); assertFalse(flow.begin()) }
    @Test fun noHandlerAllowsOnlyNewDeliberateAttempt() { val flow = HandoffState(); flow.begin(); flow.unavailable(); assertEquals(HandoffState.Stage.REVIEW, flow.stage); assertTrue(flow.begin()) }
    @Test fun noCallbackBeforeLaunchChangesNothing() { val flow = HandoffState(); flow.returned(); assertEquals(HandoffState.Stage.REVIEW, flow.stage) }
    @Test fun consumedRequestCannotBeResetByUnavailable() { val flow = HandoffState(HandoffState.Stage.UNVERIFIED); flow.unavailable(); assertFalse(flow.begin()) }
}
