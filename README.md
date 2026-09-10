# 🎬 CineTrack

¿Alguna vez has terminado una peli y, dos semanas después, no recordabas si ya la habías visto? Para eso nace CineTrack: un diario de cine personal, simple, sin florituras, donde buscas una película, la marcas como vista y ya está — tu propio registro, sin depender de la memoria.

Es un proyecto de portfolio hecho con Spring Boot (Java 21), pensado para ser pequeño pero cuidado de principio a fin: backend limpio, seguridad en condiciones y un frontend que no da vergüenza enseñar.

**🔗 Demo en vivo: [cinetrack-qdnr.onrender.com](https://cinetrack-qdnr.onrender.com/)**
_(está en el plan gratuito de Render, así que si nadie la ha visitado en un rato la primera carga puede tardar unos 30-50 segundos en "despertar" — luego va fluida)._

## ✨ Qué hace

- **Registro y login** con JWT (sin sesiones, sin cookies raras, todo *stateless*).
- **Busca cualquier película** gracias a la API de TMDB, con póster incluido.
- **Ficha de detalle**: año, director y sinopsis, un clic más allá del resultado de búsqueda.
- **Marca (o desmarca) como vista** con una pequeña animación de sello — el gesto queda satisfactoriamente físico, como si estamparas un billete de cine.
- Tu lista de vistas es **tuya**: cada usuario ve solo lo suyo.

Y ya. A propósito **no** hay valoraciones, listas, amigos ni estadísticas — el objetivo era hacer pocas cosas y hacerlas bien, no construir un Letterboxd.

## 📸 Capturas

**Home**
![Home de CineTrack](docs/screenshots/cinetrack_home.jpg)

**Buscar películas**
![Búsqueda de películas](docs/screenshots/cinetrack_search.jpg)

**Mis películas vistas**
![Mis películas vistas](docs/screenshots/cinetrack_mis_pelis_vistas.jpg)

**Detalle de película**
![Detalle de película](docs/screenshots/cinetrack_film_details.jpg)

## 🛠️ Con qué está hecho

- **Java 21** + **Spring Boot**
- **Spring Security** con JWT propio (librería `jjwt`)
- **Spring Data JPA** + **PostgreSQL**
- **Thymeleaf** para las vistas
- **TMDB API** como fuente de datos de películas
- **Docker** para el despliegue (imagen en dos etapas: build con JDK, runtime solo con JRE, usuario sin privilegios)

## 🚀 Ponlo a correr en tu máquina

Necesitas:

- Java 21
- Maven (o el wrapper incluido, `./mvnw`)
- PostgreSQL corriendo en local
- Una [API key de TMDB](https://www.themoviedb.org/settings/api) (gratis, se pide en dos minutos)

Pasos:

1. Crea una base de datos local (por ejemplo, `cinetrack_dev`).
2. Copia `src/main/resources/application-local.properties.example` a
   `src/main/resources/application-local.properties` y rellena tus credenciales
   de BD, un secreto para el JWT y tu API key de TMDB. Este archivo está
   en `.gitignore` — nunca se sube al repo.
3. Activa el perfil `local` de Spring; si no, la app intentará leer
   `${DATABASE_URL}`, `${JWT_SECRET}`, etc. de variables de entorno (la
   configuración de producción) y no arrancará. Actívalo así:

   ```
   SPRING_PROFILES_ACTIVE=local
   ```

   En IntelliJ: *Edit Configurations…* → tu configuración de Spring Boot →
   *Environment variables*. También vale como opción de VM
   (`-Dspring.profiles.active=local`) o argumento de programa
   (`--spring.profiles.active=local`).

4. Arranca la app (`./mvnw spring-boot:run` o desde tu IDE). Si todo va bien
   verás en el log algo como `The following 1 profile is active: "local"`.

`src/main/resources/application.properties` es la configuración base/producción:
solo lee variables de entorno (`DATABASE_URL`, `JWT_SECRET`, `TMDB_API_KEY`),
sin valores por defecto, así que es segura de subir al repo y no hace falta
tocarla para desarrollar en local.

`DATABASE_URL` es la cadena de conexión JDBC completa, credenciales incluidas
— el formato que te da Neon en *Connect* → *Java / JDBC*:

```
jdbc:postgresql://<host>/<database>?user=<user>&password=<password>&sslmode=require
```

Tiene que empezar por `jdbc:` — el formato `postgresql://user:pass@host/db`
que muestran algunos proveedores por defecto no es una URL JDBC y Hikari lo
rechazará.

## ☁️ Despliegue

Preparada para desplegarse en **Render** vía Docker: build multi-etapa,
puerto tomado de `$PORT`, y JVM ajustada con `MaxRAMPercentage` para no
desperdiciar memoria en instancias pequeñas.

## 📄 Licencia

MIT. Los datos de películas los proporciona [TMDB](https://www.themoviedb.org/) —
este producto usa su API pero no está respaldado ni certificado por TMDB.
