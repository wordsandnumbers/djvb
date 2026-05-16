# DJ Voice Box (djvb)

A web application for managing karaoke song queues, rooms, playlists, and live session control. The backend is a Spring Boot service that integrates with the **VoxBox** platform (via the internal `com.vpo:vbclient` library) to drive song search, queueing, playback, lights, and on-screen popups for connected rooms. The frontend is an AngularJS / Ionic single-page app that DJs and hosts use from a phone or tablet.

## Tech stack

**Backend**
- Java 17, Spring Boot 3.2.x
- Spring Web, Spring Security, Spring WebSocket, Spring Integration
- Spring Data MongoDB (domain persistence)
- Spring Session backed by Redis (`@EnableRedisHttpSession` in [DjvbApplication.java](src/main/java/com/vpo/djvoxbox/DjvbApplication.java))
- Firebase Admin SDK (auth)
- `com.vpo:vbclient` — internal VoxBox client, resolved from [GitHub Packages](https://github.com/wordsandnumbers/vbclient/packages) (see [Consuming vbclient](#consuming-vbclient) below)

**Frontend** ([djvb-ui/](djvb-ui/))
- AngularJS + Ionic + RequireJS
- Built and bundled with Grunt + Bower
- Unit tests via Karma + Jasmine

**Datastores**
- MongoDB — domain data (users, queues, playlists, avatars, managers)
- Redis — HTTP session store

## Project layout

```
src/main/java/com/vpo/djvoxbox/
  DjvbApplication.java        Spring Boot entry point
  app/                        Services (QueueManagementService, UserService, UpdateService)
  web/                        REST controllers under /api/v1/* (queue, songs, playlists, avatar, user, ...)
  domain/                     Mongo documents + repositories (User, UserQueue, Playlists, Avatar, Manager)
  config/                     SecurityConfiguration, FirebaseConfig, HttpsEnforcer, SimpleCORSFilter, VoxBoxConfig
  security/                   Custom remember-me / session pieces
  util/                       Shared helpers (e.g. SessionUtils)

src/main/resources/
  application.properties             Production config
  application-development.properties Local dev config
  firebaseServiceAccountKey.json     Firebase Admin credentials (do NOT commit real keys)
  static/                            Frontend bundle served by Spring Boot

djvb-ui/                      AngularJS/Ionic frontend sources (views, scripts, styles, tests)
docker-compose.yml            Local MongoDB + Redis
Gruntfile.js                  Frontend build pipeline
Procfile                      Heroku-style run command
```

## Prerequisites

- **Java 17** and **Maven 3.x**
- **Docker** / Docker Compose (for local MongoDB and Redis)
- **Node.js** ≥ 18 and **npm** — [package.json](package.json) still pins `engines.node` to `6.11.1` for legacy reasons; modern Node works for the tasks we use. Pass `--legacy-peer-deps` to `npm install` to tolerate the old dependency graph.
- A **Firebase service account** JSON placed at [src/main/resources/firebaseServiceAccountKey.json](src/main/resources/firebaseServiceAccountKey.json)

Grunt and Bower CLIs are pulled in as local dev dependencies — no global install needed. Run them via `./node_modules/.bin/grunt` and `./node_modules/.bin/bower`.

## Consuming vbclient

`com.vpo:vbclient` is published to GitHub Packages at [maven.pkg.github.com/wordsandnumbers/vbclient](https://github.com/wordsandnumbers/vbclient/packages). GitHub Packages requires authentication even for read, so every dev machine needs a one-time setup:

1. Create a GitHub PAT with `read:packages` scope at https://github.com/settings/tokens.
2. Export it from your shell profile:

   ```sh
   export GITHUB_PACKAGES_TOKEN=ghp_...
   ```

3. Add a server entry to `~/.m2/settings.xml` (create the file if it doesn't exist):

   ```xml
   <settings>
     <servers>
       <server>
         <id>github-vbclient</id>
         <username>YOUR_GITHUB_USERNAME</username>
         <password>${env.GITHUB_PACKAGES_TOKEN}</password>
       </server>
     </servers>
   </settings>
   ```

The `<id>` must match the repository id in [pom.xml](pom.xml#L26-L36). Once configured, `mvn` resolves vbclient transparently.

## Local setup

1. Start MongoDB and Redis:

   ```sh
   docker compose up -d
   ```

   This brings up `mongo:6` on `27017` and `redis:7-alpine` on `6379` (see [docker-compose.yml](docker-compose.yml)).

2. Install frontend dependencies (one-time):

   ```sh
   npm install --legacy-peer-deps
   ./node_modules/.bin/bower install
   ```

   `npm install` brings in Grunt, the Bower CLI, and all the Grunt plugins listed in [package.json](package.json). `bower install` then pulls the AngularJS / Ionic / Firebase frontend libraries into `bower_components/` per [bower.json](bower.json).

3. Run the backend against the `development` profile:

   ```sh
   SPRING_PROFILES_ACTIVE=development mvn spring-boot:run -DskipTests
   ```

   The dev profile reads [application-development.properties](src/main/resources/application-development.properties), which points at the local Mongo (`mongodb://localhost:27017/djvb`) and Redis. The backend listens on `http://localhost:8080`.

4. In a separate terminal, sync the UI into the backend's static-resources directory and watch for changes:

   ```sh
   ./node_modules/.bin/grunt java
   ```

   The `java` task ([Gruntfile.js](Gruntfile.js)):

   - cleans `target/classes/static/`
   - runs `wiredep` to inject Bower CSS `@import`s into [djvb-ui/styles/main.css](djvb-ui/styles/main.css)
   - copies `bower_components/` into `target/classes/static/resources/bower_components/`
   - syncs `djvb-ui/` into `target/classes/static/resources/`
   - rewrites the RequireJS config in [djvb-ui/scripts/main.js](djvb-ui/scripts/main.js)
   - watches `djvb-ui/**` and re-syncs on every change

   Then open `http://localhost:8080/` — it will redirect through the formLogin to [resources/index.html](djvb-ui/index.html) once you're authenticated. Edits in `djvb-ui/` show up after a browser reload (no livereload wired into the `java` task).

## Configuration

Key properties (set in [application.properties](src/main/resources/application.properties) or [application-development.properties](src/main/resources/application-development.properties), or overridden via environment / `-D` flags):

| Property | Purpose |
| --- | --- |
| `spring.data.mongodb.uri` | MongoDB connection string |
| `spring.redis.host` / `spring.redis.port` / `spring.redis.password` | Redis (session store) |
| `vb.organization` | VoxBox organization ID this instance is bound to |
| `manager.name` | Manager record name used to bootstrap state |
| `default.language` | Default song search language |
| `server.session.timeout` | HTTP session timeout (seconds) |

> The committed [application.properties](src/main/resources/application.properties) contains real-looking credentials. Rotate them and move them to environment variables / a secrets manager before deploying.

## Build & test

- Backend tests: `mvn test` (entry point: [src/test/java/djboxbox/DjvbApplicationTests.java](src/test/java/djboxbox/DjvbApplicationTests.java))
- Backend jar: `mvn package -DskipTests` produces `target/djvb-0.0.1-SNAPSHOT.jar`
- Frontend production build (`grunt` default task) and `grunt test` (Karma + Jasmine) are currently **not wired up for modern Node** — they relied on `node-sass` and `phantomjs`, which were dropped during the Node 24 / Spring Boot 3 upgrade. The dev loop (`grunt java`) is the supported workflow.

## Deployment

Heroku-style deploy via [Procfile](Procfile):

```
web: java -Dserver.port=$PORT -jar target/djvb-0.0.1-SNAPSHOT.jar
```

The legacy Java runtime selector lives in [system.properties](system.properties); the actual Maven build targets Java 17.
