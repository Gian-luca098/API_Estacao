# ==============================================================================
# Etapa 1 — build: compila o projeto com Maven + JDK 21 e gera o jar executável.
# Esta imagem só existe durante o build; não vai para a imagem final.
# ==============================================================================
FROM maven:3.9.9-eclipse-temurin-25 AS build
WORKDIR /app

# Copia só o pom.xml primeiro para cachear as dependências em uma camada separada:
# elas só serão baixadas de novo se o pom.xml mudar, não a cada alteração de código-fonte.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests


# ==============================================================================
# Etapa 2 — runtime: apenas o JRE + o jar já compilado, sem Maven nem código-fonte.
# Reduz drasticamente o tamanho final da imagem e a superfície de ataque.
# ==============================================================================
FROM eclipse-temurin:25-jre-alpine AS runtime
WORKDIR /app

# Usuário dedicado, não-root, por segurança.
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/meteorologica-api.jar app.jar

EXPOSE 8080

# O Alpine já traz "wget" via busybox, então não é preciso instalar curl para o healthcheck.
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
