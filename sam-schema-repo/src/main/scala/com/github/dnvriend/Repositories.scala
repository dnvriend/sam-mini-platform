package com.github.dnvriend

import com.github.dnvriend.lambda.SamContext
import com.github.dnvriend.repo.JsonRepository
import com.github.dnvriend.repo.dynamodb.{DynamoDBCounterRepository, DynamoDBJsonRepository, DynamoDBJsonWithRangeKeyRepository}

object Repositories {
  /**
    * Returns a repository for the table "schema_by_vector" that uses the
    * partition_key 'vector' and sort_key 'version' and has a single attribute 'json'
    * that contains the JSON encoded payload
    */
  def schemaByVector(ctx: SamContext): DynamoDBJsonWithRangeKeyRepository = {
    DynamoDBJsonWithRangeKeyRepository("schema_by_vector", ctx, "vector", "version", "json")
  }

  /**
    * Returns a repository for the table "schema_by_fingerprint" that uses the
    * partition_key 'fingerprint' and a single attribute 'json' that contains the
    * JSON encoded payload
    */
  def schemaByFingerprint(ctx: SamContext): JsonRepository = {
    DynamoDBJsonRepository("schema_by_fingerprint", ctx, "fingerprint", "json")
  }

  /**
    * Returns a repository for the table "counters" that uses the partition_key 'counter_key'
    * and a single attribute 'counter_value' that will be incremented atomically.
    */
  def counterRepository(ctx: SamContext): DynamoDBCounterRepository = {
    DynamoDBCounterRepository("counters", ctx, "counter_key", "counter_value")
  }
}
