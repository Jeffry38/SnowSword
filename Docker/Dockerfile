FROM openjdk:8u322-oracle

LABEL maintainer="leixiao"

WORKDIR /root

COPY ./BackEnd ./BackEnd
COPY ./FrontEnd ./FrontEnd
COPY ./Docker/settings.xml ./
COPY ./Docker/application.properties ./
COPY ./Docker/wait-for-it.sh ./

RUN curl 'https://cdn.npmmirror.com/binaries/node/v16.5.0/node-v16.5.0-linux-x64.tar.gz' -o node-v16.5.0-linux-x64.tar.gz && \
    tar xvf node-v16.5.0-linux-x64.tar.gz && \
    ln -s /root/node-v16.5.0-linux-x64/bin/node /bin/node && \
    cd /root/FrontEnd && \
    /root/node-v16.5.0-linux-x64/bin/npm install --registry=https://registry.npm.taobao.org && \
    /root/node-v16.5.0-linux-x64/bin/npm run build:prod && \
    mv dist/* /root/BackEnd/snowsword/src/main/resources/static 

RUN curl 'https://archive.apache.org/dist/maven/maven-3/3.8.4/binaries/apache-maven-3.8.4-bin.tar.gz' -o apache-maven-3.8.4-bin.tar.gz && \
    tar xvf apache-maven-3.8.4-bin.tar.gz && \
    mv settings.xml ./apache-maven-3.8.4/conf/ && \
    mv application.properties ./BackEnd/snowsword/src/main/resources/ && \
    ./apache-maven-3.8.4/bin/mvn package -f BackEnd/snowsword/pom.xml -Dmaven.test.skip=true && \
    chmod +x wait-for-it.sh