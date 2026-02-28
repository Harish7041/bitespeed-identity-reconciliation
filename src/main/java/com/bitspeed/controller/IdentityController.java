package com.bitspeed.controller;

import com.bitspeed.dto.IdentityRequest;
import com.bitspeed.dto.IdentityResponse;
import com.bitspeed.service.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IdentityController {

    @Autowired
    private IdentityService identityService;

    @PostMapping("/identify")
    public IdentityResponse identify(@RequestBody IdentityRequest request) {
        return identityService.reconcile(request);
    }
}