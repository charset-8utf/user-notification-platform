package com.crud.config;

import com.crud.security.JwtRoleSupport;
import com.crud.security.JwtSecretKeyFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

/** JWT security helpers required by {@link JwtConfig} in {@code WebMvcTest}. */
@TestConfiguration
@Import({JwtSecretKeyFactory.class, JwtRoleSupport.class})
public class WebMvcJwtTestSupport {
}
