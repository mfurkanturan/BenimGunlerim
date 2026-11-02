package com.benimgunlerim.ui.account

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidationTest {
    @Test fun emailFormat() {
        assertTrue(AuthValidation.isEmailValid("ali@ornek.com"))
        assertTrue(AuthValidation.isEmailValid("  ali.veli+x@ornek.com.tr "))
        assertFalse(AuthValidation.isEmailValid("ali@ornek"))
        assertFalse(AuthValidation.isEmailValid("ali ornek.com"))
        assertFalse(AuthValidation.isEmailValid(""))
    }

    @Test fun registerNeedsStrongMatchingPasswordAndConsent() {
        assertTrue(AuthValidation.validate(AuthMode.Register, "a@b.co", "gunes2026", "gunes2026", true).allOk)
        assertFalse(AuthValidation.validate(AuthMode.Register, "a@b.co", "kisa1", "kisa1", true).passwordOk)
        assertFalse(AuthValidation.validate(AuthMode.Register, "a@b.co", "sadeceharf", "sadeceharf", true).passwordOk)
        assertFalse(AuthValidation.validate(AuthMode.Register, "a@b.co", "gunes2026", "gunes2027", true).confirmOk)
        assertFalse(AuthValidation.validate(AuthMode.Register, "a@b.co", "gunes2026", "gunes2026", false).consentOk)
    }

    @Test fun loginOnlyNeedsEmailAndNonEmptyPassword() {
        assertTrue(AuthValidation.validate(AuthMode.Login, "a@b.co", "x", "", false).allOk)
        assertFalse(AuthValidation.validate(AuthMode.Login, "a@b.co", "", "", false).allOk)
    }

    @Test fun resetOnlyNeedsEmail() {
        assertTrue(AuthValidation.validate(AuthMode.Reset, "a@b.co", "", "", false).allOk)
        assertFalse(AuthValidation.validate(AuthMode.Reset, "a@", "", "", false).allOk)
    }
}
