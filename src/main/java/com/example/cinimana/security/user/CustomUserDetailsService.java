// src/main/java/com/example/cinimana/security/user/CustomUserDetailsService.java
package com.example.cinimana.security.user;

import com.example.cinimana.model.Admin;
import com.example.cinimana.model.Caissier;
import com.example.cinimana.model.Client;
import com.example.cinimana.model.Commercial;
import com.example.cinimana.repository.AdminRepository;
import com.example.cinimana.repository.CaissierRepository;
import com.example.cinimana.repository.ClientRepository;
import com.example.cinimana.repository.CommercialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    //
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private CommercialRepository commercialRepository;

    @Autowired
    private CaissierRepository caissierRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 1. Admin
        if (adminRepository.findByEmail(email).isPresent()) {
            Admin admin = adminRepository.findByEmail(email).get();
            return new User(admin.getEmail(), admin.getMotDePasse(),
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
        }

        // 2. Commercial
        if (commercialRepository.findByEmail(email).isPresent()) {
            Commercial commercial = commercialRepository.findByEmail(email).get();
            if (!commercial.isActif()) {
                throw new UsernameNotFoundException("Compte commercial inactif : " + email);
            }
            return new User(commercial.getEmail(), commercial.getMotDePasse(),
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_COMMERCIAL")));
        }

        // 3. Caissier
        if (caissierRepository.findByEmail(email).isPresent()) {
            Caissier caissier = caissierRepository.findByEmail(email).get();
            if (!caissier.isActif()) {
                throw new UsernameNotFoundException("Compte caissier inactif : " + email);
            }
            return new User(caissier.getEmail(), caissier.getMotDePasse(),
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_CAISSIER")));
        }

        // 4. Client
        if (clientRepository.findByEmail(email).isPresent()) {
            Client client = clientRepository.findByEmail(email).get();
            return new User(client.getEmail(), client.getMotDePasse(),
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_CLIENT")));
        }

        throw new UsernameNotFoundException("Utilisateur introuvable : " + email);
    }
}
