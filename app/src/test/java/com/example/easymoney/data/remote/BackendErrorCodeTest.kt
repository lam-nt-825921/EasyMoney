package com.example.easymoney.data.remote

import com.example.easymoney.ui.common.error.BackendErrorCode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BackendErrorCodeTest {

    @Test
    fun `detects structured backend code`() {
        val raw = """{"status":"error","code":"CARD_REQUIRED","message":"User has no linked card"}"""

        assertEquals(BackendErrorCode.CARD_REQUIRED, BackendErrorCode.detect(raw))
    }

    @Test
    fun `detects legacy marker in plain message`() {
        val raw = "Withdraw failed: INSUFFICIENT_BALANCE"

        assertEquals(BackendErrorCode.INSUFFICIENT_BALANCE, BackendErrorCode.detect(raw))
    }

    @Test
    fun `ignores unknown structured code`() {
        val raw = """{"status":"error","code":"LIMIT_CHANGED","message":"Limit changed"}"""

        assertNull(BackendErrorCode.detect(raw))
    }
}
