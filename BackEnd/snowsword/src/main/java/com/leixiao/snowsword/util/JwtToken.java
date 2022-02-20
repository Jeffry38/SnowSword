package com.leixiao.snowsword.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import java.util.Date;
import com.leixiao.snowsword.dao.UserRepository;
import com.leixiao.snowsword.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JwtToken {
    @Autowired
    public  UserRepository userRepository;

    private static int expires = 600;  //过期时间（分钟）

    public String createToken(User user) {
        String username = user.getUsername();
        Algorithm algorithm = Algorithm.HMAC256(user.getPassword());
        String token = JWT.create()
                .withClaim("username", username)
                .withExpiresAt(new Date(System.currentTimeMillis() + expires * 1000 * 60  ))  //System.currentTimeMillis() 返回以毫秒为单位的时间戳
                .sign(algorithm);
        return token;
    }

    public String verifyToken(String token) {
        Claim username = JWT.decode(token).getClaim("username");
        User user = userRepository.findUserByUsername(username.asString());
        Algorithm algorithm = Algorithm.HMAC256(user.getPassword());
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        jwtVerifier.verify(token);

        return username.asString();
    }
}
