# CineTrack

A small web app to track which movies you've watched, backed by the TMDB API.
Built as a portfolio project with Spring Boot (Java 21), Spring Security (JWT),
Spring Data JPA, PostgreSQL and Thymeleaf.

## Features

- Registration and login (stateless JWT)
- Movie search via TMDB, with a detail view (year, director, synopsis)
- Mark a movie as watched / not watched, per user

## Requirements

- Java 21
- Maven (or the included `./mvnw` wrapper)
- PostgreSQL running locally
- A [TMDB API key](https://www.themoviedb.org/settings/api) (free)

## Local setup

1. Create a local PostgreSQL database (e.g. `cinetrack_dev`).
2. Copy `src/main/resources/application-local.properties.example` to
   `src/main/resources/application-local.properties` and fill in your local
   DB credentials, a JWT secret and your TMDB API key. This file is
   gitignored — it's never committed.
3. Activate the `local` Spring profile, otherwise the app tries to resolve
   `${DATABASE_URL}`, `${JWT_SECRET}`, etc. from the environment (the
   production config) and fails to start. Set it as an environment variable:

   ```
   SPRING_PROFILES_ACTIVE=local
   ```

   In IntelliJ: *Edit Configurations…* → your Spring Boot run configuration
   → *Environment variables*. Equivalent alternatives: VM option
   `-Dspring.profiles.active=local`, or program argument
   `--spring.profiles.active=local`.

4. Run the app (`./mvnw spring-boot:run` or from your IDE). On startup you
   should see a log line like `The following 1 profile is active: "local"`.

`src/main/resources/application.properties` is the base/production config —
it only reads values from environment variables (`DATABASE_URL`,
`JWT_SECRET`, `TMDB_API_KEY`) with no defaults, so it's safe to commit and
never needs editing for local dev.

`DATABASE_URL` is the full JDBC connection string, credentials included —
the format Neon hands you under *Connect* → *Java / JDBC*:

```
jdbc:postgresql://<host>/<database>?user=<user>&password=<password>&sslmode=require
```

It must start with `jdbc:` — the bare `postgresql://user:pass@host/db` form
some providers show by default is not a JDBC URL and Hikari will reject it.
