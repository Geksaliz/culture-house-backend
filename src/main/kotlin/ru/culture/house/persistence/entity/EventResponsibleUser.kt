package ru.culture.house.persistence.entity

import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "event_responsible_users")
class EventResponsibleUser(
    @EmbeddedId
    val id: EventResponsibleUserId = EventResponsibleUserId(),

    @MapsId("eventId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    var event: Event,

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,
) {
    @Embeddable
    data class EventResponsibleUserId(

        @Column(name = "event_id")
        var eventId: Long? = null,

        @Column(name = "user_id")
        var userId: Long? = null,
    ) : Serializable
}