package com.github.dnvriend

import com.github.dnvriend.lambda.{SamContext, ScheduledEvent, ScheduledEventHandler}
import com.github.dnvriend.lambda.annotation.ScheduleConf

/**
  * publishes 100 random generated contact details every minute
  */
@ScheduleConf(schedule = "rate(1 minute)")
class CreateClientScheduled extends ScheduledEventHandler {
  override def handle(event: ScheduledEvent, ctx: SamContext): Unit = {
    ctx.logger.log("Publishing clients...")
    GenClient.iterator.take(250).foreach { client =>
      try {
        PublishClient.publish(client, ctx)
      } catch {
        case t: Throwable =>
          ctx.logger.log(t.getMessage)
      }
    }
  }
}