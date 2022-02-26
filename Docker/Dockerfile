FROM yuyangjia/jmn:1.0
LABEL maintainer="leixiao"
WORKDIR /root

COPY ./BackEnd ./BackEnd

RUN source /etc/profile && \
    mvn package -f BackEnd/snowsword/pom.xml -Dmaven.test.skip=true