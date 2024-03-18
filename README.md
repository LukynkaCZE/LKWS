# Lightweight Kotlin Web Server

A simple, easy to use and lightweight kotlin web server for small quick projects

## How to Install

(I will write this section later once its finished)

## How to Use

You want to create new instance of `LightweightWebServer` with port supplied as parameter _(defaults to 7270)_

```kotlin
val server = LightweightWebServer(port = 7270)
```

You can then listen to get calls on specific path by calling `.get(path)` on the `server` and providing lambda expression to execute when a request is received at the specified endpoint.

```kotlin
server.get("/uwu") { res ->
    res.respond("owo :3")
}
```
You can additionally include status code after the response
```kotlin
server.get("/teapot") { res ->
    res.respond("🫖", 418)
}
```

You can add Headers by adding `Pair<string, string>`to the `headers` variable of `Response`

```kotlin
server.get("/status") { res ->
    res.headers.Add(Pair("Content-Type", "application/json"))
    res.respondRedirect("{ 'status': 'operational' }")
}
```
---
**⚠️ More request types coming soon ⚠️**

## Error Handling

Listening to errors / invalid requests is pretty much the same as listening to for example `GET` requests but with the difference of the thrown exception being included in the response object

```kotlin
server.error { res ->
    res.respond("oopsie happened: ${res.exception}", 500)
}
```

---
**⚠️ There will be more. I am working on it :3 ⚠️**
