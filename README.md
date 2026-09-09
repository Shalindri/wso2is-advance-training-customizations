# WSO2 IS 7.3.0 customization labs — v2 (beginner edition)

Eight small customizations for WSO2 Identity Server 7.3.0. Each one does **one obvious
thing** — print a log line, set one attribute, reject one password — so you can see how the
plumbing works without reading a lot of code.

If you have written Java before and want the fuller versions, with error handling and the
awkward edge cases spelled out, use the **v1** project next door
(`../iam-training-7.3.0`). The two are the same eight extension points.

## The eight labs

| # | Folder | What it does | What you learn |
|---|---|---|---|
| 01 | [lab01-custom-event-handler](lab01-custom-event-handler/) | When a user is created: log a line **and give them a default `country` attribute** | reacting to things that happen |
| 02 | [lab02-custom-user-store-listener](lab02-custom-user-store-listener/) | Log a line every time someone logs in | hooking user store operations |
| 03 | [lab03-custom-post-authentication-handler](lab03-custom-post-authentication-handler/) | After a successful login, log who signed in to which app | running code after login |
| 04 | [lab04-pre-update-password-action](lab04-pre-update-password-action/) | Reject any password containing the word "password" | calling **your own web service** from IS |
| 05 | [lab05-custom-jdbc-user-store-manager](lab05-custom-jdbc-user-store-manager/) | Log a line whenever a password is checked | reusing the product's user store and changing one method |
| 06 | [lab06-custom-outbound-provisioning-connector](lab06-custom-outbound-provisioning-connector/) | Log every user create / update / delete | copying users out to another system |
| 07 | [lab07-custom-oauth2-grant-type](lab07-custom-oauth2-grant-type/) | Get a token with a **mobile number + password** instead of a username | inventing your own OAuth login recipe |
| 08 | [lab08-custom-local-authenticator](lab08-custom-local-authenticator/) | Replace the login step with your own check | plugging in your own way of identifying people |

Every folder has its own `README.md` with copy-paste commands, and the original tutorial
under `docs/`.

## Start here

| If you are… | Read |
|---|---|
| **a participant, new to Java** | [JAVA-BASICS-FOR-THE-LABS.md](JAVA-BASICS-FOR-THE-LABS.md) — the Java you need, in about 20 minutes. Written to be read on your own; hand this one out |
| **the trainer, preparing** | [JAVA-IN-15-MINUTES.md](JAVA-IN-15-MINUTES.md) — a timed 15-minute script for delivering the same material live |
| **setting up a Windows machine** | [PREREQUISITES-WINDOWS.md](PREREQUISITES-WINDOWS.md) — software, versions, install steps, and the Windows command equivalents for every lab |
| **ready to build** | keep reading |

Print-ready PDFs of all of the above are in [`pdf/`](pdf/). Regenerate them after editing any
markdown with `./make-pdfs.sh` (needs pandoc + Chrome; the script's header notes the Windows
alternatives).

> **Windows users:** the commands in these READMEs are written for Linux/macOS. Section 8 of
> [PREREQUISITES-WINDOWS.md](PREREQUISITES-WINDOWS.md) has the PowerShell equivalent of every
> one of them, including how to watch the log and how to check that your jar loaded.

## Before you start

You need:

* **JDK 21.** Not 17, not 24. Every class inside IS 7.3.0 is Java 21 bytecode, so 21 is
  needed both to compile against it and to run it. Check with `java -version`.
  (`wso2server.bat` claims "JDK 11 to JDK 21" — that is a generic launcher message and is
  misleading for this build. Use 21.)
* **Maven** (`mvn -version`).
* A WSO2 IS 7.3.0 folder. Below, `$IS_HOME` means that folder — the one containing `bin/`
  and `repository/`.

```bash
export JAVA_HOME=/path/to/jdk-21
export IS_HOME=/path/to/wso2is-7.3.0
```

## Build all eight

```bash
mvn clean install
```

You get seven `.jar` files for the server, plus one standalone service (lab 04).

## Put them into the server

```bash
./deploy.sh $IS_HOME
```

That only copies the jars. Most labs also need a few lines of configuration:

```bash
cp $IS_HOME/repository/conf/deployment.toml $IS_HOME/repository/conf/deployment.toml.backup
cat deployment.toml.sample >> $IS_HOME/repository/conf/deployment.toml
```

Open `deployment.toml` afterwards and read what you just added — those few lines are half of
what these labs are teaching.

Then start the server:

```bash
$IS_HOME/bin/wso2server.sh
```

To take it all out again: `./undeploy.sh $IS_HOME`, remove the same lines from
`deployment.toml`, restart.

## Did it work?

Watch the log while you use the server. Every lab prints lines that start with `v2`:

```bash
tail -f $IS_HOME/repository/logs/wso2carbon.log | grep v2
```

Then create a user in the Console. You should see labs 01, 02 and 06 all react.

### If nothing happens

Work through these in order — it is almost always one of the first three.

1. **Did the jar actually start?** Copying a jar into `dropins` is not enough; it has to
   load. Start the server with an inspection console open:

   ```bash
   mkfifo /tmp/osgi.in
   sleep 100000 > /tmp/osgi.in &
   $IS_HOME/bin/wso2server.sh -DosgiConsole < /tmp/osgi.in > /tmp/is.log 2>&1 &
   # once it has started:
   echo "lb | grep lab0" > /tmp/osgi.in
   ```

   Every line must say **`Active`**:

   ```
   82|Active     |    4|lab01-custom-event-handler-v2 (2.0.0)|2.0.0
   ```

   `Installed` instead of `Active` means the jar could not load. Ask why with
   `echo "equinox:diag 82" > /tmp/osgi.in`.

2. **Did you add the configuration and restart?** Labs 01, 03, 05, 07 and 08 do nothing
   until their lines are in `deployment.toml`. The server only reads that file at startup.

3. **Do the names match?** In labs 01 and 08 the `name` in `deployment.toml` must be exactly
   what `getName()` returns in the Java class. If they differ, nothing breaks and nothing is
   logged — the code is simply never called. This is the most common problem by far.

4. **Are you on JDK 21?** A jar built on a different Java version will not load.

5. **Some labs are quiet at startup.** Labs 01, 02, 03 and 06 print their "registered" line
   before the server's logging is fully awake, so you may not see it. That is normal — they
   are working. Test them by using the feature instead.

## A word about two labs

* **Lab 03** runs on *every* login, including your own Console login. The v2 version only
  writes a log line so it cannot lock you out — but keep the off switch in mind:
  `enable = false` in `deployment.toml`, then restart.
* **Lab 04's action**, while it is switched on, is called every time anyone changes a
  password. If the little web service is not running, password changes fail. Switch the action
  off in the Console when you are done with that lab.

## Naming

Every class starts with `CRDBTrainingCustom` and ends with `V2`, so you can spot them
anywhere — in `dropins`, in a stack trace, in the Console.

The `V2` part also keeps this project out of the way of v1: different packages
(`org.wso2.iam.training.v2.*`), different config names, and lab 04 listens on port 8091
instead of 8090. You can have both installed at once, though for teaching it is much less
confusing to install one at a time.
