package com.platform.bff.controller;

import com.platform.bff.dto.MeResponse;
import com.platform.bff.facade.MeFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bff")
@RequiredArgsConstructor
public class MeController {

    private final MeFacade meFacade;

    @GetMapping("/me")
    public MeResponse me(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        return meFacade.loadCurrentUser(authorization);
    }
}
