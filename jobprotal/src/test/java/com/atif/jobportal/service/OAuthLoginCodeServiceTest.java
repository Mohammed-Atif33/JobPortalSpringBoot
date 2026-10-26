package com.atif.jobportal.service;

import com.atif.jobportal.entity.OAuthLoginCode;
import com.atif.jobportal.entity.User;
import com.atif.jobportal.repository.OAuthLoginCodeRepository;
import com.atif.jobportal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuthLoginCodeServiceTest {

    @Mock
    private OAuthLoginCodeRepository codeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OAuthLoginCodeService codeService;

    @Test
    void createsCryptographicallyStrongCodeAndStoresOnlyHash() {
        User user = new User();
        when(userRepository.findById(17L)).thenReturn(Optional.of(user));
        when(codeRepository.save(any(OAuthLoginCode.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String code = codeService.createCode(17L, "browser-session");

        var stored = org.mockito.ArgumentCaptor.forClass(OAuthLoginCode.class);
        verify(codeRepository).save(stored.capture());
        assertEquals(43, code.length());
        assertNotEquals(code, stored.getValue().getCodeHash());
        assertEquals("browser-session", stored.getValue().getSessionId());
        assertEquals(user, stored.getValue().getUser());
        assertTrueExpiresSoon(stored.getValue().getExpiresAt());
    }

    @Test
    void codeCanOnlyBeConsumedOnce() {
        User user = new User();
        OAuthLoginCode storedCode = new OAuthLoginCode(
                "hash",
                "browser-session",
                user,
                Instant.now().plusSeconds(60));
        when(codeRepository.consumeIfValid(
                anyString(),
                anyString(),
                any(Instant.class)))
                .thenReturn(1, 0);
        when(codeRepository.findByCodeHashAndSessionId(
                anyString(),
                anyString()))
                .thenReturn(Optional.of(storedCode));

        assertEquals(user, codeService.consumeCode("opaque-code", "browser-session"));
        assertThrows(
                InvalidOAuthLoginCodeException.class,
                () -> codeService.consumeCode("opaque-code", "browser-session"));
        verify(codeRepository, times(1)).findByCodeHashAndSessionId(
                anyString(),
                anyString());
    }

    @Test
    void codeCannotBeConsumedFromAnotherBrowserSession() {
        when(codeRepository.consumeIfValid(
                anyString(),
                anyString(),
                any(Instant.class)))
                .thenReturn(0);

        assertThrows(
                InvalidOAuthLoginCodeException.class,
                () -> codeService.consumeCode("opaque-code", "different-session"));
        verify(codeRepository, never()).findByCodeHashAndSessionId(
                anyString(),
                anyString());
    }

    private void assertTrueExpiresSoon(Instant expiresAt) {
        Instant now = Instant.now();
        org.junit.jupiter.api.Assertions.assertTrue(
                expiresAt.isAfter(now)
                        && expiresAt.isBefore(now.plusSeconds(121)));
    }
}
