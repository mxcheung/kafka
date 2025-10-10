onException(Exception.class)
    .handled(true)
    .log("🔥 Kafka error: ${exception.class} - ${exception.message}")
    .log("Stack trace:\n${exception.stacktrace}")
    .log("Full cause:\n${exception.cause}");