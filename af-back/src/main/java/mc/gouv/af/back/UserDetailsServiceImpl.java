//package mc.gouv.af.back;
//
//import java.util.Collection;
//
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.AuthorityUtils;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.stereotype.Component;
//
//@Component
//public class UserDetailsServiceImpl implements UserDetailsService {
//
//    @Override
//    public UserDetails loadUserByUsername(String username) {
//        return new User(username, "abc", getGrantedAuthorities(username));
//    }
//
//    private Collection<? extends GrantedAuthority> getGrantedAuthorities(String username) {
//        return AuthorityUtils.createAuthorityList("ROLE_A");
//    }
//}
