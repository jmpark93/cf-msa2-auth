package com.jmworks.auth.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JwtResponse {
    private String accessToken;
    private String type = "Bearer";

    private Long id;
    private String username;
    private String email;

    private String imageURL;

    private List<String> roles;

    public JwtResponse(String accessToken, Long id, String username, String email, String imageURL, List<String> roles) {
        this.accessToken = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.imageURL = imageURL;
        this.roles = roles;
    }
}
