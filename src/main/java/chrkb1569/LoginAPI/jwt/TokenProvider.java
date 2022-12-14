package chrkb1569.LoginAPI.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TokenProvider implements InitializingBean {

    private final Long tokenValidationTime;

    private final String AUTHORITIES_KEY = "auth";

    private Key key;

    private final String secret;

    public TokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.valid_time}") long time) {
        this.tokenValidationTime = time * 1000;
        this.secret = secret;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] key_set = Decoders.BASE64.decode(this.secret);
        this.key = Keys.hmacShaKeyFor(key_set);
    }

    public String createToken(Authentication authentication) {
        String authorities = authentication.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));

        Date date = new Date();

        long now = date.getTime();

        Date tokenExpiration = new Date(now + this.tokenValidationTime);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .setExpiration(tokenExpiration)
                .claim(this.AUTHORITIES_KEY, authorities)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(this.key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(this.AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new).collect(Collectors.toList());

        User user = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(user, token, authorities);
    }

    public boolean isValidate(String token) {
        try {
            Jwts
                    .parserBuilder()
                    .setSigningKey(this.key)
                    .build()
                    .parseClaimsJws(token);

            return true;
        }
        catch(SecurityException | MalformedJwtException e) {
            log.info("????????? JWT ???????????????.");
        }
        catch(ExpiredJwtException e) {
            log.info("????????? JWT ???????????????.");
        }
        catch(UnsupportedJwtException e) {
            log.info("???????????? ?????? JWT ???????????????.");
        }
        catch(IllegalArgumentException e) {
            log.info("?????? ????????? ?????????????????????.");
        }

        return false;
    }
}
