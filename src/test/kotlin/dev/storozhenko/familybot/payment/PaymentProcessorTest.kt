package dev.storozhenko.familybot.payment

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import dev.storozhenko.familybot.services.settings.EasyKeyValueService
import dev.storozhenko.familybot.suits.FamilybotApplicationTest

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
