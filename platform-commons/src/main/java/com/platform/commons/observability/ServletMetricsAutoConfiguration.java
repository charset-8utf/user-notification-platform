package com.platform.commons.observability;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnClass(name = "jakarta.servlet.http.HttpServletRequest")
@Import(HttpErrorMetricsFilter.class)
public class ServletMetricsAutoConfiguration {
}
