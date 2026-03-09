# Stage 1: Webpack — minify CSS + JS
FROM node:22-alpine AS webpack
WORKDIR /assets

COPY frontend/package.json frontend/package-lock.json* ./
RUN npm install

COPY frontend/webpack.config.js ./
COPY frontend/src/main/resources/static-src/ ./src/main/resources/static-src/
RUN npm run build

# Stage 2: Maven — build the Spring Boot JAR (includes Webpack output)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY frontend/pom.xml .
RUN apk add --no-cache maven && \
    mvn dependency:go-offline -B

COPY frontend/ .
COPY --from=webpack /assets/src/main/resources/static/dist/ ./src/main/resources/static/dist/
RUN mvn package -DskipTests -B

# Stage 3: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache dumb-init
COPY --from=builder /build/target/frontend-*.jar app.jar

ENV BACKEND_URL=http://backend:8000
EXPOSE 8080
ENTRYPOINT ["dumb-init", "--"]
CMD ["java", "-jar", "app.jar"]
