package br.com.springsecurity.service;

import br.com.springsecurity.dto.CreateAdmin;
import br.com.springsecurity.entities.Role;
import br.com.springsecurity.entities.User;
import br.com.springsecurity.repository.RoleRepository;
import br.com.springsecurity.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
public class AdminService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminService(RoleRepository roleRepository,
                        UserRepository userRepository,
                        BCryptPasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void createAdmin(CreateAdmin dto) {
        var roleAdmin = roleRepository.findByName(Role.Values.ADMIN.name());
        if (roleAdmin == null) {
            roleAdmin = new Role();
            roleAdmin.setName(Role.Values.ADMIN.name());
            roleRepository.save(roleAdmin);
        }

        var existingUser = userRepository.findByUsername(dto.username());
        if (existingUser.isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Admin already exists");

        var user = new User();
        user.setUsername(dto.username());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setRoles(Set.of(roleAdmin));

        userRepository.save(user);
    }

}
