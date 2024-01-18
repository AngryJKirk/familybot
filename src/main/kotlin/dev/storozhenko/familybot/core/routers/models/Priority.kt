package dev.storozhenko.familybot.core.routers.models

enum class Priority(val priorityValue: Int) {

    HIGH(1),
    MEDIUM(0),
    LOW(-1),
    VERY_LOW(-2),
    RANDOM(-1000);

    infix fun higherThan(other: Priority) = this.priorityValue > other.priorityValue

}
