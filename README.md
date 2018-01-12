# w2018

A small application designed to store and display weather observations.

## Development Mode

Run the server with `lein run` and use figwheel for easier developement with `lein figwheel`.
PostgreSQL can be started with `docker-compose up -d`.

## Production Build

```
lein clean
lein uberjar
```

That should compile the clojurescript code first, and then create the standalone jar.

When you run the jar you can set the port the ring server will use by setting the environment variable PORT.
If it's not set, it will run on port 3000 by default.
