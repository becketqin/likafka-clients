/**
 * Copyright 2016 LinkedIn Corp. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.linkedin.kafka.clients.auditing;

import com.linkedin.kafka.clients.producer.LiKafkaProducerConfig;

import org.apache.kafka.common.Configurable;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The auditor interface. This class allows user to implement their own auditing solution.
 * <p>
 * Notice that the auditor may be used by multiple threads, so the implementation should be thread safe.
 */
public interface Auditor<K, V> extends Configurable {

  /**
   * This method will be invoked by LiKafkaProducer on instantiation.
   * Notice that if the auditor is used by the producer, there will be an additional
   * {@link KafkaProducer KafkaProducer<byte[], byte[]>} object passed in the configuration with the key of
   * {@link LiKafkaProducerConfig#CURRENT_PRODUCER}. User can use this producer send auditing events to the same Kafka
   * cluster the producer is is producing to. This is to avoid creating another producer.
   *
   * @param configs The configurations for the auditor
   */
  void configure(Map<String, ?> configs);

  /**
   * Start the auditor.
   */
  void start();

  /**
   * Get the custom audit information from the key and value of the record.
   * This method helps the producer avoid holding the key and value until the message sending is completed.
   * <p>
   * Ideally we want use a template here, but that will require LiKafkaProducer and LiKafkaConsumer to also take a
   * template type. Which makes them no longer a drop-in replacement of open source clients. So we just use the Object
   * type here.
   *
   * @param key the key of the record.
   * @param value the value of the record.
   * @return the custom audit information.
   */
  Object getCustomAuditInfo(K key, V value);

  /**
   * Audit the record based on the given information.
   *
   * @param customAuditInfo The user extracted auditing information.
   * @param topic The topic of the record.
   * @param partition The partition of the record.
   * @param timestamp The timestamp of the record.
   * @param messageCount The number of messages to record.
   * @param bytesCount The number of bytes to record.
   */
  void record(Object customAuditInfo,
              String topic,
              Integer partition,
              Long timestamp,
              Long messageCount,
              Long bytesCount,
              AuditType auditType);

  /**
   * Close the auditor with timeout.
   * This method will be called when producer is closed with a timeout.
   *
   * @param timeout the maximum time to wait to close the auditor.
   * @param unit The time unit.
   */
  void close(long timeout, TimeUnit unit);

  /**
   * The LiKafkaProducer and LiKafkaConsumer will call this method when the producer or consumer is closed.
   * Close the auditor.
   */
  void close();


}
