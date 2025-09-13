package br.com.springsecurity.service;

import br.com.springsecurity.dto.CreateUserDto;
import br.com.springsecurity.dto.LoginRequest;
import br.com.springsecurity.dto.LoginResponse;
import br.com.springsecurity.entities.Role;
import br.com.springsecurity.entities.User;
import br.com.springsecurity.repository.RoleRepository;
import br.com.springsecurity.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final JwtEncoder jwtEncoder;
    private final JwtTokenService jwtTokenService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(JwtEncoder jwtEncoder,
                       JwtTokenService jwtTokenService,
                       UserRepository userRepository,
                       RoleRepository roleRepository,
                       BCryptPasswordEncoder passwordEncoder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest dto) {
        var user = userRepository.findByUsername(dto.username());
        if (user.isEmpty() || !user.get().isLoginCorrect(dto, passwordEncoder)) {
            throw new BadCredentialsException("User or password is invalid!");
        }

        var expiresIn = 300L;
        var token = jwtTokenService.generateToken(user.get(), expiresIn);

        return new LoginResponse(token, expiresIn);
    }

    @Transactional
    public void register(CreateUserDto dto) {
        var basicRole = roleRepository.findByName(Role.Values.BASIC.name());
        var userFromDb = userRepository.findByUsername(dto.username());

        if (userFromDb.isPresent())
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "User already exists.");

        var user = new User();
        user.setUsername(dto.username());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setRoles(Set.of(basicRole));

        userRepository.save(user);
    }

}
