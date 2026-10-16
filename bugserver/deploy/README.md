# Bug report server

Standalone HTTP service behind `https://bug.tribaltrouble.org`. The game POSTs a JSON
`BugReport` (stack trace, build info, gzipped event log) to `/report`. The server stores it
on disk, answers with the report id, then files it in Plane as a work item, attaching the
event log. Each report is kept as:

```
/var/bugreports/<yyyy-MM-dd>/<epoch-millis>-<random>/
    stack_trace.txt
    event.log.gz        # the crashed run's event log, if it was being recorded
    report.properties   # received time, client IP, version, commit, OS, Java, Plane work item id
```

If Plane is unreachable or misconfigured, the report stays on disk without a `plane_issue`
entry and is retried every 15 minutes (and on startup).

Reproduce a report with (on the reported commit):

```
./gradlew :tt:run --args="--eventload zipped /path/to/<id>/event.log.gz"
```

## Running locally

```
./gradlew :bugserver:run          # listens on 127.0.0.1:8091, stores in bugserver/build/reports
curl -i http://127.0.0.1:8091/health
```

Point the game at it with `-Dcom.oddlabs.tt.bugreport.url=http://127.0.0.1:8091/report`.

## Deploying

1. Build: `./gradlew :bugserver:installDist`, copy `bugserver/build/install/bugserver/`
   to `/opt/bugserver/current` on the box.
2. `sudo mkdir -p /var/bugreports && sudo chown <user> /var/bugreports`
3. Plane: create an API token (workspace settings > API tokens), then copy
   `bugserver.env.template` to `/etc/bugserver.env`, fill it in and `sudo chmod 600 /etc/bugserver.env`.
4. Install the unit: replace `__SERVICE_USER__` in `bugserver.service`, copy it to
   `/etc/systemd/system/`, then `sudo systemctl enable --now bugserver`.
5. Install `nginx-bug.tribaltrouble.org.conf` into `/etc/nginx/sites-enabled/`, point the
   `bug.tribaltrouble.org` DNS record at the box, and run
   `sudo certbot --nginx -d bug.tribaltrouble.org` for TLS.
6. Check: `curl https://bug.tribaltrouble.org/health` prints `ok`, and
   `journalctl -u bugserver` does not warn that Plane is not configured.

Send a test report from the box:

```
curl -i -H 'Content-Type: application/json' \
  -d '{"version":"test","commit":"test","os":"curl","stack_trace":"java.lang.RuntimeException: test report\n\tat Test.main(Test.java:1)"}' \
  http://127.0.0.1:8091/report
```

Settings (environment variables, or `-Dbugserver.<key>` system properties with the
lowercase key without the prefix):

| Variable                     | Default           |
| ---------------------------- | ----------------- |
| `BUGSERVER_BIND`             | `127.0.0.1`       |
| `BUGSERVER_PORT`             | `8091`            |
| `BUGSERVER_DIR`              | `/var/bugreports` |
| `BUGSERVER_PER_IP_HOURLY`    | `20`              |
| `BUGSERVER_MIN_FREE_MB`      | `1024`            |
| `BUGSERVER_PLANE_*`          | see `bugserver.env.template` |
