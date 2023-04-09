package dev.storozhenko.familybot.payment

import dev.storozhenko.familybot.core.keyvalue.EasyKeyValueService
import dev.storozhenko.familybot.suits.FamilybotApplicationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

abstract class PaymentProcessorTest : FamilybotApplicationTest() {

    @Autowired
    lateinit var easyKeyValueService: EasyKeyValueService

    @Test
    fun preCheckOutTestRun() {
        preCheckOutTest()
    }

    @Test
    fun processSuccessTestRun() {
        processSuccessTest()
    }

    abstract fun preCheckOutTest()

    abstract fun processSuccessTest()
}
