package org.example.models.db_models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "user_preferences")
data class UserPreferences(
    @Id
    @Column(name = "user_id")
    val userId: UUID,

    @Column(name = "match_notifications_enabled", nullable = false)
    val matchNotificationsEnabled: Boolean = true,

    @Column(name = "message_notifications_enabled", nullable = false)
    val messageNotificationsEnabled: Boolean = true,

    @Column(name = "hide_my_posts_from_non_matches", nullable = false)
    val hideMyPostsFromNonMatches: Boolean = false,
)
