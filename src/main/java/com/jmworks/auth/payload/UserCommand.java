package com.jmworks.auth.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserCommand {
    String oper;
    Long id;
}
