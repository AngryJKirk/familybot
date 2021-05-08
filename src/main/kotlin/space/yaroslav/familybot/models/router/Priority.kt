package space.yaroslav.familybot.models.router

enum class Priority(val priorityValue: Int) {

    HIGH(1),
    MEDIUM(0),
    LOW(-1),
    VERY_LOW(-2),
    RANDOM(-1000);

    infix fun higherThan(other: Priority): Boolean {
        return this.priorityValue > other.priorityValue
    }
}

