# Inconsistent error handling when micronaut.server.max-request-size exceeded

There seems to be some inconsistency in handling `ContentLengthExceededException` when posting files with multipart forms. The variations I've observed in behaviour depend on:
* How much `micronaut.server.max-request-size` was exceeded.
  And
* Is an error handler used and which status code that error handler returns.

Also the client being used seems to play a role, which makes me question if this a bug in mn or have I just done something dumb.

Below you'll find three test cases for uploading files and handling errors:
* No error handler (So what ever mn does by default)
* Local error handler returning HTTP 413
* Local error handler returning HTTP 500

Each case is tested with three different clients: Firefox, HTTPie and curl. With curl I can consistently see expected behaviour, but with Firefox and HTTPie I can consistently see the actual behaviour.    
Unfortunately I was not able to write JUnit tests with which I could replicate these.

## Steps to Reproduce

Sample application can be found from [here].


### Case: No custom error handler

1. Start sample application by running: `./mvnw mn:run`
2. See client specific steps below

| Firefox                                                                | HTTPie                                                                                     | curl                                                                          |
|------------------------------------------------------------------------|--------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------|
| Navigate to http://localhost:8080/                                     | `http --print=hHb --form http://localhost:8080/upload files@src/test/resources/900k.dummy` | `curl http://localhost:8080/upload -F 'files=@src/test/resources/900k.dummy'` |
| Under **None or global** select `900k.dummy` from `src/test/resources` |                                                                                            |                                                                               |
| Push `Upload attachments`                                              |                                                                                            |                                                                               |

#### Expected behaviour

Regardles of client used, all return `HTTP 413` error with message `The content length [921600] exceeds the maximum allowed content length [614400]`.

#### Actual behaviour

| Firefox (NOK)                                    | HTTPie (NOK)                                     | curl (OK)                                                                                                 |
|--------------------------------------------------|--------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| POST to  `/upload`  endpoint hangs indefinetely. | POST to  `/upload`  endpoint hangs indefinetely. | `HTTP 413` with message `The content length [921803] exceeds the maximum allowed content length [614400]` |

It's worth noting, that if you choose a slightly smaller file (say, the 800k.dummy provided) you can observe the expected behaviour with all clients.


### Case: Local error handler that returns HTTP 413

The expected behaviour and actual behaviour are consistent with previous case. Only the endpoint called differes as it has a local error handler.

1. Start sample application by running: `./mvnw mn:run`
2. See client specific steps below

| Firefox                                                                           | HTTPie                                                                                                        | curl                                                                                                |
|-----------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------|
| Navigate to http://localhost:8080/                                                | `http --print=hHb --form http://localhost:8080/uploadLocalErrorHandling files2@src/test/resources/900k.dummy` | `curl -v http://localhost:8080/uploadLocalErrorHandling -F 'files2=@src/test/resources/900k.dummy'` |
| Under **Local with HttpStatus 413** select `900k.dummy` from `src/test/resources` |                                                                                                               |                                                                                                     |
| Push `Upload attachments`                                                         |                                                                                                               |                                                                                                     |                                                   |                                                                                                              |                                                                                                 |                                          |                                                                                            |                                                                               |

#### Expected behaviour

Regardles of client used, all return `HTTP 413` error with message `The content length [921600] exceeds the maximum allowed content length [614400]`.

#### Actual behaviour

| Firefox (NOK)                                                      | HTTPie (NOK)                                                       | curl (OK)                                                                                                 |
|--------------------------------------------------------------------|--------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| POST to  `/uploadLocalErrorHandling`  endpoint hangs indefinetely. | POST to  `/uploadLocalErrorHandling`  endpoint hangs indefinetely. | `HTTP 413` with message `The content length [921803] exceeds the maximum allowed content length [614400]` |

It's worth noting, that if you choose a slightly smaller file (say, the 800k.dummy provided) you can observe the expected behaviour with all clients.


### Case: Local error handler that returns HTTP 500

1. Start sample application by running: `./mvnw mn:run`
2. See client specific steps below

| Firefox                                                                           | HTTPie                                                                                                                         | curl                                                                                                                 |
|-----------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------|
| Navigate to http://localhost:8080/                                                | `http --print=hHb --form http://localhost:8080/uploadLocalErrorHandlingReturnServerError files3@src/test/resources/900k.dummy` | `curl -v http://localhost:8080/uploadLocalErrorHandlingReturnServerError -F 'files3=@src/test/resources/900k.dummy'` |
| Under **Local with HttpStatus 500** select `900k.dummy` from `src/test/resources` |                                                                                                                                |                                                                                                                      |
| Push `Upload attachments`                                                         |                                                                                                                                |                                                                                                                      |

#### Expected behaviour

Regardles of client used, all return `HTTP 500` error with message `The content length [921600] exceeds the maximum allowed content length [614400]`.

#### Actual behaviour

| Firefox (NOK)              | HTTPie (NOK)                                                                   | curl (OK)                                                                                                 |
|----------------------------|--------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| `The connection was reset` | `ConnectionError: ('Connection aborted.', BrokenPipeError(32, 'Broken pipe'))` | `HTTP 500` with message `The content length [921803] exceeds the maximum allowed content length [614400]` |

### Environment Information

- **Operating System**: macOs Big Sur 11.
- **Micronaut Version:** 2.2.0
- **JDK Version:** 1.8.0_232, vendor: AdoptOpenJDK
- **Browser:** Firefox 85.0
- **curl:** 7.64.1
- **HTTPie:** 2.3.0