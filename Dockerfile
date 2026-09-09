# syntax=docker/dockerfile:1

# ============================================================
# Etapa 1: build
# ============================================================
# Imagen con JDK completo: aqui hace falta compilador, y el wrapper
# se descarga su propio Maven (distributionType=only-script), asi la
# version de Maven es la misma que en local en vez de la que traiga
# una imagen de terceros.
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# El wrapper y el pom primero, y las dependencias en su propia capa:
# mientras el pom no cambie, Docker reutiliza esta capa y no vuelve a
# bajarse medio Maven Central en cada push.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -B dependency:go-offline

# El codigo despues, que es lo que cambia en cada commit.
COPY src/ src/
RUN ./mvnw -B clean package -DskipTests

# ============================================================
# Etapa 2: runtime
# ============================================================
# Solo JRE: ni compilador, ni Maven, ni fuentes, ni el ~/.m2. Imagen
# mas pequena y sin herramientas que un atacante pueda aprovechar si
# llega a tener ejecucion dentro del contenedor.
FROM eclipse-temurin:21-jre

WORKDIR /app

# Usuario sin privilegios: por defecto el contenedor corre como root y
# no hay ninguna razon para que esta app lo haga.
RUN useradd --system --create-home --shell /usr/sbin/nologin cinetrack
USER cinetrack

# Comodin en vez del nombre exacto: el JAR se llama
# cinetrack-<version>.jar segun el pom, y no quiero tener que tocar el
# Dockerfile cada vez que suba la version. El .jar.original que deja el
# plugin de Spring Boot no casa con *.jar, asi que solo hay un match.
COPY --from=build --chown=cinetrack:cinetrack /app/target/*.jar app.jar

# Documental: Render publica el puerto que el mismo inyecta en $PORT, y
# server.port=${PORT:8080} ya lo lee. Esta linea no abre nada, solo deja
# escrito cual es el puerto por defecto.
EXPOSE 8080

# Sin MaxRAMPercentage la JVM se reserva ~25% de la RAM del contenedor
# para el heap y desaprovecha el resto en una instancia de 512 MB.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
