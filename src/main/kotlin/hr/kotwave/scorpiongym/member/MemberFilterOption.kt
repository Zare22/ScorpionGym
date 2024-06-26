package hr.kotwave.scorpiongym.member

enum class MemberFilterOption(val displayName: String) {
    PAID("Podmireni svi dugovi"),
    UNPAID("Neplaćeno"),
    NO_ACTIVE_SUBSCRIPTION("Nema aktivne članarine")
}