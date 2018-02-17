package space.yaroslav.familybot.route.models


enum class FunctionId(val id: Int, val desc: String) {
    HUIFICATE(1, "Хуификация"),
    CHATTING(2, "Влезание в диалог"),
    PIDOR(3, "Пидор-детектор"),
    RAGE(4, "Рейдж-мод");

}