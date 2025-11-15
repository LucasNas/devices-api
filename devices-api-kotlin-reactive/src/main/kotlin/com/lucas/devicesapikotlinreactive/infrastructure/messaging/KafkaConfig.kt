package com.lucas.devicesapikotlinreactive.infrastructure.messaging

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
@EnableKafka
@EnableConfigurationProperties(KafkaProperties::class)
class KafkaConfig(
    private val kafkaProps: KafkaProperties
) {

    @Bean
    @ConditionalOnProperty(prefix = "kafka", name = ["enabled"], havingValue = "true")
    fun deviceEventConsumerFactory(): ConsumerFactory<String, DeviceEvent> {
        val valueDeserializer = JsonDeserializer(DeviceEvent::class.java, false)
        valueDeserializer.addTrustedPackages("*")

        val props = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProps.bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to "kotlin-devices",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest"
        )

        return DefaultKafkaConsumerFactory(
            props,
            StringDeserializer(),
            valueDeserializer
        )
    }

    @Bean(name = ["deviceEventKafkaListenerFactory"])
    @ConditionalOnProperty(prefix = "kafka", name = ["enabled"], havingValue = "true")
    fun deviceEventKafkaListenerFactory(
        consumerFactory: ConsumerFactory<String, DeviceEvent>
    ): ConcurrentKafkaListenerContainerFactory<String, DeviceEvent> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, DeviceEvent>()
        factory.consumerFactory = consumerFactory
        factory.containerProperties.ackMode = ContainerProperties.AckMode.BATCH
        return factory
    }


    @Bean
    @ConditionalOnProperty(prefix = "kafka", name = ["enabled"], havingValue = "true")
    fun deviceEventProducerFactory(): ProducerFactory<String, DeviceEvent> {
        val props = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProps.bootstrapServers,
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true
        )

        val keySerializer = StringSerializer()
        val valueSerializer = JsonSerializer<DeviceEvent>().apply {
            isAddTypeInfo = false
        }

        return DefaultKafkaProducerFactory(
            props,
            keySerializer,
            valueSerializer
        )
    }

    @Bean
    @ConditionalOnProperty(prefix = "kafka", name = ["enabled"], havingValue = "true")
    fun deviceEventKafkaTemplate(): KafkaTemplate<String, DeviceEvent> =
        KafkaTemplate(deviceEventProducerFactory())
}
