# Smart Campus Sensor & Room Management API

This is a coursework project for **5COSC022W - Client-Server Architectures**.

The API is built with **JAX-RS (`javax.ws.rs`) using Jersey 2.44** and runs on an embedded **Grizzly** server. It manages campus rooms, sensors inside rooms, and sensor reading history.

## Tech stack

- Java 17
- Maven
- JAX-RS (`javax.ws.rs`, Jersey 2.44)
- Grizzly HTTP server
- In-memory data structures only (`ConcurrentHashMap`, `CopyOnWriteArrayList`)

No Spring Boot and no database are used, based on the coursework rules.
Also, all REST imports use `javax.ws.rs` (not `jakarta.ws.rs`) to stay aligned with Jersey 2.44.

## API overview

Base URL:

`http://localhost:8080/api/v1`

Main resources:

- `GET /api/v1` - discovery endpoint
- `GET /api/v1/rooms`
- `POST /api/v1/rooms`
- `GET /api/v1/rooms/{roomId}`
- `DELETE /api/v1/rooms/{roomId}`
- `GET /api/v1/sensors`
- `GET /api/v1/sensors?type=CO2`
- `POST /api/v1/sensors`
- `GET /api/v1/sensors/{sensorId}/readings`
- `POST /api/v1/sensors/{sensorId}/readings`

## How to build and run

### 1) Build

```bash
mvn clean package
```

### 2) Run server

```bash
mvn exec:java
```

You should see a message saying the API started at `http://localhost:8080/api/v1/`.

## Sample curl commands

### 1) Discovery endpoint

```bash
curl -i http://localhost:8080/api/v1
```

### 2) Create a room

```bash
curl -i -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"LIB-301","name":"Library Quiet Study","capacity":80}'
```

### 3) Get all rooms

```bash
curl -i http://localhost:8080/api/v1/rooms
```

### 4) Create a sensor linked to a real room

```bash
curl -i -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-001","type":"CO2","status":"ACTIVE","currentValue":0.0,"roomId":"LIB-301"}'
```

### 5) Filter sensors by type

```bash
curl -i "http://localhost:8080/api/v1/sensors?type=CO2"
```

### 6) Add a reading to a sensor

```bash
curl -i -X POST http://localhost:8080/api/v1/sensors/CO2-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":421.8}'
```

### 7) Get reading history for one sensor

```bash
curl -i http://localhost:8080/api/v1/sensors/CO2-001/readings
```

### 8) Try deleting a room that still has sensors (expected 409)

```bash
curl -i -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

### 9) Try creating a sensor with missing linked room (expected 422)

```bash
curl -i -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-404","type":"Temperature","status":"ACTIVE","currentValue":0.0,"roomId":"NO-ROOM"}'
```

### 10) Try adding reading while sensor is MAINTENANCE (expected 403)

```bash
curl -i -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-777","type":"Temperature","status":"MAINTENANCE","currentValue":0.0,"roomId":"LIB-301"}'

curl -i -X POST http://localhost:8080/api/v1/sensors/TEMP-777/readings \
  -H "Content-Type: application/json" \
  -d '{"value":22.5}'
```

---

## Report answers (coursework questions)

### Part 1.1 - Resource lifecycle in JAX-RS

By default, JAX-RS resource classes are **request-scoped**, so a new instance is created for each incoming request (unless explicitly configured as singleton).

In practical terms, this means instance fields in resource classes are not reliable for shared app state. To avoid data loss and race conditions, I kept shared data in a central in-memory store using thread-safe structures (`ConcurrentHashMap`, `CopyOnWriteArrayList`) so concurrent requests can safely read/write without corrupting data.

### Part 1.2 - Why hypermedia (HATEOAS) is helpful

Hypermedia makes APIs easier to navigate because responses include links to related actions/resources, instead of forcing clients to hardcode every URL.

For client developers, this is nice because the API becomes more self-describing. If routes evolve over time, clients can follow provided links and adapt with less breakage compared to relying only on static docs.

### Part 2.1 - Returning only IDs vs full room objects

Returning only IDs is lighter on bandwidth and can be enough for simple list pages, but then clients usually need extra calls to get details.

Returning full room objects costs more payload size but saves follow-up requests and can simplify client logic. In this API, full room objects are returned for convenience and readability.

### Part 2.2 - Is DELETE idempotent?

Yes, the DELETE behavior is idempotent in this implementation.

If the room exists and is empty, the first request removes it and returns `204`. If the exact same request is sent again, the room is already gone, and it still returns `204`. The server state after repeated requests is the same, which is exactly what idempotent means.

### Part 3.1 - What if client sends wrong content type for POST?

The POST methods use `@Consumes(MediaType.APPLICATION_JSON)`. If a client sends something like `text/plain` or `application/xml`, JAX-RS cannot match the method for that media type and typically responds with **415 Unsupported Media Type**.

So the request is rejected before normal business logic runs.

### Part 3.2 - Why query param for filtering is better than path segment

Filtering is usually optional and non-hierarchical, so query params fit naturally (`/sensors?type=CO2`).

Using path for filter values (`/sensors/type/CO2`) makes the URL look like a different sub-resource rather than a filter on one collection. Query params also combine better when adding more filters later (`?type=CO2&status=ACTIVE`).

### Part 4.1 - Benefits of Sub-Resource Locator pattern

Sub-resource locators keep each class focused and avoid a huge "god controller" with every nested route in one place.

In this project, sensor list/create logic stays in `SensorResource`, while reading history logic is delegated to `SensorReadingResource`. That separation keeps code easier to scale, test, and maintain as the API grows.

### Part 5.2 - Why 422 is more accurate than 404 for linked room validation

The endpoint itself exists and the JSON body is syntactically valid, so this is not really a classic "resource URL not found" case.

The problem is a semantic validation issue inside the payload (`roomId` points to a missing room), so **422 Unprocessable Entity** communicates the issue more precisely than a plain 404.

### Part 5.4 - Security risk of exposing stack traces

Returning raw stack traces can leak internal package names, class names, method paths, file structures, library details, and other implementation hints.

An attacker can use that info to map your backend and craft targeted attacks around known weak points. That is why the API uses a global 500 mapper with a generic safe error message.

### Part 5.5 - Why filters are better for cross-cutting logging

Filters centralize repeated concerns like logging in one place and apply them consistently to every endpoint.

If logging is manually added in each method, it gets repetitive, easy to forget, and harder to maintain. With JAX-RS request/response filters, all calls are logged uniformly without cluttering business logic.

---

## Error handling implemented

- `RoomNotEmptyException` -> `409 Conflict`
- `LinkedResourceNotFoundException` -> `422 Unprocessable Entity`
- `SensorUnavailableException` -> `403 Forbidden`
- `ExceptionMapper<Throwable>` -> `500 Internal Server Error` (safe generic message)

## Logging implemented

`ApiLoggingFilter` implements both:

- `ContainerRequestFilter` for incoming method + URI
- `ContainerResponseFilter` for outgoing status code

## Quick video demo checklist

For the required video, a simple flow you can follow:

1. Show `GET /api/v1`
2. Create room
3. Create sensor linked to room
4. Filter sensors by type
5. Add reading and show reading history
6. Show 409 by deleting non-empty room
7. Show 422 with missing linked room
8. Show 403 using MAINTENANCE sensor reading

That covers the major coursework requirements clearly.
