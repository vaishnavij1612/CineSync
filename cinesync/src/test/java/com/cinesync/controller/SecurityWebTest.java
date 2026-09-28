package com.cinesync.controller;

import com.cinesync.config.SecurityConfig;
import com.cinesync.service.DatabaseUserDetailsService;
import com.cinesync.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {HomeController.class, LoginController.class, ProfileController.class})
@Import(SecurityConfig.class)
class SecurityWebTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DatabaseUserDetailsService userDetailsService;

    @MockitoBean
    private UserService userService;

    @Test
    void homeIsPublic() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    void profileRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().is3xxRedirection());
    }
}
