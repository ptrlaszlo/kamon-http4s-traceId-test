## TraceID

Run the project and do two different calls: `/iofail` and `/exception`.

When raising an error with `IO.raiseError`, the log in error handler is missing the traceId (Having `[undefined]`)

### IO.raiseError
`curl localhost:8080/iofail`

From the logs:

```
[info] 2018-09-07 09:16:43,495 [bd23a930d537f104] INFO  [Main$] [scala-execution-context-global-26] - test iofail
[info] 2018-09-07 09:16:43,496 [undefined] WARN  [Main$] [scala-execution-context-global-26] - Error Fail with IO.raiseError
```

### throw new Exception
`curl localhost:8080/exception`

From the logs:

```
[info] 2018-09-07 09:16:55,421 [b9b0c83aa473dfcc] INFO  [Main$] [scala-execution-context-global-25] - test exception
[info] 2018-09-07 09:16:55,421 [b9b0c83aa473dfcc] WARN  [Main$] [scala-execution-context-global-25] - Error Fail with throwing an exception
```
