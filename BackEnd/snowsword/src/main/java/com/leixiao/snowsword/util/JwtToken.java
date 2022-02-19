package com.leixiao.snowsword.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;

import java.util.Date;

public class JwtToken {
    private static Algorithm algorithm = Algorithm.HMAC256("178d34u7dgahk");
    private static int expires = 600;  //过期时间（分钟）

    public String createToken(String username) {
        String token = JWT.create()
                .withClaim("username", username)
                .withExpiresAt(new Date(System.currentTimeMillis() + expires * 1000 * 60  ))  //System.currentTimeMillis() 返回以毫秒为单位的时间戳
                .sign(algorithm);
        return token;
    }

    public String verifyToken(String token) {
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        jwtVerifier.verify(token);
        Claim username = JWT.decode(token).getClaim("username");
        return username.asString();
    }
}
