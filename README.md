# scalamind

Mastermind game made with Scala

I know there's 2.6.x akka version, when I'll have time to migrate the project I'll do.

You have to add your telegram bot token in `application.conf` inside `telegram/src/main/resources/application.conf`.
The conf path is:
```
telegram {
  ...,
  token = "<<your_token>>",
  webhookUrl = "<<your_domain>>",
  ...
}
```