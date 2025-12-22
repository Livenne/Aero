package io.github.livenne.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import java.util.Date;

public class JwtUtils {
    private static final String SECRET = "ZsDSF@DFGaaDBVyAd89nhf3uhgNhUUnPsLhXnVkhESxiZXuGwSh2aio981ef";
    private static final long EXPIRE_TIME = 1000*60*60*24*3;


    public static String getToken(Long id) {
        Algorithm algorithm = Algorithm.HMAC256(SECRET);
        return JWT.create()
                .withIssuer(String.valueOf(id))
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .sign(algorithm);
    }

    public static DecodedJWT getDecoded(String token) {
        return JWT.decode(token);
    }

    public static Boolean validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
        }catch (JWTVerificationException e){
            return false;
        }

        return true;
    }
}
