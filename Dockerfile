FROM openjdk:8-jdk

ENV GRADLE_VERSION 5.5.1
ENV GRADLE_SHA 222a03fcf2fcaf3691767ce9549f78ebd4a77e73f9e23a396899fb70b420cd00
ENV SPRING_PROFILES_ACTIVE production

RUN cd /usr/lib \
 && curl -fl https://downloads.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip -o gradle-bin.zip \
 && echo "$GRADLE_SHA gradle-bin.zip" | sha256sum -c - \
 && unzip "gradle-bin.zip" \
 && ln -s "/usr/lib/gradle-${GRADLE_VERSION}/bin/gradle" /usr/bin/gradle \
 && rm "gradle-bin.zip"

ENV GRADLE_HOME /usr/lib/gradle
ENV PATH $PATH:$GRADLE_HOME/bin

WORKDIR /usr/bin/app

COPY . .
RUN gradle clean ktlintCheck build -x test
RUN ls -la build/libs
EXPOSE 8080 8085

CMD ["gradle", "bootRun"]
